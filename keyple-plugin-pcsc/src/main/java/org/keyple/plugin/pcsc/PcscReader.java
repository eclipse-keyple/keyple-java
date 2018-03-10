/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.smartcardio.*;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InvalidMessageException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class PcscReader extends ObservableReader implements ConfigurableReader {

    private static final ILogger logger = SLoggerFactory.getLogger(PcscReader.class);
    private static final String SETTING_KEY_PROTOCOL = "protocol";
    private static final String SETTING_PROTOCOL_T0 = "T0";
    private static final String SETTING_PROTOCOL_T1 = "T1";
    private static final String SETTING_PROTOCOL_TX = "Tx";
    private static final String SETTING_KEY_MODE = "mode";
    private static final String SETTING_MODE_EXCLUSIVE = "exclusive";
    private static final String SETTING_MODE_SHARED = "shared";
    // private static final String SETTING_MODE_DIRECT = "direct";
    private static final String SETTING_KEY_DISCONNECT = "disconnect";
    private static final String SETTING_KEY_THREAD_TIMEOUT = "thread_wait_timeout";
    private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 5000;

    private final CardTerminal terminal;
    private final String terminalName;

    // private final Map<String, String> settings;
    private String parameterCardProtocol;
    private boolean cardExclusiveMode;

    private Card card;
    private CardChannel channel;

    private ByteBuffer aidCurrentlySelected;
    private ApduResponse fciDataSelected;
    private boolean atrDefaultSelected = false;

    private EventThread thread;
    private static final AtomicInteger threadCount = new AtomicInteger();

    /**
     * Thread wait timeout in ms
     */
    private long threadWaitTimeout;


    protected PcscReader(CardTerminal terminal) { // PcscReader constructor may be
        // called only by PcscPlugin
        this.terminal = terminal;
        this.terminalName = terminal.getName();
        this.card = null;
        this.channel = null;

        // Using null values to use the standard method for defining default values
        try {
            setAParameter(SETTING_KEY_PROTOCOL, null);
            setAParameter(SETTING_KEY_MODE, null);
            setAParameter(SETTING_KEY_DISCONNECT, null);
        } catch (IOReaderException ex) {
            // It's actually impossible to reach that state
            throw new IllegalStateException("COuld not initialize properly", ex);
        }
    }

    /**
     * Flurent setter to change the PC/SC wait timeout in ms. Defaults to 5000.
     *
     * @param timeout Timeout to use
     * @return Current instance
     */
    public PcscReader setThreadWaitTimeout(long timeout) {
        this.threadWaitTimeout = timeout;
        return this;
    }


    @Override
    public String getName() {
        return terminalName;
    }

    @Override
    public SeResponse transmit(SeRequest seApplicationRequest) throws IOReaderException {
        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();

        if (isSEPresent()) { // TODO si vrai ET pas vrai => retourne un SeResponse vide de manière
            // systématique - return new SeResponse(false, fciDataSelected,
            // apduResponseList);
            try {
                prepareAndConnectToTerminalAndSetChannel();
            } catch (CardException e) {
                throw new ChannelStateReaderException(e);
            }
            // gestion du select application ou du getATR
            if (seApplicationRequest.getAidToSelect() != null && aidCurrentlySelected == null) {
                fciDataSelected = connect(seApplicationRequest.getAidToSelect());
            } else if (!atrDefaultSelected) {
                fciDataSelected = new ApduResponse(
                        ByteBufferUtils.concat(ByteBuffer.wrap(card.getATR().getBytes()),
                                ByteBuffer.wrap(new byte[] {(byte) 0x90, 0x00})),
                        true);
                atrDefaultSelected = true;
            }

            // fclairamb(2018-03-03): Is there a more elegant way to do this ?
            if (fciDataSelected.getStatusCode() != 0x9000) {
                throw new InvalidMessageException("FCI failed !", fciDataSelected);
            }

            for (ApduRequest apduRequest : seApplicationRequest.getApduRequests()) {
                ResponseAPDU apduResponseData;
                try {
                    ByteBuffer buffer = apduRequest.getBuffer();
                    { // Sending data
                      // We shouldn't have to re-use the buffer that was used to be sent but we have
                      // some code that does it.
                        final int posBeforeRead = buffer.position();
                        apduResponseData = channel.transmit(new CommandAPDU(buffer));
                        buffer.position(posBeforeRead);
                    }

                    byte[] statusCode = new byte[] {(byte) apduResponseData.getSW1(),
                            (byte) apduResponseData.getSW2()};
                    // gestion du getResponse en case 4 avec reponse valide et
                    // retour vide
                    hackCase4AndGetResponse(apduRequest.isCase4(), statusCode, apduResponseData,
                            channel);

                    apduResponseList.add(new ApduResponse(apduResponseData.getBytes(), true));
                } catch (CardException e) {
                    throw new ChannelStateReaderException(e);
                }
                // fclairamb(2018-03-07): Catching NPE here definitely isn't a good idea. We can't
                // accept our library to throw NPE internally
                /*
                 * catch (NullPointerException e) { logger.error(getName() +
                 * " : Error executing command", e); apduResponseList.add(new ApduResponse((byte[])
                 * null, false)); break; }
                 */
            }

            if (!seApplicationRequest.keepChannelOpen()) {
                this.disconnect();
            }
        }

        return new SeResponse(false, fciDataSelected, apduResponseList);
    }

    private void prepareAndConnectToTerminalAndSetChannel() throws CardException {
        // final String protocol = getCardProtocol();
        if (card == null) {
            this.card = this.terminal.connect(parameterCardProtocol);
            if (cardExclusiveMode) {
                card.beginExclusive();
            }
        }

        this.channel = card.getBasicChannel();
    }

    /*
     * private String getCardProtocol() { String protocol = settings.get(SETTING_KEY_PROTOCOL); if
     * (protocol == null) { protocol = "*"; } return protocol; }
     */

    private static void hackCase4AndGetResponse(boolean isCase4, byte[] statusCode,
            ResponseAPDU responseFciData, CardChannel channel) throws CardException {
        if (isCase4 && statusCode[0] == (byte) 0x90 && statusCode[1] == (byte) 0x00
                && responseFciData.getData().length == 0) {
            ByteBuffer command = ByteBuffer.allocate(5);
            command.put((byte) 0x00);
            command.put((byte) 0xC0);
            command.put((byte) 0x00);
            command.put((byte) 0x00);
            command.put((byte) 0x00);
            logger.info("Case4 hack", "action", "pcsc_reader.case4_hack");
            responseFciData = channel.transmit(new CommandAPDU(command));

            statusCode[0] = (byte) responseFciData.getSW1();
            statusCode[1] = (byte) responseFciData.getSW2();
        }
    }

    /*
     * private static String formatLogRequest(byte[] request) { String c =
     * DatatypeConverter.printHexBinary(request); String log = c;
     * 
     * if (request.length == 4) { log = extractData(c, 0, 2) + " " + extractData(c, 2, 4) + " " +
     * extractData(c, 4, 8); } if (request.length == 5) { log = extractData(c, 0, 2) + " " +
     * extractData(c, 2, 4) + " " + extractData(c, 4, 8) + " " + extractData(c, 8, 10); } else if
     * (request.length > 5) { log = extractData(c, 0, 2) + " " + extractData(c, 2, 4) + " " +
     * extractData(c, 4, 8) + " " + extractData(c, 8, 10) + " " + extractData(c, 10, c.length()); }
     * 
     * return log; }
     */

    private static String extractData(String str, int pos1, int pos2) {
        String data = "";
        for (int i = pos1; i < pos2; i++) {
            data += str.charAt(i);
        }
        return data;
    }

    @Override
    public boolean isSEPresent() throws IOReaderException {
        try {
            return terminal.isCardPresent();
        } catch (CardException e) {
            throw new IOReaderException(e);
        }
    }

    /**
     * method to connect to the card from the terminal
     *
     * @throws ChannelStateReaderException
     */
    private ApduResponse connect(ByteBuffer aid) throws ChannelStateReaderException {
        logger.info("Connecting to card", "action", "pcsc_reader.connect", "aid",
                ByteBufferUtils.toHex(aid), "readerName", getName());
        try {
            ByteBuffer command = ByteBuffer.allocate(aid.limit() + 6);
            command.put((byte) 0x00);
            command.put((byte) 0xA4);
            command.put((byte) 0x04);
            command.put((byte) 0x00);
            command.put((byte) aid.limit());
            command.put(aid);
            command.put((byte) 0x00);
            command.position(0);
            ResponseAPDU res = channel.transmit(new CommandAPDU(command));

            byte[] statusCode = new byte[] {(byte) res.getSW1(), (byte) res.getSW2()};
            hackCase4AndGetResponse(true, statusCode, res, channel);
            ApduResponse fciResponse = new ApduResponse(res.getBytes(), true);
            aidCurrentlySelected = aid;
            return fciResponse;
        } catch (CardException e1) {
            throw new ChannelStateReaderException(e1);
        }
    }

    /**
     * method to disconnect the card from the terminal
     *
     * @throws IOReaderException
     * @throws CardException
     */
    private void disconnect() throws IOReaderException {
        logger.info("Disconnecting", "action", "pcsc_reader.disconnect");
        try {
            aidCurrentlySelected = null;
            fciDataSelected = null;
            atrDefaultSelected = false;

            if (this.card != null) {
                this.channel = null;
                this.card.disconnect(false);
                this.card = null;
            }
        } catch (CardException e) {
            throw new IOReaderException(e);
        }

    }

    /**
     * Set a list of parameters on a reader.
     *
     * See {@link #setAParameter(String, String)} for more details
     *
     * @param parameters the new parameters
     * @throws IOReaderException This method can fail when disabling the exclusive mode as it's
     *         executed instantly
     */
    @Override
    public void setParameters(Map<String, String> parameters) throws IOReaderException {
        for (Map.Entry<String, String> en : parameters.entrySet()) {
            setAParameter(en.getKey(), en.getValue());
        }
    }

    /*
     * TODO Paramètres PC/SC dont le support est à intégré paramètre 'Protocol' pouvant prendre les
     * valeurs String 'T0', 'T1', 'Tx' paramètre 'Mode' pouvant prendre les valeurs String 'Shared',
     * 'Exclusive', 'Direct' paramètre 'Disconnect' pouvant prendre les valeurs String 'Leave',
     * 'Reset', 'Unpower', 'Eject' Il s'agit des valeurs de paramètre définies par le standard
     * 'PC/SC'.
     *
     * Si on traduit ses paramètres pour l'API SmartCard IO cela donne: pour 'Protocol' :
     * javax.smartcardio.CardTerminal.connect(String protocol) paramétré avec "T=0" si 'T0', "T=1"
     * si 'T1', "*" si 'Tx' => voir définition
     * https://docs.oracle.com/javase/6/docs/jre/api/security/smartcardio/spec/javax/smartcardio/
     * CardTerminal.html#connect(java.lang.String) le comportement par défaut pour 'Protocol' doit
     * être 'Tx'
     *
     * paramètre 'Mode' : le comportement par défaut pour 'Protocol' doit être 'Exclusive', dans ce
     * cas une exclusivité d'accès est gérée via javax.smartcardio.Card.beginExclusive() et
     * endExclusive() cf.
     * https://docs.oracle.com/javase/6/docs/jre/api/security/smartcardio/spec/javax/smartcardio/
     * Card.html#beginExclusive() sinon le 'Mode' doit être considéré comme 'Shared' à vérifier avec
     * Jean-Pierre Fortune, le mode 'Direct' ne devrait pas être supporté pour un
     * ProxyReader.transmit(), (l'envoi de commandes directes de paramétrage du lecteur PC/SC
     * devrait se faire avec un setParameter spécial)
     *
     * Pour 'Disconnect', un paramétrage 'Reset', fera que la commande
     * javax.smartcardio.Carddisconnect(boolean reset) sera paramétrée à 'true' si 'Reset', à
     * 'false' si 'Unpower' Les valeurs 'Leave' et 'Eject' ne serait pas gérée.
     *
     */

    /**
     * Set a parameter.
     *
     * These are the parameters you can use with their associated values:
     * <ul>
     * <li><strong>protocol</strong>:
     * <ul>
     * <li>Tx: Automatic negociation (default)</li>
     * <li>T0: T0 protocol</li>
     * <li>T1: T1 protocol</li>
     * </ul>
     * </li>
     * <li><strong>mode</strong>:
     * <ul>
     * <li>shared: Shared between apps and threads (default)</li>
     * <li>exclusive: Exclusive to this app and the current thread</li>
     * </ul>
     * </li>
     * <li><strong>thread_wait_timeout</strong>: Number of milliseconds to wait</li>
     * </ul>
     *
     * @param name Parameter name
     * @param value Parameter value
     * @throws IOReaderException This method can fail when disabling the exclusive mode as it's
     *         executed instantly
     */
    @Override
    public void setAParameter(String name, String value) throws IOReaderException {
        logger.info("PCSC: Set a parameter", "action", "pcsc_reader.set_parameter", "name", name,
                "value", value);
        if (name == null) {
            throw new IllegalArgumentException("Parameter shouldn't be null");
        }
        if (name.equals(SETTING_KEY_PROTOCOL)) {
            if (value == null || value.equals(SETTING_PROTOCOL_TX)) {
                parameterCardProtocol = "*";
            } else {
                parameterCardProtocol = value;
            }
        } else if (name.equals(SETTING_KEY_MODE)) {
            if (value == null || value.equals(SETTING_MODE_EXCLUSIVE)) {
                cardExclusiveMode = true;
            } else if (value.equals(SETTING_MODE_SHARED)) {
                if (cardExclusiveMode && card != null) {
                    try {
                        card.endExclusive();
                    } catch (CardException e) {
                        throw new IOReaderException("Couldn't disable exclusive mode", e);
                    }
                }
                cardExclusiveMode = false;
            }
        } else if (name.equals(SETTING_KEY_THREAD_TIMEOUT)) {
            if (value == null) {
                threadWaitTimeout = SETTING_THREAD_TIMEOUT_DEFAULT;
            } else {
                long timeout = Long.parseLong(value);

                if (timeout <= 0) {
                    throw new IllegalArgumentException("Timeout has to be of at least 1ms");
                }

                threadWaitTimeout = timeout;
            }
        }
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        { // Returning the protocol
            String protocol = parameterCardProtocol;
            if (protocol.equals("*")) {
                protocol = SETTING_PROTOCOL_TX;
            }
            parameters.put(SETTING_KEY_PROTOCOL, protocol);
        }

        { // The mode ?
            if (!cardExclusiveMode) {
                parameters.put(SETTING_KEY_MODE, SETTING_MODE_SHARED);
            }
        }

        { // The thread wait timeout
            if (threadWaitTimeout != SETTING_THREAD_TIMEOUT_DEFAULT) {
                parameters.put(SETTING_KEY_THREAD_TIMEOUT, Long.toString(threadWaitTimeout));
            }
        }


        return parameters;
    }

    @Override
    public void addObserver(ReaderObserver observer) {
        // We don't need synchronization for the list itself, we need to make sure we're not
        // starting and closing the thread at the same time.
        synchronized (readerObservers) {
            super.addObserver(observer);
            if (readerObservers.size() == 1) {
                if (thread != null) { // <-- This should never happen and can probably be dropped at
                    // some point
                    throw new IllegalStateException("The reader thread shouldn't null");
                }

                thread = new EventThread(this);
                thread.start();
            }
        }
    }

    @Override
    public void deleteObserver(ReaderObserver observer) {
        synchronized (readerObservers) {
            super.deleteObserver(observer);
            if (readerObservers.isEmpty()) {
                if (thread == null) { // <-- This should never happen and can probably be dropped at
                    // some point
                    throw new IllegalStateException("The reader thread should be null");
                }

                // We'll let the thread calmly end its course after the waitForCard(Absent|Present)
                // timeout occurs
                thread.end();
                thread = null;
            }
        }
    }


    /**
     * Thread in charge of reporting live events
     */
    class EventThread extends Thread {
        /**
         * Reader that we'll report about
         */
        private final PcscReader reader;

        /**
         * If the thread should be kept a alive
         */
        private volatile boolean running = true;

        /**
         * Constructor
         *
         * @param reader PcscReader
         */
        EventThread(PcscReader reader) {
            super("pcsc-events-" + threadCount.addAndGet(1));
            setDaemon(true);
            this.reader = reader;
        }

        /**
         * Marks the thread as one that should end when the last cardWaitTimeout occurs
         */
        void end() {
            running = false;
        }

        private void cardRemoved() {
            notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.SE_REMOVAL));
        }

        private void cardInserted() {
            notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.SE_INSERTED));
        }

        /**
         * Event failed
         *
         * @param ex Exception
         */
        private void exceptionThrown(Exception ex) {
            logger.error("PCSC Reader: Error handling events", "action", "pcsc_reader.event_error",
                    "readerName", getName(), "exception", ex);
            if (ex instanceof CardException || ex instanceof IOReaderException) {
                notifyObservers(new ReaderEvent(reader, ReaderEvent.EventType.IO_ERROR));
            }
        }

        public void run() {
            try {
                // First thing we'll do is to notify that a card was inserted if one is already
                // present.
                if (isSEPresent()) {
                    cardInserted();
                }

                while (running) {
                    // If we have a card,
                    if (isSEPresent()) {
                        // we will wait for it to disappear
                        if (terminal.waitForCardAbsent(threadWaitTimeout)) {
                            disconnect();
                            // and notify about it.
                            cardRemoved();
                        }
                        // false means timeout, and we go back to the beginning of the loop
                    }
                    // If we don't,
                    else {
                        // we will wait for it to appear
                        if (terminal.waitForCardPresent(threadWaitTimeout)) {
                            cardInserted();
                        }
                        // false means timeout, and we go back to the beginning of the loop
                    }
                }
            } catch (Exception e) {
                exceptionThrown(e);
            }
        }
    }
}

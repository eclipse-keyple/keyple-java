/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc;

import javax.smartcardio.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import javax.smartcardio.*;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InconsistentParameterValueException;
import org.keyple.seproxy.exceptions.InvalidMessageException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class PcscReader extends ObservableReader implements ConfigurableReader {

    private static final ILogger logger = SLoggerFactory.getLogger(PcscReader.class);
    public static final String SETTING_KEY_PROTOCOL = "protocol";
    public static final String SETTING_PROTOCOL_T0 = "T0";
    public static final String SETTING_PROTOCOL_T1 = "T1";
    public static final String SETTING_PROTOCOL_TX = "Tx";
    public static final String SETTING_KEY_MODE = "mode";
    public static final String SETTING_MODE_EXCLUSIVE = "exclusive";
    public static final String SETTING_MODE_SHARED = "shared";
    public static final String SETTING_KEY_DISCONNECT = "disconnect";
    public static final String SETTING_DISCONNECT_RESET = "reset";
    public static final String SETTING_DISCONNECT_UNPOWER = "unpower";
    public static final String SETTING_DISCONNECT_LEAVE = "leave";
    public static final String SETTING_DISCONNECT_EJECT = "eject";
    public static final String SETTING_KEY_THREAD_TIMEOUT = "thread_wait_timeout";
    public static final String SETTING_KEY_LOGGING = "logging";
    public static final String SETTING_KEY_PO_SOLUTION_PREFIX = "po_solution"; // TODO To factorize
                                                                               // in the common
                                                                               // abstract reader
                                                                               // class?
    private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 5000;

    private final CardTerminal terminal;
    private final String terminalName;

    // private final Map<String, String> settings;
    private String parameterCardProtocol;
    private boolean cardExclusiveMode;
    private boolean cardReset;

    private Card card;
    private CardChannel channel;

    private ByteBuffer aidCurrentlySelected;
    private ApduResponse fciDataSelected;
    private boolean atrDefaultSelected = false;

    private EventThread thread;
    private static final AtomicInteger threadCount = new AtomicInteger();

    private boolean logging;

    /**
     * Thread wait timeout in ms
     */
    private long threadWaitTimeout;

    /**
     * PO selection map associating po solution and atr regex string
     */
    private Map<String, String> protocolsMap;


    PcscReader(CardTerminal terminal) { // PcscReader constructor may be
        // called only by PcscPlugin
        this.terminal = terminal;
        this.terminalName = terminal.getName();
        this.card = null;
        this.channel = null;
        this.protocolsMap = new HashMap<String, String>();

        // Using null values to use the standard method for defining default values
        try {
            setParameter(SETTING_KEY_PROTOCOL, null);
            setParameter(SETTING_KEY_MODE, null);
            setParameter(SETTING_KEY_DISCONNECT, null);
            setParameter(SETTING_KEY_LOGGING, null);
        } catch (IOReaderException ex) {
            // It's actually impossible to reach that state
            throw new IllegalStateException("Could not initialize properly", ex);
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
    public SeResponse transmit(SeRequest request) throws IOReaderException {
        return logging ? transmitWithTiming(request) : transmitActual(request);
    }

    private SeResponse transmitWithTiming(SeRequest request) throws IOReaderException {
        long before = System.nanoTime();
        try {
            SeResponse response = transmitActual(request);
            // Switching to the 10th of milliseconds and dividing by 10 to get the ms
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.info("PCSCReader: Data exchange", "action", "pcsc_reader.transmit", "request",
                    request, "response", response, "elapsedMs", elapsedMs);
            return response;
        } catch (IOReaderException ex) {
            // Switching to the 10th of milliseconds and dividing by 10 to get the ms
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.info("PCSCReader: Data exchange", "action", "pcsc_reader.transmit_failure",
                    "request", request, "elapsedMs", elapsedMs);
            throw ex;
        }
    }

    private SeResponse transmitActual(SeRequest request) throws IOReaderException {
        try {
            prepareAndConnectToTerminalAndSetChannel();
        } catch (CardException e) {
            throw new ChannelStateReaderException(e);
        }

        boolean previouslyOpen = false;

        // #82: Updating the code to support more than one element transmission
        List<SeResponseElement> respElements = new ArrayList<SeResponseElement>();
        for (SeRequestElement reqElement : request.getElements()) {

            // This is the target selection code introduced by JP
            String protocolFlag = reqElement.getProtocolFlag();
            if (protocolFlag != null && !protocolFlag.isEmpty()) {
                String selectionMask = protocolsMap.get(protocolFlag);
                if (selectionMask == null) {
                    throw new InvalidMessageException("Target selector mask not found!", null);
                }
                Pattern p = Pattern.compile(selectionMask);
                String atr = ByteBufferUtils.toHex(ByteBuffer.wrap(card.getATR().getBytes()));
                if (!p.matcher(atr).matches()) {
                    logger.info("Protocol selection: unmatching SE: " + protocolFlag, "action",
                            "pcsc_reader.transmit_actual");
                    respElements.add(null); // add empty response
                    continue; // try next request
                }
            }
            logger.info("Protocol selection: matching SE: " + protocolFlag, "action",
                    "pcsc_reader.transmit_actual");

            List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();

            // florent: #82: I don't see the point of doing simple SE presence check here. We should
            // either NOT check for SE element presence or directly throw an exception.
            // if (isSEPresent()) {
            if (reqElement.getAidToSelect() != null && aidCurrentlySelected == null) {
                fciDataSelected = connect(reqElement.getAidToSelect());
            } else if (!atrDefaultSelected) {
                fciDataSelected = new ApduResponse(
                        ByteBufferUtils.concat(ByteBuffer.wrap(card.getATR().getBytes()),
                                ByteBuffer.wrap(new byte[] {(byte) 0x90, 0x00})),
                        true);
                atrDefaultSelected = true;
            }

            // fclairamb(2018-03-03): Is there a more elegant way to do this ?
            if (fciDataSelected.getStatusCode() != 0x9000) {
                // if channel is set to be left open, we stop here
                if (reqElement.keepChannelOpen()) {
                    throw new InvalidMessageException("FCI failed !", fciDataSelected);
                } else {
                    continue; // app selection failed, let's try next request
                }
            }

            for (ApduRequest apduRequest : reqElement.getApduRequests()) {
                apduResponseList.add(transmit(apduRequest));
            }

            respElements
                    .add(new SeResponseElement(previouslyOpen, fciDataSelected, apduResponseList));

            // #82: We can now correctly exploit the SeResponseElement.previouslyOpen property
            if (!reqElement.keepChannelOpen()) {
                disconnect();
                previouslyOpen = false;
                break; // we do not go further, exit for loop
            } else {
                previouslyOpen = true;
            }
        }
        return new SeResponse(respElements);
    }

    /**
     * Transmission of each APDU request
     *
     * @param apduRequest APDU request
     * @return APDU response
     * @throws ChannelStateReaderException Exception faced
     */
    private ApduResponse transmit(ApduRequest apduRequest) throws ChannelStateReaderException {
        ResponseAPDU apduResponseData;
        long before = logging ? System.nanoTime() : 0;
        try {
            ByteBuffer buffer = apduRequest.getBuffer();
            { // Sending data
              // We shouldn't have to re-use the buffer that was used to be sent but we have
              // some code that does it.
                final int posBeforeRead = buffer.position();
                apduResponseData = channel.transmit(new CommandAPDU(buffer));
                buffer.position(posBeforeRead);
            }

            byte[] statusCode =
                    new byte[] {(byte) apduResponseData.getSW1(), (byte) apduResponseData.getSW2()};
            // gestion du getResponse en case 4 avec reponse valide et
            // retour vide
            hackCase4AndGetResponse(apduRequest.isCase4(), statusCode, apduResponseData, channel);

            ApduResponse apduResponse = new ApduResponse(apduResponseData.getBytes(), true);

            if (logging) {
                double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
                logger.info("PCSCReader: Transmission", "action", "pcsc_reader.transmit",
                        "apduRequest", apduRequest, "apduResponse", apduResponse, "elapsedMs",
                        elapsedMs, "apduName", apduRequest.getName());
            }

            return apduResponse;
        } catch (CardException e) {
            throw new ChannelStateReaderException(e);
        }
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


            responseFciData = channel.transmit(new CommandAPDU(command));
            logger.info("Case4 hack", "action", "pcsc_reader.case4_hack");


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

            if (card != null) {
                channel = null;
                card.disconnect(cardReset);
                card = null;
            }
        } catch (CardException e) {
            throw new IOReaderException(e);
        }

    }

    /**
     * Set a list of parameters on a reader.
     * <p>
     * See {@link #setParameter(String, String)} for more details
     *
     * @param parameters the new parameters
     * @throws IOReaderException This method can fail when disabling the exclusive mode as it's
     *         executed instantly
     */
    @Override
    public void setParameters(Map<String, String> parameters) throws IOReaderException {
        for (Map.Entry<String, String> en : parameters.entrySet()) {
            setParameter(en.getKey(), en.getValue());
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
     * <p>
     * These are the parameters you can use with their associated values:
     * <ul>
     * <li><strong>protocol</strong>:
     * <ul>
     * <li>Tx: Automatic negotiation (default)</li>
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
     * <li><strong>disconnect</strong>:
     * <ul>
     * <li>reset: Reset the card</li>
     * <li>unpower: Simply unpower it</li>
     * <li>leave: Unsupported</li>
     * <li>eject: Eject</li>
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
    public void setParameter(String name, String value) throws IOReaderException {
        logger.info("PCSC: Set a parameter", "action", "pcsc_reader.set_parameter", "name", name,
                "value", value);
        if (name == null) {
            throw new IllegalArgumentException("Parameter shouldn't be null");
        }
        if (name.equals(SETTING_KEY_PROTOCOL)) {
            if (value == null || value.equals(SETTING_PROTOCOL_TX)) {
                parameterCardProtocol = "*";
            } else if (value.equals(SETTING_PROTOCOL_T0)) {
                parameterCardProtocol = "T=0";
            } else if (value.equals(SETTING_PROTOCOL_T1)) {
                parameterCardProtocol = "T=1";
            } else {
                throw new InconsistentParameterValueException("Bad protocol", name, value);
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
            } else {
                throw new InconsistentParameterValueException(name, value);
            }
        } else if (name.equals(SETTING_KEY_THREAD_TIMEOUT)) {
            if (value == null) {
                threadWaitTimeout = SETTING_THREAD_TIMEOUT_DEFAULT;
            } else {
                long timeout = Long.parseLong(value);

                if (timeout <= 0) {
                    throw new InconsistentParameterValueException(
                            "Timeout has to be of at least 1ms", name, value);
                }

                threadWaitTimeout = timeout;
            }
        } else if (name.equals(SETTING_KEY_DISCONNECT)) {
            if (value == null || value.equals(SETTING_DISCONNECT_RESET)) {
                cardReset = true;
            } else if (value.equals(SETTING_DISCONNECT_UNPOWER)) {
                cardReset = false;
            } else if (value.equals(SETTING_DISCONNECT_EJECT)
                    || value.equals(SETTING_DISCONNECT_LEAVE)) {
                throw new InconsistentParameterValueException(
                        "This disconnection parameter is not supported by this plugin", name,
                        value);
            } else {
                throw new InconsistentParameterValueException(name, value);
            }
        } else if (name.equals(SETTING_KEY_LOGGING)) {
            logging = Boolean.parseBoolean(value); // default is null and perfectly acceptable
        } else if (name.startsWith(SETTING_KEY_PO_SOLUTION_PREFIX)) {
            if (value == null || value.length() == 0) {
                this.protocolsMap.remove(value);
            } else {
                this.protocolsMap.put(name, value);
            }
        } else {
            throw new InconsistentParameterValueException("This parameter is unknown !", name,
                    value);
        }
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        { // Returning the protocol
            String protocol = parameterCardProtocol;
            if (protocol.equals("*")) {
                protocol = SETTING_PROTOCOL_TX;
            } else if (protocol.equals("T=0")) {
                protocol = SETTING_PROTOCOL_T0;
            } else if (protocol.equals("T=1")) {
                protocol = SETTING_PROTOCOL_T1;
            } else {
                throw new IllegalStateException("Illegal protocol: " + protocol);
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
    public void addObserver(Observer<? super ReaderEvent> observer) {
        // We don't need synchronization for the list itself, we need to make sure we're not
        // starting and closing the thread at the same time.
        synchronized (observers) {
            super.addObserver(observer);
            if (observers.size() == 1) {
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
    public void removeObserver(Observer<? super ReaderEvent> observer) {
        synchronized (observers) {
            super.removeObserver(observer);
            if (observers.isEmpty()) {
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

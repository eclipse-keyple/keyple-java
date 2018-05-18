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
import java.util.regex.Pattern;
import javax.smartcardio.*;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InconsistentParameterValueException;
import org.keyple.seproxy.exceptions.InvalidMessageException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class PcscReader extends AbstractObservableReader implements ConfigurableReader {

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
    public SeResponseSet transmit(SeRequestSet requestSet) throws IOReaderException {
        return logging ? transmitWithTiming(requestSet) : transmitActual(requestSet);
    }

    private SeResponseSet transmitWithTiming(SeRequestSet requestSet) throws IOReaderException {
        long before = System.nanoTime();
        try {
            SeResponseSet responseSet = transmitActual(requestSet);
            // Switching to the 10th of milliseconds and dividing by 10 to get the ms
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.info("PCSCReader: Data exchange", "action", "pcsc_reader.transmit", "requestSet",
                    requestSet, "responseSet", responseSet, "elapsedMs", elapsedMs);
            return responseSet;
        } catch (IOReaderException ex) {
            // Switching to the 10th of milliseconds and dividing by 10 to get the ms
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.info("PCSCReader: Data exchange", "action", "pcsc_reader.transmit_failure",
                    "requestSet", requestSet, "elapsedMs", elapsedMs);
            throw ex;
        }
    }

    /**
     * Do the transmission of all needed requestSet elements contained in the provided requestSet
     * according to the protocol flag selection logic. The responseSet elements are returned in the
     * responseSet object. The requestSet elements are ordered at application level and the
     * responses match this order. When a requestSet is not matching the current PO, the responseSet
     * elements pushed in the responseSet object is set to null.
     *
     * @param requestSet
     * @return responseSet
     * @throws IOReaderException
     */
    private SeResponseSet transmitActual(SeRequestSet requestSet) throws IOReaderException {
        // first step: init of the physical SE channel: if not yet established, opening of the
        // physical channel
        try {
            if (card == null) {
                this.card = this.terminal.connect(parameterCardProtocol);
                if (cardExclusiveMode) {
                    card.beginExclusive();
                    logger.info("Opening of a physical SE channel in exclusive mode.", "action",
                            "pcsc_reader.transmit_actual");
                } else {
                    logger.info("Opening of a physical SE channel in shared mode.", "action",
                            "pcsc_reader.transmit_actual");
                }
            }
            this.channel = card.getBasicChannel();
        } catch (CardException e) {
            throw new ChannelStateReaderException(e);
        }

        boolean previouslyOpen = false;
        boolean elementMatchProtocol[] = new boolean[requestSet.getElements().size()];
        int elementIndex = 0, lastElementIndex;

        // Determine which requestElements are matching the current ATR
        for (SeRequest reqElement : requestSet.getElements()) {
            // Get protocolFlag to check if ATR filtering is required
            String protocolFlag = reqElement.getProtocolFlag();
            if (protocolFlag != null && !protocolFlag.isEmpty()) {
                // the requestSet will be executed only if the protocol match the requestElement
                String selectionMask = protocolsMap.get(protocolFlag);
                if (selectionMask == null) {
                    throw new InvalidMessageException("Target selector mask not found!", null);
                }
                Pattern p = Pattern.compile(selectionMask);
                String atr = ByteBufferUtils.toHex(ByteBuffer.wrap(card.getATR().getBytes()));
                if (!p.matcher(atr).matches()) {
                    logger.info("Protocol selection: unmatching SE: " + protocolFlag, "action",
                            "pcsc_reader.transmit_actual");
                    elementMatchProtocol[elementIndex] = false;
                } else {
                    logger.info("Protocol selection: matching SE: " + protocolFlag, "action",
                            "pcsc_reader.transmit_actual");
                    elementMatchProtocol[elementIndex] = true;
                }
            } else {
                // when no protocol is defined the requestSet has to be executed
                elementMatchProtocol[elementIndex] = true;
            }
            elementIndex++;
        }

        // we have now a boolean array saying whether the corresponding requestElement and the
        // current SE match or not

        lastElementIndex = elementIndex;
        elementIndex = 0;

        // The current requestSet is possibly made of several APDU command lists
        // If the elementMatchProtocol is true we process the requestSet
        // If the elementMatchProtocol is false we skip to the next requestSet
        // If keepChannelOpen is false, we close the physical channel for the last requestElement.
        List<SeResponse> respElements = new ArrayList<SeResponse>();
        for (SeRequest reqElement : requestSet.getElements()) {
            if (elementMatchProtocol[elementIndex] == true) {
                boolean executeRequest = true;
                List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
                if (reqElement.getAidToSelect() != null && aidCurrentlySelected == null) {
                    // Opening of a logical channel with a SE application
                    fciDataSelected = connect(reqElement.getAidToSelect());
                    if (fciDataSelected.getStatusCode() != 0x9000) {
                        // TODO: Remark, for a Calypso PO, the status 6283h (DF invalidated) is
                        // considered as successful for the Select Application command.
                        logger.info("Application selection failed!", "action",
                                "pcsc_reader.transmit_actual");
                        executeRequest = false;
                    }
                } else {
                    // In this case, the SE application is implicitly selected (and only one logical
                    // channel is managed by the SE).
                    fciDataSelected = new ApduResponse(
                            ByteBufferUtils.concat(ByteBuffer.wrap(card.getATR().getBytes()),
                                    ByteBuffer.wrap(new byte[] {(byte) 0x90, 0x00})),
                            true);
                }

                if (executeRequest) {
                    for (ApduRequest apduRequest : reqElement.getApduRequests()) {
                        apduResponseList.add(transmit(apduRequest));
                    }
                }
                respElements.add(new SeResponse(previouslyOpen, fciDataSelected, apduResponseList));
            } else {
                // in case the protocolFlag of a SeRequest doesn't match the reader status, a
                // null SeResponse is added to the SeResponseSet.
                respElements.add(null);
            }
            elementIndex++;
            if (!reqElement.keepChannelOpen()) {
                if (lastElementIndex == elementIndex) {
                    // For the processing of the last SeRequest with a protocolFlag matching
                    // the SE reader status, if the logical channel doesn't require to be kept open,
                    // then the physical channel is closed.
                    disconnect();
                    logger.info("Closing of the physical SE channel.", "action",
                            "pcsc_reader.transmit_actual");
                }
            } else {
                previouslyOpen = true;
                // When keepChannelOpen is true, we stop after the first matching requestElement
                // we exit the for loop here
                // For the processing of a SeRequest with a protocolFlag which matches the
                // current SE reader status, in case it's requested to keep the logical channel
                // open, then the other remaining SeRequest are skipped, and null
                // SeRequest are returned for them.
                break;
            }
        }
        return new SeResponseSet(respElements);
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

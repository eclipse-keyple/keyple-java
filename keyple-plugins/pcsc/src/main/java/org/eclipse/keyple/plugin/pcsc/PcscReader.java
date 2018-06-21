/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.pcsc;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.smartcardio.*;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.exception.*;
import org.eclipse.keyple.seproxy.local.AbstractThreadedLocalReader;
import org.eclipse.keyple.util.ByteBufferUtils;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class PcscReader extends AbstractThreadedLocalReader {

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

    private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 5000;

    private boolean logicalChannelOpen;
    private boolean physicalChannelOpen;

    private final CardTerminal terminal;
    private final String terminalName;

    private String parameterCardProtocol;
    private boolean cardExclusiveMode;
    private boolean cardReset;

    private Card card;
    private CardChannel channel;

    private boolean logging;


    /**
     * This constructor should only be called by PcscPlugin PCSC reader parameters are initialized
     * with their default values as defined in setParameter. See
     * {@link #setParameter(String, String)} for more details
     *
     * @param terminal
     */
    protected PcscReader(CardTerminal terminal) {
        //
        this.terminal = terminal;
        this.terminalName = terminal.getName();
        this.card = null;
        this.channel = null;
        this.protocolsMap = new HashMap<SeProtocol, String>();

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

    @Override
    public final String getName() {
        return terminalName;
    }

    @Override
    protected final void closePhysicalChannel() throws IOReaderException {
        logger.info("Closing of the physical SE channel.", "action",
                "pcsc_reader.closePhysicalChannel");
        try {
            if (card != null) {
                channel = null;
                card.disconnect(cardReset);
                card = null;
            }
        } catch (CardException e) {
            throw new IOReaderException(e);
        }
    }

    @Override
    public final boolean isSePresent() throws IOReaderException {
        try {
            return terminal.isCardPresent();
        } catch (CardException e) {
            throw new IOReaderException(e);
        }
    }

    @Override
    public final boolean waitForCardPresent(long timeout) throws IOReaderException {
        try {
            return terminal.waitForCardPresent(timeout);
        } catch (CardException e) {
            throw new IOReaderException(e);
        }
    }

    @Override
    public final boolean waitForCardAbsent(long timeout) throws IOReaderException {
        try {
            if (terminal.waitForCardAbsent(timeout)) {
                closeLogicalChannel();
                closePhysicalChannel();
                return true;
            } else {
                return false;
            }
        } catch (CardException e) {
            throw new IOReaderException(e);
        }
    }

    /**
     * Transmission of single APDU
     *
     * @param apduIn APDU in buffer
     * @return apduOut buffer
     * @throws ChannelStateReaderException Exception faced
     */
    @Override
    protected final ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
        ResponseAPDU apduResponseData;
        try {
            apduResponseData = channel.transmit(new CommandAPDU(apduIn));
        } catch (CardException e) {
            throw new ChannelStateReaderException(e);
        }
        return ByteBuffer.wrap(apduResponseData.getBytes());
    }

    /**
     * Tells if the current SE protocol matches the provided protocol flag. If the protocol flag is
     * not defined (null), we consider here that it matches. An exception is returned when the
     * provided protocolFlag is not found in the current protocolMap.
     *
     * @param protocolFlag
     * @return
     * @throws InvalidMessageException
     */
    @Override
    protected final boolean protocolFlagMatches(SeProtocol protocolFlag) throws IOReaderException {
        boolean result;
        // Get protocolFlag to check if ATR filtering is required
        if (protocolFlag != null) {
            if (!isPhysicalChannelOpen()) {
                openPhysicalChannel();
            }
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
                result = false;
            } else {
                logger.info("Protocol selection: matching SE: " + protocolFlag, "action",
                        "pcsc_reader.transmit_actual");
                result = true;
            }
        } else {
            // no protocol defined returns true
            result = true;
        }
        return result;
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
            // TODO use setter
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
    protected final ByteBuffer getATR() {
        return ByteBuffer.wrap(card.getATR().getBytes());
    }

    /**
     * Tells if a physical channel is open
     * 
     * @return true if the physical channel is open
     */
    @Override
    protected final boolean isPhysicalChannelOpen() {
        return card != null;
    }

    /**
     * Opens a physical channel
     * 
     * @throws IOReaderException
     */
    @Override
    protected final void openPhysicalChannel()
            throws IOReaderException, ChannelStateReaderException {
        // init of the physical SE channel: if not yet established, opening of a new physical
        // channel
        try {
            if (card == null) {
                if (isLogicalChannelOpen()) {
                    throw new ChannelStateReaderException(
                            "Logical channel found open while physical channel is not!");
                }
                this.card = this.terminal.connect(parameterCardProtocol);
                if (cardExclusiveMode) {
                    card.beginExclusive();
                    logger.info("Opening of a physical SE channel in exclusive mode.", "action",
                            "pcsc_reader.openPhysicalChannel");

                } else {
                    logger.info("Opening of a physical SE channel in shared mode.", "action",
                            "pcsc_reader.openPhysicalChannel");
                }
            }
            this.channel = card.getBasicChannel();
        } catch (CardException e) {
            throw new ChannelStateReaderException(e);
        }
    }
}

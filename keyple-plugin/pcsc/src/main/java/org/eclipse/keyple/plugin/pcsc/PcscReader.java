/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.plugin.pcsc;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.smartcardio.*;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.exception.*;
import org.eclipse.keyple.seproxy.plugin.AbstractThreadedLocalReader;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcscReader extends AbstractThreadedLocalReader {

    private static final Logger logger = LoggerFactory.getLogger(PcscReader.class);
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
     * @param pluginName the name of the plugin
     * @param terminal the PC/SC terminal
     */
    protected PcscReader(String pluginName, CardTerminal terminal) {
        super(pluginName, terminal.getName());
        this.terminal = terminal;
        this.card = null;
        this.channel = null;
        this.protocolsMap = new HashMap<SeProtocol, String>();

        // Using null values to use the standard method for defining default values
        try {
            setParameter(SETTING_KEY_PROTOCOL, null);
            setParameter(SETTING_KEY_MODE, null);
            setParameter(SETTING_KEY_DISCONNECT, null);
            setParameter(SETTING_KEY_LOGGING, null);
        } catch (KeypleBaseException ex) {
            // can not fail with null value
        }
    }

    @Override
    protected final void closePhysicalChannel() throws KeypleChannelStateException {
        try {
            if (card != null) {
                if (logging) {
                    logger.trace("[{}] closePhysicalChannel => closing the channel.",
                            this.getName());
                }
                channel = null;
                card.disconnect(cardReset);
                card = null;
            } else {
                if (logging) {
                    logger.trace("[{}] closePhysicalChannel => card object is null.",
                            this.getName());
                }
            }
        } catch (CardException e) {
            throw new KeypleChannelStateException("Error while closing physical channel", e);
        }
    }

    @Override
    public final boolean isSePresent() throws NoStackTraceThrowable {
        try {
            return terminal.isCardPresent();
        } catch (CardException e) {
            logger.trace("[{}] Exception occured in isSePresent. Message: {}", this.getName(),
                    e.getMessage());
            throw new NoStackTraceThrowable();
        }
    }

    @Override
    public final boolean waitForCardPresent(long timeout) throws NoStackTraceThrowable {
        try {
            return terminal.waitForCardPresent(timeout);
        } catch (CardException e) {
            logger.trace("[{}] Exception occured in waitForCardPresent. Message: {}",
                    this.getName(), e.getMessage());
            throw new NoStackTraceThrowable();
        }
    }

    @Override
    public final boolean waitForCardAbsent(long timeout) throws NoStackTraceThrowable {
        try {
            if (terminal.waitForCardAbsent(timeout)) {
                closeLogicalChannel();
                closePhysicalChannel();
                return true;
            } else {
                return false;
            }
        } catch (KeypleChannelStateException e) {
            logger.trace("[{}] Exception occured in waitForCardAbsent. Message: {}", this.getName(),
                    e.getMessage());
            throw new NoStackTraceThrowable();
        } catch (CardException e) {
            logger.trace("[{}] Exception occured in waitForCardAbsent. Message: {}", this.getName(),
                    e.getMessage());
            throw new NoStackTraceThrowable();
        }
    }

    /**
     * Transmission of single APDU
     *
     * @param apduIn APDU in buffer
     * @return apduOut buffer
     * @throws KeypleIOReaderException if the transmission failed
     */
    @Override
    protected final byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        ResponseAPDU apduResponseData;
        try {
            apduResponseData = channel.transmit(new CommandAPDU(apduIn));
        } catch (CardException e) {
            throw new KeypleIOReaderException(this.getName() + ":" + e.getMessage());
        }
        return apduResponseData.getBytes();
    }

    /**
     * Tells if the current SE protocol matches the provided protocol flag. If the protocol flag is
     * not defined (null), we consider here that it matches. An exception is returned when the
     * provided protocolFlag is not found in the current protocolMap.
     *
     * @param protocolFlag the protocol flag
     * @return true if the current SE matches the protocol flag
     * @throws KeypleReaderException if the protocol mask is not found
     */
    @Override
    protected final boolean protocolFlagMatches(SeProtocol protocolFlag)
            throws KeypleReaderException {
        boolean result;
        // Get protocolFlag to check if ATR filtering is required
        if (protocolFlag != null) {
            if (!isPhysicalChannelOpen()) {
                openPhysicalChannel();
            }
            // the requestSet will be executed only if the protocol match the requestElement
            String selectionMask = protocolsMap.get(protocolFlag);
            if (selectionMask == null) {
                throw new KeypleReaderException("Target selector mask not found!", null);
            }
            Pattern p = Pattern.compile(selectionMask);
            String atr = ByteArrayUtils.toHex(card.getATR().getBytes());
            if (!p.matcher(atr).matches()) {
                if (logging) {
                    logger.trace("[{}] protocolFlagMatches => unmatching SE. PROTOCOLFLAG = {}",
                            this.getName(), protocolFlag);
                }
                result = false;
            } else {
                if (logging) {
                    logger.trace("[{}] protocolFlagMatches => matching SE. PROTOCOLFLAG = {}",
                            this.getName(), protocolFlag);
                }
                result = true;
            }
        } else {
            // no protocol defined returns true
            result = true;
        }
        return result;
    }

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
     * @throws KeypleBaseException This method can fail when disabling the exclusive mode as it's
     *         executed instantly
     * @throws IllegalArgumentException when parameter is wrong
     *
     *
     */
    @Override
    public void setParameter(String name, String value)
            throws IllegalArgumentException, KeypleBaseException {
        if (logging) {
            logger.trace("[{}] setParameter => PCSC: Set a parameter. NAME = {}, VALUE = {}",
                    this.getName(), name, value);
        }
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
                throw new IllegalArgumentException("Bad protocol " + name + " : " + value);
            }
        } else if (name.equals(SETTING_KEY_MODE)) {
            if (value == null || value.equals(SETTING_MODE_SHARED)) {
                if (cardExclusiveMode && card != null) {
                    try {
                        card.endExclusive();
                    } catch (CardException e) {
                        throw new KeypleReaderException("Couldn't disable exclusive mode", e);
                    }
                }
                cardExclusiveMode = false;
            } else if (value.equals(SETTING_MODE_EXCLUSIVE)) {
                cardExclusiveMode = true;
            } else {
                throw new IllegalArgumentException(
                        "Parameter value not supported " + name + " : " + value);
            }
        } else if (name.equals(SETTING_KEY_THREAD_TIMEOUT)) {
            // TODO use setter
            if (value == null) {
                threadWaitTimeout = SETTING_THREAD_TIMEOUT_DEFAULT;
            } else {
                long timeout = Long.parseLong(value);

                if (timeout <= 0) {
                    throw new IllegalArgumentException(
                            "Timeout has to be of at least 1ms " + name + value);
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
                throw new IllegalArgumentException(
                        "This disconnection parameter is not supported by this plugin" + name
                                + " : " + value);
            } else {
                throw new IllegalArgumentException(
                        "Parameters not supported : " + name + " : " + value);
            }
        } else if (name.equals(SETTING_KEY_LOGGING)) {
            logging = Boolean.parseBoolean(value); // default is null and perfectly acceptable
        } else {
            throw new IllegalArgumentException(
                    "This parameter is unknown !" + name + " : " + value);
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
    protected final byte[] getATR() {
        return card.getATR().getBytes();
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
     * The card access may be set to 'Exclusive' through the reader's settings.
     *
     * In this case be aware that on some platforms (ex. Windows 8+), the exclusivity is granted for
     * a limited time (ex. 5 seconds). After this delay, the card is automatically resetted.
     * 
     * @throws KeypleChannelStateException if a reader error occurs
     */
    @Override
    protected final void openPhysicalChannel() throws KeypleChannelStateException {
        // init of the physical SE channel: if not yet established, opening of a new physical
        // channel
        try {
            if (card == null) {
                if (isLogicalChannelOpen()) {
                    throw new KeypleChannelStateException(
                            "Logical channel found open while physical channel is not!");
                }
                this.card = this.terminal.connect(parameterCardProtocol);
                if (cardExclusiveMode) {
                    card.beginExclusive();
                    if (logging) {
                        logger.trace("[{}] Opening of a physical SE channel in exclusive mode.",
                                this.getName());
                    }
                } else {
                    if (logging) {
                        logger.trace("[{}] Opening of a physical SE channel in shared mode.",
                                this.getName());
                    }
                }
            }
            this.channel = card.getBasicChannel();
        } catch (CardException e) {
            throw new KeypleChannelStateException("Error while opening Physical Channel", e);
        }
    }
}

/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.pcsc;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.smartcardio.*;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.plugin.*;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PcscReaderImpl extends AbstractThreadedObservableLocalReader
        implements PcscReader, SmartInsertionReader, SmartPresenceReader {

    private static final Logger logger = LoggerFactory.getLogger(PcscReaderImpl.class);

    private static final String PROTOCOL_T0 = "T=0";
    private static final String PROTOCOL_T1 = "T=1";
    private static final String PROTOCOL_T_CL = "T=CL";
    private static final String PROTOCOL_ANY = "T=0";

    /* timeout monitoring is disabled by default */
    private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 0;

    private final CardTerminal terminal;

    private String parameterCardProtocol;
    private boolean cardExclusiveMode;
    private boolean cardReset;
    private TransmissionMode transmissionMode;

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
    protected PcscReaderImpl(String pluginName, CardTerminal terminal) {
        super(pluginName, terminal.getName());
        this.terminal = terminal;
        this.card = null;
        this.channel = null;

        // Using null values to use the standard method for defining default values
        try {
            setParameter(SETTING_KEY_TRANSMISSION_MODE, null);
            setParameter(SETTING_KEY_PROTOCOL, null);
            setParameter(SETTING_KEY_MODE, null);
            setParameter(SETTING_KEY_DISCONNECT, null);
            setParameter(SETTING_KEY_LOGGING, null);
        } catch (KeypleBaseException ex) {
            // can not fail with null value
        }
    }

    @Override
    protected void closePhysicalChannel() throws KeypleChannelStateException {
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
    protected boolean checkSePresence() throws NoStackTraceThrowable {
        try {
            return terminal.isCardPresent();
        } catch (CardException e) {
            logger.trace("[{}] Exception occured in isSePresent. Message: {}", this.getName(),
                    e.getMessage());
            throw new NoStackTraceThrowable();
        }
    }

    @Override
    public boolean waitForCardPresent(long timeout) {
        try {
            return terminal.waitForCardPresent(timeout);
        } catch (CardException e) {
            logger.trace("[{}] Exception occured in waitForCardPresent. Message: {}",
                    this.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Wait for the card absent event from smartcard.io
     * 
     * @param timeout waiting time in ms
     * @return true if the card is removed within the delay
     */
    // @Override
    public boolean waitForCardAbsentNative(long timeout) {
        try {
            if (terminal.waitForCardAbsent(timeout)) {
                return true;
            } else {
                return false;
            }
        } catch (CardException e) {
            logger.trace("[{}] Exception occured in waitForCardAbsentNative. Message: {}",
                    this.getName(), e.getMessage());
            return false;
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
    protected byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        ResponseAPDU apduResponseData;
        try {
            apduResponseData = channel.transmit(new CommandAPDU(apduIn));
        } catch (CardException e) {
            throw new KeypleIOReaderException(this.getName() + ":" + e.getMessage());
        } catch (IllegalArgumentException e) {
            // card could have been removed prematurely
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
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) throws KeypleReaderException {
        boolean result;
        // Test protocolFlag to check if ATR based protocol filtering is required
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
            String atr = ByteArrayUtil.toHex(card.getATR().getBytes());
            if (!p.matcher(atr).matches()) {
                if (logging) {
                    logger.trace(
                            "[{}] protocolFlagMatches => unmatching SE. PROTOCOLFLAG = {}, ATR = {}, MASK = {}",
                            this.getName(), protocolFlag, atr, selectionMask);
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
        if (name.equals(SETTING_KEY_TRANSMISSION_MODE)) {
            if (value == null) {
                transmissionMode = null;
            } else if (value.equals(SETTING_TRANSMISSION_MODE_CONTACTS)) {
                transmissionMode = TransmissionMode.CONTACTS;
            } else if (value.equals(SETTING_TRANSMISSION_MODE_CONTACTLESS)) {
                transmissionMode = TransmissionMode.CONTACTLESS;
            } else {
                throw new IllegalArgumentException("Bad tranmission mode " + name + " : " + value);
            }
        } else if (name.equals(SETTING_KEY_PROTOCOL)) {
            if (value == null || value.equals(SETTING_PROTOCOL_TX)) {
                parameterCardProtocol = "*";
            } else if (value.equals(SETTING_PROTOCOL_T0)) {
                parameterCardProtocol = "T=0";
            } else if (value.equals(SETTING_PROTOCOL_T1)) {
                parameterCardProtocol = "T=1";
            } else if (value.equals(SETTING_PROTOCOL_T_CL)) {
                parameterCardProtocol = "T=CL";
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
            if (value == null) {
                setThreadWaitTimeout(SETTING_THREAD_TIMEOUT_DEFAULT);
            } else {
                long timeout = Long.parseLong(value);

                if (timeout <= 0) {
                    throw new IllegalArgumentException(
                            "Timeout has to be of at least 1ms " + name + value);
                }
                setThreadWaitTimeout(timeout);
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

        return parameters;
    }

    @Override
    protected byte[] getATR() {
        return card.getATR().getBytes();
    }

    /**
     * Tells if a physical channel is open
     * <p>
     * This status may be wrong if the card has been removed.
     * <p>
     * The caller should test the card presence with isSePresent before calling this method.
     * 
     * @return true if the physical channel is open
     */
    @Override
    protected boolean isPhysicalChannelOpen() {
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
    protected void openPhysicalChannel() throws KeypleChannelStateException {
        // init of the physical SE channel: if not yet established, opening of a new physical
        // channel
        try {
            if (card == null) {
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

    /**
     * The transmission mode can set with setParameter(SETTING_KEY_TRANSMISSION_MODE, )
     * <p>
     * When the transmission mode has not explicitly set, it is deduced from the protocol:
     * <ul>
     * <li>T=0: contacts mode</li>
     * <li>T=1: contactless mode</li>
     * </ul>
     * 
     * @return the current transmission mode
     */
    @Override
    public TransmissionMode getTransmissionMode() {
        if (transmissionMode != null) {
            return transmissionMode;
        } else {
            if (parameterCardProtocol.contentEquals(PROTOCOL_T1)
                    || parameterCardProtocol.contentEquals(PROTOCOL_T_CL)) {
                return TransmissionMode.CONTACTLESS;
            } else {
                return TransmissionMode.CONTACTS;
            }
        }
    }
}

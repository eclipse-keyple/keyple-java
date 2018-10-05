/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * List of APDU requests that will result in a {@link SeResponse}
 * 
 * @see SeResponse
 */
public final class SeRequest implements Serializable {

    static final long serialVersionUID = 6018469841127325812L;

    /**
     * The Selector inner class is dedicated to handle the selection of the SE either through a
     * selection command with AID (AtrSelector) or through a matching test between the SE ATR and a
     * regular expression (AtrSelector).
     *
     */
    public static abstract class Selector {
    }

    public static final class AidSelector extends SeRequest.Selector {

        public static final int AID_MIN_LENGTH = 5;
        public static final int AID_MAX_LENGTH = 16;

        /**
         * - AID’s bytes of the SE application to select. In case the SE application is currently
         * not selected, a logical channel is established and the corresponding SE application is
         * selected by the SE reader, otherwise keep the current channel.
         *
         * - Could be missing when operating SE which don’t support the Select Application command
         * (as it is the case for CSM).
         */
        private byte[] aidToSelect;

        /**
         * AID based selector
         * 
         * @param aidToSelect ByteBuffer
         */
        public AidSelector(byte[] aidToSelect) {
            if (aidToSelect.length < AID_MIN_LENGTH || aidToSelect.length > AID_MAX_LENGTH) {
                throw new IllegalArgumentException(
                        String.format("Bad AID length: %d", aidToSelect.length));
            }
            this.aidToSelect = aidToSelect;
        }

        public byte[] getAidToSelect() {
            return aidToSelect;
        }

        public String toString() {
            return String.format("AID:%s",
                    aidToSelect == null ? "null" : ByteArrayUtils.toHex(aidToSelect));
        }
    }

    public static final class AtrSelector extends SeRequest.Selector {
        /**
         * Regular expression dedicated to handle SE logical channel opening based on ATR pattern
         */
        private String atrRegex;

        /**
         * ATR based selection
         *
         * @param atrRegex String hex regular expression
         */
        public AtrSelector(String atrRegex) {
            this.atrRegex = atrRegex;
        }

        /**
         * Getter for the regular expression of the selector
         * 
         * @return Regular expression
         */
        public String getAtrRegex() {
            return atrRegex;
        }

        /**
         * Tells if the provided ATR matches the registered regular expression
         *
         * If the registered regular expression is empty, the ATR is always matching.
         *
         * @param atr a buffer containing the ATR to be checked
         * @return a boolean true the ATR matches the current regex
         */
        public boolean atrMatches(byte[] atr) {
            boolean m;
            if (atrRegex.length() != 0) {
                Pattern p = Pattern.compile(atrRegex);
                String atrString = ByteArrayUtils.toHex(atr);
                m = p.matcher(atrString).matches();
            } else {
                m = true;
            }
            return m;
        }

        public String toString() {
            return String.format("ATR regex:%s", atrRegex.length() != 0 ? atrRegex : "empty");
        }
    }

    /**
     * SE selector is either an AID or an ATR regular expression
     */
    private final Selector selector;

    /**
     * List of status codes in response to the select application command that should be considered
     * successful although they are different from 9000
     */
    private Set<Short> successfulSelectionStatusCodes = new LinkedHashSet<Short>();

    /**
     * contains a group of APDUCommand to operate on the selected SE application by the SE reader.
     */
    private List<ApduRequest> apduRequests;


    /**
     * the protocol flag is used to target specific SE technologies for a given request
     */
    private SeProtocol protocolFlag = null;

    /**
     * the final logical channel status: if true, the SE reader keep active the logical channel of
     * the SE application after processing the group of APDU commands. If false, the SE reader will
     * close the logical channel of the SE application after processing the group of APDU commands
     * (i.e. after the receipt of the last APDU response).
     */
    private boolean keepChannelOpen;

    /**
     * The constructor called by a ProxyReader in order to open a logical channel, to send a set of
     * APDU commands to a SE application, or both of them.
     * <ul>
     * <li>For SE requiring an AID based selection, the Selector should be defined with a non null
     * ByteBuffer value.</li>
     * <li>For SE requiring an ATR based selection, the Selector should be defined with a non null
     * String regular expression.</li>
     * <li>For SE supporting neither AID selection nor ATR selection, the Selector should be defined
     * as null.</li>
     * <li>The protocolFlag parameter is optional.</li>
     * </ul>
     *
     * @param selector the SE selector
     * @param apduRequests the apdu requests
     * @param keepChannelOpen the keep channel open
     * @param protocolFlag the expected protocol
     * @param successfulSelectionStatusCodes a list of successful status codes for the select
     *        application command
     */
    public SeRequest(Selector selector, List<ApduRequest> apduRequests, boolean keepChannelOpen,
            SeProtocol protocolFlag, Set<Short> successfulSelectionStatusCodes) {
        this.selector = selector;
        this.apduRequests = apduRequests;
        this.keepChannelOpen = keepChannelOpen;
        this.protocolFlag = protocolFlag;
        this.successfulSelectionStatusCodes = successfulSelectionStatusCodes;
    }

    /**
     * Alternate constructor with no list of successful selection status codes set and a protocol
     * flag
     * 
     * @param selector the AID or ATR selector
     * @param apduRequests a list of ApudRequest
     * @param keepChannelOpen a flag to tell if the channel has to be closed at the end
     * @param protocolFlag the expected protocol flag
     */
    public SeRequest(Selector selector, List<ApduRequest> apduRequests, boolean keepChannelOpen,
            SeProtocol protocolFlag) {
        this(selector, apduRequests, keepChannelOpen, protocolFlag, null);
    }

    /**
     * Alternate constructor with a list of successful selection status codes set and no protocol
     * flag
     *
     * @param selector the AID or ATR selector
     * @param apduRequests a list of ApudRequest
     * @param keepChannelOpen a flag to tell if the channel has to be closed at the end
     * @param successfulSelectionStatusCodes a list of successful status codes for the select
     *        application command
     *
     */
    public SeRequest(Selector selector, List<ApduRequest> apduRequests, boolean keepChannelOpen,
            Set<Short> successfulSelectionStatusCodes) {
        this(selector, apduRequests, keepChannelOpen, null, successfulSelectionStatusCodes);
    }

    /**
     * Alternate constructor with no protocol flag set
     * 
     * @param selector the AID or ATR selector
     * @param apduRequests a list of ApudRequest
     * @param keepChannelOpen a flag to tell if the channel has to be closed at the end
     */
    public SeRequest(Selector selector, List<ApduRequest> apduRequests, boolean keepChannelOpen) {
        this(selector, apduRequests, keepChannelOpen, null, null);
    }


    /**
     * Gets the SE selector.
     *
     * @return the current SE selector
     */
    public Selector getSelector() {
        return this.selector;
    }

    /**
     * Gets the apdu requests.
     *
     * @return the group of APDUs to be transmitted to the SE application for this instance of
     *         SERequest.
     */
    public List<ApduRequest> getApduRequests() {
        return apduRequests;
    }

    /**
     * Define if the channel should be kept open after the the {@link SeRequestSet} has been
     * executed.
     *
     * @return If the channel should be kept open
     */
    public boolean isKeepChannelOpen() {
        return keepChannelOpen;
    }

    /**
     * Gets the protocol flag of the request
     * 
     * @return protocolFlag
     */
    public SeProtocol getProtocolFlag() {
        return this.protocolFlag;
    }

    /**
     * Gets the list of successful selection status codes
     * 
     * @return the list of status codes
     */
    public Set<Short> getSuccessfulSelectionStatusCodes() {
        return successfulSelectionStatusCodes;
    }

    @Override
    public String toString() {
        return String.format("SeRequest:{REQUESTS = %s, SELECTOR = %s, KEEPCHANNELOPEN = %s}",
                getApduRequests(), getSelector(), this.keepChannelOpen);
    }
}

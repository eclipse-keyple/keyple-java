/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * List of APDU requests that will result in a {@link SeResponse}
 * 
 * @see SeResponse
 */
public final class SeRequest {

    /**
     * - AID’s bytes of the SE application to select. In case the SE application is currently not
     * selected, a logical channel is established and the corresponding SE application is selected
     * by the SE reader, otherwise keep the current channel.
     *
     * - Could be missing when operating SE which don’t support the Select Application command (as
     * it is the case for CSM).
     */
    private ByteBuffer aidToSelect;

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
     * <li>For PO requiring an AID selection, the aidToSelect should be defined with non null
     * value.</li>
     * <li>For PO not supporting AID selection, the aidToSelect should be defined as null. - The
     * protocolFlag parameter is optional.</li>
     * </ul>
     *
     * @param aidToSelect the aid to select
     * @param apduRequests the apdu requests
     * @param keepChannelOpen the keep channel open
     * @param protocolFlag the expected protocol
     * @param successfulSelectionStatusCodes a list of successful status codes for the select
     *        application command
     */
    public SeRequest(ByteBuffer aidToSelect, List<ApduRequest> apduRequests,
            boolean keepChannelOpen, SeProtocol protocolFlag,
            Set<Short> successfulSelectionStatusCodes) {
        this.aidToSelect = aidToSelect;
        this.apduRequests = apduRequests;
        this.keepChannelOpen = keepChannelOpen;
        this.protocolFlag = protocolFlag;
        this.successfulSelectionStatusCodes = successfulSelectionStatusCodes;
    }

    /**
     * Alternate constructor with no list of successful selection status codes set and a protocol
     * flag
     * 
     * @param aidToSelect
     * @param apduRequests
     * @param keepChannelOpen
     * @param protocolFlag
     */
    public SeRequest(ByteBuffer aidToSelect, List<ApduRequest> apduRequests,
            boolean keepChannelOpen, SeProtocol protocolFlag) {
        this(aidToSelect, apduRequests, keepChannelOpen, protocolFlag, null);
    }

    /**
     * Alternate constructor with a list of successful selection status codes set and no protocol
     * flag
     *
     * @param aidToSelect
     * @param apduRequests
     * @param keepChannelOpen
     * @param successfulSelectionStatusCodes a list of successful status codes for the select
     *        application command
     *
     */
    public SeRequest(ByteBuffer aidToSelect, List<ApduRequest> apduRequests,
            boolean keepChannelOpen, Set<Short> successfulSelectionStatusCodes) {
        this(aidToSelect, apduRequests, keepChannelOpen, null, successfulSelectionStatusCodes);
    }

    /**
     * Alternate constructor with no protocol flag set
     * 
     * @param aidToSelect
     * @param apduRequests
     * @param keepChannelOpen
     */
    public SeRequest(ByteBuffer aidToSelect, List<ApduRequest> apduRequests,
            boolean keepChannelOpen) {
        this(aidToSelect, apduRequests, keepChannelOpen, null, null);
    }


    /**
     * Gets the aid to select.
     *
     * @return the current AID set to select
     */
    public ByteBuffer getAidToSelect() {
        return aidToSelect;
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
        return String.format("SeRequest{requests=%s}", getApduRequests());
    }
}

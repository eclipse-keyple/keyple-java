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
package org.eclipse.keyple.seproxy.message;

import java.io.Serializable;
import java.util.List;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * Group of SE responses received in response to a {@link SeRequest}.
 * 
 * @see SeRequest
 */
@SuppressWarnings("PMD.NPathComplexity")
public final class SeResponse implements Serializable {

    static final long serialVersionUID = 265369841119873812L;

    /**
     * is defined as true by the SE reader in case a logical channel was already open with the
     * target SE application.
     */
    private boolean channelPreviouslyOpen;

    /**
     * true if the channel is open
     */
    private final boolean logicalChannelIsOpen;

    private final SelectionStatus selectionStatus;

    /**
     * could contain a group of APDUResponse returned by the selected SE application on the SE
     * reader.
     */
    private List<ApduResponse> apduResponses;

    /**
     * the constructor called by a ProxyReader during the processing of the ‘transmit’ method.
     *
     * @param logicalChannelIsOpen the current channel status
     * @param channelPreviouslyOpen the channel previously open
     * @param selectionStatus the SE selection status
     * @param apduResponses the apdu responses
     */
    public SeResponse(boolean logicalChannelIsOpen, boolean channelPreviouslyOpen,
            SelectionStatus selectionStatus, List<ApduResponse> apduResponses) {
        this.logicalChannelIsOpen = logicalChannelIsOpen;
        this.channelPreviouslyOpen = channelPreviouslyOpen;
        this.selectionStatus = selectionStatus;
        this.apduResponses = apduResponses;
    }

    /**
     * Was channel previously open.
     *
     * @return the previous state of the logical channel.
     */
    public boolean wasChannelPreviouslyOpen() {
        return channelPreviouslyOpen;
    }

    /**
     * Get the logical channel status
     * 
     * @return true if the logical channel is open
     */
    public boolean isLogicalChannelOpen() {
        return logicalChannelIsOpen;
    }

    /**
     * Gets the selection status and its associated data.
     *
     * @return a {@link SelectionStatus} object.
     */
    public SelectionStatus getSelectionStatus() {
        return this.selectionStatus;
    }

    /**
     * Gets the apdu responses.
     *
     * @return the group of APDUs responses returned by the SE application for this instance of
     *         SEResponse.
     */
    public List<ApduResponse> getApduResponses() {
        return apduResponses;
    }

    @Override
    public String toString() {
        /*
         * getAtr() can return null, we must check it to avoid the call to getBytes() that would
         * raise an exception. In case of a null value, String.format prints "null" in the string,
         * the same is done here.
         */
        String string;
        if (selectionStatus != null) {
            string = String.format(
                    "SeResponse:{RESPONSES = %s, ATR = %s, FCI = %s, HASMATCHED = %b CHANNELWASOPEN = %b "
                            + "LOGICALCHANNEL = %s}",
                    getApduResponses(),
                    selectionStatus.getAtr().getBytes() == null ? "null"
                            : ByteArrayUtils.toHex(selectionStatus.getAtr().getBytes()),
                    ByteArrayUtils.toHex(selectionStatus.getFci().getBytes()),
                    selectionStatus.hasMatched(), wasChannelPreviouslyOpen(),
                    logicalChannelIsOpen ? "OPEN" : "CLOSED");
        } else {
            string = String.format(
                    "SeResponse:{RESPONSES = %s, ATR = null, FCI = null, HASMATCHED = false CHANNELWASOPEN = %b "
                            + "LOGICALCHANNEL = %s}",
                    getApduResponses(), wasChannelPreviouslyOpen(),
                    logicalChannelIsOpen ? "OPEN" : "CLOSED");
        }
        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SeResponse)) {
            return false;
        }

        SeResponse seResponse = (SeResponse) o;
        return seResponse.getSelectionStatus().equals(selectionStatus)
                && (seResponse.getApduResponses() == null ? apduResponses == null
                        : seResponse.getApduResponses().equals(apduResponses))
                && seResponse.isLogicalChannelOpen() == logicalChannelIsOpen
                && seResponse.wasChannelPreviouslyOpen() == channelPreviouslyOpen;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash
                + (selectionStatus.getAtr() == null ? 0 : selectionStatus.getAtr().hashCode());
        hash = 7 * hash + (apduResponses == null ? 0 : this.apduResponses.hashCode());
        hash = 29 * hash + (this.channelPreviouslyOpen ? 1 : 0);
        hash = 37 * hash + (this.logicalChannelIsOpen ? 1 : 0);
        return hash;
    }
}

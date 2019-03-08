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
package org.eclipse.keyple.transaction;

import org.eclipse.keyple.seproxy.message.SeResponse;

/**
 * MatchingSe is the class to manage the elements of the result of a selection.
 *
 */
public class MatchingSe {
    private final boolean channelIsKeptOpen;
    private final String extraInfo;
    private SeResponse selectionSeResponse;

    /**
     * Constructor taking a SeSelector as an argument. Keeps the isKeepChannelOpen flag and the
     * extraInfo for later usage.
     * 
     * @param seSelectionRequest the seSelector
     */
    public MatchingSe(SeSelectionRequest seSelectionRequest) {
        this.channelIsKeptOpen = seSelectionRequest.getSelectionRequest().isKeepChannelOpen();
        extraInfo = seSelectionRequest.getSeSelector().getExtraInfo();
    }

    /**
     * Sets the SeResponse obtained in return from the selection process
     * 
     * @param selectionResponse the selection SeResponse
     */
    public void setSelectionResponse(SeResponse selectionResponse) {
        this.selectionSeResponse = selectionResponse;
    }

    /**
     * Gets the SeResponse obtained in return from the selection process
     * 
     * @return the selection SeResponse
     */
    public final SeResponse getSelectionSeResponse() {
        return selectionSeResponse;
    }

    /**
     * Indicates whether the current SE is eligible to application selection in preparation for a
     * transaction.
     * <p>
     * The SE will only be eligible if the logical channel is required to be kept open after the
     * selection process.
     * 
     * @return true or false
     */
    protected final boolean isSelectable() {
        return channelIsKeptOpen;
    }

    /**
     * Indicates whether the current SE has been identified as selected: the logical channel is open
     * and the selection process returned either a FCI or an ATR
     * 
     * @return true or false
     */
    public final boolean isSelected() {
        return channelIsKeptOpen && selectionSeResponse != null
                && selectionSeResponse.getSelectionStatus() != null
                && selectionSeResponse.getSelectionStatus().hasMatched();
    }

    /**
     * Gets back the information string provided in the constructor for information purposes (logs)
     * 
     * @return a string
     */
    public final String getExtraInfo() {
        return extraInfo;
    }

    /**
     * Restore the initial state of the MatchingSe.
     * <p>
     * Called by SeSelection at the beginning of the processing of a selection
     * <p>
     * This method should be overloaded by the objects derived from MatchingSe in order to reset
     * their additional attributes.
     */
    protected void reset() {
        selectionSeResponse = null;
    }
}

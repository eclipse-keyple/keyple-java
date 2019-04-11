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
import org.eclipse.keyple.seproxy.message.SelectionStatus;

/**
 * MatchingSe is the class to manage the elements of the result of a selection.
 *
 */
public class MatchingSe {
    private final SeResponse selectionResponse;
    private final SelectionStatus selectionStatus;
    private final String selectionExtraInfo;

    /**
     * Constructor.
     */
    public MatchingSe(SeResponse selectionResponse, String extraInfo) {
        this.selectionResponse = selectionResponse;
        if (selectionResponse != null) {
            this.selectionStatus = selectionResponse.getSelectionStatus();
        } else {
            this.selectionStatus = null;
        }
        this.selectionExtraInfo = extraInfo;
    }

    /**
     * Indicates whether the current SE has been identified as selected: the logical channel is open
     * and the selection process returned either a FCI or an ATR
     * 
     * @return true or false
     */
    public final boolean isSelected() {
        boolean isSelected;
        if (selectionStatus != null) {
            isSelected = selectionStatus.hasMatched() && selectionResponse.isLogicalChannelOpen();
        } else {
            isSelected = false;
        }
        return isSelected;
    }

    /**
     * @return the SE {@link SelectionStatus}
     */
    public SelectionStatus getSelectionStatus() {
        return selectionStatus;
    }

    /**
     * @return the selection extra info string
     */
    public String getSelectionExtraInfo() {
        return selectionExtraInfo;
    }
}

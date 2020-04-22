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
package org.eclipse.keyple.core.selection;

import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

/**
 * AbstractMatchingSe is the class to manage the elements of the result of a selection.
 *
 */
public abstract class AbstractMatchingSe {
    private final SeResponse selectionResponse;
    private final TransmissionMode transmissionMode;
    private final SelectionStatus selectionStatus;

    /**
     * Constructor.
     * 
     * @param selectionResponse the response from the SE
     * @param transmissionMode the transmission mode, contact or contactless
     */
    protected AbstractMatchingSe(SeResponse selectionResponse, TransmissionMode transmissionMode) {
        this.selectionResponse = selectionResponse;
        this.transmissionMode = transmissionMode;
        if (selectionResponse != null) {
            this.selectionStatus = selectionResponse.getSelectionStatus();
        } else {
            this.selectionStatus = null;
        }
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
     * @return the SE {@link TransmissionMode} (contacts or contactless)
     */
    public TransmissionMode getTransmissionMode() {
        return transmissionMode;
    }
}

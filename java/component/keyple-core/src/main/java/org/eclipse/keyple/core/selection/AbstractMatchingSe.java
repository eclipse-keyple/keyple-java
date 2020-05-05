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
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

/**
 * AbstractMatchingSe is the class to manage the elements of the result of a selection.<br>
 * This class should be extended for the management of specific SE.<br>
 * Nevertheless it gives access to the generic parameters common to all SEs which are the
 * transmission mode, the FCI (response to select command) and the ATR (SE's answer to reset) when
 * they are available.
 */
public abstract class AbstractMatchingSe {
    private final byte[] fci;
    private final byte[] atr;
    private final TransmissionMode transmissionMode;

    /**
     * Constructor.
     * 
     * @param selectionResponse the response from the SE
     * @param transmissionMode the transmission mode, contact or contactless
     */
    protected AbstractMatchingSe(SeResponse selectionResponse, TransmissionMode transmissionMode) {
        this.fci = selectionResponse.getSelectionStatus().getFci().getBytes();
        this.atr = selectionResponse.getSelectionStatus().getAtr().getBytes();
        this.transmissionMode = transmissionMode;
    }

    /**
     * @return the SE {@link TransmissionMode} (contacts or contactless)
     */
    public TransmissionMode getTransmissionMode() {
        return transmissionMode;
    }

    /**
     * @return true if the matching SE has an FCI
     */
    public boolean hasFci() {
        return fci != null && fci.length > 0;
    }

    /**
     * @return true if the matching SE has an ATR
     */
    public boolean hasAtr() {
        return fci != null && fci.length > 0;
    }

    /**
     * @return the FCI
     * @throws IllegalStateException if no FCI is available (see hasFci)
     */
    public byte[] getFci() {
        if (hasFci()) {
            return fci;
        }
        throw new IllegalStateException("No FCI is available in this AbstractMatchingSe");
    }

    /**
     * @return the ATR
     * @throws IllegalStateException if no ATR is available (see hasAtr)
     */
    public byte[] getAtr() {
        if (hasAtr()) {
            return atr;
        }
        throw new IllegalStateException("No ATR is available in this AbstractMatchingSe");
    }
}

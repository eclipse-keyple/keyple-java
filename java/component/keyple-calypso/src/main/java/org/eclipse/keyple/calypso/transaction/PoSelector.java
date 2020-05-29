/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;

import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;

/**
 * The {@link PoSelector} class extends {@link SeSelector} to handle specific PO features such as
 * the additional successful status codes list (in response to a select application command)
 */
public final class PoSelector extends SeSelector {
    private static final int SW_PO_INVALIDATED = 0x6283;

    /**
     * Indicates if an invalidated PO should be selected or not.
     * <p>
     * The acceptance of an invalid PO is determined with the additional successful status codes
     * specified in the {@link AidSelector}
     */
    public enum InvalidatedPo {
        REJECT, ACCEPT
    }

    /**
     * Create a PoSelector to perform the PO selection. See {@link SeSelector}
     *
     * @param seProtocol the SE communication protocol
     * @param atrFilter the ATR filter
     * @param aidSelector the AID selection data
     * @param authorization enum to allow invalidated POs to be accepted
     */
    public PoSelector(SeProtocol seProtocol, AtrFilter atrFilter, AidSelector aidSelector,
            InvalidatedPo authorization) {
        super(seProtocol, atrFilter, aidSelector);
        if (authorization == InvalidatedPo.ACCEPT) {
            aidSelector.addSuccessfulStatusCode(SW_PO_INVALIDATED);
        }
    }
}

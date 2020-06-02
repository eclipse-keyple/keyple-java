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

    public PoSelector(PoSelectorBuilder<?> builder) {
        super(builder);
        if (builder.invalidatedPo == InvalidatedPo.ACCEPT) {
            this.getAidSelector().addSuccessfulStatusCode(SW_PO_INVALIDATED);
        }
    }

    /**
     * Create a PoSelector to perform the PO selection. See {@link SeSelector}<br>
     * All fields are optional
     * <ul>
     * <li>aid the AID selection data</li>
     * <li>seProtocol the SE communication protocol</li>
     * <li>atrFilter the ATR filter</li>
     * <li>authorization enum to allow invalidated POs to be accepted</li>
     * </ul>
     * 
     * @since 0.9
     */
    protected abstract static class PoSelectorBuilder<T extends PoSelectorBuilder<T>>
            extends SeSelector.SeSelectorBuilder<T> {
        private InvalidatedPo invalidatedPo;

        protected PoSelectorBuilder() {
            super();
        }

        public T invalidatedPo(InvalidatedPo invalidatedPo) {
            this.invalidatedPo = invalidatedPo;
            return self();
        }

        @Override
        public PoSelector build() {
            return new PoSelector(this);
        }
    }

    /**
     * Gets a new builder.
     */
    public static class Builder extends PoSelectorBuilder<PoSelector.Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }
}

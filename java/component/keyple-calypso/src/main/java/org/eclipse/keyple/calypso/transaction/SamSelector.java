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

import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.core.seproxy.SeSelector;

/**
 * The {@link SamSelector} class extends {@link SeSelector} to handle specific Calypso SAM needs
 * such as model identification.
 */
public class SamSelector extends SeSelector {

    /** Private constructor */
    private SamSelector(SamSelectorBuilder builder) {
        super(builder);
        String atrRegex;
        String snRegex;
        /* check if serialNumber is defined */
        if (builder.serialNumber == null || builder.serialNumber.isEmpty()) {
            /* match all serial numbers */
            snRegex = ".{8}";
        } else {
            /* match the provided serial number (could be a regex substring) */
            snRegex = builder.serialNumber;
        }
        /*
         * build the final Atr regex according to the SAM subtype and serial number if any.
         *
         * The header is starting with 3B, its total length is 4 or 6 bytes (8 or 10 hex digits)
         */
        switch (builder.samRevision) {
            case C1:
            case S1D:
            case S1E:
                atrRegex = "3B(.{6}|.{10})805A..80" + builder.samRevision.getApplicationTypeMask()
                        + "20.{4}" + snRegex + "829000";
                break;
            case AUTO:
                /* match any ATR */
                atrRegex = ".*";
                break;
            default:
                throw new IllegalArgumentException("Unknown SAM subtype.");
        }
        this.getAtrFilter().setAtrRegex(atrRegex);
    }

    /**
     * Create a SeSelector to perform the SAM selection with<br>
     * either
     * <ul>
     * <li>samRevision the {@link SamRevision} of the targeted SAM</li>
     * <li>serialNumber the serial number of the targeted SAM as an hex string</li>
     * </ul>
     * or
     * <ul>
     * <li>samIdentifier the {@link SamIdentifier} object embedding the {@link SamRevision}, the
     * serial number and group reference</li>
     * </ul>
     */
    protected abstract static class SamSelectorBuilder<T extends SamSelector.SamSelectorBuilder<T>>
            extends SeSelector.SeSelectorBuilder<T> {
        private SamRevision samRevision;
        private String serialNumber;

        public SamSelectorBuilder() {
            super();
            this.atrFilter(new AtrFilter(""));
        }

        public T samRevision(SamRevision samRevision) {
            this.samRevision = samRevision;
            return self();
        }

        public T serialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return self();
        }

        public T samIdentifier(SamIdentifier samIdentifier) {
            samRevision = samIdentifier.getSamRevision();
            serialNumber = samIdentifier.getSerialNumber();
            return self();
        }

        @Override
        public SamSelector build() {
            return new SamSelector(this);
        }
    }

    /**
     * Gets a new builder.
     */
    public static class Builder extends SamSelector.SamSelectorBuilder<SamSelector.Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }
}

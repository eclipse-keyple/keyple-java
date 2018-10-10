/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.transaction;

import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.seproxy.SeResponse;

/**
 * Handles the PO characteristics such as:
 * <ul>
 * <li>revision</li>
 * <li>serial number</li>
 * <li>session buffer limit</li>
 * </ul>
 */
public final class CalypsoPO {
    private PoRevision revision;
    private GetDataFciRespPars poFciRespPars;

    public CalypsoPO(SeResponse selectionSeResponse) {
        /* Parse PO FCI - to retrieve Calypso Revision, Serial Number, &amp; DF Name (AID) */
        this.poFciRespPars = new GetDataFciRespPars(selectionSeResponse.getFci());

        /**
         * Resolve the PO revision from the application type byte:
         *
         * <ul>
         * <li>if
         * <code>%1-------</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;CLAP&nbsp;&nbsp;&rarr;&nbsp;&nbsp;
         * REV3.1</li>
         * <li>if <code>%00101---</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.2</li>
         * <li>if <code>%00100---</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.1</li>
         * <li>otherwise&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV2.4</li>
         * </ul>
         */
        // TODO Improve this code by taking into account the startup information and the atr
        byte applicationTypeByte = this.poFciRespPars.getApplicationTypeByte();
        if ((applicationTypeByte & (1 << 7)) != 0) {
            /* CLAP */
            this.revision = PoRevision.REV3_1;
        } else if ((applicationTypeByte >> 3) == (byte) (0x05)) {
            this.revision = PoRevision.REV3_2;
        } else if ((applicationTypeByte >> 3) == (byte) (0x04)) {
            this.revision = PoRevision.REV3_1;
        } else {
            this.revision = PoRevision.REV2_4;
        }
    }

    public void setDefaultRevision(PoRevision defaultPoRevision) {
        this.revision = defaultPoRevision;
    }

    public PoRevision getRevision() {
        return this.revision;
    }

    public byte[] getDfName() {
        return this.poFciRespPars.getDfName();
    }

    public byte[] getApplicationSerialNumber() {
        return this.poFciRespPars.getApplicationSerialNumber();
    }

    /**
     * Determine the PO revision from the application type byte:
     *
     * <ul>
     * <li>if
     * <code>%1-------</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;CLAP&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.1</li>
     * <li>if <code>%00101---</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.2</li>
     * <li>if <code>%00100---</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.1</li>
     * <li>otherwise&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV2.4</li>
     * </ul>
     *
     * @return the PO revision
     */
    // TODO Improve this code by taking into account the startup information and the atr
    private void resolvePoRevision(byte applicationTypeByte) {

    }
}

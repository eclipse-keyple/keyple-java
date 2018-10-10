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
    /** singleton instance of CalypsoPO */
    private static CalypsoPO uniqueInstance = new CalypsoPO();

    private static PoRevision revision;
    private static GetDataFciRespPars poFciRespPars;

    /**
     * Instantiates a new {@link CalypsoPO}.
     */
    private CalypsoPO() {}

    /**
     * Gets the single instance of SeProxyService.
     *
     * @return single instance of SeProxyService
     */
    public static CalypsoPO getInstance() {
        return uniqueInstance;
    }

    /**
     * Initialize a new CalypsoPO.
     *
     * When FCI data is not available, the AnswerToReset is interpreted to retrieve the PO profile.
     *
     * @param selectionSeResponse contains FCI data retrieved during application selection and/or
     *        the Answer To Reset
     * @return the singleton unique instance
     */
    public static CalypsoPO initialize(SeResponse selectionSeResponse) {
        /* Parse PO FCI - to retrieve Calypso Revision, Serial Number, &amp; DF Name (AID) */
        poFciRespPars = new GetDataFciRespPars(selectionSeResponse.getFci());
        setRevision(poFciRespPars.getApplicationTypeByte());
        return uniqueInstance;
    }


    public PoRevision getRevision() {
        return revision;
    }

    public byte[] getDfName() {
        return poFciRespPars.getDfName();
    }

    public byte[] getApplicationSerialNumber() {
        return poFciRespPars.getApplicationSerialNumber();
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
    private static void setRevision(byte applicationTypeByte) {
        if ((applicationTypeByte & (1 << 7)) != 0) {
            /* CLAP */
            revision = PoRevision.REV3_1;
        } else if ((applicationTypeByte >> 3) == (byte) (0x05)) {
            revision = PoRevision.REV3_2;
        } else if ((applicationTypeByte >> 3) == (byte) (0x04)) {
            revision = PoRevision.REV3_1;
        } else {
            revision = PoRevision.REV2_4;
        }
    }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso;

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
public final class PortableObject {
    /** singleton instance of PortableObject */
    private static PortableObject uniqueInstance = new PortableObject();

    private PoRevision revision;
    private GetDataFciRespPars poFciRespPars;

    /**
     * Instantiates a new {@link PortableObject}.
     */
    private PortableObject() {}

    /**
     * Gets the single instance of SeProxyService.
     *
     * @return single instance of SeProxyService
     */
    public static PortableObject getInstance() {
        return uniqueInstance;
    }

    /**
     * Instantiates a new PortableObject.
     *
     * When FCI data is not available, the AnswerToReset is interpreted to retrieve the PO profile.
     *
     * @param selectionSeResponse contains FCI data retrieved during application selection and/or
     *        the Answer To Reset
     */
    public void intialize(SeResponse selectionSeResponse) {
        /* Parse PO FCI - to retrieve Calypso Revision, Serial Number, &amp; DF Name (AID) */
        this.poFciRespPars = new GetDataFciRespPars(selectionSeResponse.getFci());
        setRevision(this.poFciRespPars.getApplicationTypeByte());
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
    public PoRevision getRevision() {
        return revision;
    }

    public byte[] getDfName() {
        return this.poFciRespPars.getDfName();
    }

    public byte[] getApplicationSerialNumber() {
        return this.poFciRespPars.getApplicationSerialNumber();
    }

    public boolean isExceedingBufferLimit(int commandLength) {
        // TODO buffer limit check according to PO revision and modification buffer indicator
        return true;
    }

    // TODO identify PO rev1
    private void setRevision(byte applicationTypeByte) {
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
}

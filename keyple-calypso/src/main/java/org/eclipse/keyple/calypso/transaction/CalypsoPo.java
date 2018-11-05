/*
 * Copyright (c) 2017 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.calypso.transaction;


import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CalypsoPo handles the Calypso SE characteristics such as:
 * <ul>
 * <li>revision</li>
 * <li>serial number</li>
 * <li>session buffer limit</li>
 * </ul>
 * TODO Complete with other PO features from the FCI and/or ATR
 */
public final class CalypsoPo extends MatchingSe {
    private static final Logger logger = LoggerFactory.getLogger(CalypsoPo.class);

    private byte[] applicationSerialNumber;
    private PoRevision revision;
    private byte[] dfName;
    private static final int PO_REV1_ATR_LENGTH = 20;
    private static final int REV1_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION = 3;
    private static final int REV2_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION = 6;
    private byte[] poAtr;
    private int modificationsCounterMax;
    private boolean modificationCounterIsInBytes = true;

    public CalypsoPo(PoSelector poSelector) {
        super(poSelector);
    }

    /**
     * Retains the selection response and analyses its relevant information to determine the
     * characteristics of the PO required to process it correctly.
     * 
     * @param selectionResponse the received response to the selection request TODO the parsing of
     *        the FCI should be done using a true BER-TLV library
     */
    @Override
    public void setSelectionResponse(SeResponse selectionResponse) {
        super.setSelectionResponse(selectionResponse);

        /* The selectionSeResponse may not include a FCI field (e.g. old PO Calypso Rev 1) */
        if (selectionResponse.getFci() != null) {
            /* Parse PO FCI - to retrieve Calypso Revision, Serial Number, &amp; DF Name (AID) */
            GetDataFciRespPars poFciRespPars = new GetDataFciRespPars(selectionResponse.getFci());

            /*
             * Resolve the PO revision from the application type byte:
             *
             * <ul> <li>if
             * <code>%1-------</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;CLAP&nbsp;&nbsp;&rarr;&nbsp;&
             * nbsp; REV3.1</li> <li>if
             * <code>%00101---</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.2</li> <li>if
             * <code>%00100---</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.1</li>
             * <li>otherwise&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV2.4</li> </ul>
             */
            // TODO Improve this code by taking into account the startup information and the atr
            byte applicationTypeByte = poFciRespPars.getApplicationTypeByte();
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

            this.dfName = poFciRespPars.getDfName();

            this.applicationSerialNumber = poFciRespPars.getApplicationSerialNumber();

            // TODO review this to take into consideration the type and subtype
            if (this.revision == PoRevision.REV2_4) {
                /* old cards have their modification counter in number of commands */
                modificationCounterIsInBytes = false;
                this.modificationsCounterMax =
                        REV2_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION;
            } else {
                this.modificationsCounterMax = poFciRespPars.getBufferSizeValue();
            }
        } else {
            /*
             * FCI is not provided: we consider it is Calypso PO rev 1, it's serial number is
             * provided in the ATR
             */
            poAtr = selectionResponse.getAtr().getBytes();

            /* basic check: we expect to be here following a selection based on the ATR */
            if (poAtr.length != PO_REV1_ATR_LENGTH) {
                throw new IllegalStateException(
                        "Unexpected ATR length: " + ByteArrayUtils.toHex(poAtr));
            }

            this.revision = PoRevision.REV1_0;
            this.dfName = null;
            this.applicationSerialNumber = new byte[8];
            /* old cards have their modification counter in number of commands */
            this.modificationCounterIsInBytes = false;
            this.modificationsCounterMax =
                    REV1_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION;
            /*
             * the array is initialized with 0 (cf. default value for primitive types)
             */
            System.arraycopy(poAtr, 12, this.applicationSerialNumber, 4, 4);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("REVISION = {}, SERIALNUMBER = {}, DFNAME = {}", this.revision,
                    ByteArrayUtils.toHex(this.applicationSerialNumber),
                    ByteArrayUtils.toHex(this.dfName));
        }
    }

    public PoRevision getRevision() {
        return this.revision;
    }

    public byte[] getDfName() {
        return dfName;
    }

    public byte[] getApplicationSerialNumber() {
        return applicationSerialNumber;
    }

    public byte[] getAtr() {
        return poAtr;
    }

    public boolean isModificationsCounterInBytes() {
        return modificationCounterIsInBytes;
    }

    public int getModificationsCounter() {
        return modificationsCounterMax;
    }
}

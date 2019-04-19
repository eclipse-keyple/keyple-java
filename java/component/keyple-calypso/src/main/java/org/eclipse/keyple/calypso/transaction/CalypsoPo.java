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
package org.eclipse.keyple.calypso.transaction;



import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.transaction.MatchingSe;
import org.eclipse.keyple.core.util.ByteArrayUtil;
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
    private final byte bufferSizeIndicator;
    private final int bufferSizeValue;
    private final byte platform;
    private final byte applicationType;
    private final boolean isRev3_2ModeAvailable;
    private final boolean isRatificationCommandRequired;
    private final boolean hasCalypsoStoredValue;
    private final boolean hasCalypsoPin;
    private final byte applicationSubtypeByte;
    private final byte softwareIssuerByte;
    private final byte softwareVersion;
    private final byte softwareRevision;
    private final boolean isDfInvalidated;
    private byte[] applicationSerialNumber;
    private PoRevision revision;
    private byte[] dfName;
    private static final int PO_REV1_ATR_LENGTH = 20;
    private static final int REV1_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION = 3;
    private static final int REV2_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION = 6;
    private byte[] poAtr;
    private int modificationsCounterMax;
    private boolean modificationCounterIsInBytes = true;

    /**
     * Constructor.
     *
     * @param extraInfo
     */
    public CalypsoPo(SeResponse selectionResponse, String extraInfo) {
        super(selectionResponse, extraInfo);

        poAtr = selectionResponse.getSelectionStatus().getAtr().getBytes();

        /* The selectionSeResponse may not include a FCI field (e.g. old PO Calypso Rev 1) */
        if (selectionResponse.getSelectionStatus().getFci().isSuccessful()) {
            ApduResponse fci = selectionResponse.getSelectionStatus().getFci();
            /* Parse PO FCI - to retrieve Calypso Revision, Serial Number, &amp; DF Name (AID) */
            GetDataFciRespPars poFciRespPars = new GetDataFciRespPars(fci);

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
            byte applicationTypeByte = poFciRespPars.getApplicationTypeByte();
            if ((applicationTypeByte & (1 << 7)) != 0) {
                /* CLAP */
                this.revision = PoRevision.REV3_1_CLAP;
            } else if ((applicationTypeByte >> 3) == (byte) (0x05)) {
                this.revision = PoRevision.REV3_2;
            } else if ((applicationTypeByte >> 3) == (byte) (0x04)) {
                this.revision = PoRevision.REV3_1;
            } else {
                this.revision = PoRevision.REV2_4;
            }

            this.dfName = poFciRespPars.getDfName();

            this.applicationSerialNumber = poFciRespPars.getApplicationSerialNumber();

            if (this.revision == PoRevision.REV2_4) {
                /* old cards have their modification counter in number of commands */
                modificationCounterIsInBytes = false;
                this.modificationsCounterMax =
                        REV2_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION;
            } else {
                this.modificationsCounterMax = poFciRespPars.getBufferSizeValue();
            }
            this.bufferSizeIndicator = poFciRespPars.getBufferSizeIndicator();
            this.bufferSizeValue = poFciRespPars.getBufferSizeValue();
            this.platform = poFciRespPars.getPlatformByte();
            this.applicationType = poFciRespPars.getApplicationTypeByte();
            this.isRev3_2ModeAvailable = poFciRespPars.isRev3_2ModeAvailable();
            this.isRatificationCommandRequired = poFciRespPars.isRatificationCommandRequired();
            this.hasCalypsoStoredValue = poFciRespPars.hasCalypsoStoredValue();
            this.hasCalypsoPin = poFciRespPars.hasCalypsoPin();
            this.applicationSubtypeByte = poFciRespPars.getApplicationSubtypeByte();
            this.softwareIssuerByte = poFciRespPars.getSoftwareIssuerByte();
            this.softwareVersion = poFciRespPars.getSoftwareVersionByte();
            this.softwareRevision = poFciRespPars.getSoftwareRevisionByte();
            this.isDfInvalidated = poFciRespPars.isDfInvalidated();
        } else {
            /*
             * FCI is not provided: we consider it is Calypso PO rev 1, it's serial number is
             * provided in the ATR
             */

            /* basic check: we expect to be here following a selection based on the ATR */
            if (poAtr.length != PO_REV1_ATR_LENGTH) {
                throw new IllegalStateException(
                        "Unexpected ATR length: " + ByteArrayUtil.toHex(poAtr));
            }

            this.revision = PoRevision.REV1_0;
            this.dfName = null;
            this.applicationSerialNumber = new byte[8];
            /* old cards have their modification counter in number of commands */
            this.modificationCounterIsInBytes = false;
            /*
             * the array is initialized with 0 (cf. default value for primitive types)
             */
            System.arraycopy(poAtr, 12, this.applicationSerialNumber, 4, 4);
            this.modificationsCounterMax =
                    REV1_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION;

            this.bufferSizeIndicator = 0;
            this.bufferSizeValue = REV1_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION;
            this.platform = poAtr[6];
            this.applicationType = poAtr[7];
            this.applicationSubtypeByte = poAtr[8];
            this.isRev3_2ModeAvailable = false;
            this.isRatificationCommandRequired = true;
            this.hasCalypsoStoredValue = false;
            this.hasCalypsoPin = false;
            this.softwareIssuerByte = poAtr[9];
            this.softwareVersion = poAtr[10];
            this.softwareRevision = poAtr[11];
            this.isDfInvalidated = false;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("REVISION = {}, SERIALNUMBER = {}, DFNAME = {}", this.revision,
                    ByteArrayUtil.toHex(this.applicationSerialNumber),
                    ByteArrayUtil.toHex(this.dfName));
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

    public byte getBufferSizeIndicator() {
        return bufferSizeIndicator;
    }

    public int getBufferSizeValue() {
        return bufferSizeValue;
    }

    public byte getPlatformByte() {
        return platform;
    }

    public byte getApplicationTypeByte() {
        return applicationType;
    }

    public boolean isRev3_2ModeAvailable() {
        return isRev3_2ModeAvailable;
    }

    public boolean isRatificationCommandRequired() {
        return isRatificationCommandRequired;
    }

    public boolean hasCalypsoStoredValue() {
        return hasCalypsoStoredValue;
    }

    public boolean hasCalypsoPin() {
        return hasCalypsoPin;
    }

    public byte getApplicationSubtypeByte() {
        return applicationSubtypeByte;
    }

    public byte getSoftwareIssuerByte() {
        return softwareIssuerByte;
    }

    public byte getSoftwareVersionByte() {
        return softwareVersion;
    }

    public byte getSoftwareRevisionByte() {
        return softwareRevision;
    }

    public boolean isDfInvalidated() {
        return isDfInvalidated;
    }

    /**
     * @return the PO class determined from the PO revision
     */
    public PoClass getPoClass() {
        /* Rev1 and Rev2 expects the legacy class byte while Rev3 expects the ISO class byte */
        if (revision == PoRevision.REV1_0 || revision == PoRevision.REV2_4) {
            if (logger.isTraceEnabled()) {
                logger.trace("PO revision = {}, PO class = {}", revision, PoClass.LEGACY);
            }
            return PoClass.LEGACY;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("PO revision = {}, PO class = {}", revision, PoClass.ISO);
            }
            return PoClass.ISO;
        }
    }
}

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
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CalypsoPo class gathers all the information about the current PO retrieved from the response
 * to the select application command.
 * <p>
 * An instance of CalypsoPo can be obtained by casting the AbstractMatchingSe object from the
 * selection process (e.g. (CalypsoPo) matchingSelection.getMatchingSe())
 * <p>
 * The various information contained in CalypsoPo is accessible by getters and includes:
 * <ul>
 * <li>The application identification fields (revision/version, class, DF name, serial number, ATR,
 * issuer)
 * <li>The indication of the presence of optional features (Stored Value, PIN, Rev3.2 mode,
 * ratification management)
 * <li>The management information of the modification buffer
 * <li>The invalidation status
 * </ul>
 */
public final class CalypsoPo extends AbstractMatchingSe {
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
     * @param selectionResponse the response to the selection application command
     * @param transmissionMode the current {@link TransmissionMode} (contacts or contactless)
     * @param extraInfo information string
     */
    public CalypsoPo(SeResponse selectionResponse, TransmissionMode transmissionMode,
            String extraInfo) {
        super(selectionResponse, transmissionMode, extraInfo);

        poAtr = selectionResponse.getSelectionStatus().getAtr().getBytes();

        /* The selectionSeResponse may not include a FCI field (e.g. old PO Calypso Rev 1) */
        if (selectionResponse.getSelectionStatus().getFci().isSuccessful()) {
            ApduResponse fci = selectionResponse.getSelectionStatus().getFci();
            /* Parse PO FCI - to retrieve Calypso Revision, Serial Number, &amp; DF Name (AID) */
            GetDataFciRespPars poFciRespPars = new GetDataFciRespPars(fci, null);

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

    /**
     * The PO revision indicates the generation of the product presented.
     * <p>
     * It will also have an impact on the internal construction of some commands to take into
     * account the specificities of the different POs.
     * 
     * @return an enum giving the identified PO revision
     */
    public PoRevision getRevision() {
        return this.revision;
    }

    /**
     * The DF name is the name of the application DF as defined in ISO/IEC 7816-4.
     * <p>
     * It also corresponds to the complete representation of the target covered by the AID value
     * provided in the selection command.
     * <p>
     * The AID selects the application by specifying all or part of the targeted DF Name (5 bytes
     * minimum).
     * 
     * @return a byte array containing the DF Name bytes (5 to 16 bytes)
     */
    public byte[] getDfName() {
        return dfName;
    }

    /**
     * The serial number for the application, is unique ID for the PO.
     * <p>
     * It is also used for key derivation.
     * 
     * @return a byte array containing the Application Serial Number (8 bytes)
     */
    public byte[] getApplicationSerialNumber() {
        return applicationSerialNumber;
    }

    /**
     * The Answer To Reset is sent by the PO is ISO7816-3 mode and in contactless mode for PC/SC
     * readers.
     * <p>
     * When the ATR is obtained in contactless mode, it is in fact reconstructed by the reader from
     * information obtained from the lower communication layers. Therefore, it may differ from one
     * reader to another depending on the interpretation that has been made by the manufacturer of
     * the PC/SC standard.
     * <p>
     * This field is not interpreted in the Calypso module.
     * 
     * @return a byte array containing the ATR (variable length)
     */
    public byte[] getAtr() {
        return poAtr;
    }

    /**
     * Specifies whether the change counter allowed in session is established in number of
     * operations or number of bytes modified.
     * <p>
     * This varies depending on the revision of the PO.
     * 
     * @return true if the counter is number of bytes
     */
    public boolean isModificationsCounterInBytes() {
        return modificationCounterIsInBytes;
    }

    /**
     * Indicates the maximum number of changes allowed in session.
     * <p>
     * This number can be a number of operations or a number of commands (see
     * isModificationsCounterInBytes)
     * 
     * @return the maximum number of modifications allowed
     */
    public int getModificationsCounter() {
        return modificationsCounterMax;
    }

    /**
     * This field is directly from the Startup Information zone of the PO.
     * <p>
     * When the modification counter is in number of operations, it is the maximum number of
     * operations allowed.
     * <p>
     * When the modification counter is in bytes, it is used to determine the maximum number of
     * modified bytes allowed. (see the formula in the PO specification)
     * 
     * @return the buffer size indicator byte
     */
    public byte getBufferSizeIndicator() {
        return bufferSizeIndicator;
    }

    /**
     * The buffer size value is the raw interpretation of the buffer size indicator to provide a
     * number of bytes.
     * <p>
     * The revision number must be taken into account at the same time to be accurate.
     * <p>
     * It is better to use getModificationsCounter and isModificationsCounterInBytes
     * 
     * @return the buffer size value evaluated from the buffer size indicator
     */
    public int getBufferSizeValue() {
        return bufferSizeValue;
    }

    /**
     * The platform identification byte is the reference of the chip
     * 
     * @return the platform identification byte
     */
    public byte getPlatformByte() {
        return platform;
    }

    /**
     * The Application Type byte determines the Calypso Revision and various options
     * 
     * @return the Application Type byte
     */
    public byte getApplicationTypeByte() {
        return applicationType;
    }

    /**
     * Indicates whether the 3.2 mode is supported or not.
     * <p>
     * This boolean is interpreted from the Application Type byte
     * 
     * @return true if the revision 3.2 mode is supported
     */
    public boolean isRev3_2ModeAvailable() {
        return isRev3_2ModeAvailable;
    }

    /**
     * Indicates if the ratification is done on deselect (ratification command not necessary)
     * <p>
     * This boolean is interpreted from the Application Type byte
     * 
     * @return true if the ratification command is required
     */
    public boolean isRatificationCommandRequired() {
        return isRatificationCommandRequired;
    }

    /**
     * Indicates whether the PO has the Calypso Stored Value feature.
     * <p>
     * This boolean is interpreted from the Application Type byte
     * 
     * @return true if the PO has the Stored Value feature
     */
    public boolean hasCalypsoStoredValue() {
        return hasCalypsoStoredValue;
    }

    /**
     * Indicates whether the PO has the Calypso PIN feature.
     * <p>
     * This boolean is interpreted from the Application Type byte
     * 
     * @return true if the PO has the PIN feature
     */
    public boolean hasCalypsoPin() {
        return hasCalypsoPin;
    }

    /**
     * The Application Subtype indicates to the terminal a reference to the file structure of the
     * Calypso DF.
     * 
     * @return the Application Subtype byte
     */
    public byte getApplicationSubtypeByte() {
        return applicationSubtypeByte;
    }

    /**
     * The Software Issuer byte indicates the entity responsible for the software of the selected
     * application.
     * 
     * @return the Software Issuer byte
     */
    public byte getSoftwareIssuerByte() {
        return softwareIssuerByte;
    }

    /**
     * The Software Version field may be set to any fixed value by the Software Issuer of the
     * Calypso application.
     * 
     * @return the Software Version byte
     */
    public byte getSoftwareVersionByte() {
        return softwareVersion;
    }

    /**
     * The Software Revision field may be set to any fixed value by the Software Issuer of the
     * Calypso application.
     * 
     * @return the Software Revision byte
     */
    public byte getSoftwareRevisionByte() {
        return softwareRevision;
    }

    /**
     * Indicated whether the PO has been invalidated or not.
     * <p>
     * An invalidated PO has 6283 as status word in response to the Select Application command.
     * 
     * @return true if the PO has been invalidated.
     */
    public boolean isDfInvalidated() {
        return isDfInvalidated;
    }

    /**
     * The PO class is the ISO7816 class to be used with the current PO.
     * <p>
     * It determined from the PO revision
     * <p>
     * Two classes are possible: LEGACY and ISO.
     * 
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

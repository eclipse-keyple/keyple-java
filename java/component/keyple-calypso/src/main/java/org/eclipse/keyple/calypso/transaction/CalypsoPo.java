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


import java.util.*;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;

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
    private final boolean isConfidentialSessionModeSupported;
    private final boolean isDeselectRatificationSupported;
    private final boolean isSvFeatureAvailable;
    private final boolean isPinFeatureAvailable;
    private final boolean isPublicAuthenticationSupported;
    private final boolean isDfInvalidated;
    private final PoClass poClass;
    private final byte[] calypsoSerialNumber;
    private final byte[] startupInfo;
    private final PoRevision revision;
    private final byte[] dfName;
    private static final int PO_REV1_ATR_LENGTH = 20;
    private static final int REV1_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION = 3;
    private static final int REV2_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION = 6;
    private static final int SI_BUFFER_SIZE_INDICATOR = 0;
    private static final int SI_PLATFORM = 1;
    private static final int SI_APPLICATION_TYPE = 2;
    private static final int SI_APPLICATION_SUBTYPE = 3;
    private static final int SI_SOFTWARE_ISSUER = 4;
    private static final int SI_SOFTWARE_VERSION = 5;
    private static final int SI_SOFTWARE_REVISION = 6;

    // Application type bitmasks features
    private static final byte APP_TYPE_WITH_CALYPSO_PIN = 0x01;
    private static final byte APP_TYPE_WITH_CALYPSO_SV = 0x02;
    private static final byte APP_TYPE_RATIFICATION_COMMAND_REQUIRED = 0x04;
    private static final byte APP_TYPE_CALYPSO_REV_32_MODE = 0x08;
    private static final byte APP_TYPE_WITH_PUBLIC_AUTHENTICATION = 0x10;

    // buffer indicator to buffer size lookup table
    private static final int[] BUFFER_SIZE_INDICATOR_TO_BUFFER_SIZE = new int[] {0, 0, 0, 0, 0, 0,
            215, 256, 304, 362, 430, 512, 608, 724, 861, 1024, 1217, 1448, 1722, 2048, 2435, 2896,
            3444, 4096, 4870, 5792, 6888, 8192, 9741, 11585, 13777, 16384, 19483, 23170, 27554,
            32768, 38967, 46340, 55108, 65536, 77935, 92681, 110217, 131072, 155871, 185363, 220435,
            262144, 311743, 370727, 440871, 524288, 623487, 741455, 881743, 1048576};

    private final byte[] poAtr;
    private final int modificationsCounterMax;
    private boolean modificationCounterIsInBytes = true;
    private DirectoryHeader directoryHeader;
    private final Map<Byte, ElementaryFile> efBySfi = new HashMap<Byte, ElementaryFile>();
    private final Map<Byte, ElementaryFile> efBySfiBackup = new HashMap<Byte, ElementaryFile>();
    private final Map<Short, Byte> sfiByLid = new HashMap<Short, Byte>();
    private final Map<Short, Byte> sfiByLidBackup = new HashMap<Short, Byte>();

    /**
     * Constructor.
     * 
     * @param selectionResponse the response to the selection application command
     * @param transmissionMode the current {@link TransmissionMode} (contacts or contactless)
     */
    public CalypsoPo(SeResponse selectionResponse, TransmissionMode transmissionMode) {
        super(selectionResponse, transmissionMode);

        int bufferSizeIndicator;
        int bufferSizeValue;

        poAtr = selectionResponse.getSelectionStatus().getAtr().getBytes();

        /* The selectionSeResponse may not include a FCI field (e.g. old PO Calypso Rev 1) */
        ApduResponse fci = selectionResponse.getSelectionStatus().getFci();

        if (fci.getBytes() != null && fci.getBytes().length > 2) {

            /* Parse PO FCI - to retrieve DF Name (AID), Serial Number, &amp; StartupInfo */
            GetDataFciRespPars poFciRespPars = new GetDataFciRespPars(fci, null);

            // 4 fields extracted by the low level parser
            dfName = poFciRespPars.getDfName();
            calypsoSerialNumber = poFciRespPars.getApplicationSerialNumber();
            startupInfo = poFciRespPars.getDiscretionaryData();
            isDfInvalidated = poFciRespPars.isDfInvalidated();

            byte applicationType = getApplicationType();
            revision = determineRevision(applicationType);

            // session buffer size
            bufferSizeIndicator = startupInfo[SI_BUFFER_SIZE_INDICATOR];
            bufferSizeValue = BUFFER_SIZE_INDICATOR_TO_BUFFER_SIZE[bufferSizeIndicator];

            if (revision == PoRevision.REV2_4) {
                /* old cards have their modification counter in number of commands */
                modificationCounterIsInBytes = false;
                modificationsCounterMax =
                        REV2_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION;
            } else {
                modificationsCounterMax = bufferSizeValue;
            }
            isConfidentialSessionModeSupported =
                    (applicationType & APP_TYPE_CALYPSO_REV_32_MODE) != 0;
            isDeselectRatificationSupported =
                    (applicationType & APP_TYPE_RATIFICATION_COMMAND_REQUIRED) == 0;
            isSvFeatureAvailable = (applicationType & APP_TYPE_WITH_CALYPSO_SV) != 0;
            isPinFeatureAvailable = (applicationType & APP_TYPE_WITH_CALYPSO_PIN) != 0;
            isPublicAuthenticationSupported =
                    (applicationType & APP_TYPE_WITH_PUBLIC_AUTHENTICATION) != 0;
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

            revision = PoRevision.REV1_0;
            dfName = null;
            calypsoSerialNumber = new byte[8];
            /* old cards have their modification counter in number of commands */
            modificationCounterIsInBytes = false;
            /*
             * the array is initialized with 0 (cf. default value for primitive types)
             */
            System.arraycopy(poAtr, 12, calypsoSerialNumber, 4, 4);
            modificationsCounterMax = REV1_PO_DEFAULT_WRITE_OPERATIONS_NUMBER_SUPPORTED_PER_SESSION;

            startupInfo = new byte[7];
            // create the startup info with the 6 bytes of the ATR from position 6
            System.arraycopy(poAtr, 6, startupInfo, 1, 6);

            // TODO check these flags
            isConfidentialSessionModeSupported = false;
            isDeselectRatificationSupported = true;
            isSvFeatureAvailable = false;
            isPinFeatureAvailable = false;
            isPublicAuthenticationSupported = false;
            isDfInvalidated = false;
        }
        /* Rev1 and Rev2 expects the legacy class byte while Rev3 expects the ISO class byte */
        if (revision == PoRevision.REV1_0 || revision == PoRevision.REV2_4) {
            poClass = PoClass.LEGACY;
        } else {
            poClass = PoClass.ISO;
        }
    }

    /**
     * Resolve the PO revision from the application type byte
     *
     * <ul>
     * <li>if <code>%1-------</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;CLAP&nbsp;&nbsp;&rarr;&nbsp;&
     * nbsp; REV3.1</li>
     * <li>if <code>%00101---</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.2</li>
     * <li>if <code>%00100---</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.1</li>
     * <li>otherwise&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV2.4</li>
     * </ul>
     *
     * @param applicationType the application type (field of startup info)
     * @return the {@link PoRevision}
     */
    private PoRevision determineRevision(byte applicationType) {
        if (((applicationType & 0xFF) & (1 << 7)) != 0) {
            /* CLAP */
            return PoRevision.REV3_1_CLAP;
        } else if ((applicationType >> 3) == (byte) (0x05)) {
            return PoRevision.REV3_2;
        } else if ((applicationType >> 3) == (byte) (0x04)) {
            return PoRevision.REV3_1;
        } else {
            return PoRevision.REV2_4;
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
        return revision;
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
    public byte[] getDfNameBytes() {
        return dfName;
    }

    /**
     * @return the DF name as an HEX string (see getDfNameBytes)
     */
    public String getDfName() {
        return ByteArrayUtil.toHex(getDfNameBytes());
    }

    /**
     * The serial number to be used as diversifier for key derivation.<br>
     * This is the complete number returned by the PO in its response to the Select command.
     * 
     * @return a byte array containing the Calypso Serial Number (8 bytes)
     */
    protected byte[] getCalypsoSerialNumber() {
        return calypsoSerialNumber;
    }

    /**
     * The serial number for the application, is unique ID for the PO.
     *
     * @return a byte array containing the Application Serial Number (8 bytes)
     */
    public byte[] getApplicationSerialNumber() {
        byte[] applicationSerialNumber = calypsoSerialNumber.clone();
        applicationSerialNumber[0] = 0;
        applicationSerialNumber[1] = 0;
        return applicationSerialNumber;
    }

    /**
     * @return the startup info field from the FCI as an HEX string
     */
    public String getStartupInfo() {
        return ByteArrayUtil.toHex(startupInfo);
    }

    protected boolean isSerialNumberExpiring() {
        throw new IllegalStateException("Not yet implemented");
    }

    protected byte[] getSerialNumberExpirationBytes() {
        throw new IllegalStateException("Not yet implemented");
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

    public int getPayloadCapacity() {
        // TODO make this value dependent on the type of PO identified
        return 250;
    }

    /**
     * Specifies whether the change counter allowed in session is established in number of
     * operations or number of bytes modified.
     * <p>
     * This varies depending on the revision of the PO.
     * 
     * @return true if the counter is number of bytes
     */
    protected boolean isModificationsCounterInBytes() {
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
    protected int getModificationsCounter() {
        return modificationsCounterMax;
    }

    /**
     * The platform identification byte is the reference of the chip
     * 
     * @return the platform identification byte
     */
    public byte getPlatform() {
        return startupInfo[SI_PLATFORM];
    }

    /**
     * The Application Type byte determines the Calypso Revision and various options
     * 
     * @return the Application Type byte
     */
    public byte getApplicationType() {
        return startupInfo[SI_APPLICATION_TYPE];
    }

    /**
     * Indicates whether the Confidential Session Mode is supported or not (since rev 3.2).
     * <p>
     * This boolean is interpreted from the Application Type byte
     * 
     * @return true if the Confidential Session Mode is supported
     */
    public boolean isConfidentialSessionModeSupported() {
        return isConfidentialSessionModeSupported;
    }

    /**
     * Indicates if the ratification is done on deselect (ratification command not necessary)
     * <p>
     * This boolean is interpreted from the Application Type byte
     * 
     * @return true if the ratification command is required
     */
    public boolean isDeselectRatificationSupported() {
        return isDeselectRatificationSupported;
    }

    /**
     * Indicates whether the PO has the Calypso Stored Value feature.
     * <p>
     * This boolean is interpreted from the Application Type byte
     * 
     * @return true if the PO has the Stored Value feature
     */
    public boolean isSvFeatureAvailable() {
        return isSvFeatureAvailable;
    }

    /**
     * Indicates whether the PO has the Calypso PIN feature.
     * <p>
     * This boolean is interpreted from the Application Type byte
     * 
     * @return true if the PO has the PIN feature
     */
    public boolean isPinFeatureAvailable() {
        return isPinFeatureAvailable;
    }

    /**
     * Indicates whether the Public Authentication is supported or not (since rev 3.3).
     * <p>
     * This boolean is interpreted from the Application Type byte
     *
     * @return true if the Public Authentication is supported
     */
    public boolean isPublicAuthenticationSupported() {
        return isPublicAuthenticationSupported;
    }

    /**
     * The Application Subtype indicates to the terminal a reference to the file structure of the
     * Calypso DF.
     * 
     * @return the Application Subtype byte
     */
    public byte getApplicationSubtype() {
        return startupInfo[SI_APPLICATION_SUBTYPE];
    }

    /**
     * The Software Issuer byte indicates the entity responsible for the software of the selected
     * application.
     * 
     * @return the Software Issuer byte
     */
    public byte getSoftwareIssuer() {
        return startupInfo[SI_SOFTWARE_ISSUER];
    }

    /**
     * The Software Version field may be set to any fixed value by the Software Issuer of the
     * Calypso application.
     * 
     * @return the Software Version byte
     */
    public byte getSoftwareVersion() {
        return startupInfo[SI_SOFTWARE_VERSION];
    }

    /**
     * The Software Revision field may be set to any fixed value by the Software Issuer of the
     * Calypso application.
     * 
     * @return the Software Revision byte
     */
    public byte getSoftwareRevision() {
        return startupInfo[SI_SOFTWARE_REVISION];
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
    protected PoClass getPoClass() {
        return poClass;
    }

    /**
     * Gets the DF metadata.
     *
     * @return null if is not set.
     * @since 0.9
     */
    public DirectoryHeader getDirectoryHeader() {
        return directoryHeader;
    }

    /**
     * (package-private)<br>
     * Sets the DF metadata.
     *
     * @param directoryHeader the DF metadata (should be not null)
     * @return the current instance.
     */
    CalypsoPo setDirectoryHeader(DirectoryHeader directoryHeader) {
        this.directoryHeader = directoryHeader;
        return this;
    }

    /**
     * Gets a reference to the {@link ElementaryFile} that has the provided SFI value.<br>
     * Note that if a secure session is actually running, then the object contains all session
     * modifications, which can be canceled if the secure session fails.
     *
     * @param sfi the SFI to search
     * @return a not null reference.
     * @throws NoSuchElementException if requested EF is not found.
     * @since 0.9
     */
    public ElementaryFile getFileBySfi(byte sfi) {
        ElementaryFile ef = efBySfi.get(sfi);
        if (ef == null) {
            throw new NoSuchElementException("EF with SFI [" + sfi + "] is not found.");
        }
        return ef;
    }

    /**
     * Gets a reference to the {@link ElementaryFile} that has the provided LID value.<br>
     * Note that if a secure session is actually running, then the object contains all session
     * modifications, which can be canceled if the secure session fails.
     *
     * @param lid the LID to search
     * @return a not null reference.
     * @throws NoSuchElementException if requested EF is not found.
     * @since 0.9
     */
    public ElementaryFile getFileByLid(short lid) {
        Byte sfi = sfiByLid.get(lid);
        if (sfi == null) {
            throw new NoSuchElementException("EF with LID [" + lid + "] is not found.");
        }
        return efBySfi.get(sfi);
    }

    /**
     * Gets a reference to a map of all known Elementary Files by their associated SFI.<br>
     * Note that if a secure session is actually running, then the map contains all session
     * modifications, which can be canceled if the secure session fails.
     *
     * @return a not null reference (may be empty if no one EF is set).
     * @since 0.9
     */
    public Map<Byte, ElementaryFile> getAllFiles() {
        return efBySfi;
    }

    /**
     * (private)<br>
     * Gets or creates the EF having the provided SFI.
     *
     * @param sfi the SFI
     * @return a not null reference.
     */
    private ElementaryFile getOrCreateFile(byte sfi) {
        ElementaryFile ef = efBySfi.get(sfi);
        if (ef == null) {
            ef = new ElementaryFile(sfi);
            efBySfi.put(sfi, ef);
        }
        return ef;
    }

    /**
     * (package-private)<br>
     * Sets the provided {@link FileHeader} to the EF having the provided SFI.<br>
     * If EF does not exist, then it is created.
     *
     * @param sfi the SFI
     * @param header the file header (should be not null)
     */
    void setFileHeader(byte sfi, FileHeader header) {
        ElementaryFile ef = getOrCreateFile(sfi);
        ef.setHeader(header);
        sfiByLid.put(header.getLid(), sfi);
    }

    /**
     * (package-private)<br>
     * Set or replace the entire content of the specified record #numRecord of the provided SFI by
     * the provided content.<br>
     * If EF does not exist, then it is created.
     *
     * @param sfi the SFI
     * @param numRecord the record number (should be {@code >=} 1)
     * @param content the content (should be not empty)
     */
    void setContent(byte sfi, int numRecord, byte[] content) {
        ElementaryFile ef = getOrCreateFile(sfi);
        ef.getData().setContent(numRecord, content);
    }

    /**
     * (package-private)<br>
     * Sets a counter value in record #1 of the provided SFI.<br>
     * If EF does not exist, then it is created.
     *
     * @param sfi the SFI
     * @param numCounter the counter number (should be {@code >=} 1)
     * @param content the counter value (should be not null and 3 bytes length)
     */
    void setCounter(byte sfi, int numCounter, byte[] content) {
        ElementaryFile ef = getOrCreateFile(sfi);
        ef.getData().setCounter(numCounter, content);
    }

    /**
     * (package-private)<br>
     * Set or replace the content at the specified offset of record #numRecord of the provided SFI
     * by a copy of the provided content.<br>
     * If EF does not exist, then it is created.<br>
     * If actual record content is not set or has a size {@code <} offset, then missing data will be
     * padded with 0.
     *
     * @param sfi the SFI
     * @param numRecord the record number (should be {@code >=} 1)
     * @param content the content (should be not empty)
     * @param offset the offset (should be {@code >=} 0)
     */
    void setContent(byte sfi, int numRecord, byte[] content, int offset) {
        ElementaryFile ef = getOrCreateFile(sfi);
        ef.getData().setContent(numRecord, content, offset);
    }

    /**
     * (package-private)<br>
     * Add cyclic content at record #1 by rolling previously all actual records contents (record #1
     * -> record #2, record #2 -> record #3,...) of the provided SFI.<br>
     * This is useful for cyclic files. Note that records are infinitely shifted.<br>
     * <br>
     * If EF does not exist, then it is created.
     *
     * @param sfi the SFI
     * @param content the content (should be not empty)
     */
    void addCyclicContent(byte sfi, byte[] content) {
        ElementaryFile ef = getOrCreateFile(sfi);
        ef.getData().addCyclicContent(content);
    }

    /**
     * (package-private)<br>
     * Make a backup of the Elementary Files.<br>
     * This method should be used before starting a PO secure session.
     */
    void backupFiles() {
        copyMapFiles(efBySfi, efBySfiBackup);
        copyMapSfi(sfiByLid, sfiByLidBackup);
    }

    /**
     * (package-private)<br>
     * Restore the last backup of Elementary Files.<br>
     * This method should be used when SW of the PO close secure session command is unsuccessful or
     * if secure session is aborted.
     */
    void restoreFiles() {
        copyMapFiles(efBySfiBackup, efBySfi);
        copyMapSfi(sfiByLidBackup, sfiByLid);
    }

    /**
     * (private)<br>
     * Copy a map of ElementaryFile by SFI to another one by cloning each element.
     *
     * @param src the source (should be not null)
     * @param dest the destination (should be not null)
     */
    private static void copyMapFiles(Map<Byte, ElementaryFile> src,
            Map<Byte, ElementaryFile> dest) {
        dest.clear();
        for (Map.Entry<Byte, ElementaryFile> entry : src.entrySet()) {
            dest.put(entry.getKey(), entry.getValue().clone());
        }
    }

    /**
     * (private)<br>
     * Copy a map of SFI by LID to another one by cloning each element.
     *
     * @param src the source (should be not null)
     * @param dest the destination (should be not null)
     */
    private static void copyMapSfi(Map<Short, Byte> src, Map<Short, Byte> dest) {
        dest.clear();
        dest.putAll(src);
    }
}

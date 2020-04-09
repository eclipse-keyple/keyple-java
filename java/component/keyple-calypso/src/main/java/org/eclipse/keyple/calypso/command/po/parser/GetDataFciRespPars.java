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
package org.eclipse.keyple.calypso.command.po.parser;

import static org.eclipse.keyple.core.util.bertlv.Tag.TagType.CONSTRUCTED;
import static org.eclipse.keyple.core.util.bertlv.Tag.TagType.PRIMITIVE;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.GetDataFciCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.KeyplePoBadExpectedLengthException;
import org.eclipse.keyple.calypso.command.po.exception.KeyplePoDataAccessException;
import org.eclipse.keyple.calypso.command.po.exception.KeyplePoIllegalParameterException;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.core.util.bertlv.TLV;
import org.eclipse.keyple.core.util.bertlv.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Extracts information from the FCI data returned is response to the selection application command.
 * <p>
 * Provides getter methods for all relevant information.
 */
public final class GetDataFciRespPars extends AbstractPoResponseParser {
    private static final Logger logger = LoggerFactory.getLogger(GetDataFciRespPars.class);

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6A88, new StatusProperties(false,
                "Data object not found (optional mode not available).", KeyplePoDataAccessException.class));
        m.put(0x6B00, new StatusProperties(false,
                "P1 or P2 value not supported (<>004fh, 0062h, 006Fh, 00C0h, 00D0h, 0185h and 5F52h, according to "
                        + "available optional modes).", KeyplePoIllegalParameterException.class));
        m.put(0x6CFF, new StatusProperties(false,
                "Le value incorrect.", KeyplePoBadExpectedLengthException.class));
        m.put(0x6283, new StatusProperties(true,
                "Successful execution, FCI request and DF is invalidated.", null));
        STATUS_TABLE = m;
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /* buffer indicator to buffer size lookup table */
    private static final int[] BUFFER_SIZE_INDICATOR_TO_BUFFER_SIZE = new int[] {0, 0, 0, 0, 0, 0,
            215, 256, 304, 362, 430, 512, 608, 724, 861, 1024, 1217, 1448, 1722, 2048, 2435, 2896,
            3444, 4096, 4870, 5792, 6888, 8192, 9741, 11585, 13777, 16384, 19483, 23170, 27554,
            32768, 38967, 46340, 55108, 65536, 77935, 92681, 110217, 131072, 155871, 185363, 220435,
            262144, 311743, 370727, 440871, 524288, 623487, 741455, 881743, 1048576};

    /* BER-TLV tags definitions */
    /* FCI Template: application class, constructed, tag number Fh => tag field 6Fh */
    private static final Tag TAG_FCI_TEMPLATE = new Tag(0x0F, Tag.APPLICATION, CONSTRUCTED);
    /* DF Name: context-specific class, primitive, tag number 4h => tag field 84h */
    private static final Tag TAG_DF_NAME = new Tag(0x04, Tag.CONTEXT, PRIMITIVE);
    /*
     * FCI Proprietary Template: context-specific class, constructed, tag number 5h => tag field A5h
     */
    private static final Tag TAG_FCI_PROPRIETARY_TEMPLATE = new Tag(0x05, Tag.CONTEXT, CONSTRUCTED);
    /*
     * FCI Issuer Discretionary Data: context-specific class, constructed, tag number Ch => tag
     * field BF0Ch
     */
    private static final Tag TAG_FCI_ISSUER_DISCRETIONARY_DATA =
            new Tag(0x0C, Tag.CONTEXT, CONSTRUCTED);
    /* Application Serial Number: private class, primitive, tag number 7h => tag field C7h */
    private static final Tag TAG_APPLICATION_SERIAL_NUMBER = new Tag(0x07, Tag.PRIVATE, PRIMITIVE);
    /* Discretionary Data: application class, primitive, tag number 13h => tag field 53h */
    private static final Tag TAG_DISCRETIONARY_DATA = new Tag(0x13, Tag.APPLICATION, PRIMITIVE);

    /** attributes result of th FCI parsing */
    private boolean isDfInvalidated = false;

    private boolean isValidCalypsoFCI = false;
    private byte[] dfName = null;
    private byte[] applicationSN = null;
    private byte siBufferSizeIndicator = 0;
    private byte siPlatform = 0;
    private byte siApplicationType = 0;
    private byte siApplicationSubtype = 0;
    private byte siSoftwareIssuer = 0;
    private byte siSoftwareVersion = 0;
    private byte siSoftwareRevision = 0;

    /** Application type bitmasks features */
    private static final byte APP_TYPE_WITH_CALYPSO_PIN = 0x01;
    private static final byte APP_TYPE_WITH_CALYPSO_SV = 0x02;
    private static final byte APP_TYPE_RATIFICATION_COMMAND_REQUIRED = 0x04;
    private static final byte APP_TYPE_CALYPSO_REV_32_MODE = 0x08;

    /**
     * Instantiates a new GetDataFciRespPars from the ApduResponse to a selection application
     * command.
     * <p>
     * The expected FCI structure of a Calypso PO follows this scheme: <code>
     * T=6F L=XX (C)                FCI Template
     *      T=84 L=XX (P)           DF Name
     *      T=A5 L=22 (C)           FCI Proprietary Template
     *           T=BF0C L=19 (C)    FCI Issuer Discretionary Data
     *                T=C7 L=8 (P)  Application Serial Number
     *                T=53 L=7 (P)  Discretionary Data (Startup Information)
     * </code>
     * <p>
     * The ApduResponse provided in argument is parsed according to the above expected structure.
     * <p>
     * DF Name, Application Serial Number and Startup Information are extracted.
     * <p>
     * The 7-byte startup information field is also split into 7 private field made available
     * through dedicated getter methods.
     * <p>
     * All fields are pre-initialized to handle the case where the parsing fails.
     * <p>
     *
     * @param response the select application response from Get Data APDU command
     * @param builderReference the reference to the builder that created this parser
     */
    public GetDataFciRespPars(ApduResponse response, GetDataFciCmdBuild builderReference) {
        super(response, builderReference);
        TLV tlv;

        /* check the command status to determine if the DF has been invalidated */
        if (response.getStatusCode() == 0x6283) {
            logger.debug(
                    "The response to the select application command status word indicates that the DF has been invalidated.");
            isDfInvalidated = true;
        }

        /* parse the raw data with the help of the TLV class */
        try {
            /* init TLV object with the raw data and extract the FCI Template */
            final byte[] responseData = response.getBytes();
            tlv = new TLV(responseData);

            /* Get the FCI template */
            if (!tlv.parse(TAG_FCI_TEMPLATE, 0)) {
                logger.error("FCI parsing error: FCI template tag not found.");
                return;
            }

            /* Get the DF Name */
            if (!tlv.parse(TAG_DF_NAME, tlv.getPosition())) {
                logger.error("FCI parsing error: DF name tag not found.");
                return;
            }

            dfName = tlv.getValue();

            /* Get the FCI Proprietary Template */
            if (!tlv.parse(TAG_FCI_PROPRIETARY_TEMPLATE, tlv.getPosition())) {
                logger.error("FCI parsing error: FCI proprietary template tag not found.");
                return;
            }

            /* Get the FCI Issuer Discretionary Data */
            if (!tlv.parse(TAG_FCI_ISSUER_DISCRETIONARY_DATA, tlv.getPosition())) {
                logger.error("FCI parsing error: FCI issuer discretionary data tag not found.");
                return;
            }

            /* Get the Application Serial Number */
            if (!tlv.parse(TAG_APPLICATION_SERIAL_NUMBER, tlv.getPosition())) {
                logger.error("FCI parsing error: serial number tag not found.");
                return;
            }

            applicationSN = tlv.getValue();

            if (logger.isDebugEnabled()) {
                logger.debug("Application Serial Number = {}", ByteArrayUtil.toHex(applicationSN));
            }

            /* Get the Discretionary Data */
            if (!tlv.parse(TAG_DISCRETIONARY_DATA, tlv.getPosition())) {
                logger.error("FCI parsing error: discretionary data tag not found.");
                return;
            }

            byte[] discretionaryData = tlv.getValue();

            if (logger.isDebugEnabled()) {
                logger.debug("Discretionary Data = {}", ByteArrayUtil.toHex(discretionaryData));
            }

            /*
             * split discretionary data in as many individual startup information
             */
            siBufferSizeIndicator = discretionaryData[0];
            siPlatform = discretionaryData[1];
            siApplicationType = discretionaryData[2];
            siApplicationSubtype = discretionaryData[3];
            siSoftwareIssuer = discretionaryData[4];
            siSoftwareVersion = discretionaryData[5];
            siSoftwareRevision = discretionaryData[6];
            /* all 3 main fields were retrieved */
            isValidCalypsoFCI = true;

        } catch (Exception e) {
            /* Silently ignore problems decoding TLV structure. Just log. */
            logger.debug("Error while parsing the FCI BER-TLV data structure ({})", e.getMessage());
        }
    }

    public boolean isValidCalypsoFCI() {
        return isValidCalypsoFCI;
    }

    public byte[] getDfName() {
        return dfName;
    }

    public byte[] getApplicationSerialNumber() {
        return applicationSN;
    }

    public byte getBufferSizeIndicator() {
        return siBufferSizeIndicator;
    }

    public int getBufferSizeValue() {
        return BUFFER_SIZE_INDICATOR_TO_BUFFER_SIZE[getBufferSizeIndicator()];
    }

    public byte getPlatformByte() {
        return siPlatform;
    }

    public byte getApplicationTypeByte() {
        return siApplicationType;
    }


    public boolean isRev3_2ModeAvailable() {
        return (siApplicationType & APP_TYPE_CALYPSO_REV_32_MODE) != 0;
    }

    public boolean isRatificationCommandRequired() {
        return (siApplicationSubtype & APP_TYPE_RATIFICATION_COMMAND_REQUIRED) != 0;
    }

    public boolean hasCalypsoStoredValue() {
        return (siApplicationSubtype & APP_TYPE_WITH_CALYPSO_SV) != 0;
    }

    public boolean hasCalypsoPin() {
        return (siApplicationSubtype & APP_TYPE_WITH_CALYPSO_PIN) != 0;
    }

    public byte getApplicationSubtypeByte() {
        return siApplicationSubtype;
    }

    public byte getSoftwareIssuerByte() {
        return siSoftwareIssuer;
    }

    public byte getSoftwareVersionByte() {
        return siSoftwareVersion;
    }

    public byte getSoftwareRevisionByte() {
        return siSoftwareRevision;
    }

    public boolean isDfInvalidated() {
        return isDfInvalidated;
    }
}

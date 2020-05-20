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


import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetDataFciRespParsTest {

    @Test
    public void testFCIparser_FCI_OK() {
        String dfName = "00112233445566778899";
        String appSerialNumber = "AABBCCDDEEFF0011";
        byte siBufferSizeIndicator = 11;
        byte siPlatform = (byte) 0x55;
        byte siApplicationType = (byte) 0xAA;
        byte siApplicationSubtype = (byte) 0x55;
        byte siSoftwareIssuer = (byte) 0xAA;
        byte siSoftwareVersion = (byte) 0x55;
        byte siSoftwareRevision = (byte) 0xAA;
        String startupInformation = String.format("%02X%02X%02X%02X%02X%02X%02X",
                siBufferSizeIndicator, siPlatform, siApplicationType, siApplicationSubtype,
                siSoftwareIssuer, siSoftwareVersion, siSoftwareRevision);
        ApduResponse apduResponse = new ApduResponse(
                ByteArrayUtil.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        GetDataFciRespPars parser = new GetDataFciRespPars(apduResponse, null);

        /* DF not invalidated */
        Assert.assertFalse(parser.isDfInvalidated());
        /* expected dfName */
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(dfName), parser.getDfName());
        /* expected Application Serial Number */
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(appSerialNumber),
                parser.getApplicationSerialNumber());
        /* expected StartupInfo */
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("0B55AA55AA55AA"),
                parser.getDiscretionaryData());

        /* Change startup information */
        siBufferSizeIndicator = 16;
        siPlatform = (byte) 0xAA;
        siApplicationType = (byte) 0x55;
        siApplicationSubtype = (byte) 0xAA;
        siSoftwareIssuer = (byte) 0x55;
        siSoftwareVersion = (byte) 0xAA;
        siSoftwareRevision = (byte) 0x55;
        startupInformation = String.format("%02X%02X%02X%02X%02X%02X%02X", siBufferSizeIndicator,
                siPlatform, siApplicationType, siApplicationSubtype, siSoftwareIssuer,
                siSoftwareVersion, siSoftwareRevision);
        apduResponse = new ApduResponse(
                ByteArrayUtil.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse, null);

        /* DF not invalidated */
        Assert.assertFalse(parser.isDfInvalidated());
        /* expected dfName */
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(dfName), parser.getDfName());
        /* expected Application Serial Number */
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(appSerialNumber),
                parser.getApplicationSerialNumber());
        /* expected StartupInfo */
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("10AA55AA55AA55"),
                parser.getDiscretionaryData());
    }

    @Test
    public void testFCIparser_FCI_Invalidated() {
        String dfName = "00112233445566778899";
        String appSerialNumber = "AABBCCDDEEFF0011";
        byte siBufferSizeIndicator = 11;
        byte siPlatform = (byte) 0x55;
        byte siApplicationType = (byte) 0xAA;
        byte siApplicationSubtype = (byte) 0x55;
        byte siSoftwareIssuer = (byte) 0xAA;
        byte siSoftwareVersion = (byte) 0x55;
        byte siSoftwareRevision = (byte) 0xAA;
        String startupInformation = String.format("%02X%02X%02X%02X%02X%02X%02X",
                siBufferSizeIndicator, siPlatform, siApplicationType, siApplicationSubtype,
                siSoftwareIssuer, siSoftwareVersion, siSoftwareRevision);
        ApduResponse apduResponse = new ApduResponse(
                ByteArrayUtil.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "6283"),
                null);

        GetDataFciRespPars parser = new GetDataFciRespPars(apduResponse, null);

        Assert.assertTrue(parser.isDfInvalidated());
    }


    @Test
    public void testFCIparser_FCI_BadTags() {
        String dfName = "00112233445566778899";
        String appSerialNumber = "AABBCCDDEEFF0011";
        byte siBufferSizeIndicator = 11;
        byte siPlatform = (byte) 0x55;
        byte siApplicationType = (byte) 0xAA;
        byte siApplicationSubtype = (byte) 0x55;
        byte siSoftwareIssuer = (byte) 0xAA;
        byte siSoftwareVersion = (byte) 0x55;
        byte siSoftwareRevision = (byte) 0xAA;
        String startupInformation = String.format("%02X%02X%02X%02X%02X%02X%02X",
                siBufferSizeIndicator, siPlatform, siApplicationType, siApplicationSubtype,
                siSoftwareIssuer, siSoftwareVersion, siSoftwareRevision);

        /* bad tag FCI Template (not constructed) */
        ApduResponse apduResponse = new ApduResponse(
                ByteArrayUtil.fromHex("4F 24 84 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        GetDataFciRespPars parser = new GetDataFciRespPars(apduResponse, null);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());

        /* bad tag DF Name (constructed) */
        apduResponse = new ApduResponse(
                ByteArrayUtil.fromHex("6F 24 C4 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse, null);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());

        /* bad tag FCI Proprietary Template (not constructed) */
        apduResponse = new ApduResponse(
                ByteArrayUtil.fromHex("6F 24 84 0A " + dfName + " 85 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse, null);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());

        /* bad tag FCI Issuer Discretionary */
        apduResponse = new ApduResponse(
                ByteArrayUtil.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0D 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse, null);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());

        /* bad tag Application Serial Number */
        apduResponse = new ApduResponse(
                ByteArrayUtil.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0C 13 87 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse, null);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());

        /* bad tag Discretionary Data */
        apduResponse = new ApduResponse(
                ByteArrayUtil.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 43 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse, null);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());
    }
}

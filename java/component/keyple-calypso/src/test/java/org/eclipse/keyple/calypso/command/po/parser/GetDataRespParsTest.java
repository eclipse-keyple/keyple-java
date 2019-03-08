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


import org.eclipse.keyple.seproxy.message.ApduResponse;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetDataRespParsTest {

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
                ByteArrayUtils.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        GetDataFciRespPars parser = new GetDataFciRespPars(apduResponse);

        /* valid Calypso FCI */
        Assert.assertTrue(parser.isValidCalypsoFCI());
        /* DF not invalidated */
        Assert.assertFalse(parser.isDfInvalidated());
        /* expected dfName */
        Assert.assertArrayEquals(ByteArrayUtils.fromHex(dfName), parser.getDfName());
        /* expected Application Serial Number */
        Assert.assertArrayEquals(ByteArrayUtils.fromHex(appSerialNumber),
                parser.getApplicationSerialNumber());
        /* Buffer size indicator and value */
        Assert.assertEquals(siBufferSizeIndicator, parser.getBufferSizeIndicator());
        Assert.assertEquals(512, parser.getBufferSizeValue());
        /* Platform */
        Assert.assertEquals(siPlatform, parser.getPlatformByte());
        /* ApplicationType */
        Assert.assertEquals(siApplicationType, parser.getApplicationTypeByte());
        Assert.assertTrue(parser.isRev3_2ModeAvailable());
        Assert.assertTrue(parser.isRatificationCommandRequired());
        Assert.assertTrue(parser.hasCalypsoPin());
        Assert.assertFalse(parser.hasCalypsoStoredValue());
        /* ApplicationSubType */
        Assert.assertEquals(siApplicationSubtype, parser.getApplicationSubtypeByte());
        /* SoftwareIssuer */
        Assert.assertEquals(siSoftwareIssuer, parser.getSoftwareIssuerByte());
        /* SoftwareVersion */
        Assert.assertEquals(siSoftwareVersion, parser.getSoftwareVersionByte());
        /* SoftwareRevision */
        Assert.assertEquals(siSoftwareRevision, parser.getSoftwareRevisionByte());

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
                ByteArrayUtils.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse);

        /* valid Calypso FCI */
        Assert.assertTrue(parser.isValidCalypsoFCI());
        /* DF not invalidated */
        Assert.assertFalse(parser.isDfInvalidated());
        /* expected dfName */
        Assert.assertArrayEquals(ByteArrayUtils.fromHex(dfName), parser.getDfName());
        /* expected Application Serial Number */
        Assert.assertArrayEquals(ByteArrayUtils.fromHex(appSerialNumber),
                parser.getApplicationSerialNumber());
        /* Buffer size indicator and value */
        Assert.assertEquals(siBufferSizeIndicator, parser.getBufferSizeIndicator());
        Assert.assertEquals(1217, parser.getBufferSizeValue());
        /* Platform */
        Assert.assertEquals(siPlatform, parser.getPlatformByte());
        /* ApplicationType */
        Assert.assertEquals(siApplicationType, parser.getApplicationTypeByte());
        Assert.assertFalse(parser.isRev3_2ModeAvailable());
        Assert.assertFalse(parser.isRatificationCommandRequired());
        Assert.assertFalse(parser.hasCalypsoPin());
        Assert.assertTrue(parser.hasCalypsoStoredValue());
        /* ApplicationSubType */
        Assert.assertEquals(siApplicationSubtype, parser.getApplicationSubtypeByte());
        /* SoftwareIssuer */
        Assert.assertEquals(siSoftwareIssuer, parser.getSoftwareIssuerByte());
        /* SoftwareVersion */
        Assert.assertEquals(siSoftwareVersion, parser.getSoftwareVersionByte());
        /* SoftwareRevision */
        Assert.assertEquals(siSoftwareRevision, parser.getSoftwareRevisionByte());
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
                ByteArrayUtils.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "6283"),
                null);

        GetDataFciRespPars parser = new GetDataFciRespPars(apduResponse);

        /* valid Calypso FCI */
        Assert.assertTrue(parser.isValidCalypsoFCI());
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
                ByteArrayUtils.fromHex("4F 24 84 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        GetDataFciRespPars parser = new GetDataFciRespPars(apduResponse);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());

        /* bad tag DF Name (constructed) */
        apduResponse = new ApduResponse(
                ByteArrayUtils.fromHex("6F 24 C4 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());

        /* bad tag FCI Proprietary Template (not constructed) */
        apduResponse = new ApduResponse(
                ByteArrayUtils.fromHex("6F 24 84 0A " + dfName + " 85 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());

        /* bad tag FCI Issuer Discretionary */
        apduResponse = new ApduResponse(
                ByteArrayUtils.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0D 13 C7 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());

        /* bad tag Application Serial Number */
        apduResponse = new ApduResponse(
                ByteArrayUtils.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0C 13 87 08 "
                        + appSerialNumber + " 53 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());

        /* bad tag Discretionary Data */
        apduResponse = new ApduResponse(
                ByteArrayUtils.fromHex("6F 24 84 0A " + dfName + " A5 16 BF0C 13 C7 08 "
                        + appSerialNumber + " 43 07 " + startupInformation + "9000"),
                null);

        parser = new GetDataFciRespPars(apduResponse);

        /* invalid Calypso FCI */
        Assert.assertFalse(parser.isValidCalypsoFCI());
    }
}

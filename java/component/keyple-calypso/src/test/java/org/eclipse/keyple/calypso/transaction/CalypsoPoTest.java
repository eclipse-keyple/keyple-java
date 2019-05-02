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
import org.eclipse.keyple.core.seproxy.message.AnswerToReset;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CalypsoPoTest {
    private final static String ATR_VALUE = "3B8F8001805A08030400020011223344829000F3";
    private final static String ATR_VALUE_2 = "3B8F8001805A08030400020011223344829000";
    /*
    // @formatter:off
    Rev 3.1 FCI sample data
    6F
    23
        84
        09
        315449432E49434131      // AID
        A5
        16
            BF0C
            13
                C7
                08
                0000000011223344 // SERIAL NUMBER
                53
                07
                0A3C2305141001   // STARTUP INFORMATION

      STARTUP INFORMATION
          0A Buffer size indicator
          3C Type of platform
          23 Calypso revision
          05 File structure reference
          14 Software issuer reference
          10 Software version (MSB)
          01 Software version (LSB)
     // @formatter:on
     */
    private final static String FCI_REV31 =
            "6F238409315449432E49434131A516BF0C13C708000000001122334453070A3C23051410019000";
    private final static String DF_NAME = "315449432E494341";
    private final static String SERIAL_NUMBER = "0000000011223344";

    /**
     * Build a CalypsoPo from atr and fci provided as hex strings
     * 
     * @param atrStr the PO ATR
     * @param fciStr the FCI data
     * @return a CalypsoPo object
     */
    public static CalypsoPo getCalypsoPo(String atrStr, String fciStr) {

        AnswerToReset atr;
        if (atrStr == null || atrStr.length() == 0) {
            atr = null;
        } else {
            atr = new AnswerToReset(ByteArrayUtil.fromHex(atrStr));
        }

        ApduResponse fci;
        if (fciStr == null || fciStr.length() == 0) {
            fci = new ApduResponse(ByteArrayUtil.fromHex("6700"), null);;
        } else {
            fci = new ApduResponse(ByteArrayUtil.fromHex(fciStr), null);
        }

        SeResponse selectionData =
                new SeResponse(true, false, new SelectionStatus(atr, fci, true), null);
        CalypsoPo calypsoPo = new CalypsoPo(selectionData, TransmissionMode.CONTACTLESS, null);
        return calypsoPo;
    }

    /* Building FCI data with the application byte as a variant and initialize PO */
    public static CalypsoPo getPoApplicationByte(byte applicationByte) {
        String fciStr = String.format("6F 22 84 08 " + DF_NAME + "A5 16 BF0C 13 C7 08 "
                + SERIAL_NUMBER + "53 07 060A %02X 02200311 9000", applicationByte);
        return getCalypsoPo(ATR_VALUE, fciStr);
    }


    @Test
    public void getRevision() {
        Assert.assertEquals(getPoApplicationByte((byte) 0x01).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x04).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x06).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x1F).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x20).getRevision(), PoRevision.REV3_1);

        Assert.assertEquals(getPoApplicationByte((byte) 0x27).getRevision(), PoRevision.REV3_1);

        Assert.assertEquals(getPoApplicationByte((byte) 0x28).getRevision(), PoRevision.REV3_2);

        Assert.assertEquals(getPoApplicationByte((byte) 0x2F).getRevision(), PoRevision.REV3_2);

        Assert.assertEquals(getPoApplicationByte((byte) 0x90).getRevision(),
                PoRevision.REV3_1_CLAP);
    }

    @Test
    public void getDfName() {
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(DF_NAME),
                getPoApplicationByte((byte) 0x01).getDfName());
    }

    @Test
    public void getApplicationSerialNumber() {
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(SERIAL_NUMBER),
                getPoApplicationByte((byte) 0x01).getApplicationSerialNumber());
    }

    @Test
    public void getAtr() {
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(ATR_VALUE),
                getPoApplicationByte((byte) 0x01).getAtr());
    }

    @Test
    public void isModificationsCounterInBytes() {
        // false for rev 2
        Assert.assertFalse(getPoApplicationByte((byte) 0x01).isModificationsCounterInBytes());
        // true for rev 3
        Assert.assertTrue(getPoApplicationByte((byte) 0x27).isModificationsCounterInBytes());
    }

    @Test
    public void getModificationsCounter() {
        // 6 for rev 2
        Assert.assertEquals(6, getPoApplicationByte((byte) 0x01).getModificationsCounter());
        // 215 bytes for rev 3
        Assert.assertEquals(215, getPoApplicationByte((byte) 0x27).getModificationsCounter());
    }

    @Test
    public void getPoClass() {
        // LEGACY for rev 2
        Assert.assertEquals(PoClass.LEGACY, getPoApplicationByte((byte) 0x01).getPoClass());
        // ISO bytes for rev 3
        Assert.assertEquals(PoClass.ISO, getPoApplicationByte((byte) 0x27).getPoClass());
    }

    @Test(expected = IllegalStateException.class)
    public void testRev1_1() {
        AnswerToReset atr = new AnswerToReset(ByteArrayUtil.fromHex(ATR_VALUE_2));
        ApduResponse fciData = new ApduResponse(null, null);
        SeResponse selectionData =
                new SeResponse(true, false, new SelectionStatus(atr, fciData, true), null);
        CalypsoPo calypsoPo = new CalypsoPo(selectionData, TransmissionMode.CONTACTLESS, null);
    }

    @Test
    public void testRev1_2() {
        AnswerToReset atr = new AnswerToReset(ByteArrayUtil.fromHex(ATR_VALUE));
        ApduResponse fciData = new ApduResponse(null, null);
        SeResponse selectionData =
                new SeResponse(true, false, new SelectionStatus(atr, fciData, true), null);
        CalypsoPo calypsoPo = new CalypsoPo(selectionData, TransmissionMode.CONTACTLESS, null);

        Assert.assertEquals(PoRevision.REV1_0, calypsoPo.getRevision());
        Assert.assertNull(calypsoPo.getDfName());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(SERIAL_NUMBER),
                calypsoPo.getApplicationSerialNumber());
        Assert.assertFalse(calypsoPo.isModificationsCounterInBytes());
        Assert.assertEquals(3, calypsoPo.getModificationsCounter());
        Assert.assertEquals(PoClass.LEGACY, calypsoPo.getPoClass());
    }

    @Test
    public void otherAttributes() {
        CalypsoPo calypsoPo;
        /* Rev1 PO */
        calypsoPo = getCalypsoPo(ATR_VALUE, "6700");
        Assert.assertFalse(calypsoPo.isModificationsCounterInBytes());
        Assert.assertEquals(3, calypsoPo.getModificationsCounter());
        Assert.assertEquals(0, calypsoPo.getBufferSizeIndicator());
        Assert.assertEquals(3, calypsoPo.getModificationsCounter());
        Assert.assertEquals(3, calypsoPo.getBufferSizeValue());
        Assert.assertEquals(0x08, calypsoPo.getPlatformByte());
        Assert.assertEquals(0x03, calypsoPo.getApplicationTypeByte());
        Assert.assertEquals(0x04, calypsoPo.getApplicationSubtypeByte());
        Assert.assertEquals(0x00, calypsoPo.getSoftwareIssuerByte());
        Assert.assertEquals(0x02, calypsoPo.getSoftwareVersionByte());
        Assert.assertEquals(0x00, calypsoPo.getSoftwareRevisionByte());

        /* Rev 3.1 */
        calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31);
        Assert.assertTrue(calypsoPo.isModificationsCounterInBytes());
        Assert.assertEquals(10, calypsoPo.getBufferSizeIndicator());
        Assert.assertEquals(430, calypsoPo.getModificationsCounter());
        Assert.assertEquals(430, calypsoPo.getBufferSizeValue());
        Assert.assertEquals(0x3C, calypsoPo.getPlatformByte());
        Assert.assertEquals(0x23, calypsoPo.getApplicationTypeByte());
        Assert.assertEquals(0x05, calypsoPo.getApplicationSubtypeByte());
        Assert.assertEquals(0x14, calypsoPo.getSoftwareIssuerByte());
        Assert.assertEquals(0x10, calypsoPo.getSoftwareVersionByte());
        Assert.assertEquals(0x01, calypsoPo.getSoftwareRevisionByte());
    }
}

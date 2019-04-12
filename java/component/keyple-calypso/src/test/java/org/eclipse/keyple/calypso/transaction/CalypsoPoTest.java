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
import org.eclipse.keyple.seproxy.message.AnswerToReset;
import org.eclipse.keyple.seproxy.message.ApduResponse;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SelectionStatus;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CalypsoPoTest {
    private final static String ATR_VALUE = "3B8F8001805A08030400020011223344829000F3";
    private final static String ATR_VALUE_2 = "3B8F8001805A08030400020011223344829000";
    private final static String DF_NAME = "315449432E494341";
    private final static String SERIAL_NUMBER = "0000000011223344";

    /* Building FCI data with the application byte as a variant and initialize PO */
    public static CalypsoPo getPoApplicationByte(byte applicationByte) {
        AnswerToReset atr = new AnswerToReset(ByteArrayUtils.fromHex(ATR_VALUE));
        ApduResponse fciData =
                new ApduResponse(
                        ByteArrayUtils
                                .fromHex(String.format(
                                        "6F 22 84 08 " + DF_NAME + "A5 16 BF0C 13 C7 08 "
                                                + SERIAL_NUMBER + "53 07 060A %02X 02200311 9000",
                                        applicationByte)),
                        null);
        SeResponse selectionData =
                new SeResponse(true, false, new SelectionStatus(atr, fciData, true), null);
        CalypsoPo calypsoPo = new CalypsoPo(selectionData, null);
        return calypsoPo;
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
        Assert.assertArrayEquals(ByteArrayUtils.fromHex(DF_NAME),
                getPoApplicationByte((byte) 0x01).getDfName());
    }

    @Test
    public void getApplicationSerialNumber() {
        Assert.assertArrayEquals(ByteArrayUtils.fromHex(SERIAL_NUMBER),
                getPoApplicationByte((byte) 0x01).getApplicationSerialNumber());
    }

    @Test
    public void getAtr() {
        Assert.assertArrayEquals(ByteArrayUtils.fromHex(ATR_VALUE),
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
        AnswerToReset atr = new AnswerToReset(ByteArrayUtils.fromHex(ATR_VALUE_2));
        ApduResponse fciData = new ApduResponse(null, null);
        SeResponse selectionData =
                new SeResponse(true, false, new SelectionStatus(atr, fciData, true), null);
        CalypsoPo calypsoPo = new CalypsoPo(selectionData, null);
    }

    @Test
    public void testRev1_2() {
        AnswerToReset atr = new AnswerToReset(ByteArrayUtils.fromHex(ATR_VALUE));
        ApduResponse fciData = new ApduResponse(null, null);
        SeResponse selectionData =
                new SeResponse(true, false, new SelectionStatus(atr, fciData, true), null);
        CalypsoPo calypsoPo = new CalypsoPo(selectionData, null);

        Assert.assertEquals(PoRevision.REV1_0, calypsoPo.getRevision());
        Assert.assertNull(calypsoPo.getDfName());
        Assert.assertArrayEquals(ByteArrayUtils.fromHex(SERIAL_NUMBER),
                calypsoPo.getApplicationSerialNumber());
        Assert.assertFalse(calypsoPo.isModificationsCounterInBytes());
        Assert.assertEquals(3, calypsoPo.getModificationsCounter());
        Assert.assertEquals(PoClass.LEGACY, calypsoPo.getPoClass());
    }
}

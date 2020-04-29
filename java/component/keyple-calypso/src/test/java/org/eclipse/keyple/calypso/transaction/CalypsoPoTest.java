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


import static org.assertj.core.api.Assertions.*;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.core.seproxy.message.AnswerToReset;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CalypsoPoTest {
    public final static String ATR_VALUE = "3B8F8001805A08030400020011223344829000F3";
    public final static String ATR_VALUE_2 = "3B8F8001805A08030400020011223344829000";
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
          2F Calypso revision
          05 File structure reference
          14 Software issuer reference
          10 Software version (MSB)
          01 Software version (LSB)
     // @formatter:on
     */
    private final static String FCI_REV31 =
            "6F238409315449432E49434131A516BF0C13C708000000001122334453070A3C2F051410019000";
    private final static String FCI_REV31_FLAGS_FALSE =
            "6F238409315449432E49434131A516BF0C13C708000000001122334453070A3C20051410019000";
    private final static String FCI_REV31_INVALIDATED =
            "6F238409315449432E49434131A516BF0C13C708000000001122334453070A3C2F051410016283";
    private final static String FCI_REV31_HCE =
            "6F238409315449432E49434131A516BF0C13C708998811223344556653070A3C2F051410019000";

    private final static String DF_NAME = "315449432E494341";
    private final static String SERIAL_NUMBER = "0000000011223344";
    private CalypsoPo po;

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
        CalypsoPo calypsoPo = new CalypsoPo(selectionData, TransmissionMode.CONTACTLESS);
        return calypsoPo;
    }

    /* Building FCI data with the application byte as a variant and initialize PO */
    public static CalypsoPo getPoApplicationByte(byte applicationByte) {
        String fciStr = String.format("6F 22 84 08 " + DF_NAME + "A5 16 BF0C 13 C7 08 "
                + SERIAL_NUMBER + "53 07 060A %02X 02200311 9000", applicationByte);
        return getCalypsoPo(ATR_VALUE, fciStr);
    }

    @Before
    public void setUp() throws Exception {
        po = getCalypsoPo(ATR_VALUE, FCI_REV31);
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
    public void getDfNameBytes() {
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(DF_NAME),
                getPoApplicationByte((byte) 0x01).getDfNameBytes());
    }

    @Test
    public void getDfName() {
        Assert.assertEquals(DF_NAME, getPoApplicationByte((byte) 0x01).getDfName());
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

    @Test
    public void getStartupInfo() {
        // Startup info
        Assert.assertEquals("0A3C2F05141001", po.getStartupInfo());
    }

    @Test(expected = IllegalStateException.class)
    public void isSerialNumberExpiring() {
        po.isSerialNumberExpiring();
    }

    @Test(expected = IllegalStateException.class)
    public void getSerialNumberExpirationBytes() {
        po.getSerialNumberExpirationBytes();
    }

    @Test
    public void getCalypsoAndApplicationSerialNumber() {
        CalypsoPo calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31_HCE);
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("9988112233445566"),
                calypsoPo.getCalypsoSerialNumber());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("0000112233445566"),
                calypsoPo.getApplicationSerialNumber());
    }

    @Test(expected = IllegalStateException.class)
    public void testRev1_1() {
        AnswerToReset atr = new AnswerToReset(ByteArrayUtil.fromHex(ATR_VALUE_2));
        ApduResponse fciData = new ApduResponse(null, null);
        SeResponse selectionData =
                new SeResponse(true, false, new SelectionStatus(atr, fciData, true), null);
        CalypsoPo calypsoPo = new CalypsoPo(selectionData, TransmissionMode.CONTACTLESS);
    }

    @Test
    public void testRev1_2() {
        AnswerToReset atr = new AnswerToReset(ByteArrayUtil.fromHex(ATR_VALUE));
        ApduResponse fciData = new ApduResponse(null, null);
        SeResponse selectionData =
                new SeResponse(true, false, new SelectionStatus(atr, fciData, true), null);
        CalypsoPo calypsoPo = new CalypsoPo(selectionData, TransmissionMode.CONTACTLESS);

        Assert.assertEquals(PoRevision.REV1_0, calypsoPo.getRevision());
        Assert.assertNull(calypsoPo.getDfNameBytes());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(SERIAL_NUMBER),
                calypsoPo.getApplicationSerialNumber());
        Assert.assertFalse(calypsoPo.isModificationsCounterInBytes());
        Assert.assertEquals(3, calypsoPo.getModificationsCounter());
        Assert.assertEquals(PoClass.LEGACY, calypsoPo.getPoClass());
    }

    @Test
    public void testFlags_true() {
        CalypsoPo calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31);
        Assert.assertTrue(calypsoPo.isConfidentialSessionModeSupported());
        // negative logic for this one
        Assert.assertFalse(calypsoPo.isDeselectRatificationSupported());
        Assert.assertTrue(calypsoPo.isPinFeatureAvailable());
        Assert.assertTrue(calypsoPo.isSvFeatureAvailable());
    }


    @Test
    public void testFlags_false() {
        CalypsoPo calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31_FLAGS_FALSE);
        Assert.assertFalse(calypsoPo.isConfidentialSessionModeSupported());
        // negative logic for this one
        Assert.assertTrue(calypsoPo.isDeselectRatificationSupported());
        Assert.assertFalse(calypsoPo.isPinFeatureAvailable());
        Assert.assertFalse(calypsoPo.isSvFeatureAvailable());
    }

    @Test
    public void testDfInvalidated() {
        CalypsoPo calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31_INVALIDATED);
        Assert.assertTrue(calypsoPo.isDfInvalidated());
        calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31);
        Assert.assertFalse(calypsoPo.isDfInvalidated());
    }

    @Test
    public void otherAttributes() {
        CalypsoPo calypsoPo;
        /* Rev1 PO */
        calypsoPo = getCalypsoPo(ATR_VALUE, "6700");
        Assert.assertFalse(calypsoPo.isModificationsCounterInBytes());
        Assert.assertEquals(3, calypsoPo.getModificationsCounter());
        Assert.assertEquals(3, calypsoPo.getModificationsCounter());
        Assert.assertEquals(0x08, calypsoPo.getPlatform());
        Assert.assertEquals(0x03, calypsoPo.getApplicationType());
        Assert.assertEquals(0x04, calypsoPo.getApplicationSubtype());
        Assert.assertEquals(0x00, calypsoPo.getSoftwareIssuer());
        Assert.assertEquals(0x02, calypsoPo.getSoftwareVersion());
        Assert.assertEquals(0x00, calypsoPo.getSoftwareRevision());

        /* Rev 3.1 */
        calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31);
        Assert.assertTrue(calypsoPo.isModificationsCounterInBytes());
        Assert.assertEquals(430, calypsoPo.getModificationsCounter());
        Assert.assertEquals(0x3C, calypsoPo.getPlatform());
        Assert.assertEquals(0x2F, calypsoPo.getApplicationType());
        Assert.assertEquals(0x05, calypsoPo.getApplicationSubtype());
        Assert.assertEquals(0x14, calypsoPo.getSoftwareIssuer());
        Assert.assertEquals(0x10, calypsoPo.getSoftwareVersion());
        Assert.assertEquals(0x01, calypsoPo.getSoftwareRevision());
    }

    @Test
    public void getDirectoryHeader_whenDfIsNotSet_shouldReturnNull() {
        assertThat(po.getDirectoryHeader()).isNull();
    }

    @Test
    public void getDirectoryHeader_whenDfIsSet_shouldReturnAReference() {
        DirectoryHeader df = DirectoryHeader.builder().build();
        po.setDirectoryHeader(df);
        assertThat(po.getDirectoryHeader()).isSameAs(df);
    }

    @Test
    public void setDirectoryHeader_shouldSetAReference() {
        DirectoryHeader df = DirectoryHeader.builder().build();
        po.setDirectoryHeader(df);
        assertThat(po.getDirectoryHeader()).isSameAs(df);
    }

    @Test(expected = NoSuchElementException.class)
    public void getFileBySfi_whenSfiIsNotFound_shouldThrowNSEE() {
        po.getFileBySfi((byte) 1);
    }

    @Test
    public void getFileBySfi_whenSfiIsFound_shouldReturnAReference() {
        po.setContent((byte) 1, 1, new byte[1]);
        ElementaryFile ref1 = po.getFileBySfi((byte) 1);
        ElementaryFile ref2 = po.getFileBySfi((byte) 1);
        assertThat(ref2).isSameAs(ref1);
    }

    @Test(expected = NoSuchElementException.class)
    public void getFileByLid_whenLidIsNotFound_shouldThrowNSEE() {
        po.getFileByLid((short) 1);
    }

    @Test
    public void getFileByLid_whenLidIsFound_shouldReturnAReference() {
        po.setFileHeader((byte) 1, FileHeader.builder().lid((short) 2).build());
        ElementaryFile ref1 = po.getFileByLid((short) 2);
        ElementaryFile ref2 = po.getFileByLid((short) 2);
        assertThat(ref2).isSameAs(ref1);
    }

    @Test
    public void getAllFiles_whenFilesAreNotSet_shouldReturnANotNullInstance() {
        assertThat(po.getAllFiles()).isNotNull();
    }

    @Test
    public void getAllFiles_whenFilesAreSet_shouldReturnAReference() {
        po.setContent((byte) 1, 1, new byte[1]);
        Map<Byte, ElementaryFile> ref1 = po.getAllFiles();
        Map<Byte, ElementaryFile> ref2 = po.getAllFiles();
        assertThat(ref2).isSameAs(ref1);
    }

    @Test
    public void setFileHeader_whenSfiIsNotSet_shouldCreateEf() {
        try {
            po.getFileBySfi((byte) 1);
            shouldHaveThrown(NoSuchElementException.class);
        } catch (NoSuchElementException e) {
        }
        po.setFileHeader((byte) 1, FileHeader.builder().lid((short) 2).build());
        assertThat(po.getFileBySfi((byte) 1)).isNotNull();
    }

    @Test
    public void setFileHeader_whenSfiIsSet_shouldReplaceHeader() {
        FileHeader h1 = FileHeader.builder().lid((short) 1).build();
        FileHeader h2 = FileHeader.builder().lid((short) 2).build();
        po.setFileHeader((byte) 1, h1);
        po.setFileHeader((byte) 1, h2);
        assertThat(po.getFileBySfi((byte) 1).getHeader()).isSameAs(h2);
    }

    @Test
    public void setContentP3_whenSfiIsNotSet_shouldCreateEf() {
        try {
            po.getFileBySfi((byte) 1);
            shouldHaveThrown(NoSuchElementException.class);
        } catch (NoSuchElementException e) {
        }
        po.setContent((byte) 1, 1, new byte[1]);
        assertThat(po.getFileBySfi((byte) 1)).isNotNull();
    }

    @Test
    public void setCounter_whenSfiIsNotSet_shouldCreateEf() {
        try {
            po.getFileBySfi((byte) 1);
            shouldHaveThrown(NoSuchElementException.class);
        } catch (NoSuchElementException e) {
        }
        po.setCounter((byte) 1, 1, new byte[3]);
        assertThat(po.getFileBySfi((byte) 1)).isNotNull();
    }

    @Test
    public void setContentP4_whenSfiIsNotSet_shouldCreateEf() {
        try {
            po.getFileBySfi((byte) 1);
            shouldHaveThrown(NoSuchElementException.class);
        } catch (NoSuchElementException e) {
        }
        po.setContent((byte) 1, 1, new byte[1], 1);
        assertThat(po.getFileBySfi((byte) 1)).isNotNull();
    }

    @Test
    public void addCyclicContent_whenSfiIsNotSet_shouldCreateEf() {
        try {
            po.getFileBySfi((byte) 1);
            shouldHaveThrown(NoSuchElementException.class);
        } catch (NoSuchElementException e) {
        }
        po.addCyclicContent((byte) 1, new byte[1]);
        assertThat(po.getFileBySfi((byte) 1)).isNotNull();
    }

    @Test
    public void backupFiles_and_restoreFiles() {

        byte[] content;

        po.setContent((byte) 1, 1, new byte[1]);
        content = po.getFileBySfi((byte) 1).getData().getContent(1);
        byte[] contentV1 = Arrays.copyOf(content, content.length);

        po.backupFiles();

        content = po.getFileBySfi((byte) 1).getData().getContent(1);
        assertThat(content).isEqualTo(contentV1);

        po.setContent((byte) 1, 1, new byte[2]);
        content = po.getFileBySfi((byte) 1).getData().getContent(1);
        assertThat(content).isNotEqualTo(contentV1);

        po.restoreFiles();

        content = po.getFileBySfi((byte) 1).getData().getContent(1);
        assertThat(content).isEqualTo(contentV1);
    }
}

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CalypsoPoTest {
    public static final String ATR_VALUE = "3B8F8001805A08030400020011223344829000F3";
    public static final String ATR_VALUE_2 = "3B8F8001805A08030400020011223344829000";
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
    private static final String FCI_REV31 =
            "6F238409315449432E49434131A516BF0C13C708000000001122334453070A3C2F051410019000";
    private static final String FCI_REV31_FLAGS_FALSE =
            "6F238409315449432E49434131A516BF0C13C708000000001122334453070A3C20051410019000";
    private static final String FCI_REV31_INVALIDATED =
            "6F238409315449432E49434131A516BF0C13C708000000001122334453070A3C2F051410016283";
    private static final String FCI_REV31_HCE =
            "6F238409315449432E49434131A516BF0C13C708998811223344556653070A3C2F051410019000";

    private static final String DF_NAME = "315449432E494341";
    private static final String SERIAL_NUMBER = "0000000011223344";
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
    public void setUp() {
        po = getCalypsoPo(ATR_VALUE, FCI_REV31);
    }

    @Test
    public void getRevision() {
        assertThat(getPoApplicationByte((byte) 0x01).getRevision()).isEqualTo(PoRevision.REV2_4);
        assertThat(getPoApplicationByte((byte) 0x04).getRevision()).isEqualTo(PoRevision.REV2_4);
        assertThat(getPoApplicationByte((byte) 0x06).getRevision()).isEqualTo(PoRevision.REV2_4);
        assertThat(getPoApplicationByte((byte) 0x1F).getRevision()).isEqualTo(PoRevision.REV2_4);
        assertThat(getPoApplicationByte((byte) 0x20).getRevision()).isEqualTo(PoRevision.REV3_1);
        assertThat(getPoApplicationByte((byte) 0x27).getRevision()).isEqualTo(PoRevision.REV3_1);
        assertThat(getPoApplicationByte((byte) 0x28).getRevision()).isEqualTo(PoRevision.REV3_2);
        assertThat(getPoApplicationByte((byte) 0x2F).getRevision()).isEqualTo(PoRevision.REV3_2);
        assertThat(getPoApplicationByte((byte) 0x90).getRevision())
                .isEqualTo(PoRevision.REV3_1_CLAP);
    }

    @Test
    public void getDfNameBytes() {
        assertThat(getPoApplicationByte((byte) 0x01).getDfNameBytes())
                .isEqualTo(ByteArrayUtil.fromHex(DF_NAME));
    }

    @Test
    public void getDfName() {
        assertThat(getPoApplicationByte((byte) 0x01).getDfName()).isEqualTo(DF_NAME);
    }

    @Test
    public void getApplicationSerialNumber() {
        assertThat(getPoApplicationByte((byte) 0x01).getApplicationSerialNumberBytes())
                .isEqualTo(ByteArrayUtil.fromHex(SERIAL_NUMBER));
    }

    @Test
    public void getAtrBytes() {
        assertThat(getPoApplicationByte((byte) 0x01).getAtrBytes())
                .isEqualTo(ByteArrayUtil.fromHex(ATR_VALUE));
    }

    @Test
    public void getAtr() {
        assertThat(getPoApplicationByte((byte) 0x01).getAtr()).isEqualTo(ATR_VALUE);
    }

    @Test
    public void isModificationsCounterInBytes() {
        // false for rev 2
        assertThat(getPoApplicationByte((byte) 0x01).isModificationsCounterInBytes()).isFalse();
        // true for rev 3
        assertThat(getPoApplicationByte((byte) 0x27).isModificationsCounterInBytes()).isTrue();
    }

    @Test
    public void getModificationsCounter() {
        // 6 for rev 2
        assertThat(getPoApplicationByte((byte) 0x01).getModificationsCounter()).isEqualTo(6);
        // 215 bytes for rev 3
        assertThat(getPoApplicationByte((byte) 0x27).getModificationsCounter()).isEqualTo(215);
    }

    @Test
    public void getPoClass() {
        // LEGACY for rev 2
        assertThat(getPoApplicationByte((byte) 0x01).getPoClass()).isEqualTo(PoClass.LEGACY);
        // ISO bytes for rev 3
        assertThat(getPoApplicationByte((byte) 0x27).getPoClass()).isEqualTo(PoClass.ISO);
    }

    @Test
    public void getStartupInfo() {
        // Startup info
        assertThat(po.getStartupInfo()).isEqualTo("0A3C2F05141001");
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
        assertThat(calypsoPo.getCalypsoSerialNumber())
                .isEqualTo(ByteArrayUtil.fromHex("9988112233445566"));
        assertThat(calypsoPo.getApplicationSerialNumber()).isEqualTo("0000112233445566");
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

        assertThat(calypsoPo.getRevision()).isEqualTo(PoRevision.REV1_0);
        assertThat(calypsoPo.getDfNameBytes()).isNull();
        assertThat(calypsoPo.getApplicationSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(calypsoPo.isModificationsCounterInBytes()).isFalse();
        assertThat(calypsoPo.getModificationsCounter()).isEqualTo(3);
        assertThat(calypsoPo.getPoClass()).isEqualTo(PoClass.LEGACY);
    }

    @Test
    public void testFlags_true() {
        CalypsoPo calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31);
        assertThat(calypsoPo.isConfidentialSessionModeSupported()).isTrue();
        // negative logic for this one
        assertThat(calypsoPo.isDeselectRatificationSupported()).isFalse();
        assertThat(calypsoPo.isPinFeatureAvailable()).isTrue();
        assertThat(calypsoPo.isSvFeatureAvailable()).isTrue();
    }


    @Test
    public void testFlags_false() {
        CalypsoPo calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31_FLAGS_FALSE);
        assertThat(calypsoPo.isConfidentialSessionModeSupported()).isFalse();
        // negative logic for this one
        assertThat(calypsoPo.isDeselectRatificationSupported()).isTrue();
        assertThat(calypsoPo.isPinFeatureAvailable()).isFalse();
        assertThat(calypsoPo.isSvFeatureAvailable()).isFalse();
    }

    @Test
    public void testDfInvalidated() {
        CalypsoPo calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31_INVALIDATED);
        assertThat(calypsoPo.isDfInvalidated()).isTrue();
        calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31);
        assertThat(calypsoPo.isDfInvalidated()).isFalse();
    }

    @Test
    public void otherAttributes() {
        CalypsoPo calypsoPo;
        /* Rev1 PO */
        calypsoPo = getCalypsoPo(ATR_VALUE, "6700");
        assertThat(calypsoPo.isModificationsCounterInBytes()).isFalse();
        assertThat(calypsoPo.getModificationsCounter()).isEqualTo(3);
        assertThat(calypsoPo.getSessionModification()).isEqualTo((byte) 0x03);
        assertThat(calypsoPo.getPlatform()).isEqualTo((byte) 0x08);
        assertThat(calypsoPo.getApplicationType()).isEqualTo((byte) 0x03);
        assertThat(calypsoPo.getApplicationSubtype()).isEqualTo((byte) 0x04);
        assertThat(calypsoPo.getSoftwareIssuer()).isEqualTo((byte) 0x00);
        assertThat(calypsoPo.getSoftwareVersion()).isEqualTo((byte) 0x02);
        assertThat(calypsoPo.getSoftwareRevision()).isEqualTo((byte) 0x00);

        /* Rev 3.1 */
        calypsoPo = getCalypsoPo(ATR_VALUE, FCI_REV31);
        assertThat(calypsoPo.isModificationsCounterInBytes()).isTrue();
        assertThat(calypsoPo.getModificationsCounter()).isEqualTo(430);
        assertThat(calypsoPo.getSessionModification()).isEqualTo((byte) 0x0A);
        assertThat(calypsoPo.getPlatform()).isEqualTo((byte) 0x3C);
        assertThat(calypsoPo.getApplicationType()).isEqualTo((byte) 0x2F);
        assertThat(calypsoPo.getApplicationSubtype()).isEqualTo((byte) 0x05);
        assertThat(calypsoPo.getSoftwareIssuer()).isEqualTo((byte) 0x14);
        assertThat(calypsoPo.getSoftwareVersion()).isEqualTo((byte) 0x10);
        assertThat(calypsoPo.getSoftwareRevision()).isEqualTo((byte) 0x01);
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
    public void fillContent_whenSfiIsNotSet_shouldCreateEf() {
        try {
            po.getFileBySfi((byte) 1);
            shouldHaveThrown(NoSuchElementException.class);
        } catch (NoSuchElementException e) {
        }
        po.fillContent((byte) 1, 1, new byte[1]);
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

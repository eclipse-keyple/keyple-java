/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PoSelectionRequestTest {
    private final static String ATR_VALUE = "3B8F8001805A08030400020011223344829000F3";
    private final static String FILE_PATH = "3F00";
    private final static String DF_NAME = "315449432E494341";
    private final static String SERIAL_NUMBER = "0000000011223344";
    private final static String RECORD_CONTENT = "00112233445566778899";
    private final static String FILE_FCI_1 = "85170001000000101000000103000000797979616770003F00";
    private final static String FILE_FCI_2 = "851704040210011F1000000001000000000000000000003F04";
    private final static String FILE_FCI_3 = "85170804041D031F1010100003030300000000000000002010";
    private final ApduResponse READ_REC_APDU_RESPONSE =
            new ApduResponse(ByteArrayUtil.fromHex(RECORD_CONTENT + "9000"), null);
    private final ApduResponse SELECT_FILE_APDU_RESPONSE_1 =
            new ApduResponse(ByteArrayUtil.fromHex(FILE_FCI_1 + "9000"), null);
    private final ApduResponse SELECT_FILE_APDU_RESPONSE_2 =
            new ApduResponse(ByteArrayUtil.fromHex(FILE_FCI_2 + "9000"), null);
    private final ApduResponse SELECT_FILE_APDU_RESPONSE_3 =
            new ApduResponse(ByteArrayUtil.fromHex(FILE_FCI_3 + "9000"), null);
    private PoSelectionRequest poSelectionRequest;

    @Before
    public void setUp() throws Exception {
        poSelectionRequest =
                new PoSelectionRequest(
                        new PoSelector(SeCommonProtocols.PROTOCOL_ISO14443_4,
                                new PoSelector.PoAtrFilter(".*"), null, "ATR: .*"),
                        ChannelState.KEEP_OPEN);
    }

    @Test
    public void parse() {
        AnswerToReset atr = new AnswerToReset(ByteArrayUtil.fromHex(ATR_VALUE));
        ApduResponse fciData = new ApduResponse(ByteArrayUtil.fromHex("6F 22 84 08 " + DF_NAME
                + "A5 16 BF0C 13 C7 08 " + SERIAL_NUMBER + "53 07 060A 27 02200311 9000"), null);
        CalypsoPo calypsoPo = poSelectionRequest
                .parse(new SeResponse(true, false, new SelectionStatus(atr, fciData, true), null));
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(SERIAL_NUMBER),
                calypsoPo.getApplicationSerialNumber());
    }

    @Test
    public void prepareReadRecordsCmd() {
        int readParserIndex1 = poSelectionRequest.prepareReadRecordsCmd((byte) 0x01,
                ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, "Read record SFI 01");
        int readParserIndex2 = poSelectionRequest.prepareReadRecordsCmd((byte) 0x02,
                ReadDataStructure.SINGLE_COUNTER, (byte) 0x01, "Read counter SFI 02");
        Assert.assertEquals(readParserIndex1, 0);
        Assert.assertEquals(readParserIndex2, 1);

        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
        // add two response
        ((ArrayList) apduResponseList).add(READ_REC_APDU_RESPONSE);
        ((ArrayList) apduResponseList).add(READ_REC_APDU_RESPONSE);

        ReadRecordsRespPars readRecordsRespPars1 =
                (ReadRecordsRespPars) poSelectionRequest.getCommandParser(
                        new SeResponse(true, true, null, apduResponseList), readParserIndex1);

        ReadRecordsRespPars readRecordsRespPars2 =
                (ReadRecordsRespPars) poSelectionRequest.getCommandParser(
                        new SeResponse(true, true, null, apduResponseList), readParserIndex2);

        Assert.assertArrayEquals(ByteArrayUtil.fromHex(RECORD_CONTENT),
                readRecordsRespPars1.getRecords().get(1));
        Assert.assertEquals((Integer) 0x001122,
                (Integer) readRecordsRespPars2.getCounters().get(1));
        Assert.assertEquals((Integer) 0x334455,
                (Integer) readRecordsRespPars2.getCounters().get(2));
    }

    @Test
    public void prepareReadRecordsCmd1() {
        int readParserIndex1 = poSelectionRequest.prepareReadRecordsCmd((byte) 0x01,
                ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, "Read rec SFI 01");
        int readParserIndex2 = poSelectionRequest.prepareReadRecordsCmd((byte) 0x01,
                ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, "Read rec SFI 01");
        Assert.assertEquals(readParserIndex1, 0);
        Assert.assertEquals(readParserIndex2, 1);

        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
        // add two response
        ((ArrayList) apduResponseList).add(READ_REC_APDU_RESPONSE);
        ((ArrayList) apduResponseList).add(READ_REC_APDU_RESPONSE);

        ReadRecordsRespPars readRecordsRespPars1 =
                (ReadRecordsRespPars) poSelectionRequest.getCommandParser(
                        new SeResponse(true, true, null, apduResponseList), readParserIndex1);

        Assert.assertArrayEquals(ByteArrayUtil.fromHex(RECORD_CONTENT),
                readRecordsRespPars1.getRecords().get(1));
    }

    @Test
    public void prepareReadRecordsCmd2() {
        int readParserIndex1 = poSelectionRequest.prepareReadRecordsCmd((byte) 0x01,
                ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, 29, "Read rec SFI 01");
        Assert.assertEquals(readParserIndex1, 0);
        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
        // add one response
        ((ArrayList) apduResponseList).add(READ_REC_APDU_RESPONSE);

        ReadRecordsRespPars readRecordsRespPars1 =
                (ReadRecordsRespPars) poSelectionRequest.getCommandParser(
                        new SeResponse(true, true, null, apduResponseList), readParserIndex1);

        Assert.assertArrayEquals(ByteArrayUtil.fromHex(RECORD_CONTENT),
                readRecordsRespPars1.getRecords().get(1));
    }

    @Test
    public void prepareSelectFileCmd1() {
        int selectIndex1 = poSelectionRequest
                .prepareSelectFileCmd(SelectFileCmdBuild.SelectControl.CURRENT_DF, "CurrentDF");
        int selectIndex2 = poSelectionRequest
                .prepareSelectFileCmd(SelectFileCmdBuild.SelectControl.FIRST, "First");
        int selectIndex3 = poSelectionRequest
                .prepareSelectFileCmd(SelectFileCmdBuild.SelectControl.NEXT, "Next");

        Assert.assertEquals(selectIndex1, 0);
        Assert.assertEquals(selectIndex2, 1);
        Assert.assertEquals(selectIndex3, 2);

        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
        ((ArrayList) apduResponseList).add(SELECT_FILE_APDU_RESPONSE_1);
        ((ArrayList) apduResponseList).add(SELECT_FILE_APDU_RESPONSE_2);
        ((ArrayList) apduResponseList).add(SELECT_FILE_APDU_RESPONSE_3);

        SelectFileRespPars selectFileRespPars1 = (SelectFileRespPars) poSelectionRequest
                .getCommandParser(new SeResponse(true, true, null, apduResponseList), selectIndex1);

        SelectFileRespPars selectFileRespPars2 = (SelectFileRespPars) poSelectionRequest
                .getCommandParser(new SeResponse(true, true, null, apduResponseList), selectIndex2);

        SelectFileRespPars selectFileRespPars3 = (SelectFileRespPars) poSelectionRequest
                .getCommandParser(new SeResponse(true, true, null, apduResponseList), selectIndex3);

        Assert.assertTrue(selectFileRespPars1.isSelectionSuccessful());
        Assert.assertTrue(selectFileRespPars2.isSelectionSuccessful());
        Assert.assertTrue(selectFileRespPars3.isSelectionSuccessful());

        // file 1 (MF)
        Assert.assertEquals(0x3F00, selectFileRespPars1.getLid());
        Assert.assertArrayEquals(new byte[] {(byte) 0x61, (byte) 0x67, (byte) 0x70},
                selectFileRespPars1.getKifInfo());
        Assert.assertArrayEquals(new byte[] {(byte) 0x79, (byte) 0x79, (byte) 0x79},
                selectFileRespPars1.getKvcInfo());
        Assert.assertEquals(SelectFileRespPars.FILE_TYPE_MF, selectFileRespPars1.getFileType());
        Assert.assertEquals(SelectFileRespPars.EF_TYPE_DF, selectFileRespPars1.getEfType());

        // file 2 (EF Lin)
        Assert.assertEquals(0x3F04, selectFileRespPars2.getLid());
        Assert.assertNull(selectFileRespPars2.getKifInfo());
        Assert.assertNull(selectFileRespPars2.getKvcInfo());
        Assert.assertEquals(SelectFileRespPars.FILE_TYPE_EF, selectFileRespPars2.getFileType());
        Assert.assertEquals(SelectFileRespPars.EF_TYPE_LINEAR, selectFileRespPars2.getEfType());

        // file 3 (EF Cyc)
        Assert.assertEquals(0x2010, selectFileRespPars3.getLid());
        Assert.assertNull(selectFileRespPars3.getKifInfo());
        Assert.assertNull(selectFileRespPars3.getKvcInfo());
        Assert.assertArrayEquals(new byte[] {(byte) 0x1F, (byte) 0x10, (byte) 0x10, (byte) 0x10},
                selectFileRespPars3.getAccessConditions());
        Assert.assertArrayEquals(new byte[] {(byte) 0x00, (byte) 0x03, (byte) 0x03, (byte) 0x03},
                selectFileRespPars3.getKeyIndexes());
        Assert.assertEquals(SelectFileRespPars.FILE_TYPE_EF, selectFileRespPars3.getFileType());
        Assert.assertEquals(SelectFileRespPars.EF_TYPE_CYCLIC, selectFileRespPars3.getEfType());
        Assert.assertEquals(29, selectFileRespPars3.getRecSize());
        Assert.assertEquals(3, selectFileRespPars3.getNumRec());
        Assert.assertEquals(8, selectFileRespPars3.getSfi());
    }

    @Test
    public void prepareSelectFileCmd2() {
        int selectIndex1 = poSelectionRequest.prepareSelectFileCmd(ByteArrayUtil.fromHex(FILE_PATH),
                "Path 3F00");

        Assert.assertEquals(selectIndex1, 0);

        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
        ((ArrayList) apduResponseList).add(SELECT_FILE_APDU_RESPONSE_1);

        SelectFileRespPars selectFileRespPars1 = (SelectFileRespPars) poSelectionRequest
                .getCommandParser(new SeResponse(true, true, null, apduResponseList), selectIndex1);
        Assert.assertEquals(0x3F00, selectFileRespPars1.getLid());
    }

    @Test
    public void preparePoCustomReadCmd() {
        ApduRequest apduRequest = new ApduRequest(ByteArrayUtil.fromHex("FFCA000000"), false);
        int readParserIndex1 =
                poSelectionRequest.preparePoCustomReadCmd("Custom command read", apduRequest);
        Assert.assertEquals(0, readParserIndex1);
    }

    @Test
    public void preparePoCustomModificationCmd() {
        ApduRequest apduRequest = new ApduRequest(ByteArrayUtil.fromHex("FFCA000000"), false);
        int modifyParserIndex1 = poSelectionRequest
                .preparePoCustomModificationCmd("Custom command modify", apduRequest);
        Assert.assertEquals(0, modifyParserIndex1);
    }
}

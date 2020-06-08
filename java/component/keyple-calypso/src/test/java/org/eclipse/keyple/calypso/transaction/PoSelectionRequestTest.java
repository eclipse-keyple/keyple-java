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
import org.eclipse.keyple.calypso.SelectFileControl;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoDesynchronizedExchangesException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PoSelectionRequestTest {
    private static final String ATR_VALUE = "3B8F8001805A08030400020011223344829000F3";
    private static final String FCI =
            "6F238409315449432E49434131A516BF0C13C708000000001122334453070A3C2F051410019000";
    private static final String RECORD_CONTENT1 = "00112233445566778899";
    private static final String RECORD_CONTENT2 = "99887766554433221100";
    private static final String EF_SELECT_DATA =
            "85170804041D031F1010100003030300000000000000002010";
    private static final String DF_SELECT_DATA =
            "85170001000000101000000103000000797979616770003F00";

    private final ApduResponse SELECT_APPLICATION_RESPONSE =
            new ApduResponse(ByteArrayUtil.fromHex(FCI), null);
    private final ApduResponse READ_REC_APDU_RESPONSE1 =
            new ApduResponse(ByteArrayUtil.fromHex(RECORD_CONTENT1 + "9000"), null);
    private final ApduResponse READ_REC_APDU_RESPONSE2 =
            new ApduResponse(ByteArrayUtil.fromHex(RECORD_CONTENT2 + "9000"), null);
    private final ApduResponse SELECT_DF_APDU_RESPONSE =
            new ApduResponse(ByteArrayUtil.fromHex(DF_SELECT_DATA + "9000"), null);
    private final ApduResponse SELECT_EF_APDU_RESPONSE =
            new ApduResponse(ByteArrayUtil.fromHex(EF_SELECT_DATA + "9000"), null);
    private PoSelectionRequest poSelectionRequest;

    @Before
    public void setUp() throws Exception {
        poSelectionRequest = new PoSelectionRequest(
                PoSelector.builder().seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                        .atrFilter(new PoSelector.AtrFilter(".*"))
                        .invalidatedPo(PoSelector.InvalidatedPo.REJECT).build());
    }

    @Test
    public void testPrepareReadRecordFile()
            throws CalypsoDesynchronizedExchangesException, CalypsoPoCommandException {
        byte sfi1 = (byte) 0x10;
        byte sfi2 = (byte) 0x11;
        int recNumber = 1;
        poSelectionRequest.prepareReadRecordFile(sfi1, recNumber);
        poSelectionRequest.prepareReadRecordFile(sfi2, recNumber);
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(READ_REC_APDU_RESPONSE1);
        apduResponses.add(READ_REC_APDU_RESPONSE2);
        SelectionStatus selectionStatus =
                new SelectionStatus(new AnswerToReset(ByteArrayUtil.fromHex(ATR_VALUE)),
                        SELECT_APPLICATION_RESPONSE, true);
        SeResponse seResponse = new SeResponse(true, true, selectionStatus, apduResponses);
        CalypsoPo calypsoPo = poSelectionRequest.parse(seResponse);

        ElementaryFile ef1 = calypsoPo.getFileBySfi(sfi1);
        FileData records1 = ef1.getData();
        byte[] record1 = records1.getContent();
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(RECORD_CONTENT1), record1);

        ElementaryFile ef2 = calypsoPo.getFileBySfi(sfi2);
        FileData records2 = ef2.getData();
        byte[] record2 = records2.getContent();
        Assert.assertArrayEquals(ByteArrayUtil.fromHex(RECORD_CONTENT2), record2);
    }

    @Test
    public void testPrepareSelectFile1_lid()
            throws CalypsoDesynchronizedExchangesException, CalypsoPoCommandException {
        short lid1 = 0x2010;
        short lid2 = 0x3F00;
        poSelectionRequest.prepareSelectFile(lid1);
        poSelectionRequest.prepareSelectFile(lid2);
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(SELECT_EF_APDU_RESPONSE);
        apduResponses.add(SELECT_DF_APDU_RESPONSE);
        SelectionStatus selectionStatus =
                new SelectionStatus(new AnswerToReset(ByteArrayUtil.fromHex(ATR_VALUE)),
                        SELECT_APPLICATION_RESPONSE, true);
        SeResponse seResponse = new SeResponse(true, true, selectionStatus, apduResponses);
        CalypsoPo calypsoPo = poSelectionRequest.parse(seResponse);

        ElementaryFile ef = calypsoPo.getFileByLid(lid1);
        FileHeader efHeader = ef.getHeader();
        Assert.assertEquals(lid1, efHeader.getLid());
        Assert.assertEquals(29, efHeader.getRecordSize());
        Assert.assertEquals(3, efHeader.getRecordsNumber());
        Assert.assertEquals(FileHeader.FileType.CYCLIC, efHeader.getType());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("1F101010"), efHeader.getAccessConditions());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("00030303"), efHeader.getKeyIndexes());
        Assert.assertEquals(0, efHeader.getDfStatus());
        Short sharedReference = 0;
        Assert.assertEquals(sharedReference, efHeader.getSharedReference());

        DirectoryHeader dfHeader = calypsoPo.getDirectoryHeader();
        Assert.assertEquals(lid2, dfHeader.getLid());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("10100000"), dfHeader.getAccessConditions());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("01030000"), dfHeader.getKeyIndexes());
        Assert.assertEquals(0, dfHeader.getDfStatus());
    }

    @Test
    public void testPrepareSelectFile_control()
            throws CalypsoDesynchronizedExchangesException, CalypsoPoCommandException {
        short lid1 = 0x2010;
        short lid2 = 0x3F00;
        poSelectionRequest.prepareSelectFile(SelectFileControl.CURRENT_DF);
        poSelectionRequest.prepareSelectFile(SelectFileControl.FIRST_EF);
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(SELECT_EF_APDU_RESPONSE);
        apduResponses.add(SELECT_DF_APDU_RESPONSE);
        SelectionStatus selectionStatus =
                new SelectionStatus(new AnswerToReset(ByteArrayUtil.fromHex(ATR_VALUE)),
                        SELECT_APPLICATION_RESPONSE, true);
        SeResponse seResponse = new SeResponse(true, true, selectionStatus, apduResponses);
        CalypsoPo calypsoPo = poSelectionRequest.parse(seResponse);

        ElementaryFile ef = calypsoPo.getFileByLid(lid1);
        FileHeader efHeader = ef.getHeader();
        Assert.assertEquals(lid1, efHeader.getLid());
        Assert.assertEquals(29, efHeader.getRecordSize());
        Assert.assertEquals(3, efHeader.getRecordsNumber());
        Assert.assertEquals(FileHeader.FileType.CYCLIC, efHeader.getType());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("1F101010"), efHeader.getAccessConditions());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("00030303"), efHeader.getKeyIndexes());
        Assert.assertEquals(0, efHeader.getDfStatus());
        Short sharedReference = 0;
        Assert.assertEquals(sharedReference, efHeader.getSharedReference());

        DirectoryHeader dfHeader = calypsoPo.getDirectoryHeader();
        Assert.assertEquals(lid2, dfHeader.getLid());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("10100000"), dfHeader.getAccessConditions());
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("01030000"), dfHeader.getKeyIndexes());
        Assert.assertEquals(0, dfHeader.getDfStatus());
    }
}

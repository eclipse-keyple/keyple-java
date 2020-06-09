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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild.ReadMode;
import java.util.SortedMap;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReadRecordsRespParsTest {
    private static final String SW1SW2_KO = "6A82";
    private static final String SW1SW2_OK = "9000";
    private static final String REC1 = "112233445566778899AABBCCDDEEFF00";
    private static final String REC2 = "00FFEEDDCCBBAA998877665544332211";
    private static final String APDU_BAD_SW1SW2 = SW1SW2_KO;
    private static final String APDU_ONE_RECORD = REC1 + SW1SW2_OK;
    private static final String APDU_TWO_RECORDS = String.format("01%02X", REC1.length() / 2) + REC1
            + String.format("02%02X", REC2.length() / 2) + REC2 + SW1SW2_OK;
    private static final int SFI1 = 1;
    private static final int FIRST_REC1 = 1;
    private static final int EXPECTED_LENGTH1 = REC1.length();
    private static final int EXPECTED_LENGTH2 = (REC1.length() + REC2.length()) / 2;

    @Test(expected = CalypsoPoCommandException.class)
    public void readRecordRespPars_badStatus() throws CalypsoPoCommandException {
        ReadRecordsRespPars readRecordRespPars = new ReadRecordsRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(APDU_BAD_SW1SW2), null), null);
        readRecordRespPars.checkStatus();
        shouldHaveThrown(CalypsoPoCommandException.class);
    }

    @Test
    public void readRecordRespPars_goodStatus() throws CalypsoPoCommandException {
        ReadRecordsRespPars readRecordRespPars = new ReadRecordsRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(APDU_ONE_RECORD), null), null);
        readRecordRespPars.checkStatus();
    }

    @Test
    public void readRecordRespPars_getRecords_singleRecord() throws CalypsoPoCommandException {
        ReadRecordsCmdBuild readRecordsCmdBuild = new ReadRecordsCmdBuild(PoClass.ISO, SFI1,
                FIRST_REC1, ReadMode.ONE_RECORD, EXPECTED_LENGTH1);
        ReadRecordsRespPars readRecordRespPars = readRecordsCmdBuild.createResponseParser(
                new ApduResponse(ByteArrayUtil.fromHex(APDU_ONE_RECORD), null));
        readRecordRespPars.checkStatus();
        SortedMap<Integer, byte[]> records = readRecordRespPars.getRecords();
        assertThat(records.size()).isEqualTo(1);
        assertThat(records.get(FIRST_REC1)).isEqualTo(ByteArrayUtil.fromHex(REC1));
    }

    @Test
    public void readRecordRespPars_getRecords_twoRecords() throws CalypsoPoCommandException {
        ReadRecordsCmdBuild readRecordsCmdBuild = new ReadRecordsCmdBuild(PoClass.ISO, SFI1,
                FIRST_REC1, ReadMode.MULTIPLE_RECORD, EXPECTED_LENGTH2);
        ReadRecordsRespPars readRecordRespPars = readRecordsCmdBuild.createResponseParser(
                new ApduResponse(ByteArrayUtil.fromHex(APDU_TWO_RECORDS), null));
        readRecordRespPars.checkStatus();
        SortedMap<Integer, byte[]> records = readRecordRespPars.getRecords();
        assertThat(records.size()).isEqualTo(2);
        assertThat(records.get(FIRST_REC1)).isEqualTo(ByteArrayUtil.fromHex(REC1));
        assertThat(records.get(FIRST_REC1 + 1)).isEqualTo(ByteArrayUtil.fromHex(REC2));
    }
}

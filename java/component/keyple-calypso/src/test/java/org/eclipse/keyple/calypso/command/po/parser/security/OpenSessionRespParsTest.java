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
package org.eclipse.keyple.calypso.command.po.parser.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.security.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenSessionRespParsTest {
    private static final String SW1SW2_OK = "9000";
    private static final String SW1SW2_KO = "6A83";
    private static final String SAM_CHALLENGE_4 = "11223344";
    private static final String SAM_CHALLENGE_8 = "1122334444332211";
    private static final String PO_TN = "112233";
    private static final String PO_RANDOM_1 = "44";
    private static final String PO_RANDOM_5 = "4433221100";
    private static final String RATIFIED = "00";
    private static final String NOT_RATIFIED = "01";
    private static final String REV10_24_RATIFICATION_BYTES = "AABB";
    private static final String KIF = "12";
    private static final String KIF_FF = "FF";
    private static final String KVC = "34";
    private static final String KVC_FF = "FF";
    private static final String DATA = "0011223344556677889900112233445566778899001122334455667788";
    private static final String DATA_LENGTH = String.format("%02X", DATA.length() / 2);
    private static final String DATA_EMPTY = "";
    private static final String DATA_LENGTH_0 = "00";
    private static final byte SFI = (byte) 0x01;
    private static final byte REC = (byte) 1;
    private static final String OPEN_SECURE_SESSION_RESP_10_KVC_RATIFIED = PO_TN + PO_RANDOM_1;
    private static final String OPEN_SECURE_SESSION_RESP_10_KVC_NOT_RATIFIED =
            PO_TN + PO_RANDOM_1 + REV10_24_RATIFICATION_BYTES;
    private static final String OPEN_SECURE_SESSION_RESP_24_KVC_RATIFIED =
            KVC + PO_TN + PO_RANDOM_1;
    private static final String OPEN_SECURE_SESSION_RESP_24_KVC_NOT_RATIFIED =
            KVC + PO_TN + PO_RANDOM_1 + REV10_24_RATIFICATION_BYTES;
    private static final String OPEN_SECURE_SESSION_RESP_31_RATIFIED =
            PO_TN + PO_RANDOM_1 + RATIFIED + KIF + KVC;
    private static final String OPEN_SECURE_SESSION_RESP_31_NOT_RATIFIED =
            PO_TN + PO_RANDOM_1 + NOT_RATIFIED + KIF + KVC;
    private static final String OPEN_SECURE_SESSION_RESP_32_RATIFIED =
            PO_TN + PO_RANDOM_5 + RATIFIED + KIF + KVC;
    private static final String OPEN_SECURE_SESSION_RESP_32_NOT_RATIFIED =
            PO_TN + PO_RANDOM_5 + NOT_RATIFIED + KIF + KVC;

    @Test
    public void openSessionRespPars_rev1_0_readingData() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(OPEN_SECURE_SESSION_RESP_10_KVC_RATIFIED + DATA + SW1SW2_OK),
                null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV1_0,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), SFI, REC);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF_FF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC_FF)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA));
        assertThat(responseParser.wasRatified()).isTrue();
    }

    @Test
    public void openSessionRespPars_rev1_0_notReadingData() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(OPEN_SECURE_SESSION_RESP_10_KVC_RATIFIED + SW1SW2_OK), null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV1_0,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF_FF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC_FF)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA_EMPTY));
        assertThat(responseParser.wasRatified()).isTrue();
    }

    @Test
    public void openSessionRespPars_rev1_0_readingData_notRatified() {
        ApduResponse response = new ApduResponse(ByteArrayUtil
                .fromHex(OPEN_SECURE_SESSION_RESP_10_KVC_NOT_RATIFIED + DATA + SW1SW2_OK), null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV1_0,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), SFI, REC);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF_FF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC_FF)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA));
        assertThat(responseParser.wasRatified()).isFalse();
    }

    @Test
    public void openSessionRespPars_rev1_0_notReadingData_notRatified() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(OPEN_SECURE_SESSION_RESP_10_KVC_NOT_RATIFIED + SW1SW2_OK),
                null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV1_0,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF_FF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC_FF)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA_EMPTY));
        assertThat(responseParser.wasRatified()).isFalse();
    }

    @Test(expected = CalypsoPoCommandException.class)
    public void openSessionRespPars_rev1_0_badStatus() {
        ApduResponse response = new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV1_0,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        shouldHaveThrown(CalypsoPoCommandException.class);
    }

    @Test
    public void openSessionRespPars_rev2_4_readingData() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(OPEN_SECURE_SESSION_RESP_24_KVC_RATIFIED + DATA + SW1SW2_OK),
                null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV2_4,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), SFI, REC);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF_FF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA));
        assertThat(responseParser.wasRatified()).isTrue();
    }

    @Test
    public void openSessionRespPars_rev2_4_notReadingData() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(OPEN_SECURE_SESSION_RESP_24_KVC_RATIFIED + SW1SW2_OK), null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV2_4,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF_FF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA_EMPTY));
        assertThat(responseParser.wasRatified()).isTrue();
    }

    @Test
    public void openSessionRespPars_rev2_4_readingData_notRatified() {
        ApduResponse response = new ApduResponse(ByteArrayUtil
                .fromHex(OPEN_SECURE_SESSION_RESP_24_KVC_NOT_RATIFIED + DATA + SW1SW2_OK), null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV2_4,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), SFI, REC);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF_FF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA));
        assertThat(responseParser.wasRatified()).isFalse();
    }

    @Test
    public void openSessionRespPars_rev2_4_notReadingData_notRatified() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(OPEN_SECURE_SESSION_RESP_24_KVC_NOT_RATIFIED + SW1SW2_OK),
                null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV2_4,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF_FF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA_EMPTY));
        assertThat(responseParser.wasRatified()).isFalse();
    }

    @Test(expected = CalypsoPoCommandException.class)
    public void openSessionRespPars_rev2_4_badStatus() {
        ApduResponse response = new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV2_4,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        shouldHaveThrown(CalypsoPoCommandException.class);
    }

    @Test
    public void openSessionRespPars_rev3_1_readingData() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(
                        OPEN_SECURE_SESSION_RESP_31_RATIFIED + DATA_LENGTH + DATA + SW1SW2_OK),
                null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV3_1,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), SFI, REC);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA));
        assertThat(responseParser.wasRatified()).isTrue();
    }

    @Test
    public void openSessionRespPars_rev3_1_notReadingData() {
        ApduResponse response = new ApduResponse(ByteArrayUtil
                .fromHex(OPEN_SECURE_SESSION_RESP_31_RATIFIED + DATA_LENGTH_0 + SW1SW2_OK), null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV3_1,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA_EMPTY));
        assertThat(responseParser.wasRatified()).isTrue();
    }

    @Test
    public void openSessionRespPars_rev3_1_readingData_notRatified() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(
                        OPEN_SECURE_SESSION_RESP_31_NOT_RATIFIED + DATA_LENGTH + DATA + SW1SW2_OK),
                null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV3_1,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), SFI, REC);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA));
        assertThat(responseParser.wasRatified()).isFalse();
    }

    @Test
    public void openSessionRespPars_rev3_1_notReadingData_notRatified() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(
                        OPEN_SECURE_SESSION_RESP_31_NOT_RATIFIED + DATA_LENGTH_0 + SW1SW2_OK),
                null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV3_1,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_1));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA_EMPTY));
        assertThat(responseParser.wasRatified()).isFalse();
    }

    @Test(expected = CalypsoPoCommandException.class)
    public void openSessionRespPars_rev3_1_badStatus() {
        ApduResponse response = new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV3_1,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_4), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        shouldHaveThrown(CalypsoPoCommandException.class);
    }

    @Test
    public void openSessionRespPars_rev3_2_readingData() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(
                        OPEN_SECURE_SESSION_RESP_32_RATIFIED + DATA_LENGTH + DATA + SW1SW2_OK),
                null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV3_2,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_8), SFI, REC);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_5));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA));
        assertThat(responseParser.wasRatified()).isTrue();
    }

    @Test
    public void openSessionRespPars_rev3_2_notReadingData() {
        ApduResponse response = new ApduResponse(ByteArrayUtil
                .fromHex(OPEN_SECURE_SESSION_RESP_32_RATIFIED + DATA_LENGTH_0 + SW1SW2_OK), null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV3_2,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_8), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_5));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA_EMPTY));
        assertThat(responseParser.wasRatified()).isTrue();
    }

    @Test
    public void openSessionRespPars_rev3_2_readingData_notRatified() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(
                        OPEN_SECURE_SESSION_RESP_32_NOT_RATIFIED + DATA_LENGTH + DATA + SW1SW2_OK),
                null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV3_2,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_8), SFI, REC);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_5));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA));
        assertThat(responseParser.wasRatified()).isFalse();
    }

    @Test
    public void openSessionRespPars_rev3_2_notReadingData_notRatified() {
        ApduResponse response = new ApduResponse(
                ByteArrayUtil.fromHex(
                        OPEN_SECURE_SESSION_RESP_32_NOT_RATIFIED + DATA_LENGTH_0 + SW1SW2_OK),
                null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV3_2,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_8), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        assertThat(responseParser.getPoChallenge()).isEqualTo(ByteArrayUtil.fromHex(PO_RANDOM_5));
        assertThat(responseParser.getTransactionCounterValue())
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(PO_TN), 0));
        assertThat(responseParser.getSelectedKif()).isEqualTo(ByteArrayUtil.fromHex(KIF)[0]);
        assertThat(responseParser.getSelectedKvc()).isEqualTo(ByteArrayUtil.fromHex(KVC)[0]);
        assertThat(responseParser.getRecordDataRead()).isEqualTo(ByteArrayUtil.fromHex(DATA_EMPTY));
        assertThat(responseParser.wasRatified()).isFalse();
    }

    @Test(expected = CalypsoPoCommandException.class)
    public void openSessionRespPars_rev3_2_badStatus() {
        ApduResponse response = new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null);
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(PoRevision.REV3_2,
                        PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT.getSessionKey(),
                        ByteArrayUtil.fromHex(SAM_CHALLENGE_8), 0, 0);
        AbstractOpenSessionRespPars responseParser =
                openSessionCmdBuild.createResponseParser(response);
        responseParser.checkStatus();
        shouldHaveThrown(CalypsoPoCommandException.class);
    }
}

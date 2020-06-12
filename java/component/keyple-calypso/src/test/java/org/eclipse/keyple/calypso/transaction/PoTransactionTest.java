/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.AccessLevel;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.keyple.calypso.SelectFileControl;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoAtomicTransactionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoAuthenticationNotVerifiedException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoCloseSecureSessionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoIOException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionIllegalStateException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoSessionAuthenticationException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoUnauthorizedKvcException;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.AnswerToReset;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class PoTransactionTest {
    // The default KIF values for personalization, loading and debiting
    final byte DEFAULT_KIF_PERSO = (byte) 0x21;
    final byte DEFAULT_KIF_LOAD = (byte) 0x27;
    final byte DEFAULT_KIF_DEBIT = (byte) 0x30;
    // The default key record number values for personalization, loading and debiting
    // The actual value should be adjusted.
    final byte DEFAULT_KEY_RECORD_NUMBER_PERSO = (byte) 0x01;
    final byte DEFAULT_KEY_RECORD_NUMBER_LOAD = (byte) 0x02;
    final byte DEFAULT_KEY_RECORD_NUMBER_DEBIT = (byte) 0x03;
    private SeReader poReader;
    private PoTransaction poTransaction;
    private SamResource samResource;

    private final Map<String, String> poCommandsTestSet = new HashMap<String, String>();
    private final Map<String, String> samCommandsTestSet = new HashMap<String, String>();
    private static final String FCI_REV10 =
            "6F228408315449432E494341A516BF0C13C708   0000000011223344 5307060A01032003119000";
    private static final String FCI_REV24 =
            "6F2A8410A0000004040125090101000000000000A516BF0C13C708 0000000011223344 53070A2E11420001019000";
    private static final String FCI_REV31 =
            "6F238409315449432E49434131A516BF0C13C708 0000000011223344 53070A3C23121410019000";

    private static final String ATR1 = "3B3F9600805A0080C120000012345678829000";

    private static final byte FILE7 = (byte) 0x07;
    private static final byte FILE8 = (byte) 0x08;
    private static final byte FILE9 = (byte) 0x09;
    private static final byte FILE10 = (byte) 0x10;
    private static final byte FILE11 = (byte) 0x11;

    private static final String SW1SW2_OK = "9000";
    private static final String SAM_CHALLENGE = "C1C2C3C4";
    private static final String PO_DIVERSIFIER = "0000000011223344";
    private static final String SAM_SIGNATURE = "12345678";
    private static final String PO_SIGNATURE = "9ABCDEF0";

    private static final String FILE7_REC1_29B =
            "7111111111111111111111111111111111111111111111111111111111";
    private static final String FILE7_REC2_29B =
            "7222222222222222222222222222222222222222222222222222222222";
    private static final String FILE7_REC3_29B =
            "7333333333333333333333333333333333333333333333333333333333";
    private static final String FILE7_REC4_29B =
            "7444444444444444444444444444444444444444444444444444444444";
    private static final String FILE7_REC1_4B = "00112233";
    private static final String FILE8_REC1_29B =
            "8111111111111111111111111111111111111111111111111111111111";
    private static final String FILE8_REC1_5B = "8122334455";
    private static final String FILE8_REC1_4B = "84332211";
    private static final String FILE9_REC1_4B = "8899AABB";

    private static final String FILE10_REC1_COUNTER =
            "00112200000000000000000000000000000000000000000000000000000000000000";
    private static final String FILE11_REC1_COUNTER =
            "00221100000000000000000000000000000000000000000000000000000000000000";

    private static final String FILE7_REC1_COUNTER1 = "A55AA5";
    private static final String FILE7_REC1_COUNTER2 = "5AA55A";

    private static final byte[] FILE7_REC1_29B_BYTES = ByteArrayUtil.fromHex(FILE7_REC1_29B);
    private static final byte[] FILE7_REC2_29B_BYTES = ByteArrayUtil.fromHex(FILE7_REC2_29B);
    private static final byte[] FILE7_REC3_29B_BYTES = ByteArrayUtil.fromHex(FILE7_REC3_29B);
    private static final byte[] FILE7_REC4_29B_BYTES = ByteArrayUtil.fromHex(FILE7_REC4_29B);
    private static final byte[] FILE8_REC1_29B_BYTES = ByteArrayUtil.fromHex(FILE8_REC1_29B);
    private static final byte[] FILE8_REC1_5B_BYTES = ByteArrayUtil.fromHex(FILE8_REC1_5B);
    private static final byte[] FILE8_REC1_4B_BYTES = ByteArrayUtil.fromHex(FILE8_REC1_4B);

    private static final short LID_3F00 = (short) 0x3F00;
    private static final short LID_0002 = (short) 0x0002;
    private static final short LID_0003 = (short) 0x0003;
    private static final String LID_3F00_STR = "3F00";
    private static final String LID_0002_STR = "0002";
    private static final String LID_0003_STR = "0003";
    private static final String ACCESS_CONDITIONS_3F00 = "10100000";
    private static final String KEY_INDEXES_3F00 = "01030101";
    private static final String ACCESS_CONDITIONS_0002 = "1F000000";
    private static final String KEY_INDEXES_0002 = "01010101";
    private static final String ACCESS_CONDITIONS_0003 = "01100000";
    private static final String KEY_INDEXES_0003 = "01020101";

    private static final String SW1SW2_OK_RSP = SW1SW2_OK;
    private static final String PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD =
            "008A0B3904" + SAM_CHALLENGE + "00";
    private static final String PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP =
            "030490980030791D" + FILE7_REC1_29B + SW1SW2_OK;
    private static final String PO_OPEN_SECURE_SESSION_SFI7_REC1_NOT_RATIFIED_RSP =
            "030490980130791D" + FILE7_REC1_29B + SW1SW2_OK;
    private static final String PO_OPEN_SECURE_SESSION_CMD = "008A030104" + SAM_CHALLENGE + "00";
    private static final String PO_OPEN_SECURE_SESSION_RSP = "0304909800307900" + SW1SW2_OK;
    private static final String PO_OPEN_SECURE_SESSION_KVC_78_CMD = "0304909800307800" + SW1SW2_OK;
    private static final String PO_OPEN_SECURE_SESSION_SFI7_REC1_2_4_CMD = "948A8B3804C1C2C3C400";
    private static final String PO_OPEN_SECURE_SESSION_SFI7_REC1_2_4_RSP =
            "79030D307124B928480805CBABAE30001240800000000000000000000000000000009000";
    private static final String PO_CLOSE_SECURE_SESSION_CMD = "008E800004" + SAM_SIGNATURE + "00";
    private static final String PO_CLOSE_SECURE_SESSION_NOT_RATIFIED_CMD =
            "008E000004" + SAM_SIGNATURE + "00";
    private static final String PO_CLOSE_SECURE_SESSION_RSP = PO_SIGNATURE + SW1SW2_OK;
    private static final String PO_CLOSE_SECURE_SESSION_FAILED_RSP = "6988";
    private static final String PO_ABORT_SECURE_SESSION_CMD = "008E000000";
    private static final String PO_RATIFICATION_CMD = "00B2000000";
    private static final String PO_RATIFICATION_RSP = "6B00";

    private static final String PO_READ_REC_SFI7_REC1_CMD = "00B2013C00";
    private static final String PO_READ_REC_SFI7_REC1_RSP = FILE7_REC1_29B + SW1SW2_OK;
    private static final String PO_READ_REC_SFI7_REC1_6B_COUNTER_CMD = "00B2013C06";
    private static final String PO_READ_REC_SFI7_REC1_6B_COUNTER_RSP =
            FILE7_REC1_COUNTER1 + FILE7_REC1_COUNTER2 + SW1SW2_OK;
    private static final String PO_READ_REC_SFI8_REC1_CMD = "00B2014400";
    private static final String PO_READ_REC_SFI8_REC1_RSP = FILE8_REC1_29B + SW1SW2_OK;
    private static final String PO_READ_REC_SFI7_REC3_4_CMD = "00B2033D3E";
    private static final String PO_READ_REC_SFI7_REC3_4_RSP =
            "031D" + FILE7_REC3_29B + "041D" + FILE7_REC4_29B + SW1SW2_OK;
    private static final String PO_READ_REC_SFI10_REC1_CMD = "00B2018400";
    private static final String PO_READ_REC_SFI10_REC1_RSP = FILE10_REC1_COUNTER + SW1SW2_OK;
    private static final String PO_READ_REC_SFI11_REC1_CMD = "00B2018C00";
    private static final String PO_READ_REC_SFI11_REC1_RSP = FILE11_REC1_COUNTER + SW1SW2_OK;
    private static final String PO_UPDATE_REC_SFI7_REC1_4B_CMD = "00DC013C0400112233";
    private static final String PO_UPDATE_REC_SFI8_REC1_29B_CMD = "00DC01441D" + FILE8_REC1_29B;
    private static final String PO_UPDATE_REC_SFI8_REC1_5B_CMD = "00DC014405" + FILE8_REC1_5B;
    private static final String PO_UPDATE_REC_SFI8_REC1_4B_CMD = "00DC014404" + FILE8_REC1_4B;
    private static final String PO_UPDATE_REC_SFI8_REC1_29B_2_4_CMD = "94DC01441D" + FILE8_REC1_29B;
    private static final String PO_WRITE_REC_SFI8_REC1_4B_CMD = "00D2014404" + FILE8_REC1_4B;
    private static final String PO_APPEND_REC_SFI9_REC1_4B_CMD = "00E2004804" + FILE9_REC1_4B;
    private static final String PO_DECREASE_SFI10_REC1_100U_CMD = "003001800300006400";
    private static final String PO_DECREASE_SFI10_REC1_100U_RSP = "0010BE9000";
    private static final String PO_DECREASE_SFI11_REC1_100U_CMD = "003201880300006400";
    private static final String PO_DECREASE_SFI11_REC1_100U_RSP = "0022759000";

    private static final String PO_SELECT_FILE_CURRENT_CMD = "00A4090002000000";
    private static final String PO_SELECT_FILE_FIRST_CMD = "00A4020002000000";
    private static final String PO_SELECT_FILE_NEXT_CMD = "00A4020202000000";
    private static final String PO_SELECT_FILE_3F00_CMD = "00A40900023F0000";
    private static final String PO_SELECT_FILE_0002_CMD = "00A4090002000200";
    private static final String PO_SELECT_FILE_0003_CMD = "00A4090002000300";
    private static final String PO_SELECT_FILE_3F00_RSP = "85170001000000" + ACCESS_CONDITIONS_3F00
            + KEY_INDEXES_3F00 + "00777879616770003F009000";
    private static final String PO_SELECT_FILE_0002_RSP = "85170204021D01" + ACCESS_CONDITIONS_0002
            + KEY_INDEXES_0002 + "003F02000000000000029000";
    private static final String PO_SELECT_FILE_0003_RSP = "85170304021D01" + ACCESS_CONDITIONS_0003
            + KEY_INDEXES_0003 + "003F03000000000000039000";


    private static final String SAM_SELECT_DIVERSIFIER_CMD = "8014000008" + PO_DIVERSIFIER;
    private static final String SAM_GET_CHALLENGE_CMD = "8084000004";
    private static final String SAM_GET_CHALLENGE_RSP = SAM_CHALLENGE + SW1SW2_OK;
    private static final String SAM_DIGEST_INIT_OPEN_SECURE_SESSION_SFI7_REC1_CMD =
            "808A00FF273079030490980030791D" + FILE7_REC1_29B;
    private static final String SAM_DIGEST_INIT_OPEN_SECURE_SESSION_CMD =
            "808A00FF0A30790304909800307900";
    private static final String SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_CMD = "808C00000500B2013C00";
    private static final String SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_RSP_CMD =
            "808C00001F\" + FILE7_REC1_29B+ \"9000";
    private static final String SAM_DIGEST_UPDATE_READ_REC_SFI8_REC1_RSP_CMD =
            "808C00001F" + FILE8_REC1_29B + "9000";
    private static final String SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_RSP =
            "808C00001F" + FILE7_REC1_29B + SW1SW2_OK;
    private static final String SAM_DIGEST_UPDATE_READ_REC_SFI8_REC1_CMD = "808C00000500B2014400";
    private static final String SAM_DIGEST_UPDATE_READ_REC_SFI10_REC1_CMD = "808C00000500B2018C00";
    private static final String SAM_DIGEST_UPDATE_READ_REC_SFI10_REC1_RSP_CMD =
            "808C000024001122000000000000000000000000000000000000000000000000000000000000009000";
    private static final String SAM_DIGEST_UPDATE_READ_REC_SFI11_REC1_CMD = "808C00000500B2018400";
    private static final String SAM_DIGEST_UPDATE_READ_REC_SFI11_REC1_RSP_CMD =
            "808C000024002211000000000000000000000000000000000000000000000000000000000000009000";
    private static final String SAM_DIGEST_UPDATE_RSP_OK_CMD = "808C0000029000";
    private static final String SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_29B_CMD =
            "808C00002200DC01441D" + FILE8_REC1_29B;
    private static final String SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_5B_CMD =
            "808C00000A00DC0144058122334455";
    private static final String SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_4B_CMD =
            "808C00000900DC014404" + FILE8_REC1_4B;
    private static final String SAM_DIGEST_UPDATE_UPDATE_REC_SFI7_REC1_4B_CMD =
            "808C00000900DC013C04" + FILE7_REC1_4B;
    private static final String SAM_DIGEST_UPDATE_DECREASE_SFI10_CMD = "808C0000080030018003000064";
    private static final String SAM_DIGEST_UPDATE_DECREASE_SFI10_RESP = "808C0000050010BE9000";
    private static final String SAM_DIGEST_UPDATE_INCREASE_SFI11_CMD = "808C0000080032018803000064";
    private static final String SAM_DIGEST_UPDATE_INCREASE_SFI11_RESP = "808C0000050022759000";
    private static final String SAM_DIGEST_UPDATE_WRITE_REC_SFI8_REC1_4B_CMD =
            "808C00000900D2014404" + FILE8_REC1_4B;
    private static final String SAM_DIGEST_UPDATE_APPEND_REC_SFI9_REC1_4B_CMD =
            "808C00000900E2004804" + FILE9_REC1_4B;
    private static final String SAM_DIGEST_CLOSE_CMD = "808E000004";
    private static final String SAM_DIGEST_CLOSE_RSP = SAM_SIGNATURE + SW1SW2_OK;
    private static final String SAM_DIGEST_AUTHENTICATE = "8082000004" + PO_SIGNATURE;
    private static final String SAM_DIGEST_AUTHENTICATE_FAILED = "6988";


    @Before
    public void setUp() throws Exception {
        poCommandsTestSet.clear();
        samCommandsTestSet.clear();
        poReader = createMockReader("PO", TransmissionMode.CONTACTLESS, poCommandsTestSet);
        SeReader samReader = createMockReader("SAM", TransmissionMode.CONTACTS, samCommandsTestSet);
        CalypsoSam calypsoSam = createCalypsoSam();

        samResource = new SamResource(samReader, calypsoSam);
    }

    /* Standard opening with two records read */
    @Test(expected = CalypsoPoTransactionIllegalStateException.class)
    public void testProcessOpening_noSamResource() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);

        // PoTransaction without PoSecuritySettings
        poTransaction = new PoTransaction(new PoResource(poReader, calypsoPoRev31));

        // should raise an exception
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
    }

    /* Standard opening with two records read */
    @Test(expected = CalypsoPoTransactionIllegalStateException.class)
    public void testProcessOpening_readReopen() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);
        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD,
                PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI8_REC1_CMD, PO_READ_REC_SFI8_REC1_RSP);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        poTransaction.prepareReadRecordFile(FILE8, 1);
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent())
                .isEqualTo(ByteArrayUtil.fromHex(FILE7_REC1_29B));
        assertThat(calypsoPoRev31.getFileBySfi(FILE8).getData().getContent())
                .isEqualTo(ByteArrayUtil.fromHex(FILE8_REC1_29B));
        assertThat(calypsoPoRev31.isDfRatified()).isTrue();

        // expected exception: session is already open
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
    }

    /* Standard opening, DF not previously ratified */
    @Test
    public void testProcessOpening_dfNotRatified() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);
        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

        // addCommandPo(OPEN_SECURE_SESSION_SFI7_REC1, SAM_CHALLENGE_4, OPEN_SECURE_SESSION_RESP +
        // String.format("%02X", FILE_7_REC_1.length() / 2) + FILE_7_REC_1, SW1SW2_OK);
        //
        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD,
                PO_OPEN_SECURE_SESSION_SFI7_REC1_NOT_RATIFIED_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI8_REC1_CMD, PO_READ_REC_SFI8_REC1_RSP);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        poTransaction.prepareReadRecordFile(FILE8, 1);
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent())
                .isEqualTo(ByteArrayUtil.fromHex(FILE7_REC1_29B));
        assertThat(calypsoPoRev31.getFileBySfi(FILE8).getData().getContent())
                .isEqualTo(ByteArrayUtil.fromHex(FILE8_REC1_29B));
        assertThat(calypsoPoRev31.isDfRatified()).isFalse();
    }

    /* Standard opening with 1 multiple records read */
    @Test
    public void testProcessOpening_readMultipleRecords() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();
        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);
        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI7_REC3_4_CMD, PO_READ_REC_SFI7_REC3_4_RSP);

        poTransaction.prepareReadRecordFile(FILE7, 3, 2, 29);
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(3))
                .isEqualTo(ByteArrayUtil.fromHex(FILE7_REC3_29B));
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(4))
                .isEqualTo(ByteArrayUtil.fromHex(FILE7_REC4_29B));
    }

    /* Standard opening but KVC is not present authorized list */
    @Test(expected = CalypsoUnauthorizedKvcException.class)
    public void testProcessOpening_kvcNotAuthorized() throws CalypsoPoCommandException,
            CalypsoSamCommandException, CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);

        List<Byte> authorizedKvc = new ArrayList<Byte>();
        authorizedKvc.add((byte) 0x79);

        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .sessionAuthorizedKvcList(authorizedKvc).build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);
        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD,
                PO_OPEN_SECURE_SESSION_KVC_78_CMD);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        // an exception is expected
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
    }

    /*
     * Buffer overflow limit in atomic mode (counter in bytes): session buffer size = 430 b,
     * consumed size 430 b
     */
    @Test
    public void testProcessOpening_sessionBuffer_limit() throws CalypsoPoCommandException,
            CalypsoSamCommandException, CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();
        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);
        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD,
                PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_4B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);

        // add additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE7, 1);
        }
        // 12 x update (29 b) = 12 x (29 + 6) = 420 consumed in the session buffer
        for (int i = 0; i < 12; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // insert additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE7, 1);
        }
        // 4 additional bytes (10 b consumed)
        poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_4B_BYTES);
        // ATOMIC transaction should be ok (430 / 430 bytes consumed)
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        assertThat(true).isTrue();
    }

    /*
     * Buffer overflowed in atomic mode (counter in bytes): session buffer size = 430 b, consumed
     * size 431 b
     */
    @Test(expected = CalypsoAtomicTransactionException.class)
    public void testProcessOpening_sessionBuffer_overflowBytesCounter()
            throws CalypsoPoCommandException, CalypsoSamCommandException,
            CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();
        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        // 12 x update (29 b) = 12 x (29 + 6) = 420 consumed in the session buffer
        for (int i = 0; i < 12; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // 5 additional bytes (11 b consumed)
        poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_5B_BYTES);

        // expected exception: session buffer overflow
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
    }

    /*
     * Buffer overflow limit in atomic mode (counter in operations): session buffer size = 6 op,
     * consumed 6 op
     */
    @Test
    public void testProcessOpening_sessionBuffer_limitOperationsCounter()
            throws CalypsoPoCommandException, CalypsoSamCommandException,
            CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev24 = createCalypsoPo(FCI_REV24);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();
        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev24), poSecuritySettings);
        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_2_4_CMD,
                PO_OPEN_SECURE_SESSION_SFI7_REC1_2_4_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_29B_2_4_CMD, SW1SW2_OK_RSP);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        // 6 x update (29 b) = 6 operations consumed in the session buffer
        for (int i = 0; i < 6; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // ATOMIC transaction should be ok (6 / 6 operations consumed)
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        assertThat(true).isTrue();
    }

    /*
     * Buffer overflow limit in atomic mode (counter in operations): session buffer size = 6 op,
     * consumed 7 op
     */
    @Test(expected = CalypsoAtomicTransactionException.class)
    public void testProcessOpening_sessionBuffer_overflowOperationsCounter()
            throws CalypsoPoCommandException, CalypsoSamCommandException,
            CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev24 = createCalypsoPo(FCI_REV24);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();
        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev24), poSecuritySettings);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        // 7 x update (29 b) = 7 operations consumed in the session buffer
        for (int i = 0; i < 7; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // ATOMIC transaction should be ko (7 / 6 operations consumed)
        // expected exception: session buffer overflow
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
    }

    /*
     * Buffer overflowed in multiple mode (counter in bytes): session buffer size = 430 b, consumed
     * size 431 b
     */
    @Test
    public void testProcessOpening_sessionBuffer_overflowBytesCounter_MulitpleMode()
            throws CalypsoPoCommandException, CalypsoSamCommandException,
            CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)//
                        .sessionModificationMode(
                                PoTransaction.SessionSetting.ModificationMode.MULTIPLE)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);
        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_SFI7_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_AUTHENTICATE, SW1SW2_OK_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD,
                PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_5B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_RSP);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        // 12 x update (29 b) = 12 x (29 + 6) = 420 consumed in the session buffer
        for (int i = 0; i < 12; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // 5 additional bytes (11 b consumed)
        poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_5B_BYTES);
        // ATOMIC transaction should be ok (430 / 431 bytes consumed)
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        assertThat(true).isTrue();
    }

    /* standard process Po commands */
    @Test
    public void testProcessPoCommands_nominalCase()
            throws CalypsoPoCommandException, CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        poTransaction = new PoTransaction(new PoResource(poReader, calypsoPoRev31));

        poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI8_REC1_CMD, PO_READ_REC_SFI8_REC1_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI7_REC3_4_CMD, PO_READ_REC_SFI7_REC3_4_RSP);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        poTransaction.prepareReadRecordFile(FILE8, 1);
        poTransaction.prepareReadRecordFile(FILE7, 3, 2, 29);
        poTransaction.processPoCommands(ChannelControl.KEEP_OPEN);
        assertThat(calypsoPoRev31.getFileBySfi(FILE8).getData().getContent(1))
                .isEqualTo(FILE8_REC1_29B_BYTES);
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(1))
                .isEqualTo(FILE7_REC1_29B_BYTES);
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(3))
                .isEqualTo(FILE7_REC3_29B_BYTES);
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(4))
                .isEqualTo(FILE7_REC4_29B_BYTES);
    }

    /* standard process Po commands: session open before */
    @Test(expected = CalypsoPoTransactionIllegalStateException.class)
    public void testProcessPoCommands_sessionOpen() throws CalypsoPoCommandException,
            CalypsoPoTransactionException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)//
                        .sessionModificationMode(
                                PoTransaction.SessionSetting.ModificationMode.MULTIPLE)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD,
                PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
        poTransaction.prepareReadRecordFile(FILE7, 1);
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        poTransaction.prepareReadRecordFile(FILE8, 1);
        // PoTransaction while a session is open
        // expected exception: a session is open
        poTransaction.processPoCommands(ChannelControl.KEEP_OPEN);
    }

    /* No session open */
    @Test(expected = CalypsoPoTransactionIllegalStateException.class)
    public void testProcessPoCommandsInSession_noSessionOpen() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        poTransaction = new PoTransaction(new PoResource(poReader, calypsoPoRev31));

        poTransaction.prepareReadRecordFile(FILE8, 1);
        // expected exception: no session is open
        poTransaction.processPoCommandsInSession();
    }

    /* Standard processPoCommandsInSession */
    @Test
    public void testProcessPoCommandsInSession_nominalCase() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD,
                PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI8_REC1_CMD, PO_READ_REC_SFI8_REC1_RSP);
        poTransaction.prepareReadRecordFile(FILE7, 1);
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        poTransaction.prepareReadRecordFile(FILE8, 1);
        // PoTransaction after a session is open
        poTransaction.processPoCommandsInSession();
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(1))
                .isEqualTo(FILE7_REC1_29B_BYTES);
        assertThat(calypsoPoRev31.getFileBySfi(FILE8).getData().getContent(1))
                .isEqualTo(FILE8_REC1_29B_BYTES);
    }

    /* processClosing no session open */
    @Test(expected = CalypsoPoTransactionIllegalStateException.class)
    public void testProcessClosing_noSessionOpen() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        poTransaction = new PoTransaction(new PoResource(poReader, calypsoPoRev31));

        poTransaction.prepareReadRecordFile(FILE8, 1);
        // expected exception: no session is open
        poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
    }

    /* Standard processClosing - default ratification */
    @Test
    public void testProcessClosing_nominalCase() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_SFI7_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI8_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI10_REC1_RSP_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI10_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI11_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI11_REC1_RSP_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_DECREASE_SFI10_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_DECREASE_SFI10_RESP, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_INCREASE_SFI11_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_INCREASE_SFI11_RESP, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI7_REC1_4B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_4B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_WRITE_REC_SFI8_REC1_4B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_APPEND_REC_SFI9_REC1_4B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI11_REC1_CMD, SW1SW2_OK_RSP);

        samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_AUTHENTICATE, SW1SW2_OK_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD,
                PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI10_REC1_CMD, PO_READ_REC_SFI10_REC1_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI11_REC1_CMD, PO_READ_REC_SFI11_REC1_RSP);
        poCommandsTestSet.put(PO_DECREASE_SFI10_REC1_100U_CMD, PO_DECREASE_SFI10_REC1_100U_RSP);
        poCommandsTestSet.put(PO_DECREASE_SFI11_REC1_100U_CMD, PO_DECREASE_SFI11_REC1_100U_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI7_REC1_4B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_WRITE_REC_SFI8_REC1_4B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_APPEND_REC_SFI9_REC1_4B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_RATIFICATION_CMD, PO_RATIFICATION_RSP);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        poTransaction.prepareReadRecordFile(FILE10, 1);
        poTransaction.prepareReadRecordFile(FILE11, 1);
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        poTransaction.prepareDecrease(FILE10, (byte) 1, 100);
        poTransaction.prepareIncrease(FILE11, (byte) 1, 100);
        poTransaction.prepareUpdateRecord(FILE7, (byte) 1, ByteArrayUtil.fromHex(FILE7_REC1_4B));
        poTransaction.prepareWriteRecord(FILE8, (byte) 1, ByteArrayUtil.fromHex(FILE8_REC1_4B));
        poTransaction.prepareAppendRecord(FILE9, ByteArrayUtil.fromHex(FILE9_REC1_4B));

        // PoTransaction after a session is open
        poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
        assertThat(calypsoPoRev31.getFileBySfi(FILE10).getData().getContentAsCounterValue(1))
                .isEqualTo(0x1122 - 100);
        assertThat(calypsoPoRev31.getFileBySfi(FILE11).getData().getContentAsCounterValue(1))
                .isEqualTo(0x2211 + 100);
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(1))
                .isEqualTo(ByteArrayUtil.fromHex(FILE7_REC1_4B));
        assertThat(calypsoPoRev31.getFileBySfi(FILE8).getData().getContent(1))
                .isEqualTo(ByteArrayUtil.fromHex(FILE8_REC1_4B));
        assertThat(calypsoPoRev31.getFileBySfi(FILE9).getData().getContent(1))
                .isEqualTo(ByteArrayUtil.fromHex(FILE9_REC1_4B));
    }

    /* processClosing - PO fail on closing */
    @Test(expected = CalypsoPoCloseSecureSessionException.class)
    public void testProcessClosing_poCloseFail() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_CMD, SW1SW2_OK_RSP);

        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_FAILED_RSP);

        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        poTransaction.prepareReadRecordFile(FILE7, 1);

        // PoTransaction after a session is open
        // should raise a CalypsoPoCloseSecureSessionException
        poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
    }

    /* processClosing - SAM authentication fail on closing */
    @Test(expected = CalypsoSessionAuthenticationException.class)
    public void testProcessClosing_samAuthenticateFail() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_AUTHENTICATE, SAM_DIGEST_AUTHENTICATE_FAILED);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_RSP);

        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        poTransaction.prepareReadRecordFile(FILE7, 1);

        // PoTransaction after a session is open
        // should raise a CalypsoSessionAuthenticationException
        poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
    }


    /* processClosing - SAM IO error while authenticating */
    @Test(expected = CalypsoAuthenticationNotVerifiedException.class)
    public void testProcessClosing_samIoErrorAuthenticating() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);


        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_RSP);

        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        poTransaction.prepareReadRecordFile(FILE7, 1);

        // PoTransaction after a session is open
        // should raise a CalypsoAuthenticationNotVerifiedException
        poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
    }

    /*
     * Buffer overflow limit in atomic mode (counter in bytes): session buffer size = 430 b,
     * consumed size 430 b
     */
    @Test
    public void testProcessClosing_sessionBuffer_limit() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI8_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_4B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_AUTHENTICATE, SW1SW2_OK_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_4B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_RSP);

        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        // add additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE7, 1);
        }
        // 12 x update (29 b) = 12 x (29 + 6) = 420 consumed in the session buffer
        for (int i = 0; i < 12; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // insert additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE7, 1);
        }
        // 4 additional bytes (10 b consumed)
        poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_4B_BYTES);

        // PoTransaction after a session is open
        poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
        assertThat(calypsoPoRev31.getFileBySfi(FILE8).getData().getContent(1))
                .isEqualTo(FILE8_REC1_4B_BYTES);
    }

    /*
     * Buffer overflowed in atomic mode (counter in bytes): session buffer size = 430 b, consumed
     * size 431 b
     */
    @Test
    public void testProcessClosing_sessionBuffer_overflowed() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);

        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        // add additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE7, 1);
        }
        // 12 x update (29 b) = 12 x (29 + 6) = 420 consumed in the session buffer
        for (int i = 0; i < 12; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, ByteArrayUtil
                    .fromHex("8111111111111111111111111111111111111111111111111111111111"));
        }
        // insert additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE7, 1);
        }
        // 4 additional bytes (10 b consumed)
        poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_5B_BYTES);

        try {
            // PoTransaction after a session is open
            poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
        } catch (CalypsoAtomicTransactionException ex) {
            // expected exception: session buffer overflow
            return;
        }
        fail("Unexpected behaviour");
    }

    /*
     * Buffer overflowed in multiple mode (counter in bytes): session buffer size = 430 b, consumed
     * size 431 b
     */
    @Test
    public void testProcessClosing_sessionBuffer_overflowMultipleMode()
            throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)//
                        .sessionModificationMode(
                                PoTransaction.SessionSetting.ModificationMode.MULTIPLE)//
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI8_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_4B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_RSP, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI7_REC1_4B_CMD, SW1SW2_OK_RSP);

        samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_AUTHENTICATE, SW1SW2_OK_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_4B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_RSP);

        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        // add additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE7, 1);
        }
        // 12 x update (29 b) = 12 x (29 + 6) = 420 consumed in the session buffer
        for (int i = 0; i < 12; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // insert additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE7, 1);
        }
        // 4 additional bytes (10 b consumed)
        poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_4B_BYTES);

        // PoTransaction after a session is open
        poTransaction.processClosing(ChannelControl.CLOSE_AFTER);

        assertThat(true).isTrue();
    }

    /* Standard processClosing - close not ratified */
    @Test
    public void testProcessClosing_nominalCase_closeNotRatified()
            throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)//
                        .ratificationMode(
                                PoTransaction.SessionSetting.RatificationMode.CLOSE_NOT_RATIFIED)//
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_SFI7_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_AUTHENTICATE, SW1SW2_OK_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD,
                PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_NOT_RATIFIED_CMD,
                PO_CLOSE_SECURE_SESSION_RSP);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        poTransaction.processClosing(ChannelControl.CLOSE_AFTER);

        assertThat(true).isTrue();
    }

    /* Session buffer overflow in atomic mode: the overflow happens at closing */
    @Test
    public void testTransaction_sessionBuffer_overflowAtomic() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)//
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI8_REC1_CMD, PO_READ_REC_SFI8_REC1_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_RSP);

        // 4 x update (29 b) = 4 x (29 + 6) = 140 consumed in the session buffer
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        // add additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE8, 1);
        }
        // 4 x update (29 b) = 4 x (29 + 6) = 140 consumed in the session buffer
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // insert additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE8, 1);
        }
        poTransaction.processPoCommandsInSession();

        // 5 x update (29 b) = 5 x (29 + 6) = 140 consumed in the session buffer
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // 4 additional bytes (10 b consumed)
        poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_5B_BYTES);

        try {
            // PoTransaction after a session is open
            poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
        } catch (CalypsoAtomicTransactionException ex) {
            // expected exception: buffer overflow
            return;
        }
        fail("Unexpected behaviour");
    }

    /* Session buffer overflow in multiple mode: the overflow happens and is handled at closing */
    @Test
    public void testTransaction_sessionBuffer_overflowMultiple()
            throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)//
                        .sessionModificationMode(
                                PoTransaction.SessionSetting.ModificationMode.MULTIPLE)//
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);

        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI8_REC1_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_RSP_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI8_REC1_RSP_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_5B_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);
        samCommandsTestSet.put(SAM_DIGEST_AUTHENTICATE, SW1SW2_OK_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI8_REC1_CMD, PO_READ_REC_SFI8_REC1_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_UPDATE_REC_SFI8_REC1_5B_CMD, SW1SW2_OK_RSP);
        poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_RSP);

        // 4 x update (29 b) = 4 x (29 + 6) = 140 consumed in the session buffer
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

        // add additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE8, 1);
        }
        // 24 x update (29 b) = 24 x (29 + 6) = 840 consumed in the session buffer
        // force multiple cycles
        for (int i = 0; i < 24; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // insert additional non modifying commands (should not affect the session buffer)
        for (int i = 0; i < 4; i++) {
            poTransaction.prepareReadRecordFile(FILE8, 1);
        }
        poTransaction.processPoCommandsInSession();

        // 24 x update (29 b) = 24 x (29 + 6) = 840 consumed in the session buffer
        // force multiple cycles
        for (int i = 0; i < 24; i++) {
            poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
        }
        // 4 additional bytes (10 b consumed)
        poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_5B_BYTES);

        // PoTransaction after a session is open
        poTransaction.processClosing(ChannelControl.CLOSE_AFTER);

        assertThat(true).isTrue();
    }

    /* open, cancel and reopen */
    @Test
    public void testProcessCancel_open_cancelOpen() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();

        poTransaction =
                new PoTransaction(new PoResource(poReader, calypsoPoRev31), poSecuritySettings);
        samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
        samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD,
                PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
        poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
        poCommandsTestSet.put(PO_READ_REC_SFI8_REC1_CMD, PO_READ_REC_SFI8_REC1_RSP);
        // Abort session
        poCommandsTestSet.put(PO_ABORT_SECURE_SESSION_CMD, SW1SW2_OK_RSP);

        poTransaction.prepareReadRecordFile(FILE7, 1);
        poTransaction.prepareReadRecordFile(FILE8, 1);
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent())
                .isEqualTo(FILE7_REC1_29B_BYTES);
        assertThat(calypsoPoRev31.getFileBySfi(FILE8).getData().getContent())
                .isEqualTo(FILE8_REC1_29B_BYTES);
        assertThat(calypsoPoRev31.isDfRatified()).isTrue();
        poTransaction.processCancel(ChannelControl.KEEP_OPEN);
        poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
    }

    @Test
    public void testPrepareSelectFile_selectControl()
            throws CalypsoPoCommandException, CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        poTransaction = new PoTransaction(new PoResource(poReader, calypsoPoRev31));

        poCommandsTestSet.put(PO_SELECT_FILE_CURRENT_CMD, PO_SELECT_FILE_3F00_RSP);
        poCommandsTestSet.put(PO_SELECT_FILE_FIRST_CMD, PO_SELECT_FILE_0002_RSP);
        poCommandsTestSet.put(PO_SELECT_FILE_NEXT_CMD, PO_SELECT_FILE_0003_RSP);

        poTransaction.prepareSelectFile(SelectFileControl.CURRENT_DF);
        poTransaction.prepareSelectFile(SelectFileControl.FIRST_EF);
        poTransaction.prepareSelectFile(SelectFileControl.NEXT_EF);
        poTransaction.processPoCommands(ChannelControl.KEEP_OPEN);
        DirectoryHeader directoryHeader = calypsoPoRev31.getDirectoryHeader();
        FileHeader fileHeader1 = calypsoPoRev31.getFileByLid((short) 0x02).getHeader();
        FileHeader fileHeader2 = calypsoPoRev31.getFileByLid((short) 0x03).getHeader();
        System.out.println(directoryHeader);
        System.out.println(fileHeader1);
        System.out.println(fileHeader2);

        assertThat(directoryHeader.getLid()).isEqualTo(LID_3F00);
        assertThat(directoryHeader.getAccessConditions())
                .isEqualTo(ByteArrayUtil.fromHex(ACCESS_CONDITIONS_3F00));
        assertThat(directoryHeader.getKeyIndexes())
                .isEqualTo(ByteArrayUtil.fromHex(KEY_INDEXES_3F00));
        assertThat(directoryHeader.getDfStatus()).isEqualTo((byte) 0x00);
        assertThat(directoryHeader.getKif(AccessLevel.SESSION_LVL_PERSO)).isEqualTo((byte) 0x61);
        assertThat(directoryHeader.getKif(AccessLevel.SESSION_LVL_LOAD)).isEqualTo((byte) 0x67);
        assertThat(directoryHeader.getKif(AccessLevel.SESSION_LVL_DEBIT)).isEqualTo((byte) 0x70);
        assertThat(directoryHeader.getKvc(AccessLevel.SESSION_LVL_PERSO)).isEqualTo((byte) 0x77);
        assertThat(directoryHeader.getKvc(AccessLevel.SESSION_LVL_LOAD)).isEqualTo((byte) 0x78);
        assertThat(directoryHeader.getKvc(AccessLevel.SESSION_LVL_DEBIT)).isEqualTo((byte) 0x79);

        assertThat(fileHeader1.getLid()).isEqualTo(LID_0002);
        assertThat(fileHeader1.getRecordsNumber()).isEqualTo(1);
        assertThat(fileHeader1.getRecordSize()).isEqualTo(29);
        assertThat(fileHeader1.getType()).isEqualTo(FileHeader.FileType.LINEAR);
        assertThat(fileHeader1.getAccessConditions())
                .isEqualTo(ByteArrayUtil.fromHex(ACCESS_CONDITIONS_0002));
        assertThat(fileHeader1.getKeyIndexes()).isEqualTo(ByteArrayUtil.fromHex(KEY_INDEXES_0002));
        assertThat(fileHeader1.getDfStatus()).isEqualTo((byte) 0x00);
        assertThat(fileHeader1.getSharedReference()).isEqualTo(Short.valueOf((short) 0x3F02));

        assertThat(fileHeader2.getLid()).isEqualTo(LID_0003);
        assertThat(fileHeader2.getRecordsNumber()).isEqualTo(1);
        assertThat(fileHeader2.getRecordSize()).isEqualTo(29);
        assertThat(fileHeader2.getType()).isEqualTo(FileHeader.FileType.LINEAR);
        assertThat(fileHeader2.getAccessConditions())
                .isEqualTo(ByteArrayUtil.fromHex(ACCESS_CONDITIONS_0003));
        assertThat(fileHeader2.getKeyIndexes()).isEqualTo(ByteArrayUtil.fromHex(KEY_INDEXES_0003));
        assertThat(fileHeader2.getDfStatus()).isEqualTo((byte) 0x00);
        assertThat(fileHeader2.getSharedReference()).isEqualTo(Short.valueOf((short) 0x3F03));
    }

    @Test
    public void testPrepareSelectFile_lid()
            throws CalypsoPoCommandException, CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        poTransaction = new PoTransaction(new PoResource(poReader, calypsoPoRev31));

        poCommandsTestSet.put(PO_SELECT_FILE_3F00_CMD, PO_SELECT_FILE_3F00_RSP);
        poCommandsTestSet.put(PO_SELECT_FILE_0002_CMD, PO_SELECT_FILE_0002_RSP);
        poCommandsTestSet.put(PO_SELECT_FILE_0003_CMD, PO_SELECT_FILE_0003_RSP);

        poTransaction.prepareSelectFile(ByteArrayUtil.fromHex(LID_3F00_STR));
        poTransaction.prepareSelectFile(ByteArrayUtil.fromHex(LID_0002_STR));
        poTransaction.prepareSelectFile(ByteArrayUtil.fromHex(LID_0003_STR));
        poTransaction.processPoCommands(ChannelControl.KEEP_OPEN);

        DirectoryHeader directoryHeader = calypsoPoRev31.getDirectoryHeader();
        ElementaryFile file1 = calypsoPoRev31.getFileByLid((short) 0x02);
        ElementaryFile file2 = calypsoPoRev31.getFileByLid((short) 0x03);
        byte sfi1 = file1.getSfi();
        byte sfi2 = file2.getSfi();
        System.out.println(directoryHeader);
        System.out.println(file1);
        System.out.println(file2);

        assertThat(calypsoPoRev31.getFileBySfi(sfi1)).isEqualTo(file1);
        assertThat(calypsoPoRev31.getFileBySfi(sfi2)).isEqualTo(file2);

        assertThat(directoryHeader.getLid()).isEqualTo(LID_3F00);
        assertThat(directoryHeader.getAccessConditions())
                .isEqualTo(ByteArrayUtil.fromHex(ACCESS_CONDITIONS_3F00));
        assertThat(directoryHeader.getKeyIndexes())
                .isEqualTo(ByteArrayUtil.fromHex(KEY_INDEXES_3F00));
        assertThat(directoryHeader.getDfStatus()).isEqualTo((byte) 0x00);
        assertThat(directoryHeader.getKif(AccessLevel.SESSION_LVL_PERSO)).isEqualTo((byte) 0x61);
        assertThat(directoryHeader.getKif(AccessLevel.SESSION_LVL_LOAD)).isEqualTo((byte) 0x67);
        assertThat(directoryHeader.getKif(AccessLevel.SESSION_LVL_DEBIT)).isEqualTo((byte) 0x70);
        assertThat(directoryHeader.getKvc(AccessLevel.SESSION_LVL_PERSO)).isEqualTo((byte) 0x77);
        assertThat(directoryHeader.getKvc(AccessLevel.SESSION_LVL_LOAD)).isEqualTo((byte) 0x78);
        assertThat(directoryHeader.getKvc(AccessLevel.SESSION_LVL_DEBIT)).isEqualTo((byte) 0x79);

        FileHeader fileHeader1 = file1.getHeader();
        assertThat(fileHeader1.getLid()).isEqualTo(LID_0002);
        assertThat(fileHeader1.getRecordsNumber()).isEqualTo(1);
        assertThat(fileHeader1.getRecordSize()).isEqualTo(29);
        assertThat(fileHeader1.getType()).isEqualTo(FileHeader.FileType.LINEAR);
        assertThat(fileHeader1.getAccessConditions())
                .isEqualTo(ByteArrayUtil.fromHex(ACCESS_CONDITIONS_0002));
        assertThat(fileHeader1.getKeyIndexes()).isEqualTo(ByteArrayUtil.fromHex(KEY_INDEXES_0002));
        assertThat(fileHeader1.getDfStatus()).isEqualTo((byte) 0x00);
        assertThat(fileHeader1.getSharedReference()).isEqualTo(Short.valueOf((short) 0x3F02));

        FileHeader fileHeader2 = file2.getHeader();
        assertThat(fileHeader2.getLid()).isEqualTo(LID_0003);
        assertThat(fileHeader2.getRecordsNumber()).isEqualTo(1);
        assertThat(fileHeader2.getRecordSize()).isEqualTo(29);
        assertThat(fileHeader2.getType()).isEqualTo(FileHeader.FileType.LINEAR);
        assertThat(fileHeader2.getAccessConditions())
                .isEqualTo(ByteArrayUtil.fromHex(ACCESS_CONDITIONS_0003));
        assertThat(fileHeader2.getKeyIndexes()).isEqualTo(ByteArrayUtil.fromHex(KEY_INDEXES_0003));
        assertThat(fileHeader2.getDfStatus()).isEqualTo((byte) 0x00);
        assertThat(fileHeader2.getSharedReference()).isEqualTo(Short.valueOf((short) 0x3F03));
    }

    @Test
    public void testPrepareReadCounterFile()
            throws CalypsoPoCommandException, CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        poTransaction = new PoTransaction(new PoResource(poReader, calypsoPoRev31));

        poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_6B_COUNTER_CMD,
                PO_READ_REC_SFI7_REC1_6B_COUNTER_RSP);

        poTransaction.prepareReadCounterFile(FILE7, 2);
        poTransaction.processPoCommands(ChannelControl.KEEP_OPEN);
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContentAsCounterValue(1))
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(FILE7_REC1_COUNTER1),
                        0));
        assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContentAsCounterValue(2))
                .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(FILE7_REC1_COUNTER2),
                        0));
    }

    @Test(expected = CalypsoPoIOException.class)
    public void testPoIoException()
            throws CalypsoPoCommandException, CalypsoPoTransactionException {
        CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
        poTransaction = new PoTransaction(new PoResource(poReader, calypsoPoRev31));
        poTransaction.prepareReadRecordFile(FILE7, 1);
        poTransaction.processPoCommands(ChannelControl.KEEP_OPEN);
    }

    @Test
    public void testAccessLevel() {
        assertThat(AccessLevel.SESSION_LVL_PERSO.getName()).isEqualTo("perso");
        assertThat(AccessLevel.SESSION_LVL_LOAD.getName()).isEqualTo("load");
        assertThat(AccessLevel.SESSION_LVL_DEBIT.getName()).isEqualTo("debit");
        assertThat(AccessLevel.SESSION_LVL_PERSO.getSessionKey()).isEqualTo((byte) 0x01);
        assertThat(AccessLevel.SESSION_LVL_LOAD.getSessionKey()).isEqualTo((byte) 0x02);
        assertThat(AccessLevel.SESSION_LVL_DEBIT.getSessionKey()).isEqualTo((byte) 0x03);
    }

    private CalypsoPo createCalypsoPo(String FCI) {
        SeResponse selectionData = new SeResponse(true, false,
                new SelectionStatus(null, new ApduResponse(ByteArrayUtil.fromHex(FCI), null), true),
                null);
        return new CalypsoPo(selectionData, TransmissionMode.CONTACTLESS);
    }

    private CalypsoSam createCalypsoSam() {

        SelectionStatus selectionStatus =
                new SelectionStatus(new AnswerToReset(ByteArrayUtil.fromHex(ATR1)), null, true);
        return new CalypsoSam(new SeResponse(true, true, selectionStatus, null),
                TransmissionMode.CONTACTS);
    }

    private ProxyReader createMockReader(final String name, TransmissionMode transmissionMode,
            final Map<String, String> commandTestSet) throws KeypleReaderIOException {

        // configure mock native reader
        ProxyReader mockReader = Mockito.spy(ProxyReader.class);
        doReturn(name).when(mockReader).getName();
        doReturn(transmissionMode).when(mockReader).getTransmissionMode();

        doAnswer(new Answer<SeResponse>() {
            @Override
            public SeResponse answer(InvocationOnMock invocation) throws KeypleReaderIOException {
                Object[] args = invocation.getArguments();
                SeRequest seRequest = (SeRequest) args[0];
                List<ApduRequest> apduRequests = seRequest.getApduRequests();
                List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
                try {
                    for (ApduRequest apduRequest : apduRequests) {
                        ApduResponse apduResponse = getResponses(name, commandTestSet, apduRequest);
                        apduResponses.add(apduResponse);
                        System.out.println("Request=" + apduRequest.toString() + ", Response="
                                + apduResponse.toString());
                    }
                } catch (KeypleReaderIOException ex) {
                    ex.setSeResponse(new SeResponse(true, true, null, apduResponses));
                    throw ex;
                }
                return new SeResponse(true, true, null, apduResponses);
            }
        }).when(mockReader).transmitSeRequest(any(SeRequest.class), any(ChannelControl.class));
        return mockReader;
    }

    private ApduResponse getResponses(String name, Map<String, String> cmdRespMap,
            ApduRequest apduRequest) throws KeypleReaderIOException {
        String apdu_c = ByteArrayUtil.toHex(apduRequest.getBytes());
        String apdu_r = cmdRespMap.get(apdu_c);
        // return matching hexa response if found
        if (apdu_r != null) {
            return new ApduResponse(ByteArrayUtil.fromHex(apdu_r), null);
        }
        System.out.println(name + ": no response available for "
                + ByteArrayUtil.toHex(apduRequest.getBytes()));
        // throw a KeypleReaderIOException if not found
        throw new KeypleReaderIOException("No response available for this request.");
    }
}

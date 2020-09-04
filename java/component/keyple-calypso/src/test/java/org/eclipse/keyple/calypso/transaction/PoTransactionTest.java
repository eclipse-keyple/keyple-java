/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.calypso.transaction;

import static org.assertj.core.api.Assertions.*;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.AccessLevel;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.eclipse.keyple.calypso.SelectFileControl;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoAtomicTransactionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoAuthenticationNotVerifiedException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoCloseSecureSessionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoIOException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionIllegalStateException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoSessionAuthenticationException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoUnauthorizedKvcException;
import org.eclipse.keyple.core.selection.SeResource;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.AnswerToReset;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
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
  private SeResource<CalypsoSam> samResource;

  private final Map<String, String> poCommandsTestSet = new HashMap<String, String>();
  private final Map<String, String> samCommandsTestSet = new HashMap<String, String>();
  private static final String FCI_REV10 =
      "6F228408315449432E494341A516BF0C13C708   0000000011223344 5307060A01032003119000";
  private static final String FCI_REV24 =
      "6F2A8410A0000004040125090101000000000000A516BF0C13C708 0000000011223344 53070A2E11420001019000";
  private static final String FCI_REV31 =
      "6F238409315449432E49434131A516BF0C13C708 0000000011223344 53070A3C23121410019000";
  private static final String FCI_STORED_VALUE_REV31 =
      "6F238409315449432E49434131A516BF0C13C708 0000000011223344 53070A3C23201410019000";
  private static final String FCI_REV31_INVALIDATED =
      "6F238409315449432E49434131A516BF0C13C708 0000000011223344 53070A3C23121410016283";

  private static final String ATR1 = "3B3F9600805A0080C120000012345678829000";

  private static final String PIN_OK = "0000";
  private static final String CIPHER_PIN_OK = "1122334455667788";
  private static final String PIN_KO = "0000";

  private static final byte FILE7 = (byte) 0x07;
  private static final byte FILE8 = (byte) 0x08;
  private static final byte FILE9 = (byte) 0x09;
  private static final byte FILE10 = (byte) 0x10;
  private static final byte FILE11 = (byte) 0x11;

  private static final String SW1SW2_OK = "9000";
  private static final String SW1SW2_KO = "6700";
  private static final String SAM_CHALLENGE = "C1C2C3C4";
  private static final String PO_CHALLENGE = "C1C2C3C4C5C6C7C8";
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
  private static final String PO_SELECT_FILE_3F00_RSP =
      "85170001000000" + ACCESS_CONDITIONS_3F00 + KEY_INDEXES_3F00 + "00777879616770003F009000";
  private static final String PO_SELECT_FILE_0002_RSP =
      "85170204021D01" + ACCESS_CONDITIONS_0002 + KEY_INDEXES_0002 + "003F02000000000000029000";
  private static final String PO_SELECT_FILE_0003_RSP =
      "85170304021D01" + ACCESS_CONDITIONS_0003 + KEY_INDEXES_0003 + "003F03000000000000039000";

  private static final String PO_VERIFY_PIN_PLAIN_OK_CMD =
      "0020000004" + ByteArrayUtil.toHex(PIN_OK.getBytes());
  private static final String PO_VERIFY_PIN_ENCRYPTED_OK_CMD = "0020000008" + CIPHER_PIN_OK;
  private static final String PO_CHECK_PIN_CMD = "0020000000";
  private static final String PO_VERIFY_PIN_OK_RSP = "9000";
  private static final String PO_VERIFY_PIN_KO_RSP = "63C2";

  private static int SV_BALANCE = 0x123456;
  private static String SV_BALANCE_STR = "123456";
  private static final String PO_SV_GET_DEBIT_CMD = "007C000900";
  private static final String PO_SV_GET_DEBIT_RSP =
      "790073A54BC97DFA" + SV_BALANCE_STR + "FFFE0000000079123456780000DD0000160072" + SW1SW2_OK;
  private static final String PO_SV_GET_RELOAD_CMD = "007C000700";
  private static final String PO_SV_GET_RELOAD_RSP =
      "79007221D35F0E36"
          + SV_BALANCE_STR
          + "000000790000001A0000020000123456780000DB0070"
          + SW1SW2_OK;
  private static final String PO_SV_RELOAD_CMD =
      "00B89591171600000079000000020000123456780000DE2C8CB3D280";
  private static final String PO_SV_RELOAD_RSP = "A54BC9" + SW1SW2_OK;
  private static final String PO_SV_DEBIT_CMD =
      "00BACD001434FFFE0000000079123456780000DF0C9437AABB";
  private static final String PO_SV_DEBIT_RSP = "A54BC9" + SW1SW2_OK;
  private static final String PO_SV_UNDEBIT_CMD =
      "00BCCD00143400020000000079123456780000DF0C9437AABB";
  private static final String PO_SV_UNDEBIT_RSP = "A54BC9" + SW1SW2_OK;
  private static final String PO_READ_SV_LOAD_LOG_FILE_CMD = "00B201A400";
  private static final String PO_READ_SV_LOAD_LOG_FILE_RSP =
      "000000780000001A0000020000AABBCCDD0000DB007000000000000000" + SW1SW2_OK;
  private static final String PO_READ_SV_DEBIT_LOG_FILE_CMD = "00B201AD5D";
  private static final String PO_READ_SV_DEBIT_LOG_FILE_RSP =
      "011DFFFE0000000079AABBCC010000DA000018006F00000000000000000000"
          + "021DFFFE0000000079AABBCC020000DA000018006F00000000000000000000"
          + "031DFFFE0000000079AABBCC030000DA000018006F00000000000000000000"
          + SW1SW2_OK;

  private static final String PO_INVALIDATE_CMD = "0004000000";
  private static final String PO_REHABILITATE_CMD = "0044000000";

  private static final String PO_GET_CHALLENGE_CMD = "0084000008";
  private static final String PO_GET_CHALLENGE_RSP = PO_CHALLENGE + SW1SW2_OK;

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

  private static final String SAM_CARD_CIPHER_PIN_CMD =
      "801280FF060000" + ByteArrayUtil.toHex(PIN_OK.getBytes());
  private static final String SAM_CARD_CIPHER_PIN_RSP = CIPHER_PIN_OK + SW1SW2_OK;
  private static final String SAM_GIVE_RANDOM_CMD = "8086000008" + PO_CHALLENGE;
  private static final String SAM_GIVE_RANDOM_RSP = SW1SW2_OK;
  private static final String SAM_PREPARE_LOAD_CMD =
      "805601FF367C00070079007221D35F0E36"
          + SV_BALANCE_STR
          + "000000790000001A0000020000123456780000DB00709000B80000170000000079000000020000";
  private static final String SAM_PREPARE_LOAD_RSP = "9591160000DE2C8CB3D280" + SW1SW2_OK;
  private static final String SAM_PREPARE_DEBIT_CMD =
      "805401FF307C000900790073A54BC97DFA"
          + SV_BALANCE_STR
          + "FFFE0000000079123456780000DD00001600729000BA00001400FFFE0000000079";
  private static final String SAM_PREPARE_DEBIT_RSP = "CD00340000DF0C9437AABB" + SW1SW2_OK;
  private static final String SAM_PREPARE_UNDEBIT_CMD =
      "805C01FF307C000900790073A54BC97DFA"
          + SV_BALANCE_STR
          + "FFFE0000000079123456780000DD00001600729000BC0000140000020000000079";
  private static final String SAM_PREPARE_UNDEBIT_RSP = "CD00340000DF0C9437AABB" + SW1SW2_OK;
  private static final String SAM_SV_CHECK_CMD = "8058000003A54BC9";

  @Before
  public void setUp() {
    poCommandsTestSet.clear();
    samCommandsTestSet.clear();
    poReader = createMockReader("PO", TransmissionMode.CONTACTLESS, poCommandsTestSet);
    SeReader samReader = createMockReader("SAM", TransmissionMode.CONTACTS, samCommandsTestSet);
    CalypsoSam calypsoSam = createCalypsoSam();

    samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
  }

  /* Standard opening with two records read */
  @Test(expected = CalypsoPoTransactionIllegalStateException.class)
  public void testProcessOpening_noSamResource() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);

    // PoTransaction without PoSecuritySettings
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    // should raise an exception
    poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
  }

  /* Standard opening with two records read */
  @Test(expected = CalypsoPoTransactionIllegalStateException.class)
  public void testProcessOpening_readReopen() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);
    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

    poCommandsTestSet.put(
        PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD, PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
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
  public void testProcessOpening_dfNotRatified() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);
    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

    // addCommandPo(OPEN_SECURE_SESSION_SFI7_REC1, SAM_CHALLENGE_4, OPEN_SECURE_SESSION_RESP +
    // String.format("%02X", FILE_7_REC_1.length() / 2) + FILE_7_REC_1, SW1SW2_OK);
    //
    poCommandsTestSet.put(
        PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD, PO_OPEN_SECURE_SESSION_SFI7_REC1_NOT_RATIFIED_RSP);
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
  public void testProcessOpening_readMultipleRecords() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();
    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);
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
  public void testProcessOpening_kvcNotAuthorized() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);

    List<Byte> authorizedKvc = new ArrayList<Byte>();
    authorizedKvc.add((byte) 0x79);

    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .sessionAuthorizedKvcList(authorizedKvc)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);
    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

    poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD, PO_OPEN_SECURE_SESSION_KVC_78_CMD);

    poTransaction.prepareReadRecordFile(FILE7, 1);
    // an exception is expected
    poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
  }

  /*
   * Buffer overflow limit in atomic mode (counter in bytes): session buffer size = 430 b,
   * consumed size 430 b
   */
  @Test
  public void testProcessOpening_sessionBuffer_limit() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();
    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);
    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

    poCommandsTestSet.put(
        PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD, PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
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
  public void testProcessOpening_sessionBuffer_overflowBytesCounter() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();
    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

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
  public void testProcessOpening_sessionBuffer_limitOperationsCounter() {
    CalypsoPo calypsoPoRev24 = createCalypsoPo(FCI_REV24);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();
    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev24), poSecuritySettings);
    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

    poCommandsTestSet.put(
        PO_OPEN_SECURE_SESSION_SFI7_REC1_2_4_CMD, PO_OPEN_SECURE_SESSION_SFI7_REC1_2_4_RSP);
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
  public void testProcessOpening_sessionBuffer_overflowOperationsCounter() {
    CalypsoPo calypsoPoRev24 = createCalypsoPo(FCI_REV24);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();
    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev24), poSecuritySettings);

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
  public void testProcessOpening_sessionBuffer_overflowBytesCounter_MulitpleMode() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT) //
            .sessionModificationMode(PoTransaction.SessionSetting.ModificationMode.MULTIPLE)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);
    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
    samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_SFI7_REC1_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_DIGEST_UPDATE_UPDATE_REC_SFI8_REC1_29B_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);
    samCommandsTestSet.put(SAM_DIGEST_AUTHENTICATE, SW1SW2_OK_RSP);

    poCommandsTestSet.put(
        PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD, PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
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
  public void testProcessPoCommands_nominalCase() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);
    poCommandsTestSet.put(PO_READ_REC_SFI8_REC1_CMD, PO_READ_REC_SFI8_REC1_RSP);
    poCommandsTestSet.put(PO_READ_REC_SFI7_REC3_4_CMD, PO_READ_REC_SFI7_REC3_4_RSP);

    poTransaction.prepareReadRecordFile(FILE7, 1);
    poTransaction.prepareReadRecordFile(FILE8, 1);
    poTransaction.prepareReadRecordFile(FILE7, 3, 2, 29);
    poTransaction.processPoCommands();
    assertThat(calypsoPoRev31.getFileBySfi(FILE8).getData().getContent(1))
        .isEqualTo(FILE8_REC1_29B_BYTES);
    assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(1))
        .isEqualTo(FILE7_REC1_29B_BYTES);
    assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(3))
        .isEqualTo(FILE7_REC3_29B_BYTES);
    assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(4))
        .isEqualTo(FILE7_REC4_29B_BYTES);
  }

  /* Standard processPoCommands */
  @Test
  public void testprocessPoCommands_nominalCase() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

    poCommandsTestSet.put(
        PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD, PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
    poCommandsTestSet.put(PO_READ_REC_SFI8_REC1_CMD, PO_READ_REC_SFI8_REC1_RSP);
    poTransaction.prepareReadRecordFile(FILE7, 1);
    poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

    poTransaction.prepareReadRecordFile(FILE8, 1);
    // PoTransaction after a session is open
    poTransaction.processPoCommands();
    assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContent(1))
        .isEqualTo(FILE7_REC1_29B_BYTES);
    assertThat(calypsoPoRev31.getFileBySfi(FILE8).getData().getContent(1))
        .isEqualTo(FILE8_REC1_29B_BYTES);
  }

  /* processClosing no session open */
  @Test(expected = CalypsoPoTransactionIllegalStateException.class)
  public void testProcessClosing_noSessionOpen() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poTransaction.prepareReadRecordFile(FILE8, 1);
    poTransaction.prepareReleasePoChannel();
    // expected exception: no session is open
    poTransaction.processClosing();
  }

  /* Standard processClosing - default ratification */
  @Test
  public void testProcessClosing_nominalCase() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

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

    poCommandsTestSet.put(
        PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD, PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
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

    poTransaction.prepareDecreaseCounter(FILE10, (byte) 1, 100);
    poTransaction.prepareIncreaseCounter(FILE11, (byte) 1, 100);
    poTransaction.prepareUpdateRecord(FILE7, (byte) 1, ByteArrayUtil.fromHex(FILE7_REC1_4B));
    poTransaction.prepareWriteRecord(FILE8, (byte) 1, ByteArrayUtil.fromHex(FILE8_REC1_4B));
    poTransaction.prepareAppendRecord(FILE9, ByteArrayUtil.fromHex(FILE9_REC1_4B));

    // PoTransaction after a session is open
    poTransaction.prepareReleasePoChannel();
    poTransaction.processClosing();
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

  /* processClosing - PO fail on closing #1 Close Session is failing */
  @Test(expected = CalypsoPoCloseSecureSessionException.class)
  public void testProcessClosing_poCloseFail() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
    samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_CMD, SW1SW2_OK_RSP);

    samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_DIGEST_UPDATE_APPEND_REC_SFI9_REC1_4B_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);

    poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
    poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);
    poCommandsTestSet.put(PO_APPEND_REC_SFI9_REC1_4B_CMD, SW1SW2_OK_RSP);
    poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_FAILED_RSP);

    poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

    poTransaction.prepareReadRecordFile(FILE7, 1);

    // PoTransaction after a session is open
    // should raise a CalypsoPoCloseSecureSessionException due to the Close Session failure
    poTransaction.prepareReleasePoChannel();
    poTransaction.prepareAppendRecord(FILE9, ByteArrayUtil.fromHex(FILE9_REC1_4B));
    poTransaction.processClosing();
  }

  /* processClosing - PO fail on closing #2 Command is failing */
  @Test(expected = CalypsoPoCommandException.class)
  public void testProcessClosing_poCommandFail() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
    samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_CMD, SW1SW2_OK_RSP);

    samCommandsTestSet.put(SAM_DIGEST_UPDATE_READ_REC_SFI7_REC1_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_DIGEST_UPDATE_RSP_OK_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_DIGEST_UPDATE_APPEND_REC_SFI9_REC1_4B_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);

    poCommandsTestSet.put(PO_OPEN_SECURE_SESSION_CMD, PO_OPEN_SECURE_SESSION_RSP);
    poCommandsTestSet.put(PO_READ_REC_SFI7_REC1_CMD, PO_READ_REC_SFI7_REC1_RSP);
    poCommandsTestSet.put(PO_APPEND_REC_SFI9_REC1_4B_CMD, SW1SW2_KO);
    poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_CMD, PO_CLOSE_SECURE_SESSION_RSP);

    poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

    poTransaction.prepareReadRecordFile(FILE7, 1);

    // PoTransaction after a session is open
    // should raise a CalypsoPoCommandException due to the append record failure
    poTransaction.prepareReleasePoChannel();
    poTransaction.prepareAppendRecord(FILE9, ByteArrayUtil.fromHex(FILE9_REC1_4B));
    poTransaction.processClosing();
  }

  /* processClosing - SAM authentication fail on closing */
  @Test(expected = CalypsoSessionAuthenticationException.class)
  public void testProcessClosing_samAuthenticateFail() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

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
    poTransaction.prepareReleasePoChannel();
    poTransaction.processClosing();
  }

  /* processClosing - SAM IO error while authenticating */
  @Test(expected = CalypsoAuthenticationNotVerifiedException.class)
  public void testProcessClosing_samIoErrorAuthenticating() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

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
    poTransaction.prepareReleasePoChannel();
    poTransaction.processClosing();
  }

  /*
   * Buffer overflow limit in atomic mode (counter in bytes): session buffer size = 430 b,
   * consumed size 430 b
   */
  @Test
  public void testProcessClosing_sessionBuffer_limit() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

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
    poTransaction.prepareReleasePoChannel();
    poTransaction.processClosing();
    assertThat(calypsoPoRev31.getFileBySfi(FILE8).getData().getContent(1))
        .isEqualTo(FILE8_REC1_4B_BYTES);
  }

  /*
   * Buffer overflowed in atomic mode (counter in bytes): session buffer size = 430 b, consumed
   * size 431 b
   */
  @Test
  public void testProcessClosing_sessionBuffer_overflowed() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

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
      poTransaction.prepareUpdateRecord(
          FILE8,
          (byte) 1,
          ByteArrayUtil.fromHex("8111111111111111111111111111111111111111111111111111111111"));
    }
    // insert additional non modifying commands (should not affect the session buffer)
    for (int i = 0; i < 4; i++) {
      poTransaction.prepareReadRecordFile(FILE7, 1);
    }
    // 4 additional bytes (10 b consumed)
    poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_5B_BYTES);

    try {
      // PoTransaction after a session is open
      poTransaction.prepareReleasePoChannel();
      poTransaction.processClosing();
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
  public void testProcessClosing_sessionBuffer_overflowMultipleMode() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT) //
            .sessionModificationMode(PoTransaction.SessionSetting.ModificationMode.MULTIPLE) //
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

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
    poTransaction.prepareReleasePoChannel();
    poTransaction.processClosing();

    assertThat(true).isTrue();
  }

  /* Standard processClosing - close not ratified */
  @Test
  public void testProcessClosing_nominalCase_closeNotRatified() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT) //
            .ratificationMode(PoTransaction.SessionSetting.RatificationMode.CLOSE_NOT_RATIFIED) //
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);
    samCommandsTestSet.put(SAM_DIGEST_INIT_OPEN_SECURE_SESSION_SFI7_REC1_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_DIGEST_CLOSE_CMD, SAM_DIGEST_CLOSE_RSP);
    samCommandsTestSet.put(SAM_DIGEST_AUTHENTICATE, SW1SW2_OK_RSP);

    poCommandsTestSet.put(
        PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD, PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
    poCommandsTestSet.put(PO_CLOSE_SECURE_SESSION_NOT_RATIFIED_CMD, PO_CLOSE_SECURE_SESSION_RSP);

    poTransaction.prepareReadRecordFile(FILE7, 1);
    poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);

    poTransaction.prepareReleasePoChannel();
    poTransaction.processClosing();

    assertThat(true).isTrue();
  }

  /* Session buffer overflow in atomic mode: the overflow happens at closing */
  @Test
  public void testTransaction_sessionBuffer_overflowAtomic() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT) //
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

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
    poTransaction.processPoCommands();

    // 5 x update (29 b) = 5 x (29 + 6) = 140 consumed in the session buffer
    for (int i = 0; i < 4; i++) {
      poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
    }
    // 4 additional bytes (10 b consumed)
    poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_5B_BYTES);

    try {
      // PoTransaction after a session is open
      poTransaction.prepareReleasePoChannel();
      poTransaction.processClosing();
    } catch (CalypsoAtomicTransactionException ex) {
      // expected exception: buffer overflow
      return;
    }
    fail("Unexpected behaviour");
  }

  /* Session buffer overflow in multiple mode: the overflow happens and is handled at closing */
  @Test
  public void testTransaction_sessionBuffer_overflowMultiple() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT) //
            .sessionModificationMode(PoTransaction.SessionSetting.ModificationMode.MULTIPLE) //
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

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
    poTransaction.processPoCommands();

    // 24 x update (29 b) = 24 x (29 + 6) = 840 consumed in the session buffer
    // force multiple cycles
    for (int i = 0; i < 24; i++) {
      poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_29B_BYTES);
    }
    // 4 additional bytes (10 b consumed)
    poTransaction.prepareUpdateRecord(FILE8, (byte) 1, FILE8_REC1_5B_BYTES);

    // PoTransaction after a session is open
    poTransaction.prepareReleasePoChannel();
    poTransaction.processClosing();

    assertThat(true).isTrue();
  }

  /* open, cancel and reopen */
  @Test
  public void testProcessCancel_open_cancelOpen() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .build();

    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);
    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GET_CHALLENGE_CMD, SAM_GET_CHALLENGE_RSP);

    poCommandsTestSet.put(
        PO_OPEN_SECURE_SESSION_SFI7_REC1_CMD, PO_OPEN_SECURE_SESSION_SFI7_REC1_RSP);
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
    poTransaction.processCancel();
    poTransaction.processOpening(AccessLevel.SESSION_LVL_DEBIT);
  }

  /* Verify PIN Po commands */
  @Test(expected = IllegalStateException.class)
  public void testProcessVerifyPin_no_pin_command_executed() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    assertThat(calypsoPoRev31.getPinAttemptRemaining()).isEqualTo(3);
  }

  @Test
  public void testProcessVerifyPin_plain_outside_secureSession() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(PO_VERIFY_PIN_PLAIN_OK_CMD, PO_VERIFY_PIN_OK_RSP);

    poTransaction.processVerifyPin(PIN_OK);
    assertThat(calypsoPoRev31.getPinAttemptRemaining()).isEqualTo(3);
  }

  @Test
  public void testProcessCheckPinStatus_outside_secureSession() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(PO_CHECK_PIN_CMD, PO_VERIFY_PIN_OK_RSP);
    poTransaction.prepareCheckPinStatus();
    poTransaction.processPoCommands();
    assertThat(calypsoPoRev31.getPinAttemptRemaining()).isEqualTo(3);

    poCommandsTestSet.put(PO_CHECK_PIN_CMD, PO_VERIFY_PIN_KO_RSP);
    poTransaction.prepareCheckPinStatus();
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
    assertThat(calypsoPoRev31.getPinAttemptRemaining()).isEqualTo(2);
  }

  @Test
  public void testProcessVerifyPin_encrypted_outside_secureSession() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT) //
            .sessionDefaultKeyRecordNumber(
                AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
            .pinTransmissionMode(PoTransaction.PinTransmissionMode.ENCRYPTED)
            .build();
    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_GIVE_RANDOM_CMD, SAM_GIVE_RANDOM_RSP);
    samCommandsTestSet.put(SAM_CARD_CIPHER_PIN_CMD, SAM_CARD_CIPHER_PIN_RSP);

    poCommandsTestSet.put(PO_GET_CHALLENGE_CMD, PO_GET_CHALLENGE_RSP);
    poCommandsTestSet.put(PO_VERIFY_PIN_ENCRYPTED_OK_CMD, PO_VERIFY_PIN_OK_RSP);

    poTransaction.processVerifyPin(PIN_OK);
    assertThat(calypsoPoRev31.getPinAttemptRemaining()).isEqualTo(3);
  }

  @Test
  public void testPrepareSelectFile_selectControl() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(PO_SELECT_FILE_CURRENT_CMD, PO_SELECT_FILE_3F00_RSP);
    poCommandsTestSet.put(PO_SELECT_FILE_FIRST_CMD, PO_SELECT_FILE_0002_RSP);
    poCommandsTestSet.put(PO_SELECT_FILE_NEXT_CMD, PO_SELECT_FILE_0003_RSP);

    poTransaction.prepareSelectFile(SelectFileControl.CURRENT_DF);
    poTransaction.prepareSelectFile(SelectFileControl.FIRST_EF);
    poTransaction.prepareSelectFile(SelectFileControl.NEXT_EF);
    poTransaction.processPoCommands();
    DirectoryHeader directoryHeader = calypsoPoRev31.getDirectoryHeader();
    FileHeader fileHeader1 = calypsoPoRev31.getFileByLid((short) 0x02).getHeader();
    FileHeader fileHeader2 = calypsoPoRev31.getFileByLid((short) 0x03).getHeader();
    System.out.println(directoryHeader);
    System.out.println(fileHeader1);
    System.out.println(fileHeader2);

    assertThat(directoryHeader.getLid()).isEqualTo(LID_3F00);
    assertThat(directoryHeader.getAccessConditions())
        .isEqualTo(ByteArrayUtil.fromHex(ACCESS_CONDITIONS_3F00));
    assertThat(directoryHeader.getKeyIndexes()).isEqualTo(ByteArrayUtil.fromHex(KEY_INDEXES_3F00));
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
  public void testPrepareSelectFile_lid() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(PO_SELECT_FILE_3F00_CMD, PO_SELECT_FILE_3F00_RSP);
    poCommandsTestSet.put(PO_SELECT_FILE_0002_CMD, PO_SELECT_FILE_0002_RSP);
    poCommandsTestSet.put(PO_SELECT_FILE_0003_CMD, PO_SELECT_FILE_0003_RSP);

    poTransaction.prepareSelectFile(ByteArrayUtil.fromHex(LID_3F00_STR));
    poTransaction.prepareSelectFile(ByteArrayUtil.fromHex(LID_0002_STR));
    poTransaction.prepareSelectFile(ByteArrayUtil.fromHex(LID_0003_STR));
    poTransaction.processPoCommands();

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
    assertThat(directoryHeader.getKeyIndexes()).isEqualTo(ByteArrayUtil.fromHex(KEY_INDEXES_3F00));
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
  public void testPrepareReadCounterFile() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(
        PO_READ_REC_SFI7_REC1_6B_COUNTER_CMD, PO_READ_REC_SFI7_REC1_6B_COUNTER_RSP);

    poTransaction.prepareReadCounterFile(FILE7, 2);
    poTransaction.processPoCommands();
    assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContentAsCounterValue(1))
        .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(FILE7_REC1_COUNTER1), 0));
    assertThat(calypsoPoRev31.getFileBySfi(FILE7).getData().getContentAsCounterValue(2))
        .isEqualTo(ByteArrayUtil.threeBytesToInt(ByteArrayUtil.fromHex(FILE7_REC1_COUNTER2), 0));
  }

  @Test
  public void testPrepareSvGet_Reload() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource).build();
    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_PREPARE_LOAD_CMD, SAM_PREPARE_LOAD_RSP);
    samCommandsTestSet.put(SAM_SV_CHECK_CMD, SW1SW2_OK);

    poCommandsTestSet.put(PO_SV_GET_RELOAD_CMD, PO_SV_GET_RELOAD_RSP);
    poCommandsTestSet.put(PO_SV_RELOAD_CMD, PO_SV_RELOAD_RSP);

    poTransaction.prepareSvGet(
        PoTransaction.SvSettings.Operation.RELOAD, PoTransaction.SvSettings.Action.DO);
    poTransaction.processPoCommands();
    poTransaction.prepareSvReload(2);
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
    assertThat(calypsoPoRev31.getSvBalance()).isEqualTo(SV_BALANCE);
    assertThat(calypsoPoRev31.getSvLoadLogRecord()).isNotNull();
    try {
      assertThat(calypsoPoRev31.getSvDebitLogLastRecord()).isNull();
      shouldHaveThrown(NoSuchElementException.class);
    } catch (NoSuchElementException ex) {
    }
  }

  @Test
  public void testPrepareSvGet_Reload_AllLogs() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource) //
            .svGetLogReadMode(PoTransaction.SvSettings.LogRead.ALL)
            .build();
    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_PREPARE_LOAD_CMD, SAM_PREPARE_LOAD_RSP);
    samCommandsTestSet.put(SAM_SV_CHECK_CMD, SW1SW2_OK);

    poCommandsTestSet.put(PO_SV_GET_DEBIT_CMD, PO_SV_GET_DEBIT_RSP);
    poCommandsTestSet.put(PO_SV_GET_RELOAD_CMD, PO_SV_GET_RELOAD_RSP);
    poCommandsTestSet.put(PO_SV_RELOAD_CMD, PO_SV_RELOAD_RSP);

    poTransaction.prepareSvGet(
        PoTransaction.SvSettings.Operation.RELOAD, PoTransaction.SvSettings.Action.DO);
    poTransaction.processPoCommands();
    poTransaction.prepareSvReload(2);
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
    assertThat(calypsoPoRev31.getSvBalance()).isEqualTo(SV_BALANCE);
    assertThat(calypsoPoRev31.getSvLoadLogRecord()).isNotNull();
    assertThat(calypsoPoRev31.getSvDebitLogLastRecord()).isNotNull();
  }

  @Test
  public void testPrepareSvGet_Debit() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource).build();
    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_PREPARE_DEBIT_CMD, SAM_PREPARE_DEBIT_RSP);
    samCommandsTestSet.put(SAM_SV_CHECK_CMD, SW1SW2_OK);

    poCommandsTestSet.put(PO_SV_GET_DEBIT_CMD, PO_SV_GET_DEBIT_RSP);
    poCommandsTestSet.put(PO_SV_DEBIT_CMD, PO_SV_DEBIT_RSP);

    poTransaction.prepareSvGet(
        PoTransaction.SvSettings.Operation.DEBIT, PoTransaction.SvSettings.Action.DO);
    poTransaction.processPoCommands();
    poTransaction.prepareSvDebit(2);
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
    assertThat(calypsoPoRev31.getSvBalance()).isEqualTo(SV_BALANCE);
    assertThat(calypsoPoRev31.getSvDebitLogLastRecord()).isNotNull();
  }

  @Test
  public void testPrepareSvGet_Undebit() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource).build();
    poTransaction =
        new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31), poSecuritySettings);

    samCommandsTestSet.put(SAM_SELECT_DIVERSIFIER_CMD, SW1SW2_OK_RSP);
    samCommandsTestSet.put(SAM_PREPARE_UNDEBIT_CMD, SAM_PREPARE_UNDEBIT_RSP);
    samCommandsTestSet.put(SAM_SV_CHECK_CMD, SW1SW2_OK);

    poCommandsTestSet.put(PO_SV_GET_DEBIT_CMD, PO_SV_GET_DEBIT_RSP);
    poCommandsTestSet.put(PO_SV_UNDEBIT_CMD, PO_SV_UNDEBIT_RSP);

    poTransaction.prepareSvGet(
        PoTransaction.SvSettings.Operation.DEBIT, PoTransaction.SvSettings.Action.UNDO);
    poTransaction.processPoCommands();
    poTransaction.prepareSvDebit(2);
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
    assertThat(calypsoPoRev31.getSvBalance()).isEqualTo(SV_BALANCE);
    assertThat(calypsoPoRev31.getSvDebitLogLastRecord()).isNotNull();
  }

  @Test
  public void testPrepareSvReadAllLogs() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_STORED_VALUE_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(PO_READ_SV_LOAD_LOG_FILE_CMD, PO_READ_SV_LOAD_LOG_FILE_RSP);
    poCommandsTestSet.put(PO_READ_SV_DEBIT_LOG_FILE_CMD, PO_READ_SV_DEBIT_LOG_FILE_RSP);

    poTransaction.prepareSvReadAllLogs();
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();

    assertThat(calypsoPoRev31.getSvLoadLogRecord()).isNotNull();
    assertThat(calypsoPoRev31.getSvDebitLogLastRecord()).isNotNull();
    List<SvDebitLogRecord> allDebitLogs = calypsoPoRev31.getSvDebitLogAllRecords();
    assertThat(calypsoPoRev31.getSvDebitLogAllRecords().size()).isEqualTo(3);
    assertThat(allDebitLogs.get(0).getSamId()).isEqualTo(0xAABBCC01);
    assertThat(allDebitLogs.get(1).getSamId()).isEqualTo(0xAABBCC02);
    assertThat(allDebitLogs.get(2).getSamId()).isEqualTo(0xAABBCC03);
  }

  @Test
  public void testPrepareInvalidate_notInvalidated() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(PO_INVALIDATE_CMD, SW1SW2_OK_RSP);

    poTransaction.prepareInvalidate();
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
  }

  @Test(expected = CalypsoPoTransactionIllegalStateException.class)
  public void testPrepareInvalidate_invalidated() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31_INVALIDATED);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(PO_INVALIDATE_CMD, SW1SW2_OK_RSP);

    poTransaction.prepareInvalidate();
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
  }

  @Test(expected = CalypsoPoTransactionIllegalStateException.class)
  public void testPrepareRehabilitate_notInvalidated() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(PO_REHABILITATE_CMD, SW1SW2_OK_RSP);

    poTransaction.prepareRehabilitate();
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
  }

  @Test
  public void testPrepareRehabilitate_invalidated() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31_INVALIDATED);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));

    poCommandsTestSet.put(PO_REHABILITATE_CMD, SW1SW2_OK_RSP);

    poTransaction.prepareRehabilitate();
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
  }

  @Test(expected = CalypsoPoIOException.class)
  public void testPoIoException() {
    CalypsoPo calypsoPoRev31 = createCalypsoPo(FCI_REV31);
    poTransaction = new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPoRev31));
    poTransaction.prepareReadRecordFile(FILE7, 1);
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
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
    SeResponse selectionData =
        new SeResponse(
            true,
            false,
            new SelectionStatus(null, new ApduResponse(ByteArrayUtil.fromHex(FCI), null), true),
            null);
    return new CalypsoPo(selectionData);
  }

  private CalypsoSam createCalypsoSam() {

    SelectionStatus selectionStatus =
        new SelectionStatus(new AnswerToReset(ByteArrayUtil.fromHex(ATR1)), null, true);
    return new CalypsoSam(new SeResponse(true, true, selectionStatus, null));
  }

  private ProxyReader createMockReader(
      final String name,
      TransmissionMode transmissionMode,
      final Map<String, String> commandTestSet) {

    // configure mock native reader
    ProxyReader mockReader = Mockito.spy(ProxyReader.class);
    doReturn(name).when(mockReader).getName();
    doReturn(transmissionMode).when(mockReader).getTransmissionMode();

    doAnswer(
            new Answer<SeResponse>() {
              @Override
              public SeResponse answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                SeRequest seRequest = (SeRequest) args[0];
                List<ApduRequest> apduRequests = seRequest.getApduRequests();
                List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
                try {
                  for (ApduRequest apduRequest : apduRequests) {
                    ApduResponse apduResponse = getResponses(name, commandTestSet, apduRequest);
                    apduResponses.add(apduResponse);
                    System.out.println(
                        "Request="
                            + apduRequest.toString()
                            + ", Response="
                            + apduResponse.toString());
                  }
                } catch (KeypleReaderIOException ex) {
                  ex.setSeResponse(new SeResponse(true, true, null, apduResponses));
                  throw ex;
                }
                return new SeResponse(true, true, null, apduResponses);
              }
            })
        .when(mockReader)
        .transmitSeRequest(any(SeRequest.class), any(ChannelControl.class));
    return mockReader;
  }

  private ApduResponse getResponses(
      String name, Map<String, String> cmdRespMap, ApduRequest apduRequest) {
    String apdu_c = ByteArrayUtil.toHex(apduRequest.getBytes());
    String apdu_r = cmdRespMap.get(apdu_c);
    // return matching hexa response if found
    if (apdu_r != null) {
      return new ApduResponse(ByteArrayUtil.fromHex(apdu_r), null);
    }
    System.out.println(
        name + ": no response available for " + ByteArrayUtil.toHex(apduRequest.getBytes()));
    // throw a KeypleReaderIOException if not found
    throw new KeypleReaderIOException("No response available for this request.");
  }
}

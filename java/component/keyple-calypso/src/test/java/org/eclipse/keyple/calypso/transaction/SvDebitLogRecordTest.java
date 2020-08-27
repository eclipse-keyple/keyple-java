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

import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class SvDebitLogRecordTest {
  private static final String HEADER = "79007013DE31A75F00001A";
  private static final String AMOUNT_STR = "FFFE";
  private static final String DATE_STR = "1234";
  private static final String TIME_STR = "5678";
  private static final String KVC_STR = "90";
  private static final String SAMID_STR = "AABBCCDD";
  private static final String SAM_TNUM_STR = "112233";
  private static final String BALANCE_STR = "445566";
  private static final String SV_TNUM_STR = "7788";

  private static final int AMOUNT = -2;
  private static final int DATE = 0x1234;
  private static final byte[] DATE_BYTES = ByteArrayUtil.fromHex(DATE_STR);
  private static final int TIME = 0x5678;
  private static final byte[] TIME_BYTES = ByteArrayUtil.fromHex(TIME_STR);
  private static final byte KVC = (byte) 0x90;
  private static final int SAMID = 0xAABBCCDD;
  private static final byte[] SAMID_BYTES = ByteArrayUtil.fromHex(SAMID_STR);
  private static final int SAM_TNUM = 0x112233;
  private static final byte[] SAM_TNUM_BYTES = ByteArrayUtil.fromHex(SAM_TNUM_STR);
  private static final int BALANCE = 0x445566;
  private static final int SV_TNUM = 0x7788;
  private static final byte[] SV_TNUM_BYTES = ByteArrayUtil.fromHex(SV_TNUM_STR);

  @Test
  public void svDebitLogRecord() {
    byte[] svGetDebitData =
        ByteArrayUtil.fromHex(
            HEADER
                + AMOUNT_STR
                + DATE_STR
                + TIME_STR
                + KVC_STR
                + SAMID_STR
                + SAM_TNUM_STR
                + BALANCE_STR
                + SV_TNUM_STR);
    SvDebitLogRecord svDebitLogRecord = new SvDebitLogRecord(svGetDebitData, HEADER.length() / 2);
    assertThat(svDebitLogRecord.getAmount()).isEqualTo(AMOUNT);
    assertThat(svDebitLogRecord.getBalance()).isEqualTo(BALANCE);
    assertThat(svDebitLogRecord.getDebitDate()).isEqualTo(DATE);
    assertThat(svDebitLogRecord.getDebitDateBytes()).isEqualTo(DATE_BYTES);
    assertThat(svDebitLogRecord.getDebitTime()).isEqualTo(TIME);
    assertThat(svDebitLogRecord.getDebitTimeBytes()).isEqualTo(TIME_BYTES);
    assertThat(svDebitLogRecord.getKvc()).isEqualTo(KVC);
    assertThat(svDebitLogRecord.getSamId()).isEqualTo(SAMID);
    assertThat(svDebitLogRecord.getSamIdBytes()).isEqualTo(SAMID_BYTES);
    assertThat(svDebitLogRecord.getSamTNum()).isEqualTo(SAM_TNUM);
    assertThat(svDebitLogRecord.getSamTNumBytes()).isEqualTo(SAM_TNUM_BYTES);
    assertThat(svDebitLogRecord.getSvTNum()).isEqualTo(SV_TNUM);
    assertThat(svDebitLogRecord.getSvTNumBytes()).isEqualTo(SV_TNUM_BYTES);
    assertThat(svDebitLogRecord.toString()).contains(SAMID_STR);
  }
}

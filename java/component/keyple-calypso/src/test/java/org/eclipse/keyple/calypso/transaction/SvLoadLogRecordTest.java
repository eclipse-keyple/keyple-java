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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class SvLoadLogRecordTest {
  private static final String HEADER = "79007013DE31A75F00001A";
  private static final String AMOUNT_STR = "FFFFFE";
  private static final String DATE_STR = "1234";
  private static final String TIME_STR = "5678";
  private static final String FREE1_STR = "41";
  private static final String FREE2_STR = "42";
  private static final String FREE = "AB";
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
  private static final byte[] FREE_BYTES = ByteArrayUtil.fromHex(FREE1_STR + FREE2_STR);
  private static final byte KVC = (byte) 0x90;
  private static final int SAMID = 0xAABBCCDD;
  private static final byte[] SAMID_BYTES = ByteArrayUtil.fromHex(SAMID_STR);
  private static final int SAM_TNUM = 0x112233;
  private static final byte[] SAM_TNUM_BYTES = ByteArrayUtil.fromHex(SAM_TNUM_STR);
  private static final int BALANCE = 0x445566;
  private static final int SV_TNUM = 0x7788;
  private static final byte[] SV_TNUM_BYTES = ByteArrayUtil.fromHex(SV_TNUM_STR);

  @Test
  public void svLoadLogRecord() {
    byte[] svGetLoadData =
        ByteArrayUtil.fromHex(
            HEADER
                + DATE_STR
                + FREE1_STR
                + KVC_STR
                + FREE2_STR
                + BALANCE_STR
                + AMOUNT_STR
                + TIME_STR
                + SAMID_STR
                + SAM_TNUM_STR
                + SV_TNUM_STR);
    SvLoadLogRecord svLoadLogRecord = new SvLoadLogRecord(svGetLoadData, HEADER.length() / 2);
    assertThat(svLoadLogRecord.getAmount()).isEqualTo(AMOUNT);
    assertThat(svLoadLogRecord.getBalance()).isEqualTo(BALANCE);
    assertThat(svLoadLogRecord.getLoadDate()).isEqualTo(DATE);
    assertThat(svLoadLogRecord.getLoadDateBytes()).isEqualTo(DATE_BYTES);
    assertThat(svLoadLogRecord.getLoadTime()).isEqualTo(TIME);
    assertThat(svLoadLogRecord.getLoadTimeBytes()).isEqualTo(TIME_BYTES);
    assertThat(svLoadLogRecord.getLoadTime()).isEqualTo(TIME);
    assertThat(svLoadLogRecord.getFreeByte()).isEqualTo(FREE);
    assertThat(svLoadLogRecord.getFreeByteBytes()).isEqualTo(FREE_BYTES);
    assertThat(svLoadLogRecord.getKvc()).isEqualTo(KVC);
    assertThat(svLoadLogRecord.getSamId()).isEqualTo(SAMID);
    assertThat(svLoadLogRecord.getSamIdBytes()).isEqualTo(SAMID_BYTES);
    assertThat(svLoadLogRecord.getSamTNum()).isEqualTo(SAM_TNUM);
    assertThat(svLoadLogRecord.getSamTNumBytes()).isEqualTo(SAM_TNUM_BYTES);
    assertThat(svLoadLogRecord.getSvTNum()).isEqualTo(SV_TNUM);
    assertThat(svLoadLogRecord.getSvTNumBytes()).isEqualTo(SV_TNUM_BYTES);
    assertThat(svLoadLogRecord.toString()).contains(SAMID_STR);
  }
}

/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.util;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

public class ByteArrayUtilTest {
  private static final String HEXSTRING_ODD = "0102030";
  private static final String HEXSTRING_BAD = "010203ABGH80";
  private static final String HEXSTRING_GOOD = "1234567890ABCDEFFEDCBA0987654321";
  private static final byte[] BYTEARRAY_GOOD =
      new byte[] {
        (byte) 0x12,
        (byte) 0x34,
        (byte) 0x56,
        (byte) 0x78,
        (byte) 0x90,
        (byte) 0xAB,
        (byte) 0xCD,
        (byte) 0xEF,
        (byte) 0xFE,
        (byte) 0xDC,
        (byte) 0xBA,
        (byte) 0x09,
        (byte) 0x87,
        (byte) 0x65,
        (byte) 0x43,
        (byte) 0x21
      };
  private static final byte[] BYTEARRAY_LEN_2 = new byte[] {(byte) 0x12, (byte) 0x34};
  private static final byte[] BYTEARRAY_LEN_3 = new byte[] {(byte) 0x12, (byte) 0x34, (byte) 0x56};

  @Test(expected = NullPointerException.class)
  public void fromHex_null() {
    byte[] bytes = ByteArrayUtil.fromHex(null);
  }

  @Test
  public void fromHex_empty() {
    byte[] bytes = ByteArrayUtil.fromHex("");
    assertThat(bytes.length).isEqualTo(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void fromHex_odd_length() {
    byte[] bytes = ByteArrayUtil.fromHex(HEXSTRING_ODD);
  }

  @Test
  public void fromHex_bad_hex() {
    // no verification is being carried out at the moment.
    byte[] bytes = ByteArrayUtil.fromHex(HEXSTRING_BAD);
    // just check that the conversion is wrong
    String hex = ByteArrayUtil.toHex(bytes);
    assertThat(hex).isNotEqualTo(HEXSTRING_BAD);
  }

  @Test
  public void fromHex_good_hex() {
    // no verification is being carried out at the moment.
    byte[] bytes = ByteArrayUtil.fromHex(HEXSTRING_GOOD);
    assertThat(bytes).isEqualTo(BYTEARRAY_GOOD);
  }

  @Test
  public void toHex_null() {
    String hex = ByteArrayUtil.toHex(null);
    assertThat(hex.length()).isEqualTo(0);
  }

  @Test
  public void toHex_empty() {
    byte[] bytes = new byte[0];
    String hex = ByteArrayUtil.toHex(bytes);
    assertThat(hex.length()).isEqualTo(0);
  }

  @Test
  public void toHex_bytearray_good() {
    String hex = ByteArrayUtil.toHex(BYTEARRAY_GOOD);
    assertThat(hex).isEqualTo(HEXSTRING_GOOD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void threeBytesToInt_null() {
    int value = ByteArrayUtil.threeBytesToInt(null, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void threeBytesToInt_negative_offset() {
    int value = ByteArrayUtil.threeBytesToInt(BYTEARRAY_GOOD, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void threeBytesToInt_too_short_buffer_1() {
    int value = ByteArrayUtil.threeBytesToInt(BYTEARRAY_LEN_2, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void threeBytesToInt_too_short_buffer_2() {
    int value = ByteArrayUtil.threeBytesToInt(BYTEARRAY_LEN_3, 1);
  }

  @Test
  public void threeBytesToInt_buffer_ok_1() {
    int value = ByteArrayUtil.threeBytesToInt(BYTEARRAY_LEN_3, 0);
    assertThat(value).isEqualTo(0x123456);
  }

  @Test
  public void threeBytesToInt_buffer_ok_2() {
    int value = ByteArrayUtil.threeBytesToInt(BYTEARRAY_GOOD, 0);
    assertThat(value).isEqualTo(0x123456);
  }

  @Test
  public void threeBytesToInt_buffer_ok_3() {
    int value = ByteArrayUtil.threeBytesToInt(BYTEARRAY_GOOD, 1);
    assertThat(value).isEqualTo(0x345678);
  }

  @Test
  public void threeBytesToInt_buffer_ok_4() {
    int value = ByteArrayUtil.threeBytesToInt(BYTEARRAY_GOOD, 4);
    assertThat(value).isEqualTo(0x90ABCD);
  }

  @Test
  public void threeBytesToInt_buffer_ok_5() {
    int value = ByteArrayUtil.threeBytesToInt(BYTEARRAY_GOOD, 13);
    assertThat(value).isEqualTo(0x654321);
  }
}

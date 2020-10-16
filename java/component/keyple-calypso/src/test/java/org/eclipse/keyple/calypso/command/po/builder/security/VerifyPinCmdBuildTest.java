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
package org.eclipse.keyple.calypso.command.po.builder.security;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.parser.security.VerifyPinRespPars;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.reader.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class VerifyPinCmdBuildTest {
  private static final String SW1SW2_OK_STR = "9000";
  private static final String PIN_DATA_STR = "31323334";
  private static final String CIPHERED_PIN_DATA_STR = "0011223344556677";
  private static final String PIN_DATA_LENGTH_5_STR = "3132333435";

  private static final byte[] SW1SW2_OK = ByteArrayUtil.fromHex(SW1SW2_OK_STR);
  private static final byte[] PIN_DATA = ByteArrayUtil.fromHex(PIN_DATA_STR);
  private static final byte[] CIPHERED_PIN_DATA = ByteArrayUtil.fromHex(CIPHERED_PIN_DATA_STR);
  private static final byte[] PIN_DATA_LENGTH_5 = ByteArrayUtil.fromHex(PIN_DATA_LENGTH_5_STR);

  private static final byte[] APDU_ISO_PLAIN =
      ByteArrayUtil.fromHex("00 20 0000 04" + PIN_DATA_STR);
  private static final byte[] APDU_ISO_ENCRYPTED =
      ByteArrayUtil.fromHex("00 20 0000 08" + CIPHERED_PIN_DATA_STR);
  private static final byte[] APDU_ISO_READ_COUNTER = ByteArrayUtil.fromHex("00 20 0000 00");

  @Test
  public void verifyPin_plain() {
    VerifyPinCmdBuild builder =
        new VerifyPinCmdBuild(PoClass.ISO, PoTransaction.PinTransmissionMode.PLAIN, PIN_DATA);
    byte[] apduRequestBytes = builder.getApduRequest().getBytes();
    assertThat(apduRequestBytes).isEqualTo(APDU_ISO_PLAIN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void verifyPin_pin_null() {
    VerifyPinCmdBuild builder =
        new VerifyPinCmdBuild(PoClass.ISO, PoTransaction.PinTransmissionMode.PLAIN, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void verifyPin_pin_bad_length() {
    VerifyPinCmdBuild builder =
        new VerifyPinCmdBuild(
            PoClass.ISO, PoTransaction.PinTransmissionMode.PLAIN, PIN_DATA_LENGTH_5);
  }

  @Test
  public void verifyPin_encrypted() {
    VerifyPinCmdBuild builder =
        new VerifyPinCmdBuild(
            PoClass.ISO, PoTransaction.PinTransmissionMode.ENCRYPTED, CIPHERED_PIN_DATA);
    byte[] apduRequestBytes = builder.getApduRequest().getBytes();
    assertThat(apduRequestBytes).isEqualTo(APDU_ISO_ENCRYPTED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void verifyPin_encrypted_pin_null() {
    VerifyPinCmdBuild builder =
        new VerifyPinCmdBuild(PoClass.ISO, PoTransaction.PinTransmissionMode.ENCRYPTED, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void verifyPin_encrypted_pin_bad_length() {
    VerifyPinCmdBuild builder =
        new VerifyPinCmdBuild(
            PoClass.ISO, PoTransaction.PinTransmissionMode.ENCRYPTED, PIN_DATA_LENGTH_5);
  }

  @Test
  public void verifyPin_read_presentation_counter() {
    VerifyPinCmdBuild builder = new VerifyPinCmdBuild(PoClass.ISO);
    byte[] apduRequestBytes = builder.getApduRequest().getBytes();
    assertThat(apduRequestBytes).isEqualTo(APDU_ISO_READ_COUNTER);
  }

  @Test
  public void verifyPin_various_tests() {
    VerifyPinCmdBuild builder =
        new VerifyPinCmdBuild(PoClass.ISO, PoTransaction.PinTransmissionMode.PLAIN, PIN_DATA);
    ApduResponse apduResponse = new ApduResponse(SW1SW2_OK, null);
    assertThat(builder.createResponseParser(apduResponse).getClass())
        .isEqualTo(VerifyPinRespPars.class);
    assertThat(builder.isSessionBufferUsed()).isFalse();
  }
}

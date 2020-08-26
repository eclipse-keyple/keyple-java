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
package org.eclipse.keyple.calypso.command.sam.builder;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.eclipse.keyple.calypso.KeyReference;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.security.CardCipherPinCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.CardCipherPinRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class CardCipherPinCmdBuildTest {
  private static final String SW1SW2_OK_STR = "9000";
  private static final String KIF_CIPH_KEY_STR = "56";
  private static final String KVC_CIPH_KEY_STR = "78";
  private static final String CURRENT_PIN_STR = "31323334";
  private static final String NEW_PIN_STR = "35363738";
  private static final String PIN_LENGTH_5_STR = "3132333435";

  private static final byte[] SW1SW2_OK = ByteArrayUtil.fromHex(SW1SW2_OK_STR);
  private static final byte KIF_CIPH_KEY = ByteArrayUtil.fromHex(KIF_CIPH_KEY_STR)[0];
  private static final byte KVC_CIPH_KEY = ByteArrayUtil.fromHex(KVC_CIPH_KEY_STR)[0];
  private static final KeyReference KEY_REFERENCE_CIPH_KEY =
      new KeyReference(KIF_CIPH_KEY, KVC_CIPH_KEY);
  private static final byte[] CURRENT_PIN = ByteArrayUtil.fromHex(CURRENT_PIN_STR);
  private static final byte[] NEW_PIN = ByteArrayUtil.fromHex(NEW_PIN_STR);
  private static final byte[] PIN_LENGTH_5 = ByteArrayUtil.fromHex(PIN_LENGTH_5_STR);
  private static final byte[] APDU_ISO_CHANGE_PIN =
      ByteArrayUtil.fromHex(
          "80 12 40FF 0A" + KIF_CIPH_KEY_STR + KVC_CIPH_KEY_STR + CURRENT_PIN_STR + NEW_PIN_STR);
  private static final byte[] APDU_ISO_VERIFY_PIN =
      ByteArrayUtil.fromHex(
          "80 12 80FF 06" + KIF_CIPH_KEY_STR + KVC_CIPH_KEY_STR + CURRENT_PIN_STR);

  @Test
  public void cardCipherPinCmdBuild_update_PIN() {
    CardCipherPinCmdBuild builder =
        new CardCipherPinCmdBuild(SamRevision.C1, KEY_REFERENCE_CIPH_KEY, CURRENT_PIN, NEW_PIN);
    byte[] apduRequestBytes = builder.getApduRequest().getBytes();
    assertThat(apduRequestBytes).isEqualTo(APDU_ISO_CHANGE_PIN);
  }

  @Test
  public void cardCipherPinCmdBuild_verify_PIN() {
    CardCipherPinCmdBuild builder =
        new CardCipherPinCmdBuild(SamRevision.C1, KEY_REFERENCE_CIPH_KEY, CURRENT_PIN, null);
    byte[] apduRequestBytes = builder.getApduRequest().getBytes();
    assertThat(apduRequestBytes).isEqualTo(APDU_ISO_VERIFY_PIN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cardCipherPinCmdBuild_update_PIN_bad_length_1() {
    CardCipherPinCmdBuild builder =
        new CardCipherPinCmdBuild(SamRevision.C1, KEY_REFERENCE_CIPH_KEY, PIN_LENGTH_5, NEW_PIN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cardCipherPinCmdBuild_update_PIN_bad_length_2() {
    CardCipherPinCmdBuild builder =
        new CardCipherPinCmdBuild(
            SamRevision.C1, KEY_REFERENCE_CIPH_KEY, CURRENT_PIN, PIN_LENGTH_5);
  }

  @Test
  public void cardCipherPinCmdBuild_parser() {
    CardCipherPinCmdBuild builder =
        new CardCipherPinCmdBuild(SamRevision.C1, KEY_REFERENCE_CIPH_KEY, CURRENT_PIN, NEW_PIN);
    ApduResponse apduResponse = new ApduResponse(SW1SW2_OK, null);
    assertThat(builder.createResponseParser(apduResponse).getClass())
        .isEqualTo(CardCipherPinRespPars.class);
  }
}

/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
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
import static org.assertj.core.api.Java6Assertions.shouldHaveThrown;

import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.security.DigestUpdateCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestUpdateRespPars;
import org.eclipse.keyple.core.reader.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestUpdateCmdBuildTest {
  private static final boolean ENCRYPTED_SESSION_TRUE = true;
  private static final boolean ENCRYPTED_SESSION_FALSE = false;
  private static final String DIGEST_DATA = "112233445566778899AA";
  private static final String SW1SW2_OK = "9000";
  private static final byte LENGTH_4 = (byte) 0x04;
  private static final String APDU_CLA_80 = "808C00000A" + DIGEST_DATA;
  private static final String APDU_CLA_80_ENCRYPTED_SESSION = "808C00800A" + DIGEST_DATA;
  private static final String APDU_CLA_94 = "948C00000A" + DIGEST_DATA;

  @Test
  public void digestUpdateCmdBuild_defaultRevision_createParser() {

    DigestUpdateCmdBuild digestUpdateCmdBuild =
        new DigestUpdateCmdBuild(null, ENCRYPTED_SESSION_FALSE, ByteArrayUtil.fromHex(DIGEST_DATA));
    assertThat(digestUpdateCmdBuild.getApduRequest().getBytes())
        .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
    DigestUpdateRespPars digestUpdateRespPars =
        digestUpdateCmdBuild.createResponseParser(
            new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
    assertThat(digestUpdateRespPars.getClass()).isEqualTo(DigestUpdateRespPars.class);
  }

  @Test
  public void digestUpdateCmdBuild_cla94() {

    DigestUpdateCmdBuild digestUpdateCmdBuild =
        new DigestUpdateCmdBuild(
            SamRevision.S1D, ENCRYPTED_SESSION_FALSE, ByteArrayUtil.fromHex(DIGEST_DATA));
    assertThat(digestUpdateCmdBuild.getApduRequest().getBytes())
        .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
  }

  @Test
  public void digestUpdateCmdBuild_cla80() {

    DigestUpdateCmdBuild digestUpdateCmdBuild =
        new DigestUpdateCmdBuild(
            SamRevision.C1, ENCRYPTED_SESSION_FALSE, ByteArrayUtil.fromHex(DIGEST_DATA));
    assertThat(digestUpdateCmdBuild.getApduRequest().getBytes())
        .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
  }

  @Test
  public void digestUpdateCmdBuild_cla80_encryptedSession() {

    DigestUpdateCmdBuild digestUpdateCmdBuild =
        new DigestUpdateCmdBuild(
            SamRevision.C1, ENCRYPTED_SESSION_TRUE, ByteArrayUtil.fromHex(DIGEST_DATA));
    assertThat(digestUpdateCmdBuild.getApduRequest().getBytes())
        .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_ENCRYPTED_SESSION));
  }

  @Test(expected = IllegalArgumentException.class)
  public void digestUpdateCmdBuild_digestDataNull() {
    new DigestUpdateCmdBuild(null, ENCRYPTED_SESSION_FALSE, null);
    shouldHaveThrown(IllegalArgumentException.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void digestUpdateCmdBuild_badDigestDataLength() {
    // create digest data > 255 bytes
    String digestData = "";
    while (digestData.length() < (255 * 2)) {
      digestData = digestData + DIGEST_DATA;
    }
    new DigestUpdateCmdBuild(null, ENCRYPTED_SESSION_FALSE, ByteArrayUtil.fromHex(digestData));
    shouldHaveThrown(IllegalArgumentException.class);
  }
}

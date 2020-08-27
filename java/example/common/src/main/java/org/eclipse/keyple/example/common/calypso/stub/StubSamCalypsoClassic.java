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
package org.eclipse.keyple.example.common.calypso.stub;

import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.plugin.stub.StubSecureElement;

/** This class is an example of a Stub SAM */
public final class StubSamCalypsoClassic extends StubSecureElement {

  private static final String SE_PROTOCOL = "PROTOCOL_ISO7816_3";
  private static final String ATR_HEX = "3B3F9600805A0080C120000012345678829000"; // serial number
  // : 12345678

  public StubSamCalypsoClassic() {
    /* Select Diversifier */
    addHexCommand("8014 0000 08 0000000011223344", "9000");
    /* Get Challenge */
    addHexCommand("8084000004", "C1C2C3C49000");
    /* Digest Init */
    addHexCommand(
        "808A00FF27307E0308306C00307E1D24B928480800000606F000120000000000000000000000000000000000",
        "9000");
    /* Digest Init */
    addHexCommand(
        "808A00FF273079030490980030791D01112233445566778899AABBCCDDEEFF00112233445566778899AABBCC",
        "9000");
    /* Digest Update */
    addHexCommand("808C00000500B2014400", "9000");
    /* Digest Update */
    addHexCommand(
        "808C00001F00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC9000", "9000");
    /* Digest Update */
    addHexCommand("808C00000500B201F400", "9000");
    /* Digest Update */
    addHexCommand(
        "808C00001F00000000000000000000000000000000000000000000000000000000009000", "9000");
    /* Digest Update */
    addHexCommand("808C00000500B2014C00", "9000");
    /* Digest Update */
    addHexCommand(
        "808C00001F00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF9000", "9000");
    /* Digest Update */
    addHexCommand(
        "808C00002200E200401D00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC", "9000");
    /* Digest Update */
    addHexCommand("808C0000029000", "9000");
    /* Digest Update */
    addHexCommand("808C00000500B2014D00", "9000");
    /* Digest Update */
    addHexCommand(
        "808C00007E011D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF021D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF031D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF041D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF9000",
        "9000");
    /* Digest Update */
    addHexCommand(
        "808C00002200E200401D01112233445566778899AABBCCDDEEFF00112233445566778899AABBCC", "9000");
    /* Digest Close */
    addHexCommand("808E000004", "050607089000");
    /* Digest Authenticate */
    addHexCommand("808200000401020304", "9000");
  }

  @Override
  public byte[] getATR() {
    return ByteArrayUtil.fromHex(ATR_HEX);
  }

  @Override
  public String getSeProcotol() {
    return SE_PROTOCOL;
  }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.example.pc.calypso.stub.se;


import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * This class is an example of a Stub CSM
 */
public class StubCsmCalypsoBasic extends StubSecureElement {

    final static String seProtocol = "PROTOCOL_ISO7816_3";
    final String ATR_HEX = "3B3F9600805A0080C120000012345678829000";// serial number : 12345678

    public StubCsmCalypsoBasic() {
        /* Select Diversifier */
        addHexCommand("8014 0000 08 0000000011223344", "9000");
        /* Get Challenge */
        addHexCommand("0084000004", "C1C2C3C49000");
        /* Digest Init */
        addHexCommand(
                "808A00FF27307E0308306C00307E1D24B928480800000606F000120000000000000000000000000000000000",
                "9000");
        /* Digest Update */
        addHexCommand("808C00000500B2014400", "9000");
        /* Digest Update */
        addHexCommand("808C00001F00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC9000",
                "9000");
        /* Digest Update */
        addHexCommand("808C00000500B201F400", "9000");
        /* Digest Update */
        addHexCommand("808C00001F00000000000000000000000000000000000000000000000000000000009000",
                "9000");
        /* Digest Update */
        addHexCommand("808C00000500B2014C00", "9000");
        /* Digest Update */
        addHexCommand("808C00001F00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF9000",
                "9000");
        /* Digest Update */
        addHexCommand(
                "808C00002200E200401D00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC",
                "9000");
        /* Digest Update */
        addHexCommand("808C0000029000", "9000");
        /* Digest Close */
        addHexCommand("808E000004", "050607089000");
        /* Digest Authenticate */
        addHexCommand("808200000401020304", "9000");
    }

    @Override
    public byte[] getATR() {
        return ByteArrayUtils.fromHex(ATR_HEX);
    }

    @Override
    public String getSeProcotol() {
        return seProtocol;
    }


}

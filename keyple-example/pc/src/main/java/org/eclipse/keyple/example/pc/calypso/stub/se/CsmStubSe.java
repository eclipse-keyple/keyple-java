/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.pc.calypso.stub.se;

import java.nio.ByteBuffer;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * This class is an example of a Stub CSM
 */
public class CsmStubSe extends StubSecureElement {

    final static String seProtocol = "PROTOCOL_ISO7816_3";
    final String ATR_HEX = "3B3F9600805A0080C120000012345678829000";// serial number : 12345678

    public CsmStubSe() {
        /* Select Diversifier */
        addHexCommand("8014 0000 08 1122334455667788", "9000");
        addHexCommand("9414 0000 08 1122334455667788", "9000");

        /* Get Challenge */
        addHexCommand("0084 0000 04", "11223344 9000");
        addHexCommand("8084 0000 04", "11223344 9000");
        addHexCommand("9484 0000 04", "11223344 9000");

        /* Digest Init */
        addHexCommand(
                "808A 00FF 3A 300E 55667788 00300E30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "9000");
        addHexCommand(
                "948A 00FF 3A 300E 55667788 00300E30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "9000");

        /* Digest Authenticate */
        addHexCommand("8082 0000 04 11223344", "9000");
        addHexCommand("9482 0000 04 11223344", "9000");
        addHexCommand("8082 0000 04 44332211", "9000");
        addHexCommand("9482 0000 04 44332211", "9000");

        /* Digest CLose */
        addHexCommand("808E 0000 04", "88776655 9000");
        addHexCommand("948E 0000 04", "88776655 9000");

        /* Digest update */
        addHexCommand(
                "808C 0000 32 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009000",
                "9000");
        addHexCommand(
                "948C 0000 32 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009000",
                "9000");
        addHexCommand("808C 0000 05 00B201D430", "9000");
        addHexCommand("948C 0000 05 00B201D430", "9000");
        addHexCommand(
                "808C 0000 22 00000000000000000000000000000000000000000000000000000000000000009000",
                "9000");
        addHexCommand(
                "948C 0000 22 00000000000000000000000000000000000000000000000000000000000000009000",
                "9000");
        addHexCommand("808C 0000 05 00B201A420", "9000");
        addHexCommand("948C 0000 05 00B201A420", "9000");
    }

    @Override
    public ByteBuffer getATR() {
        return ByteBufferUtils.fromHex(ATR_HEX);
    }

    @Override
    public String getSeProcotol() {
        return seProtocol;
    }


}

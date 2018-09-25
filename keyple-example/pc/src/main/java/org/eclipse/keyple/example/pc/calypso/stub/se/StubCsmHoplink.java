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
public class StubCsmHoplink extends StubSecureElement {

    final static String seProtocol = "PROTOCOL_ISO7816_3";
    final String ATR_HEX = "3B3F9600805A0080C120000012345678829000";// serial number : 12345678

    public StubCsmHoplink() {
        /* Select Diversifier */
        addHexCommand("801400000800000000C0E11FA1", "9000");
        /* Get Challenge */
        addHexCommand("0084000004", "C1A5E5009000");
        /* Digest Init */
        addHexCommand(
                "808A00FF3A300E03082ED700300E300102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F30",
                "9000");
        /* Digest Update */
        addHexCommand("808C00000500B201A420", "9000");
        /* Digest Update */
        addHexCommand(
                "808C00002200000000000000000000000000000000000000000000000000000000000000009000",
                "9000");
        /* Digest Update */
        addHexCommand("808C00000500B201D430", "9000");
        /* Digest Update */
        addHexCommand(
                "808C0000320102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F309000",
                "9000");
        /* Digest Update */
        addHexCommand("808C00000500B201A420", "9000");
        /* Digest Update */
        addHexCommand(
                "808C00002200000000000000000000000000000000000000000000000000000000000000009000",
                "9000");
        /* Digest Update */
        addHexCommand("808C00000500B201D430", "9000");
        /* Digest Update */
        addHexCommand(
                "808C0000320102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F309000",
                "9000");
        /* Digest Close */
        addHexCommand("808E000004", "C234FA7D9000");
        /* Digest Authenticate */
        addHexCommand("80820000046C8A486C", "9000");
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

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
 * This class is an example of a Stub Implementation of SecureElement. It works with the protocol
 * PROTOCOL_ISO14443_4 and simulates a Calypso PO with an Hoplink application
 */
public class StubHoplink extends StubSecureElement {

    final static String seProtocol = "PROTOCOL_ISO14443_4";
    final String ATR_HEX = "3B8E800180318066409089120802830190000B";

    public StubHoplink() {
        /* Intrinsic Select Application */
        addHexCommand("00A4040005AABBCCDDEE00", "6A82");
        /* Intrinsic Select Application */
        addHexCommand("00A404000AA000000291A00000019100",
                "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA153070A3C230C1410019000");
        /* Read Records */
        addHexCommand("00B201A420",
                "00000000000000000000000000000000000000000000000000000000000000009000");
        /* Open Secure Session V3.1 */
        addHexCommand("008A0BD104C1A5E50000",
                "03082ED700300E300102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F309000");
        /* Read Records */
        addHexCommand("00B201A420",
                "00000000000000000000000000000000000000000000000000000000000000009000");
        /* Read Records */
        addHexCommand("00B201D430",
                "0102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F309000");
        /* Read Records */
        addHexCommand("00B201A420",
                "00000000000000000000000000000000000000000000000000000000000000009000");
        /* Read Records */
        addHexCommand("00B201D430",
                "0102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F309000");
        /* Close Secure Session */
        addHexCommand("008E000004C234FA7D00", "6C8A486C9000");
        /* Read Records */
        addHexCommand("00B201A401", "009000");
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

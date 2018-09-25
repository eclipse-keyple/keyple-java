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
public class StubCalypsoBasic extends StubSecureElement {

    final static String seProtocol = "PROTOCOL_ISO14443_4";
    final String ATR_HEX = "3B8E800180318066409089120802830190000B";

    public StubCalypsoBasic() {
        /* Select fake Application */
        addHexCommand("00A4 0400 05 AABBCCDDEE 00", "6A82");

        /* Select Navigo Application */
        addHexCommand("00A4 0400 08 315449432E494341 00",
                "6F24840A315449432E4943414C54A516BF0C13C70800000000AA4201D45307063C23C0141001 9000");

        /* Read Record */
        addHexCommand("00B2 0144 00",
                "00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC 9000");
        addHexCommand("00B2 01F4 00", "01000000000000000000 9000");
        addHexCommand("00B2 01A4 01",
                "0000000000000000000000000000000000000000000000000000000000 9000");
        addHexCommand("00B2 014C 00",
                "0000000000000000000000000000000000000000000000000000000000 9000");

        /* Append Record */
        addHexCommand("00E200401D00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC",
                "9000");

        /* Open Secure Session V3.1 */
        addHexCommand("008A 0BD104 11223344 00",
                "55667788 00300E30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 9000");
        addHexCommand("008A 0BD104 11223344",
                "55667788 00300E30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 9000");
        addHexCommand("008A 0B3904 11223344 00",
                "55667788 00300E30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 9000");

        /* Close Secure Session */
        addHexCommand("008E 0000 04 88776655 00", "44332211 9000");
        addHexCommand("008E 8000 04 88776655 00", "44332211 9000");
        addHexCommand("008E 0000 04 88776655", "44332211 9000");
        addHexCommand("008E 8000 04 88776655", "44332211 9000");
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

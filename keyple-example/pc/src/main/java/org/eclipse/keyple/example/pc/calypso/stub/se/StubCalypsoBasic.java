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

        /* Intrinsic Select Application */
        addHexCommand("00A4040005AABBCCDDEE00", "6A82");
        /* Intrinsic Select Application */
        addHexCommand("00A404000AA000000404012509010100",
                "6F24840AA0000004040125090101A516BF0C13C70800000000C0E11FA153070A3C23121410019000");
        /* Read Records */
        addHexCommand("00B2014400",
                "00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC9000");
        /* Open Secure Session V3.1 */
        addHexCommand("008A0B3904DFDEB63A00",
                "0308306C00307E1D24B928480800000606F0001200000000000000000000000000000000009000");
        /* Read Records */
        addHexCommand("00B2014400",
                "00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC9000");
        /* Read Records */
        addHexCommand("00B201F400",
                "00000000000000000000000000000000000000000000000000000000009000");
        /* Read Records */
        addHexCommand("00B2014C00",
                "00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF9000");
        /* Append Record */
        addHexCommand("00E200401D00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC",
                "9000");
        /* Close Secure Session */
        addHexCommand("008E0000049E7B8C9100", "492B5BAF9000");
        /* Get Challenge */
        addHexCommand("0084011008", "030830D3762295819000");
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

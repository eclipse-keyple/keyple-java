/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.calypso.pc.stub.se;


import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * This class is an example of a Stub Implementation of SecureElement. It works with the protocol
 * PROTOCOL_ISO14443_4 and simulates a Calypso PO with an Hoplink application
 */
public class StubCalypsoClassic extends StubSecureElement {

    final static String seProtocol = "PROTOCOL_ISO14443_4";
    final String ATR_HEX = "3B8E800180318066409089120802830190000B";

    public StubCalypsoClassic() {
        /* Select Application */
        addHexCommand("00A4 0400 05 AABBCCDDEE 00", "6A82");
        /* Select Application */
        addHexCommand("00A4 0400 0A A0000004040125090101 00",
                "6F24840AA0000004040125090101A516BF0C13C708 0000000011223344 53070A3C23121410019000");
        /* Read Records */
        addHexCommand("00B2014400",
                "00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC9000");
        /* Open Secure Session V3.1 */
        addHexCommand("008A0B3904C1C2C3C400",
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
        /* no ratification asked */
        addHexCommand("008E0000040506070800", "010203049000");
        /* ratification asked */
        addHexCommand("008E8000040506070800", "010203049000");
        /* Ratification */
        addHexCommand("00B2000000", "6B00");
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

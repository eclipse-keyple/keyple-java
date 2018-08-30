/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.stub.calypso;

import java.nio.ByteBuffer;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * This class is an example of a Stub Implementation of SecureElement. It works with the protocol
 * PROTOCOL_ISO14443_4 and simualtes a Calypsi PO with an Hoplink application
 */
public class HoplinkStubSE extends StubSecureElement {


    public HoplinkStubSE() {
        addHexCommand("00A4 0400 05AABBCCDDEE00", "6A82");// Select fake Application
        addHexCommand("00A4 0400 0AA000000404012509010100",
                "6F24 840AA0000004040125090101A516BF0C13C708 1122334455667788 53070A3C2312141001 9000");// Select
                                                                                                        // Navigo
                                                                                                        // Application
        addHexCommand("00A4 0400 0AA000000291A00000019100",
                "6F25840BA000000291A00000019102A516BF0C13C708 1122334455667788 53070A3C230C141001 9000");// Select
                                                                                                         // Hoplink
                                                                                                         // Application
        addHexCommand("00A4 0400 0BA000000291A0000001910200",
                "6F25840BA000000291A00000019102A516BF0C13C708 1122334455667788 53070A3C230C141001 9000");// Select
                                                                                                         // Hoplink
                                                                                                         // Application
        addHexCommand("00B2 01A4 20",
                "0000000000000000000000000000000000000000000000000000000000000000 9000");//// Read
                                                                                         //// Record
        addHexCommand("00B2 01D4 30",
                "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 9000");// Read
                                                                                                                         // Record
        addHexCommand("00B2 01A4 01",
                "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 9000");// Read
                                                                                                                         // Record
        addHexCommand("008A 0BD104 11223344 00",
                "55667788 00300E30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 9000");// Open
                                                                                                                                          // Secure
                                                                                                                                          // Session
                                                                                                                                          // V3.1
        addHexCommand("008A 0BD104 11223344",
                "55667788 00300E30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 9000");// Open
                                                                                                                                          // Secure
                                                                                                                                          // Session
                                                                                                                                          // V3.1
        addHexCommand("008E 0000 04 88776655 00", "44332211 9000");// Close Secure Session
        addHexCommand("008E 8000 04 88776655 00", "44332211 9000");// Close Secure Session
        addHexCommand("008E 0000 04 88776655", "44332211 9000");// Close Secure Session
        addHexCommand("008E 8000 04 88776655", "44332211 9000");// Close Secure Session

    }

    @Override
    public ByteBuffer getATR() {
        return ByteBufferUtils.fromHex("3B 8E 80 01 80 31 80 66 40 90 89 12 08 02 83 01 90 00 0B");
    }

    @Override
    public SeProtocol getSeProcotol() {
        return ContactlessProtocols.PROTOCOL_ISO14443_4;
    }



}

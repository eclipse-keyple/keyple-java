/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.stub.calypso;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * This class is an example of a Stub CSM
 */
public class CSMStubSE extends StubSecureElement {

    final static SeProtocol seProtocol = ContactsProtocols.PROTOCOL_ISO7816_3;
    final String ATR_HEX = "3B3F9600805A0080C120000012345678829000";//serial number : 12345678

    public CSMStubSE() {
        addHexCommand("8014 0000 08 1122334455667788", "9000");//Select Diversifier
        addHexCommand("94140000081122334455667788", "9000");//Select Diversifier
        addHexCommand("8084000004", "11223344 9000");// Get Challenge
        addHexCommand("9484000004", "11223344 9000");// Get Challenge
        addHexCommand("808A00FF3A300E 55667788 00300E30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", "9000");//Digest Init
        addHexCommand("948A00FF3A300E5566778800300E30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", "9000");//Digest Init

        addHexCommand("8082000004 11223344", "9000");//Digest Authenticate
        addHexCommand("9482000004 11223344", "9000");//Digest Authenticate

        addHexCommand("948200000444332211", "9000");//Digest Authenticate ?


        addHexCommand("808E000004", "88776655 9000");//Digest CLose
        addHexCommand("948E000004", "88776655 9000");//Digest CLose

        addHexCommand("808C0000320000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009000", "9000");//Digest update
        addHexCommand("948C0000320000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009000", "9000");//Digest update

        addHexCommand("808C00000500B201D430", "9000");//Digest update
        addHexCommand("948C00000500B201D430", "9000");//Digest update

        addHexCommand("808C00002200000000000000000000000000000000000000000000000000000000000000009000", "9000");//Digest update
        addHexCommand("948C00002200000000000000000000000000000000000000000000000000000000000000009000", "9000");//Digest update

        addHexCommand("808C00000500B201A420", "9000");//Digest update
        addHexCommand("948C00000500B201A420", "9000");//Digest update
    }

    @Override
    public ByteBuffer getATR() {
        return ByteBufferUtils.fromHex(ATR_HEX);
    }



    @Override
    public SeProtocol getSeProcotol() {
        return seProtocol;
    }


}

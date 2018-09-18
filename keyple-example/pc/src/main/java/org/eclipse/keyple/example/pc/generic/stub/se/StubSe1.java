/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.pc.generic.stub.se;

import java.nio.ByteBuffer;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * Simple contact stub SE (no command)
 */
public class StubSe1 extends StubSecureElement {

    final static SeProtocol seProtocol = ContactsProtocols.PROTOCOL_ISO7816_3;
    final String ATR_HEX = "3B3F9600805A0080C120000012345678829000";// serial number : 12345678

    public StubSe1() {}

    @Override
    public ByteBuffer getATR() {
        return ByteBufferUtils.fromHex(ATR_HEX);
    }

    @Override
    public SeProtocol getSeProcotol() {
        return seProtocol;
    }


}

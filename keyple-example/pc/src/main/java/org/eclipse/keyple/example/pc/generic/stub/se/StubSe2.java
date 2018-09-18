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
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * Simple contactless stub SE (no command)
 */
public class StubSe2 extends StubSecureElement {

    final static SeProtocol seProtocol = ContactlessProtocols.PROTOCOL_ISO14443_4;
    final String ATR_HEX = "3B8E800180318066409089120802830190000B";

    public StubSe2() {}

    @Override
    public ByteBuffer getATR() {
        return ByteBufferUtils.fromHex(ATR_HEX);
    }

    @Override
    public SeProtocol getSeProcotol() {
        return seProtocol;
    }
}

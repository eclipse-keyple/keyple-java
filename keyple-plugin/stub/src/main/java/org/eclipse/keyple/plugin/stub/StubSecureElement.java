/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;

import java.nio.ByteBuffer;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;

public interface StubSecureElement {

    ByteBuffer getATR();

    boolean isPhysicalChannelOpen();

    void openPhysicalChannel() throws IOReaderException, ChannelStateReaderException;

    void closePhysicalChannel() throws IOReaderException;

    ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException;

    SeProtocol getSeProcotol();

}

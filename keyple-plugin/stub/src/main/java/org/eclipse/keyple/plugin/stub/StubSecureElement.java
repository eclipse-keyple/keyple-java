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

    /**
     * Getter for ATR
     * 
     * @return Secured Element ATR
     */
    ByteBuffer getATR();

    /**
     * @return True if the channel is open to the SE
     */
    boolean isPhysicalChannelOpen();

    /**
     * Open physicann channel to the SE
     * 
     * @throws IOReaderException
     * @throws ChannelStateReaderException
     */
    void openPhysicalChannel() throws IOReaderException, ChannelStateReaderException;

    /**
     * Close physical channel from the SE
     * 
     * @throws IOReaderException
     */
    void closePhysicalChannel() throws IOReaderException;

    /**
     * Return APDU Response to APDU Request
     * 
     * @param apduIn : commands to be processed
     * @return APDU response
     * @throws ChannelStateReaderException
     */
    ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException;

    /**
     * @return SE protocol supported by the SE
     */
    SeProtocol getSeProcotol();

}

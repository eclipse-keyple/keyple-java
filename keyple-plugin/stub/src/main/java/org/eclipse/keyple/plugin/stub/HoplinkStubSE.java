/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * This class is an example of a Stub Implementation of SecureElement.
 * It works with the protocol PROTOCOL_ISO14443_4 and simualtes a Calypsi PO with an Hoplink application
 */
public class HoplinkStubSE implements StubSecureElement {

    boolean isPhysicalChannelOpen = false;
    Map<String, String> hexCommands = new HashMap<String, String>();
    final static SeProtocol seProtocol = ContactlessProtocols.PROTOCOL_ISO14443_4;
    final String ATR_HEX = "3B 8E 80 01 80 31 80 66 40 90 89 12 08 02 83 01 90 00 0B";

    public HoplinkStubSE() {
        initHexCommands();
    }


    @Override
    public ByteBuffer getATR() {
        return ByteBufferUtils.fromHex(ATR_HEX);
    }

    @Override
    public boolean isPhysicalChannelOpen() {
        return isPhysicalChannelOpen;
    }

    @Override
    public void openPhysicalChannel() throws IOReaderException, ChannelStateReaderException {
        isPhysicalChannelOpen = true;
    }

    @Override
    public void closePhysicalChannel() throws IOReaderException {
        isPhysicalChannelOpen = false;
    }

    @Override
    public ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {

        String hexApdu = ByteBufferUtils.toHex(apduIn);
        if (hexCommands.containsKey(hexApdu)) {
            return ByteBufferUtils.fromHex(hexCommands.get(hexApdu));
        }

        // empty buffer
        return ByteBuffer.allocate(0);
    }

    @Override
    public SeProtocol getSeProcotol() {
        return seProtocol;
    }

    // Init commands_map with some example commands
    private void initHexCommands() {
        hexCommands.put("00A404000AA000000291A00000019100",
                "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000");
        hexCommands.put("00B201A420",
                "00000000000000000000000000000000000000000000000000000000000000009000");
    }

    /**
     * Add more simulated commands to the Stub SE
     * @param command : hexadecimal command to react to
     * @param response : hexadecimal response to be sent in reaction to command
     */
    public void addHexCommand(String command, String response) {
        assert command != null && response != null : "command and response should not be null";
        hexCommands.put(command, response);
    }

    /**
     * Remove simulated commands from the Stub SE
     * @param command : hexadecimal command to be removed
     */
    public void removeHexCommand(String command) {
        assert command != null : "command should not be null";
        hexCommands.remove(command);
    }
}

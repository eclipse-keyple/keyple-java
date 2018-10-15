/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.plugin.stub;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.util.ByteArrayUtils;

public abstract class StubSecureElement {

    /**
     * Getter for ATR
     * 
     * @return Secured Element ATR
     */
    public abstract byte[] getATR();


    boolean isPhysicalChannelOpen = false;

    public boolean isPhysicalChannelOpen() {
        return isPhysicalChannelOpen;
    }

    public void openPhysicalChannel() throws KeypleChannelStateException {
        isPhysicalChannelOpen = true;
    }

    public void closePhysicalChannel() throws KeypleChannelStateException {
        isPhysicalChannelOpen = false;
    }


    /**
     * @return SE protocol supported by the SE
     */
    public abstract String getSeProcotol();


    Map<String, String> hexCommands = new HashMap<String, String>();

    /**
     * Add more simulated commands to the Stub SE
     *
     * @param command : hexadecimal command to react to
     * @param response : hexadecimal response to be sent in reaction to command
     */
    public void addHexCommand(String command, String response) {
        assert command != null && response != null : "command and response should not be null";
        // add commands without space
        hexCommands.put(command.replace(" ", ""), response.replace(" ", ""));
    }

    /**
     * Remove simulated commands from the Stub SE
     *
     * @param command : hexadecimal command to be removed
     */
    public void removeHexCommand(String command) {
        assert command != null : "command should not be null";
        hexCommands.remove(command.trim());
    }

    /**
     * Return APDU Response to APDU Request
     *
     * @param apduIn : commands to be processed
     * @return APDU response
     * @throws KeypleIOReaderException if the transmission fails
     */
    public byte[] processApdu(byte[] apduIn) throws KeypleIOReaderException {

        if (apduIn == null) {
            return null;
        }

        // convert apduIn to hexa
        String hexApdu = ByteArrayUtils.toHex(apduIn);

        // return matching hexa response if found
        if (hexCommands.containsKey(hexApdu)) {
            return ByteArrayUtils.fromHex(hexCommands.get(hexApdu));
        }

        // throw a KeypleIOReaderException if not found
        throw new KeypleIOReaderException("No response available for this request.");
    }
}

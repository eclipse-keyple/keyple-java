/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy.plugin.mock;

import java.util.Map;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.plugin.AbstractLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.SmartSelectionReader;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

public class BlankSmartSelectionReader extends AbstractLocalReader implements SmartSelectionReader {


    public BlankSmartSelectionReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    @Override
    public boolean checkSePresence() throws NoStackTraceThrowable {
        return false;
    }

    @Override
    public byte[] getATR() {
        return new byte[0];
    }

    @Override
    public void openPhysicalChannel() throws KeypleChannelControlException {

    }

    @Override
    public void closePhysicalChannel() throws KeypleChannelControlException {

    }

    @Override
    public boolean isPhysicalChannelOpen() {
        return false;
    }

    @Override
    public boolean protocolFlagMatches(SeProtocol protocolFlag) throws KeypleReaderException {
        return false;
    }

    @Override
    public byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        return new byte[0];
    }

    @Override
    public ApduResponse openChannelForAid(SeSelector.AidSelector aidSelector)
            throws KeypleIOReaderException, KeypleChannelControlException,
            KeypleApplicationSelectionException {
        return null;
    }

    @Override
    public TransmissionMode getTransmissionMode() {
        return null;
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value)
            throws IllegalArgumentException, KeypleBaseException {

    }
}

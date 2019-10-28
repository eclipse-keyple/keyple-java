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
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.Map;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

public class BlankAbstractLocalReader extends AbstractLocalReader {


    BlankAbstractLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    @Override
    protected boolean checkSePresence() throws NoStackTraceThrowable {
        return false;
    }

    @Override
    protected byte[] getATR() {
        return new byte[0];
    }

    @Override
    protected void openPhysicalChannel() throws KeypleChannelControlException {

    }

    @Override
    protected void closePhysicalChannel() throws KeypleChannelControlException {

    }

    @Override
    protected boolean isPhysicalChannelOpen() {
        return false;
    }

    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) throws KeypleReaderException {
        return false;
    }

    @Override
    protected byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        return new byte[0];
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

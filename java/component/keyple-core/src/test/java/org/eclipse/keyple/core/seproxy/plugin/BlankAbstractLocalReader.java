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
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

public class BlankAbstractLocalReader extends AbstractLocalReader {


    public BlankAbstractLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    @Override
    public boolean checkSePresence() {
        return false;
    }

    @Override
    public byte[] getATR() {
        return new byte[0];
    }

    @Override
    public void openPhysicalChannel() {

    }

    @Override
    public void closePhysicalChannel() {

    }

    @Override
    public boolean isPhysicalChannelOpen() {
        return false;
    }

    @Override
    public boolean protocolFlagMatches(SeProtocol protocolFlag) {
        return false;
    }

    @Override
    public byte[] transmitApdu(byte[] apduIn) {
        return new byte[0];
    }

    @Override
    void terminateSeCommunication() {

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
    public void setParameter(String key, String value) {

    }
}

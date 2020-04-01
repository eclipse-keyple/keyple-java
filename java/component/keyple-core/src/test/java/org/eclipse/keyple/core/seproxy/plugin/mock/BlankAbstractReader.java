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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.plugin.AbstractReader;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

/**
 * A blank class extending AbstractReader only purpose is to be tested and spied by mockito
 */
public class BlankAbstractReader extends AbstractReader {

    public BlankAbstractReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    @Override
    protected List<SeResponse> processSeRequestSet(Set<SeRequest> requestSet,
            MultiSeRequestProcessing multiSeRequestProcessing, ChannelControl channelControl)
            throws KeypleReaderException {
        return null;
    }

    @Override
    protected SeResponse processSeRequest(SeRequest seRequest, ChannelControl channelControl)
            throws KeypleReaderException {
        return null;
    }

    @Override
    public boolean isSePresent() {
        return false;
    }

    @Override
    public void addSeProtocolSetting(SeProtocol seProtocol, String protocolRule) {

    }

    @Override
    public void setSeProtocolSetting(Map<SeProtocol, String> protocolSetting) {

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
            throws IllegalArgumentException, KeypleException {

    }
}

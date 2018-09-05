/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.local;

import java.io.IOException;
import java.util.Map;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclise.keyple.example.remote.server.transport.ClientListener;
import org.eclise.keyple.example.remote.server.transport.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServerConnection implements ServerConnection {

    private static final Logger logger = LoggerFactory.getLogger(LocalServerConnection.class);

    ClientListener client;

    public LocalServerConnection() {
        logger.info("Constructor empty");
    }

    public void setClientListener(ClientListener _client) {
        logger.info("Constructor with client listener {}", client);
        client = _client;
    }

    public ClientListener getClientListener() {
        return client;
    }


    @Override
    public String getName() {
        logger.debug("getName");
        return client.onGetName();
    }

    @Override
    public boolean isSePresent() throws NoStackTraceThrowable {
        logger.debug("isSePresent");
        return client.onIsSePresent();
    }

    @Override
    public SeResponseSet transmit(SeRequestSet seApplicationRequest) throws IOReaderException {
        logger.debug("transmit {}", seApplicationRequest);
        return client.onTransmit(seApplicationRequest);
    }

    @Override
    public void addSeProtocolSetting(SeProtocolSetting seProtocolSetting) {
        logger.debug("addSeProtocolSetting {}", seProtocolSetting);
        client.onAddSeProtocolSetting(seProtocolSetting);
    }



    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IOException {

    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IOException {

    }

    @Override
    public int compareTo(ProxyReader o) {
        return 0;
    }
}

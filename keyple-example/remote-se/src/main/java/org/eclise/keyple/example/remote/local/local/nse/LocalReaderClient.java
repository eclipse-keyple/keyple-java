/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.local.local.nse;

import org.eclipse.keyple.plugin.remote_se.nse.NseAPI;
import org.eclipse.keyple.plugin.remote_se.rse.IReaderSyncSession;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalReaderClient implements IReaderSyncSession {

    private static final Logger logger = LoggerFactory.getLogger(LocalReaderClient.class);

    String sessionId;

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public Boolean isAsync() {
        return null;
    }

    NseAPI client;

    public LocalReaderClient() {
        logger.info("Constructor empty");
    }

    public void setClientListener(NseAPI _client) {
        logger.info("Constructor with nse listener {}", client);
        client = _client;
    }

    public NseAPI getClientListener() {
        return client;
    }


    @Override
    public SeResponseSet transmit(SeRequestSet seApplicationRequest) {
        return null;
    }
}

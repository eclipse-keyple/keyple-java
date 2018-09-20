/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.local.local.rse;


import java.util.Map;
import org.eclipse.keyple.plugin.remote_se.nse.RseClient;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclise.keyple.example.remote.local.local.RseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalRseClient implements RseClient {

    private static final Logger logger = LoggerFactory.getLogger(LocalRseClient.class);

    RseAPI rseAPI;

    public LocalRseClient(RseAPI _Sync_rseAPI) {
        logger.info("LocalRseClient contructor {}", _Sync_rseAPI);
        rseAPI = _Sync_rseAPI;
    }


    @Override
    public String connectReader(ProxyReader localReader, Map<String, Object> options) {
        logger.info("connectReader {}", localReader);
        return rseAPI.onReaderConnect(localReader.getName(), null);
    }

    @Override
    public void disconnectReader(ProxyReader localReader) {
        // not implemented
    }

    @Override
    public void update(ReaderEvent event) {
        logger.info("publishReaderEvent {}", event);
        rseAPI.onRemoteReaderEvent(event, null);
    }
}

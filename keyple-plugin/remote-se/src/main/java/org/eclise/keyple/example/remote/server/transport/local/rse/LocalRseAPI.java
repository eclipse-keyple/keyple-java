/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.local.rse;

import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclise.keyple.example.remote.server.RsePlugin;
import org.eclise.keyple.example.remote.server.transport.RseAPI;
import org.eclise.keyple.example.remote.server.transport.local.LocalServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LocalRseAPI implements RseAPI {


    private static final Logger logger = LoggerFactory.getLogger(LocalRseAPI.class);

    RsePlugin plugin;

    public LocalRseAPI() {
        logger.debug("LocalRseAPI constructor");
    }

    public void setPlugin(RsePlugin plugin) {
        logger.debug("setPlugin {}", plugin);
        this.plugin = plugin;
    }

    @Override
    public String onReaderConnect(String readerName, Map<String, Object> options) {
        logger.debug("onReaderConnect {}", readerName);
        logger.info("A Remote Reader is attempting a connection to the rse");
        return plugin.connectRemoteReader(readerName,
                LocalServer.getInstance().getServerSession());

    }

    @Override
    public String onReaderDisconnect(String readerName) {
        //todo
        return null;
    }

    @Override
    public void onRemoteReaderEvent(ReaderEvent event) {
        logger.debug("onRemoteReaderEvent {}", event);
        plugin.onReaderEvent(event, null);// no need for session in local
    }
}

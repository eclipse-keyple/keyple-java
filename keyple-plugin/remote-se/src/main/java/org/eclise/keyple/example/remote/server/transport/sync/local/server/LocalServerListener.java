/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.sync.local.server;

import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclise.keyple.example.remote.server.RSEPlugin;
import org.eclise.keyple.example.remote.server.transport.sync.SyncServerListener;
import org.eclise.keyple.example.remote.server.transport.sync.local.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServerListener implements SyncServerListener {


    private static final Logger logger = LoggerFactory.getLogger(LocalServerListener.class);

    RSEPlugin plugin;

    public LocalServerListener() {
        logger.debug("LocalServerListener constructor");
    }

    public void setPlugin(RSEPlugin plugin) {
        logger.debug("setPlugin {}", plugin);
        this.plugin = plugin;
    }

    @Override
    public String onReaderConnect(String readerName) {
        logger.debug("onReaderConnect {}", readerName);
        logger.info("A Remote Reader is attempting a connection to the server");
        return plugin.connectRemoteReader(readerName, SessionFactory.getInstance().getServerSession());

    }

    @Override
    public void onRemoteReaderEvent(ReaderEvent event) {
        logger.debug("onRemoteReaderEvent {}", event);
        plugin.onReaderEvent(event, null);//no need for session in local
    }
}

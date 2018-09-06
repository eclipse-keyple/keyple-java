/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.local;

import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclise.keyple.example.remote.server.RemoteSePlugin;
import org.eclise.keyple.example.remote.server.transport.ServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServerListener implements ServerListener {


    private static final Logger logger = LoggerFactory.getLogger(LocalServerListener.class);

    RemoteSePlugin plugin;


    public LocalServerListener() {
        logger.debug("LocalServerListener constructor");
    }

    public void setPlugin(RemoteSePlugin plugin) {
        logger.debug("setPlugin {}", plugin);
        this.plugin = plugin;
    }

    @Override
    public void onReaderConnect(String readerName) {
        logger.debug("onReaderConnect {}", readerName);
        logger.info("A Remote Reader is attempting a connection to the server");
        plugin.onRemoteReaderConnect(readerName, LocalServer.getInstance().getServerConnection());

    }

    @Override
    public void onRemoteReaderEvent(ReaderEvent event) {
        logger.debug("onRemoteReaderEvent {}", event);
        plugin.onReaderEvent(event);
    }
}

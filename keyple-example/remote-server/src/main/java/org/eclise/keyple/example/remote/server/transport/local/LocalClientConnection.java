/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.local;


import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclise.keyple.example.remote.server.transport.ClientConnection;
import org.eclise.keyple.example.remote.server.transport.ServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalClientConnection implements ClientConnection {

    private static final Logger logger = LoggerFactory.getLogger(LocalClientConnection.class);

    ServerListener serverListener;

    public LocalClientConnection(ServerListener _serverListener) {
        logger.info("LocalClientConnection contructor {}", _serverListener);
        serverListener = _serverListener;
    }

    @Override
    public void connectReader(ProxyReader localReader) {
        logger.info("connectReader {}", localReader);
        serverListener.onReaderConnect(localReader.getName());
    }

    @Override
    public void update(ReaderEvent event) {
        logger.info("publishReaderEvent {}", event);
        serverListener.onRemoteReaderEvent(event);
    }
}

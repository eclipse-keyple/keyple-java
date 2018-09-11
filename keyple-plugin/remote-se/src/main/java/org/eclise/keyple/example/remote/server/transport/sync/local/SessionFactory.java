/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.sync.local;

import java.net.UnknownHostException;
import org.eclise.keyple.example.remote.server.transport.*;
import org.eclise.keyple.example.remote.server.transport.sync.SyncClientListener;
import org.eclise.keyple.example.remote.server.transport.sync.SyncServerListener;
import org.eclise.keyple.example.remote.server.transport.sync.local.client.LocalClientConnection;
import org.eclise.keyple.example.remote.server.transport.sync.local.server.LocalServerListener;
import org.eclise.keyple.example.remote.server.transport.sync.local.server.LocalServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionFactory {

    private static final Logger logger = LoggerFactory.getLogger(SessionFactory.class);

    static SessionFactory uniqueInstance = new SessionFactory();

    // ServerConnection must be initialized before the ClientConnection
    RSEReaderSession RSEReaderSession;
    SyncServerListener syncServerListener;

    RSEClient RSEClient;

    private SessionFactory() {
        // public contrusction singleton
    }

    public static SessionFactory getInstance() {
        return uniqueInstance;
    }


    public SyncServerListener initServerListener() {
        if (syncServerListener == null) {
            syncServerListener = new LocalServerListener();
            RSEReaderSession = new LocalServerSession(); // prepare transport for client
        }
        return syncServerListener;
    }

    public RSEReaderSession getServerSession() {
        return RSEReaderSession;
    }


    public RSEClient getConnection(SyncClientListener clientlistener) throws UnknownHostException {
        logger.debug("getConnection {}", clientlistener);
        if (syncServerListener == null) {
            logger.error("Server has not been initialized");
            throw new UnknownHostException("Server has not been initialized");
        } else {
            if (RSEClient == null) {
                logger.info("Init duplex connection");
                // link transport with listener in duplex (client-server and server-client)
                ((LocalServerSession) RSEReaderSession).setClientListener(clientlistener);
                RSEClient = new LocalClientConnection(syncServerListener);
            } else {
                logger.debug("Duplex connection already created");
            }
            logger.debug("returning client transport {}", RSEClient);
            return RSEClient;
        }
    }


}

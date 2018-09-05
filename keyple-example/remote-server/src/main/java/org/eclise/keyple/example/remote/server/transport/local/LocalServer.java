/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.local;

import java.net.UnknownHostException;
import org.eclise.keyple.example.remote.server.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServer implements TransportFactory {

    private static final Logger logger = LoggerFactory.getLogger(LocalServer.class);

    static LocalServer uniqueInstance = new LocalServer();

    // ServerConnection must be initialized before the ClientConnection
    ServerConnection serverConnection;
    ServerListener serverListener;

    ClientConnection clientConnection;

    private LocalServer() {
        // public contrusction singleton
    }

    public static LocalServer getInstance() {
        return uniqueInstance;
    }


    @Override
    public ServerListener initServerListener() {
        if (serverListener == null) {
            serverListener = new LocalServerListener();
            serverConnection = new LocalServerConnection(); // prepare transport for client
        }
        return serverListener;
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }



    @Override
    public ClientConnection getConnection(ClientListener clientlistener)
            throws UnknownHostException {
        logger.debug("getConnection {}", clientlistener);
        if (serverListener == null) {
            logger.error("Server has not been initialized");
            throw new UnknownHostException("Server has not been initialized");
        } else {
            if (clientConnection == null) {
                logger.info("Init duplex connection");
                // link transport with listener in duplex (client-server and server-client)
                ((LocalServerConnection) serverConnection).setClientListener(clientlistener);
                clientConnection = new LocalClientConnection(serverListener);
            } else {
                logger.debug("Duplex connection already created");
            }
            logger.debug("returning client transport {}", clientConnection);
            return clientConnection;
        }
    }


}

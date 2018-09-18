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
import org.eclise.keyple.example.remote.server.transport.NseAPI;
import org.eclise.keyple.example.remote.server.transport.RseAPI;
import org.eclise.keyple.example.remote.server.transport.local.rse.LocalRseAPI;
import org.eclise.keyple.example.remote.server.transport.local.rse.LocalRseClient;
import org.eclise.keyple.example.remote.server.transport.local.nse.LocalNseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServer {

    private static final Logger logger = LoggerFactory.getLogger(LocalServer.class);

    static LocalServer uniqueInstance = new LocalServer();

    // ServerConnection must be initialized before the ClientConnection
    RseNseSession RSEReaderSession;
    RseAPI syncRseAPI;

    RseClient rseClient;

    private LocalServer() {
        // public contrusction singleton
    }

    public static LocalServer getInstance() {
        return uniqueInstance;
    }


    public RseAPI initServerListener() {
        if (syncRseAPI == null) {
            syncRseAPI = new LocalRseAPI();
            RSEReaderSession = new LocalNseClient(); // prepare transport for nse
        }
        return syncRseAPI;
    }

    public RseNseSession getServerSession() {
        return RSEReaderSession;
    }


    public RseClient getConnection(NseAPI clientlistener) throws UnknownHostException {
        logger.debug("getConnection {}", clientlistener);
        if (syncRseAPI == null) {
            logger.error("Server has not been initialized");
            throw new UnknownHostException("Server has not been initialized");
        } else {
            if (rseClient == null) {
                logger.info("Init duplex connection");
                // link transport with listener in duplex (nse-rse and rse-nse)
                ((LocalNseClient) RSEReaderSession).setClientListener(clientlistener);
                rseClient = new LocalRseClient(syncRseAPI);
            } else {
                logger.debug("Duplex connection already created");
            }
            logger.debug("returning nse transport {}", rseClient);
            return rseClient;
        }
    }


}

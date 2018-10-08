/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.wspolling;

import java.io.IOException;
import org.eclipse.keyple.example.remote.common.ClientNode;
import org.eclipse.keyple.example.remote.common.ServerNode;
import org.eclipse.keyple.example.remote.common.TransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsPollingFactory extends TransportFactory {

    Boolean localhost = true;
    Integer port = 8002;
    String pollingUrl = "/polling";
    String keypleUrl = "/keypleDTO";
    String nodeId = "local1";
    String bindUrl = "0.0.0.0";
    String protocol = "http://";

    private static final Logger logger = LoggerFactory.getLogger(WsPollingFactory.class);


    @Override
    public ClientNode getClient(Boolean isMaster) {

        logger.info("*** Create Ws Polling Client ***");

        WsPClient client = new WsPClient(protocol + "localhost:" + port + keypleUrl,
                protocol + "localhost:" + port + pollingUrl, nodeId);
        // client.startPollingWorker(nodeId);
        return client;
    }

    @Override
    public ServerNode getServer(Boolean isMaster) throws IOException {
        if (localhost) {
            bindUrl = "0.0.0.0";
        }

        logger.info("*** Create Ws Polling Server ***");

        return new WsPServer(bindUrl, port, keypleUrl, pollingUrl);

    }
}

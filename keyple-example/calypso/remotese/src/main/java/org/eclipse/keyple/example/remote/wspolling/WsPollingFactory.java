/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.remote.wspolling;

import java.io.IOException;
import java.util.Random;
import org.eclipse.keyple.example.remote.transport.ClientNode;
import org.eclipse.keyple.example.remote.transport.ServerNode;
import org.eclipse.keyple.example.remote.transport.TransportFactory;
import org.eclipse.keyple.example.remote.wspolling.client.WsPClient;
import org.eclipse.keyple.example.remote.wspolling.server.WsPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web service factory, get {@link WsPClient} and {@link WsPServer}
 */
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class WsPollingFactory extends TransportFactory {

    // default values
    private Integer port = 8000 + new Random().nextInt((100) + 1);
    private String pollingUrl = "/polling";
    private String keypleUrl = "/keypleDTO";
    private String clientNodeId = "local1";
    private String bindUrl = "0.0.0.0";
    private String protocol = "http://";

    private static final Logger logger = LoggerFactory.getLogger(WsPollingFactory.class);

    public WsPollingFactory() {}

    private WsPollingFactory(Integer port, String pollingUrl, String keypleUrl, String clientNodeId,
            String bindUrl, String protocol) {
        this.port = port;
        this.pollingUrl = pollingUrl;
        this.keypleUrl = keypleUrl;
        this.clientNodeId = clientNodeId;
        this.bindUrl = bindUrl;
        this.protocol = protocol;
    }

    @Override
    public ClientNode getClient(Boolean isMaster) {
        logger.info("*** Create Ws Polling Client ***");
        return new WsPClient(protocol + bindUrl + ":" + port, keypleUrl, pollingUrl, clientNodeId);
    }


    @Override
    public ServerNode getServer(Boolean isMaster) throws IOException {

        logger.info("*** Create Ws Polling Server ***");
        return new WsPServer(bindUrl, port, keypleUrl, pollingUrl, clientNodeId + "server");

    }
}

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
package org.eclipse.keyple.example.remote.transport.wspolling.client;

import java.io.IOException;
import java.util.Random;
import org.eclipse.keyple.example.remote.transport.wspolling.server.WsPServer;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
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
    private String bindUrl = "0.0.0.0";
    private String protocol = "http://";
    private String serverNodeId;
    private static final Logger logger = LoggerFactory.getLogger(WsPollingFactory.class);

    public WsPollingFactory(String serverNodeId) {
        this.serverNodeId = serverNodeId;
    }

    private WsPollingFactory(Integer port, String pollingUrl, String keypleUrl, String bindUrl,
            String protocol, String serverNodeId) {
        if (port != null) {
            this.port = port;
        }
        if (pollingUrl != null) {
            this.pollingUrl = pollingUrl;
        }
        if (keypleUrl != null) {
            this.keypleUrl = keypleUrl;
        }
        if (bindUrl != null) {
            this.bindUrl = bindUrl;
        }
        if (protocol != null) {
            this.protocol = protocol;
        }
        this.serverNodeId = serverNodeId;
    }

    @Override
    public ClientNode getClient(String clientNodeId) {
        logger.info("*** Create Ws Polling Client ***");
        return new WsPClient(protocol + bindUrl + ":" + port, keypleUrl, pollingUrl, clientNodeId,
                serverNodeId);
    }


    @Override
    public ServerNode getServer() {

        logger.info("*** Create Ws Polling Server ***");
        try {
            return new WsPServer(bindUrl, port, keypleUrl, pollingUrl, serverNodeId);
        } catch (IOException e) {
            return null;
        }

    }

    @Override
    public String getServerNodeId() {
        return serverNodeId;
    }
}

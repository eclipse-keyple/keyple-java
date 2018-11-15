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
package org.eclipse.keyple.example.remote.wspolling.client_retrofit;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import org.eclipse.keyple.example.remote.transport.ClientNode;
import org.eclipse.keyple.example.remote.transport.ServerNode;
import org.eclipse.keyple.example.remote.transport.TransportFactory;
import org.eclipse.keyple.example.remote.wspolling.server.WsPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web service factory, get @{@link WsPRetrofitClientImpl} and {@link WsPServer} Optimized for
 * Android and Java
 */
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class WsPollingRetrofitFactory extends TransportFactory {

    // default values
    final private String pollingUrl = "/polling";
    final private String keypleUrl = "/keypleDTO";
    private Integer port = 8000 + new Random().nextInt((100) + 1);
    private String clientNodeId = "local1";
    private String hostname = "0.0.0.0";
    private String protocol = "http://";

    private static final Logger logger = LoggerFactory.getLogger(WsPollingRetrofitFactory.class);

    public WsPollingRetrofitFactory() {}

    private WsPollingRetrofitFactory(Integer port, String clientNodeId, String hostname,
            String protocol) {
        this.port = port;
        this.clientNodeId = clientNodeId;
        this.hostname = hostname;
        this.protocol = protocol;
    }

    private WsPollingRetrofitFactory(Properties serverProp, String clientNodeId) {
        if (serverProp.containsKey("server.port")) {
            this.port = Integer.decode(serverProp.getProperty("server.port"));
        }
        if (serverProp.containsKey("server.hostname")) {
            this.hostname = serverProp.getProperty("server.hostname");
        }

        if (serverProp.containsKey("server.protocol")) {
            this.protocol = serverProp.getProperty("server.protocol") + "://";
        }

        this.clientNodeId = clientNodeId;
    }

    @Override
    public ClientNode getClient(Boolean isMaster) {

        logger.info("*** Create RETROFIT Ws Polling Client ***");
        return new WsPRetrofitClientImpl(protocol + hostname + ":" + port, clientNodeId);
    }



    @Override
    public ServerNode getServer(Boolean isMaster) throws IOException {

        logger.info("*** Create Ws Polling Server ***");
        return new WsPServer(hostname, port, keypleUrl, pollingUrl, clientNodeId + "server");

    }
}

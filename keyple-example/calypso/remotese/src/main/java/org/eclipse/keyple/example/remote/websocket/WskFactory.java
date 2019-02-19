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
package org.eclipse.keyple.example.remote.websocket;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import org.eclipse.keyple.example.remote.transport.ClientNode;
import org.eclipse.keyple.example.remote.transport.ServerNode;
import org.eclipse.keyple.example.remote.transport.TransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web socket factory, by default works at localhost
 */
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class WskFactory extends TransportFactory {

    final private Integer port = 8000 + new Random().nextInt((100) + 1);
    final private String keypleUrl = "/keypleDTO";
    final private String bindUrl = "0.0.0.0";
    final private String protocol = "http://";
    final private String clientNodeId = "local1";

    private static final Logger logger = LoggerFactory.getLogger(WskFactory.class);


    @Override
    public ClientNode getClient(Boolean isMaster) {

        logger.info("*** Create Websocket Client ***");


        ClientNode wskClient;
        try {
            wskClient = new WskClient(new URI(protocol + "localhost:" + port + keypleUrl),
                    clientNodeId);
            // wskClient.connectAReader();
            return wskClient;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public ServerNode getServer(Boolean isMaster) throws IOException {

        logger.info("*** Create Websocket Server ***");

        InetSocketAddress inet = new InetSocketAddress(Inet4Address.getByName(bindUrl), port);
        return new WskServer(inet, !isMaster, clientNodeId + "server");

    }
}

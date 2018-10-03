/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.ws;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WsServer implements TransportNode {

    private InetSocketAddress inet;
    private String endpoint;
    private HttpServer server;
    static private Integer MAX_CONNECTION = 5;
    private KeypleDTOEndpoint keypleDTOEndpoint;

    private static final Logger logger = LoggerFactory.getLogger(WsServer.class);


    public WsServer(String url, Integer port, String endpoint) throws IOException {
        logger.info("Init Web Service Master on url : {}:{}", url, port);

        // Create Endpoints for plugin and reader API
        keypleDTOEndpoint = new KeypleDTOEndpoint();
        // ReaderEndpoint readerEndpoint = new ReaderEndpoint();

        // deploy endpoint
        this.inet = new InetSocketAddress(Inet4Address.getByName(url), port);
        this.endpoint = endpoint;
        server = HttpServer.create(inet, MAX_CONNECTION);
        server.createContext(endpoint, keypleDTOEndpoint);

        // start rse
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // creates a
                                                                                  // default
                                                                                  // executor
    }

    public void start() {
        logger.info("Starting Master on http://{}:{}{}", inet.getHostName(), inet.getPort(),
                endpoint);
        server.start();
    }

    /*
     * TransportNode
     */
    @Override
    public void setDtoDispatcher(DtoDispatcher receiver) {
        this.keypleDTOEndpoint.setDtoDispatcher(receiver);;
    }

    @Override
    public void sendDTO(TransportDTO message) {
        this.keypleDTOEndpoint.sendDTO(message);
    }

    @Override
    public void sendDTO(KeypleDTO message) {

    }

    @Override
    public void update(KeypleDTO event) {
        // not in used in ws

    }


    private class KeypleDTOEndpoint implements HttpHandler, TransportNode {

        private final Logger logger = LoggerFactory.getLogger(KeypleDTOEndpoint.class);

        DtoDispatcher dtoDispatcher;

        @Override
        public void handle(HttpExchange t) throws IOException {

            logger.trace("Incoming HttpExchange {} ", t.toString());
            logger.trace("Incoming Request {} ", t.getRequestMethod());
            String requestMethod = t.getRequestMethod();

            if (requestMethod.equals("POST")) {
                String body = HttpHelper.parseBodyToString(t.getRequestBody());// .. parse the
                                                                               // request body
                KeypleDTO incoming = KeypleDTOHelper.fromJson(body);
                TransportDTO transportDTO = new WsTransportDTO(incoming, t);

                logger.trace("Incoming DTO {} ", KeypleDTOHelper.toJson(incoming));
                TransportDTO outcoming = dtoDispatcher.onDTO(transportDTO);

                HttpHelper.setHttpResponse(t, outcoming.getKeypleDTO());

            }
        }

        /*
         * TransportNode
         */
        @Override
        public void setDtoDispatcher(DtoDispatcher receiver) {
            this.dtoDispatcher = receiver;
        }


        @Override
        public void sendDTO(TransportDTO message) {
            logger.warn("Send DTO can not be used in Web Service Server");
            // not in use, oneway communication, server do not send message
        }

        @Override
        public void sendDTO(KeypleDTO message) {

            logger.warn("Send DTO can not be used in Web Service Server");
        }




        @Override
        public void update(KeypleDTO event) {
            // not in used in ws
        }
    }

}

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
package org.eclipse.keyple.example.remote.transport.wspolling.server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WsPServer implements ServerNode {


    private InetSocketAddress inet;
    final private String apiUrl;
    final private String pollingUrl;
    final private String serverNodeId;
    final private HttpServer server;
    final private Integer MAX_CONNECTION = 10;
    final private HttpHandler keypleDTOEndpoint;
    final private HttpHandler pollingEndpoint;

    private static final Logger logger = LoggerFactory.getLogger(WsPServer.class);

    private PublishQueueManager publishQueueManager = new PublishQueueManager();
    // private PublishQueue<KeypleDto> keypleDtoQueue = new PublishQueue<KeypleDto>();

    /*
     * private final BlockingQueue<HttpExchange> requestQueue = new
     * LinkedBlockingQueue<HttpExchange>();
     */

    /**
     * Constructor
     * 
     * @param url : hostname url to bind server
     * @param port : port to bind server to
     * @param apiUrl : api URL to deploy keypleDto listener endpoint
     * @param pollingUrl : polling URL to deploy polling listener endpoint
     * @throws IOException : if server can not bind given parameters
     */
    public WsPServer(String url, Integer port, String apiUrl, String pollingUrl,
            String serverNodeId) throws IOException {
        logger.info("Init Web Service DemoMaster on url : {}:{}", url, port);

        this.serverNodeId = serverNodeId;
        this.apiUrl = apiUrl;
        this.pollingUrl = pollingUrl;

        // Create Endpoint for polling DTO
        // pollingEndpoint = new EndpointPolling(requestQueue, nodeId);
        pollingEndpoint = new EndpointPolling(publishQueueManager, serverNodeId);

        // Create Endpoint for sending DTO
        keypleDTOEndpoint = new EndpointKeypleDTO((DtoSender) pollingEndpoint, serverNodeId);



        // deploy endpoint
        this.inet = new InetSocketAddress(Inet4Address.getByName(url), port);
        server = HttpServer.create(inet, MAX_CONNECTION);
        server.createContext(apiUrl, keypleDTOEndpoint);
        server.createContext(pollingUrl, pollingEndpoint);

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // creates a
                                                                                  // default
                                                                                  // executor
    }

    public void start() {
        logger.info("Starting Http Web Polling Server on http://{}:{}", inet.getHostName(),
                inet.getPort());
        logger.info("Keyple Endpoint {}", this.apiUrl);
        logger.info("Polling Endpoint {}", this.pollingUrl);
        server.start();
    }

    /*
     * DtoNode
     */
    @Override
    public void setDtoHandler(DtoHandler receiver) {
        ((EndpointKeypleDTO) this.keypleDTOEndpoint).setDtoHandler(receiver);
    }

    @Override
    public String getNodeId() {
        return serverNodeId;
    }

    @Override
    public void sendDTO(TransportDto message) {
        ((EndpointPolling) this.pollingEndpoint).sendDTO(message.getKeypleDTO());
    }

    @Override
    public void sendDTO(KeypleDto message) {
        ((EndpointPolling) this.pollingEndpoint).sendDTO(message); // TODO to which poller should we
                                                                   // send the message?!
    }

    public HttpServer getHttpServer() {
        return server;
    }



}

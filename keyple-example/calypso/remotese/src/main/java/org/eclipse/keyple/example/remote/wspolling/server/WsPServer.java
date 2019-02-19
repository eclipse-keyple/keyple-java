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
package org.eclipse.keyple.example.remote.wspolling.server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.concurrent.*;
import org.eclipse.keyple.example.remote.transport.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.DtoHandler;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WsPServer implements ServerNode {


    private InetSocketAddress inet;
    final private String apiUrl;
    final private String pollingUrl;
    final private String nodeId;
    final private HttpServer server;
    final private Integer MAX_CONNECTION = 10;
    final private HttpHandler keypleDTOEndpoint;
    final private HttpHandler pollingEndpoint;

    private static final Logger logger = LoggerFactory.getLogger(WsPServer.class);

    private final BlockingQueue<HttpExchange> requestQueue =
            new LinkedBlockingQueue<HttpExchange>();

    /**
     * Constructor
     * 
     * @param url : hostname url to bind server
     * @param port : port to bind server to
     * @param apiUrl : api URL to deploy keypleDto listener endpoint
     * @param pollingUrl : polling URL to deploy polling listener endpoint
     * @throws IOException : if server can not bind given parameters
     */
    public WsPServer(String url, Integer port, String apiUrl, String pollingUrl, String nodeId)
            throws IOException {
        logger.info("Init Web Service DemoMaster on url : {}:{}", url, port);

        this.nodeId = nodeId;
        this.apiUrl = apiUrl;
        this.pollingUrl = pollingUrl;

        // Create Endpoint for polling DTO
        pollingEndpoint = new EndpointPolling(requestQueue, nodeId);

        // Create Endpoint for sending DTO
        keypleDTOEndpoint = new EndpointKeypleDTO((DtoSender) pollingEndpoint, nodeId);


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
     * TransportNode
     */
    @Override
    public void setDtoHandler(DtoHandler receiver) {
        ((EndpointKeypleDTO) this.keypleDTOEndpoint).setDtoHandler(receiver);
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void sendDTO(TransportDto message) {
        ((EndpointPolling) this.pollingEndpoint).update(message.getKeypleDTO());
    }

    @Override
    public void sendDTO(KeypleDto message) {
        ((EndpointPolling) this.pollingEndpoint).update(message);
    }

    @Override
    public void update(KeypleDto event) {
        ((EndpointPolling) this.pollingEndpoint).update(event);

    }

    // todo enable this?

    /**
     * Free httpExchange after 15 seconds
     * 
     * @param queue private void setPollingWorker(final Queue<HttpExchange> queue) {
     * 
     *        Thread PollingWorker = new Thread() { public void run() {
     * 
     *        logger.debug("Starting Polling Worker"); try { while (true) {
     * 
     * 
     *        // wait for 15000 Thread.sleep(15000); logger.trace("Clear all HttpExchange waiting in
     *        queue");
     * 
     *        synchronized (queue) { // close all httpEchange while (!queue.isEmpty()) {
     *        HttpExchange lastHttpExchange = queue.poll(); lastHttpExchange.close(); } } }
     * 
     *        } catch (Exception e) { e.printStackTrace(); logger.error("Error in polling worker
     *        {}", e.getCause()); }
     * 
     *        } };
     * 
     *        PollingWorker.start(); }
     */



}

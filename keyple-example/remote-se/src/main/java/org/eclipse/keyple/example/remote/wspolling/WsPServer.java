/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.wspolling;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.eclipse.keyple.example.remote.common.ServerNode;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WsPServer implements ServerNode {


    private InetSocketAddress inet;
    private String apiUrl;
    private String pollingUrl;

    private HttpServer server;
    static private Integer MAX_CONNECTION = 5;
    private HttpHandler keypleDTOEndpoint;
    private HttpHandler pollingEndpoint;

    private static final Logger logger = LoggerFactory.getLogger(WsPServer.class);

    private Queue<HttpExchange> requestQueue = new ConcurrentLinkedQueue<HttpExchange>();

    /**
     * Constructor
     * 
     * @param url
     * @param port
     * @param apiUrl
     * @param pollingUrl
     * @throws IOException
     */
    public WsPServer(String url, Integer port, String apiUrl, String pollingUrl)
            throws IOException {
        logger.info("Init Web Service Master on url : {}:{}", url, port);

        this.apiUrl = apiUrl;
        this.pollingUrl = pollingUrl;

        // Create Endpoint for polling DTO
        pollingEndpoint = new EndpointPolling(requestQueue);

        // Create Endpoint for sending DTO
        keypleDTOEndpoint = new EndpointKeypleDTO((DtoSender) pollingEndpoint);


        // deploy endpoint
        this.inet = new InetSocketAddress(Inet4Address.getByName(url), port);
        server = HttpServer.create(inet, MAX_CONNECTION);
        server.createContext(apiUrl, keypleDTOEndpoint);
        server.createContext(pollingUrl, pollingEndpoint);

        // start rse
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // creates a
        // default

        setPollingWorker(requestQueue);
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
    public void setDtoDispatcher(DtoDispatcher receiver) {
        ((EndpointKeypleDTO) this.keypleDTOEndpoint).setDtoDispatcher(receiver);;
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

    /**
     * Free httpExchange after 15 secondes
     * 
     * @param queue
     */
    private void setPollingWorker(final Queue<HttpExchange> queue) {

        Thread PollingWorker = new Thread() {
            public void run() {

                logger.debug("Starting Polling Worker");
                try {
                    while (true) {


                        // wait for 15000
                        Thread.sleep(15000);
                        logger.trace("Clear all HttpExchange waiting in queue");

                        synchronized (queue) {
                            // close all httpEchange
                            while (!queue.isEmpty()) {
                                HttpExchange lastHttpExchange = queue.poll();
                                lastHttpExchange.close();
                            }
                        }



                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error in polling worker {}", e.getCause());
                }

            }
        };

        PollingWorker.start();
    }



}

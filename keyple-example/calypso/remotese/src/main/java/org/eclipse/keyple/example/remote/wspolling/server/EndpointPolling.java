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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Endpoint for polling, used to send keypleDto to polling clients
 */

class EndpointPolling implements HttpHandler, TransportNode {


    private final Logger logger = LoggerFactory.getLogger(EndpointPolling.class);

    private DtoHandler dtoHandler;
    private final String nodeId;
    private final BlockingQueue<HttpExchange> requestQueue;

    public EndpointPolling(BlockingQueue<HttpExchange> requestQueue, String nodeId) {
        this.nodeId = nodeId;
        this.requestQueue = requestQueue;
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void handle(HttpExchange t) {

        logger.debug("Incoming HttpExchange {} ", t.toString());
        logger.debug("Incoming Request {} ", t.getRequestMethod());
        String requestMethod = t.getRequestMethod();

        if (requestMethod.equals("GET")) {

            // hold response until we got a response or timeout
            Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
            String nodeId = params.get("clientNodeId");
            // logger.trace("param clientNodeId=" + params.get("clientNodeId"));

            // set httpExchange in queue
            requestQueue.add(t);

            logger.debug("Receive a polling request {} from clientNodeId {} queue size {}",
                    t.toString(), nodeId, requestQueue.size());

        }
    }

    /*
     * TransportNode
     */
    @Override
    public void setDtoHandler(DtoHandler receiver) {
        this.dtoHandler = receiver;
    }


    @Override
    public void sendDTO(TransportDto message) {
        logger.warn("Send DTO with transport message {}", message);
        this.sendDTO(message.getKeypleDTO());
    }

    @Override
    public void sendDTO(KeypleDto message) {
        logger.debug("Using polling to send keypleDTO whose action : {}", message.getAction());

        HttpExchange t;
        try {
            t = requestQueue.poll(2, TimeUnit.SECONDS);
            logger.debug("Found a waiting HttpExchange {}", t != null ? t.toString() : "null");
            setHttpResponse(t, message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Polling has failed due to " + e.getMessage());
        } catch (InterruptedException e) {
            throw new IllegalStateException(
                    "Request Queue is still empty after timeout, impossible to send DTO");
        }

    }



    @Override
    public void update(KeypleDto event) {
        logger.info("Send DTO from update {}", event.getAction());
        this.sendDTO(event);
    }


    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private void setHttpResponse(HttpExchange t, KeypleDto resp) throws IOException {
        if (!resp.getAction().isEmpty()) {
            String responseBody = KeypleDtoHelper.toJson(resp);
            Integer responseCode = 200;
            t.getResponseHeaders().add("Content-Type", "application/json");
            t.sendResponseHeaders(responseCode, responseBody.length());
            OutputStream os = t.getResponseBody();
            os.write(responseBody.getBytes());
            os.close();
            logger.debug("Outcoming Response Code {} ", responseCode);
            logger.debug("Outcoming Response Body {} ", responseBody);


        } else {
            String responseBody = "{}";
            Integer responseCode = 200;
            t.getResponseHeaders().add("Content-Type", "application/json");
            t.sendResponseHeaders(responseCode, responseBody.length());
            OutputStream os = t.getResponseBody();
            os.write(responseBody.getBytes());
            os.close();
        }
    }
}

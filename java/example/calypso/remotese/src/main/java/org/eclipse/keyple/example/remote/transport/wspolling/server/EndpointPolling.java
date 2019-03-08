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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportNode;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
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
    // private final PublishQueue<KeypleDto> keypleDtoQueue;
    private final PublishQueueManager publishQueueManager;

    public EndpointPolling(PublishQueueManager publishQueueManager, String nodeId) {
        this.nodeId = nodeId;
        this.publishQueueManager = publishQueueManager;
        // this.keypleDtoQueue = KeypleDtoQueue;
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void handle(HttpExchange t) {
        logger.trace("Incoming Polling Request {} - {} ", t.getRequestMethod(), t.toString());
        String requestMethod = t.getRequestMethod();

        if (requestMethod.equals("GET")) {

            Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
            String nodeId = params.get("clientNodeId");

            logger.trace("Receive a polling request for KeypleDto {} from clientNodeId {} ",
                    t.toString(), nodeId);

            PublishQueue<KeypleDto> keypleDtoQueue;
            try {
                if (!publishQueueManager.exists(nodeId)) {
                    keypleDtoQueue = publishQueueManager.create(nodeId);
                } else {
                    keypleDtoQueue = publishQueueManager.get(nodeId);
                }

                // get a KeypleDto (blocking method)
                KeypleDto keypleDto = keypleDtoQueue.get(10000);
                if (keypleDto == null) {
                    // time elapsed
                    logger.trace("No keypleDto received during elapsed time");
                    setNoContent(t);
                } else {
                    logger.trace("Set keypleDto in response {}", keypleDto);
                    setHttpResponse(t, keypleDto);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


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
        logger.trace("Send DTO with transport message {}", message);
        this.sendDTO(message.getKeypleDTO());
    }

    @Override
    public void sendDTO(KeypleDto message) {
        logger.debug("Using polling to send keypleDTO this action : {}", message.getAction());
        logger.trace("Using polling to send keypleDTO : {}", message);

        PublishQueue keypleDtoQueue = publishQueueManager.get(message.getNodeId());
        if (keypleDtoQueue == null) {
            throw new IllegalStateException("Keyple Dto Queue is null, what to do?");
        }
        keypleDtoQueue.publish(message);

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
            logger.trace("Outcoming Response Code {} ", responseCode);
            logger.trace("Outcoming Response Body {} ", responseBody);
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

    private void setNoContent(HttpExchange t) throws IOException {

        String responseBody = "{}";
        Integer responseCode = 204;
        t.getResponseHeaders().add("Content-Type", "application/json");
        t.sendResponseHeaders(responseCode, -1);
        // OutputStream os = t.getResponseBody();
        // os.write(responseBody.getBytes());
        // os.close();

    }
}

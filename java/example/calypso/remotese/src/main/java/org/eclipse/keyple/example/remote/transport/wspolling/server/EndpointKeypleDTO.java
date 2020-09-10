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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import org.eclipse.keyple.example.remote.transport.wspolling.WsPTransportDTO;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.DtoNode;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


/**
 * Endpoint for receiving KeypleDTO from clients
 */
class EndpointKeypleDTO implements HttpHandler, DtoNode {


    private final Logger logger = LoggerFactory.getLogger(EndpointKeypleDTO.class);

    private DtoHandler dtoHandler;
    private final DtoSender dtoSender;
    private final String nodeId;

    public EndpointKeypleDTO(DtoSender dtoSender, String nodeId) {

        this.dtoSender = dtoSender;// endpointPolling
        this.nodeId = nodeId;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        logger.trace("Incoming Request {} - {}", t.getRequestMethod(), t.toString());
        String requestMethod = t.getRequestMethod();

        if (requestMethod.equals("POST")) {
            String body = parseBodyToString(t.getRequestBody());// .. parse the
            // request body
            KeypleDto incoming = KeypleDtoHelper.fromJson(body);
            TransportDto transportDto = new WsPTransportDTO(incoming, dtoSender);

            logger.trace("Incoming DTO {} ", KeypleDtoHelper.toJson(incoming));
            TransportDto outcoming = dtoHandler.onDTO(transportDto);

            setHttpResponse(t, outcoming.getKeypleDTO());

        }
    }

    /*
     * DtoNode
     */
    @Override
    public void setDtoHandler(DtoHandler receiver) {
        this.dtoHandler = receiver;
    }


    @Override
    public void sendDTO(TransportDto message) {
        logger.warn("Send DTO can not be used in Web Service DemoMaster");
        // not in use, one way communication, server do not send message
    }

    @Override
    public void sendDTO(KeypleDto message) {
        logger.warn("Send DTO can not be used in Web Service DemoMaster");
    }

    @Override
    public String getNodeId() {
        return this.nodeId;
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

    private String parseBodyToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}

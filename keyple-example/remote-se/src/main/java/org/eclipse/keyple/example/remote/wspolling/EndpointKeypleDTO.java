/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.wspolling;

import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.keyple.example.remote.common.TransportNode;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class EndpointKeypleDTO implements HttpHandler, TransportNode {


    private final Logger logger = LoggerFactory.getLogger(EndpointKeypleDTO.class);

    DtoDispatcher dtoDispatcher;
    DtoSender dtoSender;

    public EndpointKeypleDTO(DtoSender dtoSender) {
        this.dtoSender = dtoSender;// endpointPolling
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        logger.trace("Incoming HttpExchange {} ", t.toString());
        logger.trace("Incoming Request {} ", t.getRequestMethod());
        String requestMethod = t.getRequestMethod();

        if (requestMethod.equals("POST")) {
            String body = HttpHelper.parseBodyToString(t.getRequestBody());// .. parse the
            // request body
            KeypleDto incoming = KeypleDtoHelper.fromJson(body);
            TransportDto transportDto = new WsPTransportDTO(incoming, dtoSender);

            logger.trace("Incoming DTO {} ", KeypleDtoHelper.toJson(incoming));
            TransportDto outcoming = dtoDispatcher.onDTO(transportDto);

            setHttpResponse(t, outcoming.getKeypleDTO());

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
    public void sendDTO(TransportDto message) {
        logger.warn("Send DTO can not be used in Web Service Master");
        // not in use, oneway communication, server do not send message
    }

    @Override
    public void sendDTO(KeypleDto message) {
        logger.warn("Send DTO can not be used in Web Service Master");
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

    @Override
    public void update(KeypleDto event) {
        // not in used in ws
    }
}

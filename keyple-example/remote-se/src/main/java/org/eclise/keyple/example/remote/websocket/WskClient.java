/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.websocket;

import java.net.URI;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WskClient extends WebSocketClient implements ClientNode {

    private static final Logger logger = LoggerFactory.getLogger(WskClient.class);
    DtoDispatcher dtoDispatcher;

    public WskClient(URI url) {
        super(url);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.debug("Web socket onOpen {}", handshakedata);

    }

    @Override
    public void onMessage(String message) {
        logger.debug("Web socket onMessage {}", message);
        KeypleDTO dto = KeypleDTOHelper.fromJson(message);

        // process dto
        TransportDTO transportDTO = dtoDispatcher.onDTO(new WskTransportDTO(dto, null, this));

        // there is a response/request to send back
        if (!KeypleDTOHelper.isNoResponse(transportDTO.getKeypleDTO())) {
            this.sendDTO(transportDTO);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.debug("Web socket onClose {} {}", code, reason);

    }

    @Override
    public void onError(Exception ex) {
        logger.debug("Web socket onError {}", ex);

    }

    @Override
    public void sendDTO(TransportDTO transportDTO) {
        this.sendDTO(transportDTO.getKeypleDTO());
    }

    @Override
    public void sendDTO(KeypleDTO keypleDTO) {
        // if keypkeDTO is no empty
        if (!KeypleDTOHelper.isNoResponse(keypleDTO)) {
            logger.trace("send DTO {}", KeypleDTOHelper.toJson(keypleDTO));
            this.send(KeypleDTOHelper.toJson(keypleDTO));
        } else {
            logger.debug("No message to send back");
        }
    }

    @Override
    public void setDtoDispatcher(DtoDispatcher receiver) {
        this.dtoDispatcher = receiver;
    }


    // observer of keypleDTOSenders
    @Override
    public void update(KeypleDTO event) {
        this.sendDTO(event);
    }
}

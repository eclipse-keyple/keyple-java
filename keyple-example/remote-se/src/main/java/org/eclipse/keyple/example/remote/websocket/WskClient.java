/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.websocket;

import java.net.URI;
import org.eclipse.keyple.example.remote.common.ClientNode;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDto;
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
        logger.trace("Web socket onOpen {}", handshakedata);

    }

    @Override
    public void onMessage(String message) {
        logger.trace("Web socket onMessage {}", message);
        KeypleDto dto = KeypleDtoHelper.fromJson(message);

        // process dto
        TransportDto transportDto = dtoDispatcher.onDTO(new WskTransportDTO(dto, null, this));

        // there is a response/request to send back
        if (!KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {
            this.sendDTO(transportDto);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.trace("Web socket onClose {} {}", code, reason);

    }

    @Override
    public void onError(Exception ex) {
        logger.trace("Web socket onError {}", ex);

    }

    @Override
    public void sendDTO(TransportDto transportDto) {
        this.sendDTO(transportDto.getKeypleDTO());
    }

    @Override
    public void sendDTO(KeypleDto keypleDto) {
        // if keypkeDTO is no empty
        if (!KeypleDtoHelper.isNoResponse(keypleDto)) {
            logger.trace("send DTO {}", KeypleDtoHelper.toJson(keypleDto));
            this.send(KeypleDtoHelper.toJson(keypleDto));
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
    public void update(KeypleDto event) {
        this.sendDTO(event);
    }
}

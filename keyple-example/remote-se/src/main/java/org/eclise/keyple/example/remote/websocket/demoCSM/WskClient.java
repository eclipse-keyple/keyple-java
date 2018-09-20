/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.websocket.demoCSM;

import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class WskClient extends WebSocketClient implements DtoSender, TransportNode {

    private static final Logger logger = LoggerFactory.getLogger(WskClient.class);
    DtoReceiver dtoReceiver;

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
        KeypleDTO response = dtoReceiver.onDTO(dto,this, null);
        if(!response.getAction().isEmpty()){
            sendDTO(response);
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
    public void sendDTO(KeypleDTO message) {
        if (!message.getAction().isEmpty()) {
            logger.debug("send DTO {}", KeypleDTOHelper.toJson(message));
            this.send(KeypleDTOHelper.toJson(message));
        }
    }

    @Override
    public void setDtoReceiver(DtoReceiver receiver) {
        this.dtoReceiver = receiver;
    }

    @Override
    public Object getConnection(String sessionId) {
        return null;//not in used in wsk client
    }
}

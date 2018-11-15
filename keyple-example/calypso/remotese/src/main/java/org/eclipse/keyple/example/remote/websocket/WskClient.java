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
package org.eclipse.keyple.example.remote.websocket;

import java.net.URI;
import org.eclipse.keyple.example.remote.transport.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.DtoHandler;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.TransportDto;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web socket client
 */
public class WskClient extends WebSocketClient implements ClientNode {

    private static final Logger logger = LoggerFactory.getLogger(WskClient.class);
    private DtoHandler dtoHandler;
    private final String nodeId;

    public WskClient(URI url, String nodeId) {
        super(url);
        this.nodeId = nodeId;
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
        TransportDto transportDto = dtoHandler.onDTO(new WskTransportDTO(dto, null, this));

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
        // if keypleDTO is no empty
        if (!KeypleDtoHelper.isNoResponse(keypleDto)) {
            logger.trace("send DTO {}", KeypleDtoHelper.toJson(keypleDto));
            this.send(KeypleDtoHelper.toJson(keypleDto));
        } else {
            logger.debug("No message to send back");
        }
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void setDtoHandler(DtoHandler receiver) {
        this.dtoHandler = receiver;
    }


    // observer of keypleDTOSenders
    @Override
    public void update(KeypleDto event) {
        this.sendDTO(event);
    }

    @Override
    public void disconnect() {
        this.getConnection().close();
    }
}

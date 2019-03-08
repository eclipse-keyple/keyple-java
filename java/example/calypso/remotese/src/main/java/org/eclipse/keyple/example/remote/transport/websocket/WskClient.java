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
package org.eclipse.keyple.example.remote.transport.websocket;

import java.net.URI;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
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
    public void onMessage(final String message) {

        final WskClient thisClient = this;

        // process all incoming message in a separate thread to allow RemoteSE blocking API to work
        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.trace("Web socket onMessage {}", message);
                KeypleDto dto = KeypleDtoHelper.fromJson(message);

                // process dto
                TransportDto transportDto =
                        dtoHandler.onDTO(new WskTransportDTO(dto, null, thisClient));

                // there is a response/request to send back
                if (!KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {
                    thisClient.sendDTO(transportDto);
                }
            }
        }).start();

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
            logger.trace("send message to server (KeypleDto) {}",
                    KeypleDtoHelper.toJson(keypleDto));
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

    @Override
    public void connect(ConnectCallback connectCallback) {
        if (connectCallback != null) {
            logger.warn("Connection callback is not implemented for this client");
        }
        this.connect();
    }

    @Override
    public void disconnect() {
        this.getConnection().close();
    }
}

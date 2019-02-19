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

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.example.remote.transport.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.DtoHandler;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.TransportDto;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web socket server
 */
class WskServer extends WebSocketServer implements ServerNode {

    private static final Logger logger = LoggerFactory.getLogger(WskServer.class);
    private DtoHandler stubplugin;

    // only for when server is slave
    final private Boolean isSlave;
    private WebSocket masterWebSocket;
    final private String nodeId;

    public WskServer(InetSocketAddress address, Boolean isSlave, String nodeId) {
        super(address);

        logger.info("Create websocket server on address {}", address.toString());
        this.isSlave = isSlave;
        this.nodeId = nodeId;
    }

    /*
     * WebSocketServer
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.debug("Web socket onOpen {} {}", conn, handshake);
        masterWebSocket = conn;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.debug("Web socket onClose {} {} {} {}", conn, code, reason, remote);
    }

    /**
     * Incoming message
     * 
     * @param conn : websocket connection used
     * @param message : incoming message
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.trace("Web socket onMessage {} {}", conn, message);
        KeypleDto keypleDto = KeypleDtoHelper.fromJson(message);

        if (stubplugin != null) {

            // LOOP pass DTO and get DTO Response is any
            TransportDto transportDto =
                    stubplugin.onDTO(new WskTransportDTO(keypleDto, conn, this));

            if (!isSlave) {
                // if server is master, can have numerous clients
                if (transportDto.getKeypleDTO().getSessionId() != null) {
                    sessionId_Connection.put(transportDto.getKeypleDTO().getSessionId(), conn);
                } else {
                    logger.debug("No session defined in message {}", transportDto);
                }
            }

            this.sendDTO(transportDto);
        } else {
            throw new IllegalStateException(
                    "Received a message but no DtoHandler is defined to process the message");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.debug("Web socket onError {} {}", conn, ex);

    }

    @Override
    public void onStart() {
        logger.info("Web socket server started");
    }


    /*
     * TransportNode
     */

    final private Map<String, Object> sessionId_Connection = new HashMap<String, Object>();

    private Object getConnection(String sessionId) {
        return sessionId_Connection.get(sessionId);
    }

    public void setDtoHandler(DtoHandler stubplugin) {
        this.stubplugin = stubplugin;
    }


    @Override
    public void sendDTO(TransportDto transportDto) {
        logger.trace("sendDTO {} {}", KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));

        if (KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {
            logger.trace("Keyple DTO is empty, do not send it");
        } else {

            if (isSlave) {
                // only one client -> master
                masterWebSocket.send(KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));
            } else {
                // server is master and can have numerous clients
                if (((WskTransportDTO) transportDto).getSocketWeb() != null) {
                    logger.trace("Use socketweb included in TransportDto");
                    ((WskTransportDTO) transportDto).getSocketWeb()
                            .send(KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));
                } else {
                    if (transportDto.getKeypleDTO().getSessionId() == null) {
                        logger.warn("No sessionId defined in message, Keyple DTO can not be sent");
                    } else {
                        logger.trace("Retrieve socketweb from sessionId");
                        // retrieve connection object from the common
                        Object conn = getConnection(transportDto.getKeypleDTO().getSessionId());
                        logger.trace("send DTO {} {}",
                                KeypleDtoHelper.toJson(transportDto.getKeypleDTO()), conn);
                        ((WebSocket) conn)
                                .send(KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));
                    }
                }

            }
        }
    }

    /*
     * DTO Sender
     */
    @Override
    public void sendDTO(KeypleDto message) {
        logger.trace("Web socket sendDTO without predefined socket {}",
                KeypleDtoHelper.toJson(message));
        this.sendDTO(new WskTransportDTO(message, null));
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }


    @Override
    public void update(KeypleDto event) {
        sendDTO(event);
    }
}

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

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.eclipse.keyple.seproxy.exception.KeypleRuntimeException;
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
    private DtoHandler dtoHandler;

    // only for when server is master
    private Boolean isMaster;
    private WebSocket masterWebSocket;
    final private String serverNodeId;

    public WskServer(InetSocketAddress address, Boolean isMaster, String serverNodeId) {
        super(address);

        logger.info("Create websocket server on address {}", address.toString());
        this.serverNodeId = serverNodeId;
        this.isMaster = isMaster;
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
    public void onMessage(final WebSocket conn, final String message) {

        final WskServer thisServer = this;

        // process all incoming message in a separate thread to allow RemoteSE blocking API to work
        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.trace("Server receive a message {} {}", conn, message);
                KeypleDto keypleDto = KeypleDtoHelper.fromJson(message);

                if (dtoHandler != null) {


                    if (isMaster) {
                        // if server is master, can have numerous clients
                        if (keypleDto.getNativeReaderName() != null) {
                            logger.debug(
                                    "Websocket connection has been mapped to session defined in message {} - {}",
                                    keypleDto.getNativeReaderName(), conn);

                            addConnection(conn, keypleDto.getNativeReaderName(),
                                    keypleDto.getRequesterNodeId());
                        } else {
                            logger.debug(
                                    "No session defined in message, can not map websocket connection {} - {}",
                                    keypleDto.getNativeReaderName());
                        }
                    }

                    // LOOP pass DTO and get DTO Response is any
                    TransportDto transportDto =
                            dtoHandler.onDTO(new WskTransportDTO(keypleDto, conn, thisServer));


                    thisServer.sendDTO(transportDto);
                } else {
                    throw new IllegalStateException(
                            "Received a message but no DtoHandler is defined to process the message");
                }
            };
        }).start();


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
     * DtoNode
     */

    final private Map<String, WebSocket> nativeReaderName_session =
            new HashMap<String, WebSocket>();

    private WebSocket getConnection(String nativeReaderName, String clientNodeId) {
        return nativeReaderName_session.get(nativeReaderName + clientNodeId);
    }

    private WebSocket addConnection(WebSocket connection, String nativeReaderName,
            String clientNodeId) {
        return nativeReaderName_session.put(nativeReaderName + clientNodeId, connection);
    }

    public void setDtoHandler(DtoHandler dtoHandler) {
        this.dtoHandler = dtoHandler;
    }


    @Override
    public void sendDTO(TransportDto transportDto) {
        logger.trace("sendDTO with a TransportDto {} {}",
                KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));

        if (KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {
            logger.trace("Keyple DTO is empty, do not send it");
        } else {

            if (!isMaster) {
                logger.trace("Wsk Server is slave, use the master web socket {}", masterWebSocket);
                // if server is client -> use the master web socket
                masterWebSocket.send(KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));
            } else {
                // server is master, can have numerous slave clients
                logger.trace("Wsk Server is master, find to which client answer");
                if (((WskTransportDTO) transportDto).getSocketWeb() != null) {
                    logger.trace("Use socketweb included in TransportDto");
                    ((WskTransportDTO) transportDto).getSocketWeb()
                            .send(KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));
                } else {
                    // if there is no socketweb defined in the transport dto
                    // retrieve the socketweb by the sessionId
                    if (transportDto.getKeypleDTO().getSessionId() == null) {
                        logger.warn("No sessionId defined in message, Keyple DTO can not be sent");
                    } else {
                        logger.trace(
                                "Retrieve socketweb from nativeReaderName and clientNodeId {} {}",
                                transportDto.getKeypleDTO().getNativeReaderName(),
                                transportDto.getKeypleDTO().getTargetNodeId());
                        WebSocket conn =
                                getConnection(transportDto.getKeypleDTO().getNativeReaderName(),
                                        transportDto.getKeypleDTO().getTargetNodeId());

                        if (conn == null) {
                            throw new KeypleRuntimeException("Conn was not found for "
                                    + transportDto.getKeypleDTO().getNativeReaderName() + " "
                                    + transportDto.getKeypleDTO().getTargetNodeId());
                        }
                        logger.trace("send DTO with websocket {} {}",
                                KeypleDtoHelper.toJson(transportDto.getKeypleDTO()), conn);

                        conn.send(KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));
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
        return serverNodeId;
    }


}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.websocket;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.example.remote.common.ServerNode;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDto;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WskServer extends WebSocketServer implements ServerNode {

    private static final Logger logger = LoggerFactory.getLogger(WskServer.class);
    private DtoDispatcher stubplugin;
    private ConnectionCb connectionCb;

    // only for when server is slave
    private Boolean isSlave;
    private WebSocket masterWebSocket;

    public WskServer(InetSocketAddress address, ConnectionCb connectionCb, Boolean isSlave) {
        super(address);
        this.connectionCb = connectionCb;
        this.isSlave = isSlave;
    }

    /*
     * WebSocketServer
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.debug("Web socket onOpen {} {}", conn, handshake);
        masterWebSocket = conn;
        if (connectionCb != null) {
            connectionCb.onConnection(conn);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.debug("Web socket onClose {} {} {} {}", conn, code, reason, remote);
    }

    /**
     * Incoming message
     * 
     * @param conn
     * @param message
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
                    logger.warn("No session defined in response {}", transportDto);
                }
            }

            this.sendDTO(transportDto);
        } else {
            logger.warn("Received a message but no DtoDispatcher");
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

    Map<String, Object> sessionId_Connection = new HashMap<String, Object>();

    private Object getConnection(String sessionId) {
        return sessionId_Connection.get(sessionId);
    }

    public void setDtoDispatcher(DtoDispatcher stubplugin) {
        this.stubplugin = stubplugin;
    }


    @Override
    public void sendDTO(TransportDto tdto) {
        logger.trace("sendDTO {} {}", KeypleDtoHelper.toJson(tdto.getKeypleDTO()));

        if (KeypleDtoHelper.isNoResponse(tdto.getKeypleDTO())) {
            logger.trace("Keyple DTO is empty, do not send it");
        } else {

            if (isSlave) {
                // only one client -> master
                masterWebSocket.send(KeypleDtoHelper.toJson(tdto.getKeypleDTO()));
            } else {
                // server is master and can have numerous clients
                if (((WskTransportDTO) tdto).getSocketWeb() != null) {
                    logger.trace("Use socketweb included in TransportDto");
                    ((WskTransportDTO) tdto).getSocketWeb()
                            .send(KeypleDtoHelper.toJson(tdto.getKeypleDTO()));
                } else {
                    if (tdto.getKeypleDTO().getSessionId() == null) {
                        logger.warn("No sessionId defined in message, Keyple DTO can not be sent");
                    } else {
                        logger.trace("Retrieve socketweb from sessionId");
                        // retrieve connection object from the transport
                        Object conn = getConnection(tdto.getKeypleDTO().getSessionId());
                        logger.trace("send DTO {} {}", KeypleDtoHelper.toJson(tdto.getKeypleDTO()),
                                conn);
                        ((WebSocket) conn).send(KeypleDtoHelper.toJson(tdto.getKeypleDTO()));
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
    public void update(KeypleDto event) {
        sendDTO(event);
    }
}

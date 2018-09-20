/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.websocket.demoCSM;

import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclise.keyple.example.remote.websocket.WskTransportDTO;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class WskServer extends WebSocketServer implements TransportNode {

    private static final Logger logger = LoggerFactory.getLogger(WskServer.class);
    private DtoReceiver dtoReceiver;
    private ConnectionCb connectionCb;

    public WskServer(InetSocketAddress address, ConnectionCb connectionCb) {
        super(address);
        this.connectionCb = connectionCb;
    }


    //CSM Server, should we expect only one connection? if so, easy peasy
    WebSocket currentConn;

    /*
        WebSocketServer
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.debug("Web socket onOpen {} {}", conn, handshake);
        this.currentConn = conn;
        connectionCb.onConnection(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.debug("Web socket onClose {} {} {} {}", conn, code, reason, remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.debug("Web socket onMessage {} {}", conn, message);
        KeypleDTO keypleDTO = KeypleDTOHelper.fromJson(message);
        if (dtoReceiver != null) {
            TransportDTO response = dtoReceiver.onDTO(new WskTransportDTO(keypleDTO,conn));
            this.sendDTO(response);
        } else {
            logger.warn("Received a message but no DtoReceiver");
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
        TransportNode
    */


    @Override
    public Object getConnection(String sessionId) {
        return currentConn;
    }

    public void setDtoReceiver(DtoReceiver dtoReceiver) {
        this.dtoReceiver = dtoReceiver;
    }


    @Override
    public void sendDTO(TransportDTO message) {
        //if socket is defined in tdto, use it
        if(((WskTransportDTO)message).getConn()!=null){
            ((WskTransportDTO)message).getConn().send(KeypleDTOHelper.toJson(message.getKeypleDTO()));
        }else{
            //find socket in mapping, call the basic method
            sendDTO(message.getKeypleDTO());
        }
    }

    /*
            DTO Sender
        */
    @Override
    public void sendDTO(KeypleDTO message) {

        if (!message.getAction().isEmpty()) {
                //retrieve connection object from the transport
            Object conn = getConnection(null);
            logger.debug("send DTO {} {}", KeypleDTOHelper.toJson(message), conn);
            ((WebSocket) conn).send(KeypleDTOHelper.toJson(message));

        }
    }



}

package org.eclise.keyple.example.remote.server.transport.websocket.common;

import org.eclise.keyple.example.remote.server.transport.NseClient;
import org.eclise.keyple.example.remote.server.transport.RseAPI;
import org.eclise.keyple.example.remote.server.transport.RseClient;
import org.eclise.keyple.example.remote.server.transport.websocket.common.command.RseProcessor;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class WskServer extends WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(WskServer.class);
    private RseProcessor rseProcessor;

    public WskServer(InetSocketAddress address, RseAPI rseAPI, NseClient nseClient) {
        super(address);
        rseProcessor = new RseProcessor(rseAPI,nseClient);

    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.debug("Web socket onOpen {} {}",conn,handshake);

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.debug("Web socket onClose {} {} {} {}",conn,code,reason,remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.debug("Web socket onMessage {} {}",conn,message);

        KeypleDTO keypleDTO = KeypleDTOHelper.parseJson(message);
        KeypleDTO response = rseProcessor.processMessage(keypleDTO);
        if(!response.getAction().isEmpty()){
            conn.send(KeypleDTOHelper.getJson(response));
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.debug("Web socket onError {} {}",conn,ex);

    }

    @Override
    public void onStart() {
        logger.info("Web socket server started");
    }
}

package org.eclise.keyple.example.remote.server.transport.websocket.common;

import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;
import org.eclise.keyple.example.remote.server.transport.NseAPI;
import org.eclise.keyple.example.remote.server.transport.RseClient;
import org.eclise.keyple.example.remote.server.transport.websocket.common.command.NseProcessor;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class WskClient extends WebSocketClient {

    private static final Logger logger = LoggerFactory.getLogger(WskClient.class);
    NseProcessor nseProcessor;

    public WskClient(URI url, NseAPI nseAPI, RseClient rseClient){
        super(url);
        nseProcessor = new NseProcessor(nseAPI,rseClient);

    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.debug("Web socket onOpen {}",handshakedata);

    }

    @Override
    public void onMessage(String message) {
        logger.debug("Web socket onMessage {}",message);
        KeypleDTO keypleDTO = KeypleDTOHelper.parseJson(message);
        KeypleDTO response = nseProcessor.processMessage(keypleDTO);
        if(!response.getAction().isEmpty()){
            this.send(KeypleDTOHelper.getJson(response));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.debug("Web socket onClose {} {}",code,reason);

    }

    @Override
    public void onError(Exception ex) {
        logger.debug("Web socket onError {}",ex);

    }
}

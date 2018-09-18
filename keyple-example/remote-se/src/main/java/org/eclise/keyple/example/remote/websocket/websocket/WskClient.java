package org.eclise.keyple.example.remote.websocket.websocket;

import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.DtoReceiver;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTOHelper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class WskClient extends WebSocketClient implements DtoSender {

    private static final Logger logger = LoggerFactory.getLogger(WskClient.class);
    DtoReceiver dtoReceiver;

    public WskClient(URI url, DtoReceiver dtoReceiver){
        super(url);
        this.dtoReceiver = dtoReceiver;

    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.debug("Web socket onOpen {}",handshakedata);

    }

    @Override
    public void onMessage(String message) {
        logger.debug("Web socket onMessage {}",message);
        KeypleDTO dto = KeypleDTOHelper.fromJson(message);
        dtoReceiver.onDTO(dto);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.debug("Web socket onClose {} {}",code,reason);

    }

    @Override
    public void onError(Exception ex) {
        logger.debug("Web socket onError {}",ex);

    }

    @Override
    public void sendDTO(KeypleDTO message, Object connection) {
        this.send(KeypleDTOHelper.toJson(message));

    }
}

package org.eclise.keyple.example.remote.websocket.websocket;

import org.eclipse.keyple.plugin.remote_se.transport.DtoReceiver;
import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTOHelper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class WskServer extends WebSocketServer implements DtoSender,TransportNode {

    private static final Logger logger = LoggerFactory.getLogger(WskServer.class);
    private DtoReceiver dtoReceiver;

    public WskServer(InetSocketAddress address) {
        super(address);
    }

    public void setDtoReceiver(DtoReceiver dtoReceiver){
        this.dtoReceiver = dtoReceiver;
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
        KeypleDTO keypleDTO = KeypleDTOHelper.fromJson(message);
        if(dtoReceiver !=null){
            dtoReceiver.onDTO(keypleDTO);
        }else{
            logger.warn("Received a message but no DtoReceiver");
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

    @Override
    public void sendDTO(KeypleDTO message, Object conn) {
        if(!message.getAction().isEmpty()){
            ((WebSocket) conn).send(KeypleDTOHelper.toJson(message));
        }
    }


}

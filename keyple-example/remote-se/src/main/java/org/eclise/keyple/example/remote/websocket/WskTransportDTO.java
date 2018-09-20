package org.eclise.keyple.example.remote.websocket;

import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDTO;
import org.java_websocket.WebSocket;

public class WskTransportDTO implements TransportDTO{

    KeypleDTO dto;

    public WebSocket getConn() {
        return conn;
    }

    WebSocket conn;

    public WskTransportDTO(KeypleDTO dto , WebSocket conn){
           this.dto = dto;
           this.conn =conn;
    }

    @Override
    public KeypleDTO getKeypleDTO() {
        return dto;
    }

    @Override
    public TransportDTO nextTransportDTO(KeypleDTO kdto) {
        return null;
    }

    @Override
    public DtoSender getDtoSender() {
        return null;
    }
}

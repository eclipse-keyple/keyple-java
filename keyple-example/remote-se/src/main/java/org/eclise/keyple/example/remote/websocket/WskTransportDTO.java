package org.eclise.keyple.example.remote.websocket;

import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDTO;
import org.java_websocket.WebSocket;

public class WskTransportDTO implements TransportDTO{

    public KeypleDTO getDto() {
        return dto;
    }


    KeypleDTO dto;
    WebSocket socketWeb;


    public WskTransportDTO(KeypleDTO dto , WebSocket socketWeb){
           this.dto = dto;
           this.socketWeb =socketWeb;
    }

    @Override
    public KeypleDTO getKeypleDTO() {
        return dto;
    }

    @Override
    public TransportDTO nextTransportDTO(KeypleDTO keypleDto) {

        return new WskTransportDTO(keypleDto, this.socketWeb);
    }



    public void setDto(KeypleDTO dto) {
        this.dto = dto;
    }

    public WebSocket getSocketWeb() {
        return socketWeb;
    }

    public void setSocketWeb(WebSocket socketWeb) {
        this.socketWeb = socketWeb;
    }

}

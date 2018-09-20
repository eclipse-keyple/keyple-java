package org.eclise.keyple.example.remote.webservice.demoPO;

import com.sun.net.httpserver.HttpExchange;
import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDTO;

public class WsTransportDTO implements TransportDTO {

    KeypleDTO keypleDTO;
    HttpExchange t;
    DtoSender dtoSender;

    public HttpExchange getT() {
        return t;
    }

    public WsTransportDTO(KeypleDTO kdto, HttpExchange t){
        this.keypleDTO = kdto;
        this.t = t;

    }

    public KeypleDTO getKeypleDTO() {
        return keypleDTO;
    }

    @Override
    public TransportDTO nextTransportDTO(KeypleDTO kdto) {
        return new WsTransportDTO(kdto, t);
    }

    @Override
    public DtoSender getDtoSender() {
        return dtoSender;
    }


    public void setDtoSender(DtoSender dtoSender){
        this.dtoSender = dtoSender;
    }

}

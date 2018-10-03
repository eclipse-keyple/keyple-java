package org.eclise.keyple.example.remote.wspolling;

import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDTO;

public class WsPTransportDTO implements TransportDTO {


    KeypleDTO dto;
    DtoSender node;

    public WsPTransportDTO(KeypleDTO dto, DtoSender node) {
        this.dto = dto;
        this.node = node;
    }

    @Override
    public KeypleDTO getKeypleDTO() {
        return dto;
    }

    @Override
    public TransportDTO nextTransportDTO(KeypleDTO kdto) {
        return new WsPTransportDTO(kdto, this.getDtoSender());
    }

    @Override
    public DtoSender getDtoSender() {
        return node;
    }
}

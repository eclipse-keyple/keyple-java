/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.websocket;

import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDto;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDto;
import org.java_websocket.WebSocket;

public class WskTransportDTO implements TransportDto {

    public KeypleDto getDto() {
        return dto;
    }


    KeypleDto dto;
    WebSocket socketWeb;
    DtoSender wskNode;


    public WskTransportDTO(KeypleDto dto, WebSocket socketWeb) {
        this.dto = dto;
        this.socketWeb = socketWeb;
    }

    public WskTransportDTO(KeypleDto dto, WebSocket socketWeb, DtoSender wskNode) {
        this.dto = dto;
        this.socketWeb = socketWeb;
        this.wskNode = wskNode;
    }

    @Override
    public KeypleDto getKeypleDTO() {
        return dto;
    }

    @Override
    public TransportDto nextTransportDTO(KeypleDto keypleDto) {

        return new WskTransportDTO(keypleDto, this.socketWeb);
    }

    @Override
    public DtoSender getDtoSender() {
        return wskNode;
    }

    public WebSocket getSocketWeb() {
        return socketWeb;
    }


}

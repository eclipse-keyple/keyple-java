/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.websocket;

import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDTO;
import org.java_websocket.WebSocket;

public class WskTransportDTO implements TransportDTO {

    public KeypleDTO getDto() {
        return dto;
    }


    KeypleDTO dto;
    WebSocket socketWeb;


    public WskTransportDTO(KeypleDTO dto, WebSocket socketWeb) {
        this.dto = dto;
        this.socketWeb = socketWeb;
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

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.ws;

import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDTO;
import com.sun.net.httpserver.HttpExchange;

public class WsTransportDTO implements TransportDTO {

    KeypleDTO keypleDTO;
    HttpExchange t;


    public WsTransportDTO(KeypleDTO kdto, HttpExchange t) {
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
        // not in used in ws
        return null;
    }


}

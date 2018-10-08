/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.wspolling;

import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDto;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDto;

public class WsPTransportDTO implements TransportDto {


    KeypleDto dto;
    DtoSender node;

    public WsPTransportDTO(KeypleDto dto, DtoSender node) {
        this.dto = dto;
        this.node = node;
    }

    @Override
    public KeypleDto getKeypleDTO() {
        return dto;
    }

    @Override
    public TransportDto nextTransportDTO(KeypleDto kdto) {
        return new WsPTransportDTO(kdto, this.getDtoSender());
    }

    @Override
    public DtoSender getDtoSender() {
        return node;
    }
}

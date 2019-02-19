/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.remote.wspolling;

import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.TransportDto;

public class WsPTransportDTO implements TransportDto {


    final private KeypleDto dto;
    final private DtoSender node;

    public WsPTransportDTO(KeypleDto dto, DtoSender node) {
        this.dto = dto;
        this.node = node;
    }

    @Override
    public KeypleDto getKeypleDTO() {
        return dto;
    }

    @Override
    public TransportDto nextTransportDTO(KeypleDto keypleDto) {
        return new WsPTransportDTO(keypleDto, this.getDtoSender());
    }

    @Override
    public DtoSender getDtoSender() {
        return node;
    }
}

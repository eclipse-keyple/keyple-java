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
package org.eclipse.keyple.plugin.remotese.transport.impl.java;

import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;

public class LocalTransportDto implements TransportDto {

    private final KeypleDto keypleDto;
    private final LocalClient theClient;


    public LocalTransportDto(KeypleDto keypleDto, LocalClient theClient) {
        this.keypleDto = keypleDto;
        this.theClient = theClient;
    }

    public LocalClient getTheClient() {
        return theClient;
    }

    @Override
    public KeypleDto getKeypleDTO() {
        return keypleDto;
    }

    @Override
    public TransportDto nextTransportDTO(KeypleDto keypleDto) {
        return new LocalTransportDto(keypleDto, theClient);
    }


}

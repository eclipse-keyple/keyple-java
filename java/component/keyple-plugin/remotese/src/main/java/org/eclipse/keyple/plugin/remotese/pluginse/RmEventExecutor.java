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
package org.eclipse.keyple.plugin.remotese.pluginse;

import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.eclipse.keyple.seproxy.event.ReaderEvent;

public class RmEventExecutor implements RemoteMethodExecutor {

    private final RemoteSePlugin plugin;

    public RmEventExecutor(RemoteSePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();

        // parseResponse body
        ReaderEvent event = JsonParser.getGson().fromJson(keypleDto.getBody(), ReaderEvent.class);

        // dispatch reader event
        plugin.onReaderEvent(event, keypleDto.getSessionId());

        // chain response if needed
        // try {
        // VirtualReader virtualReader =
        // (VirtualReader) plugin.getReaderByRemoteName(keypleDto.getNativeReaderName());
        //
        // // chain response with a seRequest if needed
        // if ((virtualReader.getSession()).hasSeRequestSet()) {
        //
        // // send back seRequestSet
        // return transportDto
        // .nextTransportDTO(new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(),
        // JsonParser.getGson()
        // .toJson((virtualReader.getSession()).getSeRequestSet()),
        // true, virtualReader.getSession().getSessionId()));
        // } else {
        // return transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
        // }
        //
        // } catch (KeypleReaderNotFoundException e) {
        // return transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(keypleDto.getAction(),
        // e, keypleDto.getSessionId(), keypleDto.getNativeReaderName(),
        // keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));
        // }

        return transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());

    }
}

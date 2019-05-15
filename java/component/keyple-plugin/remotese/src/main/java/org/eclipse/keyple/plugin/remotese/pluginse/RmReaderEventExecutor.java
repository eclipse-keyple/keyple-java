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

import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;

class RmReaderEventExecutor implements RemoteMethodExecutor {

    private final VirtualReader virtualReader;
    private final RemoteSePlugin remoteSePlugin;

    public RmReaderEventExecutor(VirtualReader virtualReader, RemoteSePlugin remoteSePlugin) {
        this.virtualReader = virtualReader;
        this.remoteSePlugin = remoteSePlugin;
    }

    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();

        // parseResponse body
        ReaderEvent event = JsonParser.getGson().fromJson(keypleDto.getBody(), ReaderEvent.class);

        // substitute native reader name by virtual reader name

        ReaderEvent virtualEvent =
                new ReaderEvent(remoteSePlugin.getName(), virtualReader.getName(),
                        event.getEventType(), event.getDefaultSelectionsResponse());

        // dispatch reader event
        virtualReader.onRemoteReaderEvent(virtualEvent);

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
        // keypleDto.getVirtualReaderName(), keypleDto.getRequesterNodeId()));
        // }

        return transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());

    }
}

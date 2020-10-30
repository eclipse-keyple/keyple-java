/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.pluginse;

import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.plugin.remotese.rm.IRemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodName;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;

/** Transform and propagate the reader event to the virtual reader */
class RmReaderEventExecutor implements IRemoteMethodExecutor {

  private final RemoteSePluginImpl remoteSePlugin;

  @Override
  public RemoteMethodName getMethodName() {
    return RemoteMethodName.READER_EVENT;
  }

  public RmReaderEventExecutor(RemoteSePluginImpl remoteSePlugin) {
    this.remoteSePlugin = remoteSePlugin;
  }

  @Override
  public TransportDto execute(TransportDto transportDto) {
    KeypleDto keypleDto = transportDto.getKeypleDTO();

    // parseResponse body
    ReaderEvent event = JsonParser.getGson().fromJson(keypleDto.getBody(), ReaderEvent.class);

    // substitute native reader name by virtual reader name
    ReaderEvent virtualEvent =
        new ReaderEvent(
            remoteSePlugin.getName(),
            RemoteSePluginImpl.generateReaderName(
                event.getReaderName(), keypleDto.getRequesterNodeId()),
            event.getEventType(),
            event.getDefaultSelectionsResponse());

    // dispatch reader event
    try {
      remoteSePlugin.onReaderEvent(virtualEvent);

      return transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse(null));
    } catch (KeypleReaderNotFoundException e) {
      // reader not found;
      throw new IllegalStateException(
          "Virtual Reader was not found while processing a reader event", e);
    }
  }
}

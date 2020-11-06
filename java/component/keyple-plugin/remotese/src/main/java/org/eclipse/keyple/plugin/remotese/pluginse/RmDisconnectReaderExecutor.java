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

import com.google.gson.JsonObject;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.plugin.remotese.rm.IRemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodName;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Execute the disconnect Reader on Remote Card plugin */
class RmDisconnectReaderExecutor implements IRemoteMethodExecutor {

  private static final Logger logger = LoggerFactory.getLogger(RmDisconnectReaderExecutor.class);

  @Override
  public RemoteMethodName getMethodName() {
    return RemoteMethodName.READER_DISCONNECT;
  }

  private final RemoteSePluginImpl plugin;

  public RmDisconnectReaderExecutor(RemoteSePluginImpl plugin) {
    this.plugin = plugin;
  }

  @Override
  public TransportDto execute(TransportDto transportDto) {
    KeypleDto keypleDto = transportDto.getKeypleDTO();

    String nativeReaderName = keypleDto.getNativeReaderName();

    try {
      plugin.removeVirtualReader(
          nativeReaderName, transportDto.getKeypleDTO().getRequesterNodeId());
      JsonObject body = new JsonObject();
      body.addProperty("status", true);
      return transportDto.nextTransportDTO(
          KeypleDtoHelper.buildResponse(
              getMethodName().getName(),
              JsonParser.getGson().toJson(body, JsonObject.class),
              null,
              nativeReaderName,
              null,
              keypleDto.getTargetNodeId(),
              keypleDto.getRequesterNodeId(),
              keypleDto.getId()));
    } catch (KeypleReaderNotFoundException e) {
      logger.warn("Impossible to disconnect reader {}, reader is not connected ", nativeReaderName);
      return transportDto.nextTransportDTO(
          KeypleDtoHelper.ExceptionDTO(
              getMethodName().getName(),
              e,
              keypleDto.getSessionId(),
              keypleDto.getNativeReaderName(),
              keypleDto.getVirtualReaderName(),
              keypleDto.getTargetNodeId(),
              keypleDto.getRequesterNodeId(),
              keypleDto.getId()));
    }
  }
}

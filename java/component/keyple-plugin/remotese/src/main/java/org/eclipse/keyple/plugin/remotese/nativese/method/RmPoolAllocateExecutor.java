/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.nativese.method;

import com.google.gson.JsonObject;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.exception.KeypleAllocationNoReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleAllocationReaderException;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.plugin.remotese.rm.IRemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodName;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;

public class RmPoolAllocateExecutor implements IRemoteMethodExecutor {

  ReaderPoolPlugin poolPlugin;
  String slaveNodeId;

  @Override
  public RemoteMethodName getMethodName() {
    return RemoteMethodName.POOL_ALLOCATE_READER;
  }

  public RmPoolAllocateExecutor(ReaderPoolPlugin poolPlugin, String slaveNodeId) {
    this.poolPlugin = poolPlugin;
    this.slaveNodeId = slaveNodeId;
  }

  @Override
  public TransportDto execute(TransportDto transportDto) {

    KeypleDto keypleDto = transportDto.getKeypleDTO();
    TransportDto out = null;
    SeResponse seResponse = null;

    // Extract info from keypleDto
    JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);
    String groupReference = body.get("groupReference").getAsString();

    // Execute Remote Method
    Reader reader = null;
    try {
      reader = poolPlugin.allocateReader(groupReference);
    } catch (KeypleAllocationReaderException e) {
      // if an exception occurs, send it into a keypleDto to the Master
      return transportDto.nextTransportDTO(
          KeypleDtoHelper.ExceptionDTO(
              getMethodName().getName(),
              e,
              null,
              null,
              null,
              keypleDto.getTargetNodeId(),
              keypleDto.getRequesterNodeId(),
              keypleDto.getId()));
    } catch (KeypleAllocationNoReaderException e) {
      // if an exception occurs, send it into a keypleDto to the Master
      return transportDto.nextTransportDTO(
          KeypleDtoHelper.ExceptionDTO(
              getMethodName().getName(),
              e,
              null,
              null,
              null,
              keypleDto.getTargetNodeId(),
              keypleDto.getRequesterNodeId(),
              keypleDto.getId()));
    }

    // Build Response
    JsonObject bodyResp = new JsonObject();
    bodyResp.addProperty("nativeReaderName", reader.getName());
    bodyResp.addProperty("isContactless", reader.isContactless());

    out =
        transportDto.nextTransportDTO(
            KeypleDtoHelper.buildResponse(
                getMethodName().getName(), //
                bodyResp.toString(), //
                null, // no session yet
                reader.getName(), //
                null, // no virtualreader yet
                keypleDto.getTargetNodeId(), //
                slaveNodeId, // nodeId of the actual slave dtoNode, useful for load
                // balancing
                keypleDto.getId()));

    return out;
  }
}

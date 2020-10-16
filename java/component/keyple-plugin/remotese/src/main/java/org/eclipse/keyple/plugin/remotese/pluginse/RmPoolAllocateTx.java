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
package org.eclipse.keyple.plugin.remotese.pluginse;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.core.reader.Reader;
import org.eclipse.keyple.core.reader.exception.KeypleReaderException;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.rm.AbstractRemoteMethodTx;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodName;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmPoolAllocateTx extends AbstractRemoteMethodTx<Reader> {

  private static final Logger logger = LoggerFactory.getLogger(RmPoolAllocateTx.class);

  String groupReference;
  RemoteSePoolPluginImpl virtualPoolPlugin;
  DtoSender dtoSender;

  public RmPoolAllocateTx(
      String groupReference,
      RemoteSePoolPluginImpl virtualPoolPlugin,
      DtoSender dtoSender,
      String slaveNodeId,
      String requesterNodeId) {
    super(null, null, null, slaveNodeId, requesterNodeId);
    this.groupReference = groupReference;
    this.dtoSender = dtoSender;
    this.virtualPoolPlugin = virtualPoolPlugin;
  }

  @Override
  public RemoteMethodName getMethodName() {
    return RemoteMethodName.POOL_ALLOCATE_READER;
  }

  @Override
  protected KeypleDto dto() {
    JsonObject body = new JsonObject();
    body.addProperty("groupReference", groupReference);

    return KeypleDtoHelper.buildRequest(
        getMethodName().getName(),
        body.toString(),
        null,
        null,
        null,
        requesterNodeId,
        targetNodeId,
        id);
  }

  @Override
  protected Reader parseResponse(KeypleDto keypleDto) {
    logger.trace("KeypleDto : {}", keypleDto);
    if (KeypleDtoHelper.containsException(keypleDto)) {
      logger.trace("KeypleDto contains an exception: {}", keypleDto);
      KeypleReaderException ex =
          JsonParser.getGson().fromJson(keypleDto.getError(), KeypleReaderException.class);
      throw new KeypleRemoteException(
          "An exception occurs while calling the remote method transmitSet", ex);
    } else {
      logger.trace("KeypleDto contains a response: {}", keypleDto);

      JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);
      Boolean isContactless = body.get("isContactless").getAsBoolean();
      String slaveNodeId = keypleDto.getRequesterNodeId();
      String nativeReaderName = keypleDto.getNativeReaderName();

      // create the Virtual Reader related to the Reader Allocation
      try {
        // options are not supported in this mode
        Map<String, String> options = new HashMap<String, String>();

        VirtualReaderImpl virtualReader =
            (VirtualReaderImpl)
                this.virtualPoolPlugin.createVirtualReader(
                    slaveNodeId, nativeReaderName, this.dtoSender, isContactless, false, options);

        return virtualReader;

      } catch (KeypleReaderException e) {
        throw new KeypleRemoteException(e.getMessage());
      }
    }
  }
}

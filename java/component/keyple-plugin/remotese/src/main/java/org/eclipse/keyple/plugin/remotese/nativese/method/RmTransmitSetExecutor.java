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
package org.eclipse.keyple.plugin.remotese.nativese.method;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.reader.MultiSelectionProcessing;
import org.eclipse.keyple.core.reader.exception.KeypleReaderException;
import org.eclipse.keyple.core.reader.message.CardRequest;
import org.eclipse.keyple.core.reader.message.CardResponse;
import org.eclipse.keyple.core.reader.message.ChannelControl;
import org.eclipse.keyple.core.reader.message.ProxyReader;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.rm.IRemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodName;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute the TransmitSet on Native Reader from KeypleDto
 *
 * <p>See {@link org.eclipse.keyple.plugin.remotese.pluginse.method.RmTransmitTx}
 */
public class RmTransmitSetExecutor implements IRemoteMethodExecutor {

  private static final Logger logger = LoggerFactory.getLogger(RmTransmitSetExecutor.class);

  private final SlaveAPI slaveAPI;

  @Override
  public RemoteMethodName getMethodName() {
    return RemoteMethodName.READER_TRANSMIT_SET;
  }

  public RmTransmitSetExecutor(SlaveAPI slaveAPI) {
    this.slaveAPI = slaveAPI;
  }

  @Override
  public TransportDto execute(TransportDto transportDto) {
    KeypleDto keypleDto = transportDto.getKeypleDTO();
    TransportDto out = null;
    List<CardResponse> cardResponse = null;
    MultiSelectionProcessing multiSelectionProcessing;
    ChannelControl channelControl;

    // parse body
    JsonObject bodyJsonO = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);

    // extract info
    multiSelectionProcessing =
        MultiSelectionProcessing.valueOf(bodyJsonO.get("multiSelectionProcessing").getAsString());

    channelControl = ChannelControl.valueOf(bodyJsonO.get("channelControl").getAsString());

    List<CardRequest> cardRequests =
        JsonParser.getGson()
            .fromJson(
                bodyJsonO.get("cardRequests").getAsString(),
                new TypeToken<ArrayList<CardRequest>>() {}.getType());

    // prepare transmitSet on nativeReader
    String nativeReaderName = keypleDto.getNativeReaderName();
    logger.trace(
        "Execute locally cardRequests : {} with params {} {}",
        cardRequests,
        channelControl,
        multiSelectionProcessing);

    try {
      // find native reader by name
      ProxyReader reader = (ProxyReader) slaveAPI.findLocalReader(nativeReaderName);

      // execute transmitSet
      cardResponse =
          reader.transmitCardRequests(cardRequests, multiSelectionProcessing, channelControl);

      // prepare response
      String parseBody =
          JsonParser.getGson()
              .toJson(cardResponse, new TypeToken<ArrayList<CardResponse>>() {}.getType());
      out =
          transportDto.nextTransportDTO(
              KeypleDtoHelper.buildResponse(
                  getMethodName().getName(),
                  parseBody,
                  keypleDto.getSessionId(),
                  nativeReaderName,
                  keypleDto.getVirtualReaderName(),
                  keypleDto.getTargetNodeId(),
                  keypleDto.getRequesterNodeId(),
                  keypleDto.getId()));

    } catch (KeypleReaderException e) {
      // if an exception occurs, send it into a keypleDto to the Master
      out =
          transportDto.nextTransportDTO(
              KeypleDtoHelper.ExceptionDTO(
                  getMethodName().getName(),
                  e,
                  keypleDto.getSessionId(),
                  nativeReaderName,
                  keypleDto.getVirtualReaderName(),
                  keypleDto.getTargetNodeId(),
                  keypleDto.getRequesterNodeId(),
                  keypleDto.getId()));
    }

    return out;
  }
}

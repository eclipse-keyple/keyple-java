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

import java.util.Map;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Execute the Connect Reader on Remote Se plugin
 */
class RmConnectReaderExecutor implements RemoteMethodExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RmConnectReaderExecutor.class);

    public RemoteMethod getMethodName() {
        return RemoteMethod.READER_DISCONNECT;
    }

    private final RemoteSePluginImpl plugin;
    private final DtoSender dtoSender;

    public RmConnectReaderExecutor(RemoteSePluginImpl plugin, DtoSender dtoSender) {
        this.plugin = plugin;
        this.dtoSender = dtoSender;
    }


    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();

        JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);

        // parseResponse msg
        String nativeReaderName = keypleDto.getNativeReaderName();
        String slaveNodeId = keypleDto.getRequesterNodeId();
        String tranmissionMode = body.get("transmissionMode").getAsString();
        Map<String, String> options =
                JsonParser.getGson().fromJson(body.get("options").getAsString(), Map.class);


        VirtualReaderImpl virtualReader = null;
        try {
            // create a virtual Reader
            virtualReader =
                    (VirtualReaderImpl) this.plugin.createVirtualReader(slaveNodeId, nativeReaderName,
                            this.dtoSender, TransmissionMode.valueOf(tranmissionMode), options);


            // create response
            JsonObject respBody = new JsonObject();
            respBody.add("sessionId", new JsonPrimitive(virtualReader.getSession().getSessionId()));

            // build transport DTO with body
            return transportDto.nextTransportDTO(
                    KeypleDtoHelper.buildResponse(keypleDto.getAction(), respBody.toString(),
                            virtualReader.getSession().getSessionId(), nativeReaderName,
                            virtualReader.getName(), transportDto.getKeypleDTO().getTargetNodeId(),
                            slaveNodeId, keypleDto.getId()));

        } catch (KeypleReaderException e) {
            // virtual reader for remote reader already exists
            logger.warn("Virtual reader already exists for reader " + nativeReaderName, e);

            // send the exception inside the dto
            return transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(keypleDto.getAction(),
                    e, null, nativeReaderName, null, transportDto.getKeypleDTO().getTargetNodeId(),
                    slaveNodeId, keypleDto.getId()));

        } catch (IllegalArgumentException e) {
            // virtual reader for remote reader already exists
            logger.warn("Transmission mode is illegal " + nativeReaderName, e);

            // send the exception inside the dto
            return transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(keypleDto.getAction(),
                    e, null, nativeReaderName, null, keypleDto.getTargetNodeId(),
                    keypleDto.getRequesterNodeId(), keypleDto.getId()));

        }
    }
}

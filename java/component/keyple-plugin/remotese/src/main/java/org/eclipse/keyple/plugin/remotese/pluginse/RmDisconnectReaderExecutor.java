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

import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;


class RmDisconnectReaderExecutor implements RemoteMethodExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RmDisconnectReaderExecutor.class);


    private final RemoteSePlugin plugin;

    public RmDisconnectReaderExecutor(RemoteSePlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();

        String nativeReaderName = keypleDto.getNativeReaderName();
        String clientNodeId = keypleDto.getRequesterNodeId();
        String sessionId = keypleDto.getSessionId();

        try {
            // todo use sessionId is present
            plugin.disconnectRemoteReader(nativeReaderName);
            JsonObject body = new JsonObject();
            body.addProperty("status", true);
            return transportDto
                    .nextTransportDTO(new KeypleDto(RemoteMethod.READER_DISCONNECT.getName(),
                            JsonParser.getGson().toJson(body, JsonObject.class), false, null,
                            nativeReaderName, null, clientNodeId));
        } catch (KeypleReaderNotFoundException e) {
            logger.error("Impossible to disconnect reader " + nativeReaderName, e);
            return transportDto.nextTransportDTO(
                    KeypleDtoHelper.ExceptionDTO(RemoteMethod.READER_DISCONNECT.getName(), e,
                            keypleDto.getSessionId(), keypleDto.getNativeReaderName(),
                            keypleDto.getVirtualReaderName(), keypleDto.getRequesterNodeId()));
        }

    }
}

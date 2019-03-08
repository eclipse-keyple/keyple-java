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
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class RmConnectReaderExecutor implements RemoteMethodExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RmConnectReaderExecutor.class);


    private final RemoteSePlugin plugin;
    private final DtoSender dtoSender;

    public RmConnectReaderExecutor(RemoteSePlugin plugin, DtoSender dtoSender) {
        this.plugin = plugin;
        this.dtoSender = dtoSender;
    }


    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();

        // parseResponse msg
        String nativeReaderName = keypleDto.getNativeReaderName();
        String clientNodeId = keypleDto.getNodeId();

        VirtualReader virtualReader = null;
        try {
            // create a virtual Reader
            virtualReader = (VirtualReader) this.plugin.createVirtualReader(clientNodeId,
                    nativeReaderName, this.dtoSender);

            // create response
            JsonObject respBody = new JsonObject();
            respBody.add("statusCode", new JsonPrimitive(0));
            respBody.add("sessionId", new JsonPrimitive(virtualReader.getSession().getSessionId()));

            // build transport DTO with body
            return transportDto.nextTransportDTO(new KeypleDto(keypleDto.getAction(),
                    respBody.toString(), false, virtualReader.getSession().getSessionId(),
                    nativeReaderName, virtualReader.getName(), clientNodeId));

        } catch (KeypleReaderException e) {
            // virtual reader for remote reader already exists
            logger.warn("Virtual reader already exists for reader " + nativeReaderName, e);

            // send the exception inside the dto
            return transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(keypleDto.getAction(),
                    e, null, nativeReaderName, null, clientNodeId));

        }
    }
}

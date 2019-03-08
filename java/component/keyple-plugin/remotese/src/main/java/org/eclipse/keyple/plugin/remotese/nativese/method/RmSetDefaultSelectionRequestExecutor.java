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
package org.eclipse.keyple.plugin.remotese.nativese.method;

import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.eclipse.keyple.seproxy.event.DefaultSelectionRequest;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class RmSetDefaultSelectionRequestExecutor implements RemoteMethodExecutor {

    private static final Logger logger =
            LoggerFactory.getLogger(RmSetDefaultSelectionRequestExecutor.class);

    private final NativeReaderServiceImpl nativeReaderService;

    public RmSetDefaultSelectionRequestExecutor(NativeReaderServiceImpl nativeReaderService) {
        this.nativeReaderService = nativeReaderService;
    }


    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();

        // Extract info from keypleDto
        String body = keypleDto.getBody();
        JsonObject jsonObject = JsonParser.getGson().fromJson(body, JsonObject.class);

        JsonPrimitive selectionRequestJson =
                jsonObject.getAsJsonPrimitive("defaultSelectionRequest");
        JsonPrimitive notificationModeJson = jsonObject.getAsJsonPrimitive("notificationMode");

        logger.trace("DefaultSelectionRequest : {}", selectionRequestJson.getAsString());
        logger.trace("Notification Mode : {}", notificationModeJson.getAsString());

        DefaultSelectionRequest defaultSelectionRequest = JsonParser.getGson()
                .fromJson(selectionRequestJson.getAsString(), DefaultSelectionRequest.class);
        ObservableReader.NotificationMode notificationMode =
                ObservableReader.NotificationMode.get(notificationModeJson.getAsString());

        String nativeReaderName = keypleDto.getNativeReaderName();
        logger.trace("Execute locally SetDefaultSelectionRequest : {} - {}", notificationMode,
                defaultSelectionRequest);

        try {
            // find native reader by name
            ProxyReader reader = nativeReaderService.findLocalReader(nativeReaderName);

            if (reader instanceof ObservableReader) {
                logger.debug(reader.getName()
                        + " is an ObservableReader, invoke setDefaultSelectionRequest on it");
                ((ObservableReader) reader).setDefaultSelectionRequest(defaultSelectionRequest,
                        notificationMode);

                // prepare response
                String parseBody = "{}";
                return transportDto.nextTransportDTO(
                        new KeypleDto(RemoteMethod.DEFAULT_SELECTION_REQUEST.getName(), parseBody,
                                false, keypleDto.getSessionId(), nativeReaderName,
                                keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));
            } else {
                throw new KeypleReaderException(
                        "Reader is not observable, can not invoke SetDefaultSelectionRequest on "
                                + nativeReaderName);
            }


        } catch (KeypleReaderException e) {
            // if an exception occurs, send it into a keypleDto to the Master
            return transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(
                    RemoteMethod.DEFAULT_SELECTION_REQUEST.getName(), e, keypleDto.getSessionId(),
                    nativeReaderName, keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));
        }
    }
}

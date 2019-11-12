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

import org.eclipse.keyple.core.seproxy.event.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequestImpl;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Execute the Default Selection Request on Native Reader
 */
public class RmSetDefaultSelectionRequestExecutor implements RemoteMethodExecutor {

    private static final Logger logger =
            LoggerFactory.getLogger(RmSetDefaultSelectionRequestExecutor.class);

    @Override
    public RemoteMethod getMethodName() {
        return RemoteMethod.DEFAULT_SELECTION_REQUEST;
    }

    private final SlaveAPI slaveAPI;

    public RmSetDefaultSelectionRequestExecutor(SlaveAPI slaveAPI) {
        this.slaveAPI = slaveAPI;
    }


    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();

        // Extract info from keypleDto
        String body = keypleDto.getBody();
        JsonObject jsonObject = JsonParser.getGson().fromJson(body, JsonObject.class);

        //Selection Request
        String selectionRequestJson =
                jsonObject.getAsJsonPrimitive("defaultSelectionsRequest").getAsString();
        DefaultSelectionsRequest defaultSelectionsRequest =
                (DefaultSelectionsRequest) JsonParser.getGson().fromJson(
                        selectionRequestJson, DefaultSelectionsRequestImpl.class);

        //Notification Mode
        ObservableReader.NotificationMode notificationMode =
                ObservableReader.NotificationMode.get(jsonObject.getAsJsonPrimitive("notificationMode").getAsString());

        //Polling Mode can be set or not.
        String pollingModeJson = jsonObject.get("pollingMode").getAsString();
        ObservableReader.PollingMode pollingMode = null;
        Boolean hasPollingMode = false;
        if(!pollingModeJson.equals(KeypleDtoHelper.notSpecified())){
             pollingMode = ObservableReader.PollingMode.valueOf(pollingModeJson);
             hasPollingMode =  true;
        }

        String nativeReaderName = keypleDto.getNativeReaderName();

        logger.debug("Execute locally SetDefaultSelectionRequest : {} - {} - {}",
                notificationMode,
                hasPollingMode ? pollingMode : KeypleDtoHelper.notSpecified(),
                defaultSelectionsRequest.getSelectionSeRequestSet());

        try {
            // find native reader by name
            ProxyReader reader = slaveAPI.findLocalReader(nativeReaderName);

            if (reader instanceof ObservableReader) {
                logger.debug(reader.getName()
                        + " is an ObservableReader, invoke setDefaultSelectionRequest on it");

                //invoke a different method if polling Mode was set
                if(hasPollingMode){
                    //this method has a different behaviour with the parameter pollingMode
                    ((ObservableReader) reader).setDefaultSelectionRequest(defaultSelectionsRequest,
                            notificationMode, pollingMode);
                }else{
                    ((ObservableReader) reader).setDefaultSelectionRequest(defaultSelectionsRequest,
                            notificationMode);
                }

                // prepare response
                String parseBody = "{}";
                return transportDto
                        .nextTransportDTO(KeypleDtoHelper.buildResponse(getMethodName().getName(),
                                parseBody, keypleDto.getSessionId(), nativeReaderName,
                                keypleDto.getVirtualReaderName(), keypleDto.getTargetNodeId(),
                                keypleDto.getRequesterNodeId(), keypleDto.getId()));
            } else {
                throw new KeypleReaderException(
                        "Reader is not observable, can not invoke SetDefaultSelectionRequest on "
                                + nativeReaderName);
            }


        } catch (KeypleReaderException e) {
            // if an exception occurs, send it into a keypleDto to the Master
            return transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(
                    getMethodName().getName(), e, keypleDto.getSessionId(), nativeReaderName,
                    keypleDto.getVirtualReaderName(), keypleDto.getTargetNodeId(),
                    keypleDto.getRequesterNodeId(), keypleDto.getId()));
        }
    }
}

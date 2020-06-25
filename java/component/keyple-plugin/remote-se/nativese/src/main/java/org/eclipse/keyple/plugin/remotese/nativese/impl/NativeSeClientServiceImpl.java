/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.nativese.impl;

import java.util.List;
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.plugin.remotese.core.*;
import org.eclipse.keyple.plugin.remotese.core.impl.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.eclipse.keyple.plugin.remotese.nativese.RemoteServiceParameters;
import com.google.gson.JsonObject;

/**
 * Singleton instance of the NativeSeClientService
 *
 * @since 1.0
 */
final class NativeSeClientServiceImpl extends AbstractNativeSeService
        implements ObservableReader.ReaderObserver, NativeSeClientService {

    private Boolean withReaderObservation;

    private static NativeSeClientServiceImpl uniqueInstance;// TODO make singleton thread safe

    //private constructor
    private NativeSeClientServiceImpl(Boolean withReaderObservation) {
        this.withReaderObservation = withReaderObservation;
    }

    /**
     * Create an instance of this singleton service
     * @param withReaderObservation true is observation should be activated
     * @return a not null instance of the singleton
     */
    static NativeSeClientServiceImpl createInstance(boolean withReaderObservation) {
        if (uniqueInstance == null) {
            uniqueInstance = new NativeSeClientServiceImpl(withReaderObservation);
        }
        return uniqueInstance;
    }

    /**
     * Retrieve the instance of this singleton service
     * @return a not null instance
     */
    static NativeSeClientServiceImpl getInstance() {
        return uniqueInstance;
    }


    @Override
    public <T extends KeypleUserData> T executeRemoteService(RemoteServiceParameters parameters,
            KeypleUserDataFactory<T> userOutputDataFactory) {
        // check params nullity
        if (parameters == null || userOutputDataFactory == null) {
            throw new IllegalArgumentException("parameter and userOutDataFactory must be set");
        }

        if(isBoundToSyncNode){
            return syncExecuteRemoteService(parameters, userOutputDataFactory);
        }else{
            return asyncExecuteRemoteService(parameters, userOutputDataFactory);
        }

    }

    @Override
    protected void onMessage(KeypleMessageDto msg) {
        // used in async scenario
    }

    /**
     * Propagate Reader Events to RemoteSePlugin (internal use)
     *
     * @param event : event to be propagated
     *
     */
    @Override
    public void update(ReaderEvent event) {
        if(withReaderObservation){

            //prepare body
            String body = KeypleJsonParser.getGson().toJson(event);

            // build keypleMessageDto READER_EVENT
            KeypleMessageDto eventDto = new KeypleMessageDto()
                    .setAction(KeypleMessageDto.Action.READER_EVENT.name())
                    .setBody(body)
                    .setNativeReaderName(event.getReaderName());

            // send keypleMessageDto through the node
            node.sendMessage(eventDto);
        }
    }

    /**
     * Execute the remote service with a sync node
     */
    private <T extends KeypleUserData> T syncExecuteRemoteService(RemoteServiceParameters parameters,
                                                              KeypleUserDataFactory<T> userOutputDataFactory) {

        // build keypleMessageDto EXECUTE_REMOTE_SERVICE with user params
        KeypleMessageDto remoteServiceDto = remoteServiceDto(parameters);

        // send keypleMessageDto through the node
        List<KeypleMessageDto> keypleMessageDtos = node.sendRequest(remoteServiceDto);

        /*
         * two messages are received from the server
         */

        // first message is a READER_CONNECTED from the server notying that the virtual se context has been
        // correctly initialized
        KeypleMessageDto connectResponse = keypleMessageDtos.get(0);

        if(connectResponse.getSessionId() == null
                || connectResponse.getSessionId().isEmpty()){
            //session wasn't created. abort
            //todo throw exception
        }

        // second message should be TRANSMIT, TRANSMIT_SET.. : execute dto request locally with
        // method onMessage
        KeypleMessageDto dtoReceived = keypleMessageDtos.get(1);

        // check server response : while dto contains a request (ie while dto is not
        // TERMINATE_SERVICE, nor error)
        while (!dtoReceived.getAction().equals(KeypleMessageDto.Action.TERMINATE_SERVICE.name())) {

            // check dto is not an error
            if (!dtoReceived.getErrorCode().isEmpty()) {
                // TODO throw exception
            }

            // execute dto request locally
            KeypleMessageDto responseDto = executeRequestDto(dtoReceived);

            // get response dto - send dto response to server
            List<KeypleMessageDto> dtoReceiveds = node.sendRequest(responseDto);

            dtoReceived = dtoReceiveds.get(0);
        }

        // TERMINATE_SERVICE has been received with a userOutputData

        // return userOutputData
        return extractUserData(dtoReceived,userOutputDataFactory);
    }

    /**
     * Execute the remote service with an async node
     */
    private <T extends KeypleUserData> T asyncExecuteRemoteService(RemoteServiceParameters parameters,
                                                                  KeypleUserDataFactory<T> userOutputDataFactory) {
        //todo
        return null;
    }





    private <T extends KeypleUserData> T extractUserData(KeypleMessageDto keypleMessageDto,
                                                         KeypleUserDataFactory<T> userOutputDataFactory) {
        //todo
        return null;
    }

    /*
     * builds Dto for EXECUTE_SERVICE
     */
    KeypleMessageDto remoteServiceDto(RemoteServiceParameters parameters) {
        final AbstractMatchingSe initialSeContent = parameters.getInitialSeContent();
        final KeypleUserData userInputData = parameters.getUserInputData();

        JsonObject body = new JsonObject();

        body.add("initialSeContent", KeypleJsonParser.getGson().toJsonTree(initialSeContent));
        body.add("userInputData", KeypleJsonParser.getGson().toJsonTree(userInputData.toMap()));
        body.add("serviceId", KeypleJsonParser.getGson().toJsonTree(parameters.getServiceId()));

        return new KeypleMessageDto().setAction(KeypleMessageDto.Action.EXECUTE_SERVICE.name())
                .setNativeReaderName(parameters.getNativeReader().getName())
                .setBody(body.getAsString());

    }


}

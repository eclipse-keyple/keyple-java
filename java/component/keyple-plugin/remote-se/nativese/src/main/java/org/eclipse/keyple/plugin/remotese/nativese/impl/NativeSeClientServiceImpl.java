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
 * Singleton instance of the NatieSeClientService
 */
final class NativeSeClientServiceImpl extends AbstractNativeSeService
        implements ObservableReader.ReaderObserver, NativeSeClientService {

    private Boolean withReaderObservation;

    private static NativeSeClientServiceImpl uniqueInstance;// TODO multi thread

    private NativeSeClientServiceImpl(Boolean withReaderObservation) {
        this.withReaderObservation = withReaderObservation;
    }

    static NativeSeClientServiceImpl createInstance(boolean withReaderObservation) {
        if (uniqueInstance == null) {
            uniqueInstance = new NativeSeClientServiceImpl(withReaderObservation);
        }
        return uniqueInstance;
    }

    static NativeSeClientServiceImpl getInstance() {
        return uniqueInstance;
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
        // todo

        // build keypleMessageDto READER_EVENT

        // send keypleMessageDto through the KeypleClientSyncNode

    }

    @Override
    public <T extends KeypleUserData> T executeRemoteService(RemoteServiceParameters parameters,
            KeypleUserDataFactory<T> userOutputDataFactory) {
        // check params nullity
        if (parameters == null || userOutputDataFactory == null) {
            throw new IllegalArgumentException("parameter and userOutDataFactory must be set");
        }

        // todo check if sync or async, maybe split on two implementations

        // build keypleMessageDto EXECUTE_REMOTE_SERVICE with params
        KeypleMessageDto remoteServiceDto = remoteServiceDto(parameters);

        // send keypleMessageDto through the KeypleClientSyncNode
        List<KeypleMessageDto> keypleMessageDtos = node.sendRequest(remoteServiceDto);

        /*
         * two messages are received from the server
         */

        // first message is a READER_CONNECTED from the server that the virtual se context has been
        // initialized
        KeypleMessageDto connectResponse = keypleMessageDtos.get(0);

        // TODO handle connectResponse (ie : isSuccessful?)

        // second message should be TRANSMIT, TRANSMIT_SET.. : execute dto request locally with
        // method onMessage
        KeypleMessageDto dtoReceived = keypleMessageDtos.get(1);

        // check server response : while dto contains a request (ie while dto is not
        // TERMINATE_SERVICE, nor error)
        while (!dtoReceived.getAction().equals(KeypleMessageDto.Action.TERMINATE_SERVICE.name())) {

            // check dto is not an error
            if (!dtoReceived.getErrorCode().isEmpty()) {
                // TODO throw error or exception
            }

            // execute dto request locally
            KeypleMessageDto responseDto = executeRequestDto(dtoReceived);

            // get response dto - send dto response to server
            List<KeypleMessageDto> dtoReceiveds = node.sendRequest(responseDto);

            dtoReceived = dtoReceiveds.get(0);
        }

        // TERMINATE_SERVICE has been received with a userOutputData

        // return userOutputData
        return extractUserData(dtoReceived);
    }


    /**
     * Execute the request Dto on the local nativeReader
     * 
     * @param keypleMessageDto KeypleMessageDto request to be executed locally, must not be null
     * @return KeypleMessageDto response from the local nativeReader
     */
    private KeypleMessageDto executeRequestDto(KeypleMessageDto keypleMessageDto) {
        switch (KeypleMessageDto.Action.valueOf(keypleMessageDto.getAction())) {
            case TRANSMIT:
                return new TransmitExecutor().execute(keypleMessageDto);
            case TRANSMIT_SET:
                return new TransmitSetExecutor().execute(keypleMessageDto);
            default:
                throw new IllegalStateException("No executor found for dto " + keypleMessageDto);
        }
    }


    private <T extends KeypleUserData> T extractUserData(KeypleMessageDto keypleMessageDto) {
        // todo
        return null;
    }


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

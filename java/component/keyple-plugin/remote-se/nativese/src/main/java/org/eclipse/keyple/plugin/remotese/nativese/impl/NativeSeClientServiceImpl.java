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
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.plugin.remotese.core.*;
import org.eclipse.keyple.plugin.remotese.core.impl.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.eclipse.keyple.plugin.remotese.nativese.RemoteServiceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

/**
 * Singleton instance of the NativeSeClientService
 *
 * @since 1.0
 */
final class NativeSeClientServiceImpl extends AbstractNativeSeService
        implements ObservableReader.ReaderObserver, NativeSeClientService {

    private static final Logger logger = LoggerFactory.getLogger(NativeSeClientServiceImpl.class);

    private Boolean withReaderObservation;
    private NativeSeClientServiceFactory.KeypleClientReaderEventFilter eventFilter;

    private static NativeSeClientServiceImpl uniqueInstance;

    // private constructor
    private NativeSeClientServiceImpl(Boolean withReaderObservation,
            NativeSeClientServiceFactory.KeypleClientReaderEventFilter eventFilter) {
        super();
        this.withReaderObservation = withReaderObservation;
        if (withReaderObservation && eventFilter == null) {
            throw new IllegalArgumentException("eventFilter must not be null");
        }
        this.eventFilter = eventFilter;
    }

    /**
     * Create an instance of this singleton service
     * 
     * @param withReaderObservation true is observation should be activated
     * @return a not null instance of the singleton
     */
    static NativeSeClientServiceImpl createInstance(boolean withReaderObservation,
            NativeSeClientServiceFactory.KeypleClientReaderEventFilter eventFilter) {
        if (uniqueInstance == null) {
            uniqueInstance = new NativeSeClientServiceImpl(withReaderObservation, eventFilter);
        }
        return uniqueInstance;
    }

    /**
     * Retrieve the instance of this singleton service
     * 
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

        // get nativeReader
        ProxyReader nativeReader = (ProxyReader) parameters.getNativeReader();

        if (logger.isTraceEnabled()) {
            logger.trace("Execute remoteService {} with on reader {}", parameters.getServiceId(),
                    nativeReader);
        }

        // build keypleMessageDto EXECUTE_REMOTE_SERVICE with user params
        KeypleMessageDto remoteServiceDto = buildMessage(parameters);

        // send keypleMessageDto through the node
        List<KeypleMessageDto> keypleMessageDtos = node.sendRequest(remoteServiceDto);

        /*
         * two messages are received from the server
         */

        // first message is a VIRTUAL_READER_CREATED from the server notifying that the virtual se
        // context
        // has been
        // correctly initialized
        KeypleMessageDto connectResponse = keypleMessageDtos.get(0);

        if (connectResponse.getErrorCode() != null && !connectResponse.getErrorCode().isEmpty()) {
            // error whil created the session
            // todo throw exception
        }

        // start observation if needed
        if (withReaderObservation) {
            if (nativeReader instanceof ObservableReader) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Add NativeSeClientService as an observer for reader {}",
                            nativeReader.getName());
                }
                ((ObservableReader) nativeReader).addObserver(this);
            }
        }

        // second message should be TRANSMIT, TRANSMIT_SET.. : execute dto request locally with
        // method onMessage
        KeypleMessageDto receivedDto = keypleMessageDtos.get(1);

        // check server response : while dto is not a terminate service, execute dto locally and
        // send back response
        while (!receivedDto.getAction().equals(KeypleMessageDto.Action.TERMINATE_SERVICE.name())) {

            // execute dto request locally
            KeypleMessageDto responseDto = executeLocally(nativeReader, receivedDto);

            // get response dto - send dto response to server
            List<KeypleMessageDto> receivedDtos = node.sendRequest(responseDto);

            receivedDto = receivedDtos.get(0);
        }
        // TERMINATE_SERVICE has been received with a userOutputData

        // return userOutputData
        return extractUserData(receivedDto, userOutputDataFactory);
    }

    @Override
    protected void onMessage(KeypleMessageDto msg) {
        // not used
    }

    /**
     * Propagate Reader Events to RemoteSePlugin (internal use)
     * 
     * @param event : event to be propagated (not null)
     */
    @Override
    public void update(ReaderEvent event) {

        // invoke beforeProgagation method to gather userData
        KeypleUserData userData;

        try {
            userData = eventFilter.beforePropagation(event);
        } catch (Exception e) {
            // todo
            // do not throw event
            return;
        }

        KeypleMessageDto eventMessageDto = buildEventMessage(userData, event);

        // send keypleMessageDto through the node
        List<KeypleMessageDto> keypleMessageDtos = node.sendRequest(eventMessageDto);

        // first message should be TRANSMIT, TRANSMIT_SET.. : execute dto request locally with
        // method onMessage
        KeypleMessageDto receivedDto = keypleMessageDtos.get(0);

        // execute KeypleMessage(s) locally
        while (!receivedDto.getAction().equals(KeypleMessageDto.Action.TERMINATE_SERVICE.name())) {

            // check dto is not an error
            if (!receivedDto.getErrorCode().isEmpty()) {
                // TODO throw exception
            }
            ProxyReader nativeReader = findLocalReader(event.getReaderName());

            // execute dto request locally
            KeypleMessageDto responseDto = executeLocally(nativeReader, receivedDto);

            // get response dto - send dto response to server
            List<KeypleMessageDto> receivedDtos = node.sendRequest(responseDto);

            receivedDto = receivedDtos.get(0);
        }

        // TERMINATE_SERVICE has been received with a userOutputData

        // exract userOutputData
        KeypleUserData userOutputData =
                extractUserData(receivedDto, eventFilter.getUserOutputDataFactory());

        // invoke callback
        eventFilter.afterPropagation(userOutputData);

        // todo, invoke on error?
    }


    private <T extends KeypleUserData> T extractUserData(KeypleMessageDto keypleMessageDto,
            KeypleUserDataFactory<T> userOutputDataFactory) {
        // todo
        return null;
    }

    /*
     * builds KeypleMessageDto for EXECUTE_SERVICE
     */
    private KeypleMessageDto buildMessage(RemoteServiceParameters parameters) {
        final AbstractMatchingSe initialSeContent = parameters.getInitialSeContent();
        final KeypleUserData userInputData = parameters.getUserInputData();

        JsonObject body = new JsonObject();

        body.add("serviceId", KeypleJsonParser.getParser().toJsonTree(parameters.getServiceId()));

        if (userInputData != null) {
            body.add("userInputData",
                    KeypleJsonParser.getParser().toJsonTree(userInputData.toMap()));
        }
        if (initialSeContent != null) {
            body.add("initialSeContent", KeypleJsonParser.getParser().toJsonTree(initialSeContent));
        }

        return new KeypleMessageDto().setAction(KeypleMessageDto.Action.EXECUTE_SERVICE.name())
                .setNativeReaderName(parameters.getNativeReader().getName())
                .setBody(body.toString());

    }

    private KeypleMessageDto buildEventMessage(KeypleUserData userInputData,
            ReaderEvent readerEvent) {

        JsonObject body = new JsonObject();

        body.add("userInputData", KeypleJsonParser.getParser().toJsonTree(userInputData.toMap()));
        body.add("readerEvent", KeypleJsonParser.getParser().toJsonTree(readerEvent));

        return new KeypleMessageDto().setAction(KeypleMessageDto.Action.EXECUTE_SERVICE.name())
                .setNativeReaderName(readerEvent.getReaderName()).setBody(body.getAsString());

    }

}

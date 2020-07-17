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

import java.util.UUID;
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.*;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleDoNotPropagateEventException;
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
        uniqueInstance = new NativeSeClientServiceImpl(withReaderObservation, eventFilter);

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
            logger.trace("Execute remoteService {} for native reader {}", parameters.getServiceId(),
                    nativeReader.getName());
        }

        // build keypleMessageDto EXECUTE_REMOTE_SERVICE with user params
        KeypleMessageDto remoteServiceDto = buildRemoteServiceMessage(parameters);

        // send keypleMessageDto through the node
        KeypleMessageDto receivedDto = node.sendRequest(remoteServiceDto);

        // start observation if needed
        if (withReaderObservation) {
            if (nativeReader instanceof ObservableReader) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Add NativeSeClientService as an observer for reader {}",
                            nativeReader.getName());
                }
                ((ObservableReader) nativeReader).addObserver(this);
            } else {
                throw new IllegalArgumentException(
                        "Observation can not be activated because native reader is not observable");
            }
        }

        // check server response : while dto is not a terminate service, execute dto locally and
        // send back response
        while (!receivedDto.getAction().equals(KeypleMessageDto.Action.TERMINATE_SERVICE.name())
                && !receivedDto.getAction().equals(KeypleMessageDto.Action.ERROR.name())) {

            // execute dto request locally
            KeypleMessageDto responseDto = executeLocally(nativeReader, receivedDto);

            // get response dto - send dto response to server
            receivedDto = node.sendRequest(responseDto);

        }

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
        } catch (KeypleDoNotPropagateEventException e) {
            // do not throw event
            logger.trace("Reader event propagation is cancelled by eventFilter");
            return;
        }

        KeypleMessageDto eventMessageDto = buildEventMessage(userData, event);

        // send keypleMessageDto through the node
        KeypleMessageDto receivedDto = node.sendRequest(eventMessageDto);

        // execute KeypleMessage(s) locally
        while (!receivedDto.getAction().equals(KeypleMessageDto.Action.TERMINATE_SERVICE.name())
                && !receivedDto.getAction().equals(KeypleMessageDto.Action.ERROR.name())) {

            ProxyReader nativeReader = findLocalReader(event.getReaderName());

            // execute dto request locally
            KeypleMessageDto responseDto = executeLocally(nativeReader, receivedDto);

            // get response dto - send dto response to server
            receivedDto = node.sendRequest(responseDto);
        }

        // extract userOutputData
        KeypleUserData userOutputData =
                extractUserData(receivedDto, eventFilter.getUserOutputDataFactory());

        // invoke callback
        eventFilter.afterPropagation(userOutputData);
    }


    private <T extends KeypleUserData> T extractUserData(KeypleMessageDto keypleMessageDto,
            KeypleUserDataFactory<T> userOutputDataFactory) {
        JsonObject body =
                KeypleJsonParser.getParser().fromJson(keypleMessageDto.getBody(), JsonObject.class);
        return userOutputDataFactory.getInstance(body.get("userOutputData").getAsString());
    }

    /*
     * builds KeypleMessageDto for EXECUTE_SERVICE
     */
    private KeypleMessageDto buildRemoteServiceMessage(RemoteServiceParameters parameters) {
        final AbstractMatchingSe initialSeContent = parameters.getInitialSeContent();
        final KeypleUserData userInputData = parameters.getUserInputData();

        JsonObject body = new JsonObject();

        body.addProperty("serviceId", parameters.getServiceId());

        if (userInputData != null) {
            body.addProperty("userInputData", userInputData.toJson());
        }
        if (initialSeContent != null) {
            body.addProperty("initialSeContent", initialSeContent.toJson());
        }

        return new KeypleMessageDto()//
                .setAction(KeypleMessageDto.Action.EXECUTE_REMOTE_SERVICE.name())//
                .setSessionId(generateSessionId())//
                .setNativeReaderName(parameters.getNativeReader().getName())//
                .setBody(body.toString());

    }

    private KeypleMessageDto buildEventMessage(KeypleUserData userInputData,
            ReaderEvent readerEvent) {

        JsonObject body = new JsonObject();

        body.addProperty("userInputData", userInputData.toJson());
        body.add("readerEvent", KeypleJsonParser.getParser().toJsonTree(readerEvent));

        return new KeypleMessageDto()//
                .setSessionId(generateSessionId())//
                .setAction(KeypleMessageDto.Action.READER_EVENT.name())//
                .setSessionId(generateSessionId())//
                .setNativeReaderName(readerEvent.getReaderName())//
                .setBody(body.toString());//
    }

    /**
     * Generate a unique session Id
     * 
     * @return not null instance
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

}

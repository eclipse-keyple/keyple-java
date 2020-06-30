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

import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.impl.json.KeypleJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

/**
 * Execute locally a TRANSMIT KeypleMessageDto
 */
class TransmitExecutor implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(TransmitExecutor.class);

    /**
     * Builds a TRANSMIT KeypleMessageDto executor
     */
    TransmitExecutor() {}

    @Override
    public KeypleMessageDto execute(KeypleMessageDto keypleMessageDto) {

        KeypleMessageDto response = null;

        // Extract info from keypleDto
        JsonObject bodyObject =
                KeypleJsonParser.getGson().fromJson(keypleMessageDto.getBody(), JsonObject.class);

        ChannelControl channelControl =
                ChannelControl.valueOf(bodyObject.get("channelControl").getAsString());

        SeRequest seRequest = KeypleJsonParser.getGson()
                .fromJson(bodyObject.get("seRequest").getAsString(), SeRequest.class);

        String nativeReaderName = keypleMessageDto.getNativeReaderName();
        if (logger.isTraceEnabled()) {
            logger.trace("Execute locally seRequest : {} with params {} ", seRequest,
                    channelControl);
        }

        try {
            // find native reader by name
            ProxyReader reader =
                    (ProxyReader) AbstractNativeSeService.findLocalReader(nativeReaderName);

            // execute transmit
            SeResponse seResponse = reader.transmitSeRequest(seRequest, channelControl);

            // prepate response body
            String bodySerialized = KeypleJsonParser.getGson().toJson(seResponse, SeResponse.class);

            response = new KeypleMessageDto().setAction(keypleMessageDto.getAction())
                    .setSessionId(keypleMessageDto.getSessionId())
                    .setClientNodeId(keypleMessageDto.getClientNodeId())
                    .setVirtualReaderName(keypleMessageDto.getVirtualReaderName())
                    .setServerNodeId(keypleMessageDto.getServerNodeId()).setBody(bodySerialized);;

        } catch (KeypleReaderException e) {
            // if an exception occurs, send it into a keypleDto to the Master
            response = new KeypleMessageDto().setAction(keypleMessageDto.getAction())
                    .setSessionId(keypleMessageDto.getSessionId())
                    .setClientNodeId(keypleMessageDto.getClientNodeId())
                    .setVirtualReaderName(keypleMessageDto.getVirtualReaderName())
                    .setServerNodeId(keypleMessageDto.getServerNodeId()).setErrorCode(null)
                    .setErrorMessage(e.getMessage());// todo : handle error code
        }

        return response;
    }
}
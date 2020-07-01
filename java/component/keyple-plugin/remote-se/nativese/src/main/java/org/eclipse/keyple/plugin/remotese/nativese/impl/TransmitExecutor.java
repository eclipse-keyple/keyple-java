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
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
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

    private final ProxyReader reader;

    /**
     * Builds a TRANSMIT KeypleMessageDto executor
     */
    TransmitExecutor(ProxyReader reader) {
        this.reader = reader;
    }

    @Override
    public KeypleMessageDto execute(KeypleMessageDto keypleMessageDto) {

        KeypleMessageDto response = null;

        // Extract info from keypleDto
        JsonObject bodyObject =
                KeypleJsonParser.getParser().fromJson(keypleMessageDto.getBody(), JsonObject.class);

        ChannelControl channelControl =
                ChannelControl.valueOf(bodyObject.get("channelControl").getAsString());

        SeRequest seRequest = KeypleJsonParser.getParser()
                .fromJson(bodyObject.get("seRequest").getAsString(), SeRequest.class);

        if (logger.isTraceEnabled()) {
            logger.trace("Execute locally seRequest : {} with params {} ", seRequest,
                    channelControl);
        }

        try {

            // execute transmit
            SeResponse seResponse = reader.transmitSeRequest(seRequest, channelControl);

            // prepate response body
            String body = KeypleJsonParser.getParser().toJson(seResponse, SeResponse.class);

            response = new KeypleMessageDto().setAction(keypleMessageDto.getAction())
                    .setSessionId(keypleMessageDto.getSessionId())
                    .setClientNodeId(keypleMessageDto.getClientNodeId())
                    .setVirtualReaderName(keypleMessageDto.getVirtualReaderName())
                    .setServerNodeId(keypleMessageDto.getServerNodeId()).setBody(body);;

        } catch (KeypleReaderIOException e) {

            String body = KeypleJsonParser.getParser().toJson(e, KeypleReaderIOException.class);

            // if an exception occurs, send it into a keypleDto to the Master
            response = new KeypleMessageDto().setAction(keypleMessageDto.getAction())
                    .setSessionId(keypleMessageDto.getSessionId())
                    .setClientNodeId(keypleMessageDto.getClientNodeId())
                    .setVirtualReaderName(keypleMessageDto.getVirtualReaderName())
                    .setServerNodeId(keypleMessageDto.getServerNodeId()).setErrorCode(null)
                    .setErrorMessage(e.getMessage()).setBody(body);// todo : handle error code
            // todo KeypleReaderIOException is sent in the body
        }

        return response;
    }
}

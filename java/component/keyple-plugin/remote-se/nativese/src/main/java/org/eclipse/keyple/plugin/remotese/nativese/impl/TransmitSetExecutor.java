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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.impl.json.KeypleJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Execute locally a TRANSMIT KeypleMessageDto
 */
class TransmitSetExecutor implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(TransmitSetExecutor.class);

    /**
     * Builds a TRANSMIT KeypleMessageDto executor
     */
    TransmitSetExecutor() {}

    @Override
    public KeypleMessageDto execute(KeypleMessageDto keypleMessageDto) {
        List<SeResponse> seResponses = null;
        MultiSeRequestProcessing multiSeRequestProcessing;
        ChannelControl channelControl;
        KeypleMessageDto response = null;

        // parse body
        JsonObject bodyJsonO =
                KeypleJsonParser.getGson().fromJson(keypleMessageDto.getBody(), JsonObject.class);

        // extract info
        multiSeRequestProcessing = MultiSeRequestProcessing
                .valueOf(bodyJsonO.get("multiSeRequestProcessing").getAsString());

        channelControl = ChannelControl.valueOf(bodyJsonO.get("channelControl").getAsString());

        List<SeRequest> seRequests =
                KeypleJsonParser.getGson().fromJson(bodyJsonO.get("seRequests").getAsString(),
                        new TypeToken<ArrayList<SeRequest>>() {}.getType());


        // prepare transmitSet on nativeReader
        String nativeReaderName = keypleMessageDto.getNativeReaderName();
        logger.trace("Execute locally seRequests : {} with params {} {}", seRequests,
                channelControl, multiSeRequestProcessing);


        try {
            // find native reader by name
            ProxyReader reader =
                    (ProxyReader) AbstractNativeSeService.findLocalReader(nativeReaderName);

            // execute transmitSet
            seResponses =
                    reader.transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);

            // prepare response body
            String bodySerialized = KeypleJsonParser.getGson().toJson(seResponses,
                    new TypeToken<ArrayList<SeResponse>>() {}.getType());

            // build response Dto
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
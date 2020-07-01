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

import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.impl.json.KeypleJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

/**
 * Execute locally a TRANSMIT KeypleMessageDto
 */
class DefaultSelectionExecutor implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSelectionExecutor.class);

    private final ProxyReader reader;

    /**
     * Builds a TRANSMIT KeypleMessageDto executor
     */
    DefaultSelectionExecutor(ProxyReader reader) {
        this.reader = reader;
    }

    @Override
    public KeypleMessageDto execute(KeypleMessageDto keypleMessageDto) {
        // init value
        ObservableReader.PollingMode pollingMode = null;
        Boolean hasPollingMode = false;
        DefaultSelectionsRequest defaultSelectionsRequest = null;
        ObservableReader.NotificationMode notificationMode = null;

        // Extract info from keypleDto
        String body = keypleMessageDto.getBody();
        JsonObject jsonObject = KeypleJsonParser.getParser().fromJson(body, JsonObject.class);

        // Selection Request
        String selectionRequestJson =
                jsonObject.getAsJsonPrimitive("defaultSelectionsRequest").getAsString();
        defaultSelectionsRequest = KeypleJsonParser.getParser().fromJson(selectionRequestJson,
                DefaultSelectionsRequest.class);

        // Notification Mode
        notificationMode = ObservableReader.NotificationMode
                .get(jsonObject.getAsJsonPrimitive("notificationMode").getAsString());

        // Polling Mode can be set or not.
        String pollingModeJson = jsonObject.get("pollingMode").getAsString();

        if (!pollingModeJson.equals(KeypleMessageDto.Value.NONE.name())) {
            pollingMode = ObservableReader.PollingMode.valueOf(pollingModeJson);
            hasPollingMode = true;
        }

        logger.debug("Execute locally SetDefaultSelectionRequest : {} - {} - {}", notificationMode,
                hasPollingMode ? pollingMode : KeypleMessageDto.Value.NONE.name(),
                defaultSelectionsRequest.getSelectionSeRequests());

        if (reader instanceof ObservableReader) {
            logger.debug(reader.getName()
                    + " is an ObservableReader, invoke setDefaultSelectionRequest on it");

            // invoke a different method if polling Mode was set
            if (hasPollingMode) {
                // this method has a different behaviour with the parameter pollingMode
                ((ObservableReader) reader).setDefaultSelectionRequest(defaultSelectionsRequest,
                        notificationMode, pollingMode);
            } else {
                ((ObservableReader) reader).setDefaultSelectionRequest(defaultSelectionsRequest,
                        notificationMode);
            }

            // no body in response
            return new KeypleMessageDto().setAction(keypleMessageDto.getAction())
                    .setSessionId(keypleMessageDto.getSessionId())
                    .setNativeReaderName(keypleMessageDto.getNativeReaderName())
                    .setClientNodeId(keypleMessageDto.getClientNodeId())
                    .setServerNodeId(keypleMessageDto.getServerNodeId())
                    .setVirtualReaderName(keypleMessageDto.getVirtualReaderName());

        } else {
            // error
            return null;
        }
    }
}

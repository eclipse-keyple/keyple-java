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
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Execute locally a TRANSMIT KeypleMessageDto
 */
class DefaultSelectionExecutor implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSelectionExecutor.class);

    private final ObservableReader reader;

    /**
     * Builds a TRANSMIT KeypleMessageDto executor
     */
    DefaultSelectionExecutor(ObservableReader reader) {
        this.reader = reader;
    }

    @Override
    public KeypleMessageDto execute(KeypleMessageDto keypleMessageDto) {
        // Init value
        ObservableReader.PollingMode pollingMode = null;
        Boolean hasPollingMode = false;
        DefaultSelectionsRequest defaultSelectionsRequest = null;
        ObservableReader.NotificationMode notificationMode = null;

        // Extract info from keypleDto
        String body = keypleMessageDto.getBody();
        JsonObject jsonObject = KeypleJsonParser.getParser().fromJson(body, JsonObject.class);

        // Selection Request
        JsonElement selectionRequestJson = jsonObject.get("defaultSelectionsRequest");

        defaultSelectionsRequest = KeypleJsonParser.getParser().fromJson(selectionRequestJson,
                DefaultSelectionsRequest.class);

        // Notification Mode
        notificationMode = ObservableReader.NotificationMode
                .get(jsonObject.getAsJsonPrimitive("notificationMode").getAsString());

        // Polling Mode can be set or not.
        String pollingModeJson = jsonObject.get("pollingMode").getAsString();

        if (pollingModeJson != null) {
            pollingMode = ObservableReader.PollingMode.valueOf(pollingModeJson);
            hasPollingMode = true;

        }

        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Execute locally set DefaultSelectionExecutor on reader : {} with params {} {} {}",
                    reader.getName(), defaultSelectionsRequest, notificationMode,
                    hasPollingMode ? pollingMode : "no-polling-mode");
        }


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
        return new KeypleMessageDto(keypleMessageDto).setBody("{}");
    }
}

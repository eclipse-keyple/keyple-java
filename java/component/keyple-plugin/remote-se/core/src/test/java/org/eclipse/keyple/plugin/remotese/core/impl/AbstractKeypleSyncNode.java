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
package org.eclipse.keyple.plugin.remotese.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import com.google.gson.JsonObject;

public abstract class AbstractKeypleSyncNode {

    KeypleMessageHandlerMock handler;
    KeypleMessageHandlerErrorMock handlerError;
    KeypleMessageDto msg;
    KeypleMessageDto response;
    List<KeypleMessageDto> responses;
    ServerPushEventStrategy pollingEventStrategy;
    ServerPushEventStrategy longPollingEventStrategy;
    String bodyPolling;
    String bodyLongPolling;
    String bodyLongPollingLongTimeout;

    {
        msg = new KeypleMessageDto()//
                .setSessionId("sessionId")//
                .setAction(KeypleMessageDto.Action.EXECUTE_REMOTE_SERVICE.name())//
                .setClientNodeId("clientNodeId");

        response = new KeypleMessageDto(msg);
        response.setServerNodeId("serverNodeId");

        responses = new ArrayList<KeypleMessageDto>();
        responses.add(response);

        pollingEventStrategy =
                new ServerPushEventStrategy(ServerPushEventStrategy.Type.POLLING).setDuration(1);

        longPollingEventStrategy =
                new ServerPushEventStrategy(ServerPushEventStrategy.Type.LONG_POLLING)
                        .setDuration(1);

        JsonObject body = new JsonObject();
        body.addProperty("strategy", ServerPushEventStrategy.Type.POLLING.name());
        bodyPolling = body.toString();

        body = new JsonObject();
        body.addProperty("strategy", ServerPushEventStrategy.Type.LONG_POLLING.name());
        body.addProperty("duration", 1);
        bodyLongPolling = body.toString();

        body = new JsonObject();
        body.addProperty("strategy", ServerPushEventStrategy.Type.LONG_POLLING.name());
        body.addProperty("duration", 5);
        bodyLongPollingLongTimeout = body.toString();
    }

    class KeypleMessageHandlerMock extends AbstractKeypleMessageHandler {
        public List<KeypleMessageDto> messages = new ArrayList<KeypleMessageDto>();

        @Override
        protected void onMessage(KeypleMessageDto msg) {
            messages.add(msg);
        }
    }

    class KeypleMessageHandlerErrorMock extends AbstractKeypleMessageHandler {
        public boolean isError = false;

        @Override
        protected void onMessage(KeypleMessageDto msg) {
            isError = true;
            throw new RuntimeException("Handler error mocked");
        }
    }

    Callable<Boolean> handlerErrorOccurred() {
        return new Callable<Boolean>() {
            public Boolean call() {
                return handlerError.isError;
            }
        };
    }

    void setUp() {
        handler = new KeypleMessageHandlerMock();
        handlerError = new KeypleMessageHandlerErrorMock();
    }
}

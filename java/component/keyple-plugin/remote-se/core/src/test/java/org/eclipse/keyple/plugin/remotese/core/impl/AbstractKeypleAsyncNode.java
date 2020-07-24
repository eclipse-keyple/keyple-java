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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import java.util.concurrent.Callable;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;

public abstract class AbstractKeypleAsyncNode {

    AbstractKeypleMessageHandler handler;
    KeypleMessageDto msg;
    KeypleMessageDto response;
    KeypleMessageDto pluginEvent;
    KeypleMessageDto readerEvent;

    {
        msg = new KeypleMessageDto()//
                .setSessionId("sessionId")//
                .setAction(KeypleMessageDto.Action.EXECUTE_REMOTE_SERVICE.name())//
                .setClientNodeId("clientNodeId")//
                .setServerNodeId("serverNodeId");

        response = new KeypleMessageDto(msg);

        pluginEvent =
                new KeypleMessageDto(msg).setAction(KeypleMessageDto.Action.PLUGIN_EVENT.name());

        readerEvent =
                new KeypleMessageDto(msg).setAction(KeypleMessageDto.Action.READER_EVENT.name());
    }

    void setUp() {
        handler = mock(AbstractKeypleMessageHandler.class);
    }

    Callable<Boolean> threadHasStateTimedWaiting(final Thread thread) {
        return new Callable<Boolean>() {
            public Boolean call() {
                return thread.getState() == Thread.State.TIMED_WAITING;
            }
        };
    }

    void setHandlerError() {
        doThrow(new RuntimeException()).when(handler).onMessage(any(KeypleMessageDto.class));
    }

}

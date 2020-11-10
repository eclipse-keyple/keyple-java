/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remote.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Callable;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.exception.KeypleRemoteCommunicationException;

public abstract class AbstractNodeTest {

  static final String sessionId = "sessionId";

  AbstractMessageHandler handler;

  MessageDto msg;
  MessageDto response;

  {
    msg =
        new MessageDto() //
            .setSessionId(sessionId) //
            .setAction(MessageDto.Action.EXECUTE_REMOTE_SERVICE.name()) //
            .setClientNodeId("clientNodeId") //
            .setServerNodeId("serverNodeId");

    response = new MessageDto(msg);
  }

  void setUp() {
    handler = mock(AbstractMessageHandler.class);
  }

  void setHandlerError() {
    doThrow(new KeypleRemoteCommunicationException("TEST"))
        .when(handler)
        .onMessage(any(MessageDto.class));
  }

  Callable<Boolean> threadHasStateTimedWaiting(final Thread thread) {
    return new Callable<Boolean>() {
      public Boolean call() {
        return thread.getState() == Thread.State.TIMED_WAITING;
      }
    };
  }

  Callable<Boolean> threadHasStateTerminated(final Thread thread) {
    return new Callable<Boolean>() {
      public Boolean call() {
        return thread.getState() == Thread.State.TERMINATED;
      }
    };
  }
}

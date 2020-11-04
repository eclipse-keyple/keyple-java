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
package org.eclipse.keyple.plugin.remote.integration.common.endpoint.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remote.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.plugin.remote.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remote.nativ.impl.NativeClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Async client endpoint. Send and receive asynchronously json serialized {@link KeypleMessageDto}
 * with {@link StubAsyncServerEndpoint}.
 */
public class StubAsyncClientEndpoint implements KeypleClientAsync {

  private static final Logger logger = LoggerFactory.getLogger(StubAsyncClientEndpoint.class);
  final StubAsyncServerEndpoint server;
  final ExecutorService taskPool;
  final Boolean simulateConnectionError;
  final AtomicInteger messageSent = new AtomicInteger();

  public StubAsyncClientEndpoint(StubAsyncServerEndpoint server, Boolean simulateConnectionError) {
    this.server = server;
    this.taskPool = Executors.newCachedThreadPool(new NamedThreadFactory("client-async-pool"));
    this.simulateConnectionError = simulateConnectionError;
    messageSent.set(0);
  }

  /**
   * Receive serialized keyple message dto from the server
   *
   * @param data not null json data
   */
  void onMessage(final String data) {
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            // creer task
            logger.trace("Data received from server : {}", data);
            KeypleMessageDto message = JacksonParser.fromJson(data);
            NativeClientUtils.getAsyncNode().onMessage(message);
          }
        });
  }

  @Override
  public void openSession(String sessionId) {
    NativeClientUtils.getAsyncNode().onOpen(sessionId);
  }

  @Override
  public void sendMessage(final KeypleMessageDto msg) {
    final StubAsyncClientEndpoint thisClient = this;
    if (messageSent.incrementAndGet() == 2 && simulateConnectionError) {
      throw new StubNetworkConnectionException("Simulate a unreachable server exception");
    }
    // creer task
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            String data = JacksonParser.toJson(msg);
            logger.trace("Data sent to server session {} <- {}", msg.getSessionId(), data);
            server.onData(data, thisClient);
          }
        });
  }

  @Override
  public void closeSession(String sessionId) {
    logger.trace("Close session {} to server", sessionId);
    server.close(sessionId);
    // currentSessionIds.remove(sessionId);
    NativeClientUtils.getAsyncNode().onClose(sessionId);
  }
}

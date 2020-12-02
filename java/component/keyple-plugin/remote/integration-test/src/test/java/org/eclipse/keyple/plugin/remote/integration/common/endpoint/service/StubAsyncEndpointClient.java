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
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientUtils;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.plugin.remote.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remote.integration.common.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Async client endpoint to test {@link RemotePluginServer}. Send and receive asynchronously json
 * serialized {@link MessageDto} with {@link StubAsyncEndpointServer}.
 */
public class StubAsyncEndpointClient implements AsyncEndpointClient {

  private static final Logger logger = LoggerFactory.getLogger(StubAsyncEndpointClient.class);
  private final StubAsyncEndpointServer server;
  private final ExecutorService taskPool;
  private final Boolean simulateConnectionError;
  private final AtomicInteger messageSent = new AtomicInteger();
  private final String localServiceName;

  /**
   * Constructor
   *
   * @param server
   * @param simulateConnectionError
   */
  public StubAsyncEndpointClient(
      StubAsyncEndpointServer server, String localServiceName, Boolean simulateConnectionError) {
    this.server = server;
    this.taskPool = Executors.newCachedThreadPool(new NamedThreadFactory("client-async-pool"));
    this.simulateConnectionError = simulateConnectionError;
    this.localServiceName = localServiceName;
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
            MessageDto message = JacksonParser.fromJson(data);
            LocalServiceClientUtils.getAsyncNode(localServiceName).onMessage(message);
          }
        });
  }

  @Override
  public void openSession(String sessionId) {
    LocalServiceClientUtils.getAsyncNode(localServiceName).onOpen(sessionId);
  }

  @Override
  public void sendMessage(final MessageDto msg) {
    final StubAsyncEndpointClient thisClient = this;
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
    LocalServiceClientUtils.getAsyncNode(localServiceName).onClose(sessionId);
  }
}

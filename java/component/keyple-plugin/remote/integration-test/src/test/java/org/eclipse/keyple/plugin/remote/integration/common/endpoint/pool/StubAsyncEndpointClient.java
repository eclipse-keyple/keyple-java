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
package org.eclipse.keyple.plugin.remote.integration.common.endpoint.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.PoolRemotePluginClient;
import org.eclipse.keyple.plugin.remote.impl.PoolRemotePluginClientUtils;
import org.eclipse.keyple.plugin.remote.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Async client endpoint to test {@link PoolRemotePluginClient}. Send and receive asynchronously
 * json serialized {@link MessageDto} with {@link StubAsyncEndpointServer}.
 */
public class StubAsyncEndpointClient implements AsyncEndpointClient {

  private static final Logger logger = LoggerFactory.getLogger(StubAsyncEndpointClient.class);
  private final StubAsyncEndpointServer server;
  private final ExecutorService taskPool;

  public StubAsyncEndpointClient(StubAsyncEndpointServer server) {
    this.server = server;
    this.taskPool = Executors.newCachedThreadPool(new NamedThreadFactory("client-async-pool"));
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
            logger.trace("Data received from server : {}", data);
            MessageDto message = JacksonParser.fromJson(data);
            PoolRemotePluginClientUtils.getAsyncNode().onMessage(message);
          }
        });
  }

  @Override
  public void openSession(String sessionId) {
    PoolRemotePluginClientUtils.getAsyncNode().onOpen(sessionId);
  }

  @Override
  public void sendMessage(final MessageDto msg) {
    final StubAsyncEndpointClient thisClient = this;
    // submit task to server
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
    PoolRemotePluginClientUtils.getAsyncNode().onClose(sessionId);
  }
}

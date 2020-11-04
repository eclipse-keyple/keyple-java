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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.plugin.remote.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remote.nativ.impl.NativePoolServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulate a async server. Send and receive asynchronously serialized {@link KeypleMessageDto} with
 * connected {@link StubAsyncClientEndpoint}
 */
public class StubAsyncServerEndpoint implements KeypleServerAsync {

  private static final Logger logger = LoggerFactory.getLogger(StubAsyncServerEndpoint.class);
  final Map<String, StubAsyncClientEndpoint> clients; // sessionId_client
  final Map<String, Integer> messageCounts; // sessionId_counts
  final ExecutorService taskPool;

  boolean simulateConnectionError;

  public StubAsyncServerEndpoint() {
    clients = new HashMap<String, StubAsyncClientEndpoint>();
    messageCounts = new HashMap<String, Integer>();
    taskPool = Executors.newCachedThreadPool(new NamedThreadFactory("server-async-pool"));
    simulateConnectionError = false;
  }

  /** Simulate a close socket operation */
  public void close(String sessionId) {
    messageCounts.remove(sessionId);
    clients.remove(sessionId);
    NativePoolServerUtils.getAsyncNode().onClose(sessionId);
  }

  /**
   * Simulate data received by the socket
   *
   * @param jsonData incoming json data
   */
  public void onData(final String jsonData, final StubAsyncClientEndpoint client) {
    final KeypleMessageDto message = JacksonParser.fromJson(jsonData);
    clients.put(message.getSessionId(), client);
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            NativePoolServerUtils.getAsyncNode().onMessage(message);
          }
        });
  }

  @Override
  public void sendMessage(final KeypleMessageDto msg) {
    final String data = JacksonParser.toJson(msg);
    logger.trace("Data sent to client {}", data);
    if (incrementCountInSession(msg.getSessionId()) == 2 && simulateConnectionError) {
      simulateConnectionError = false; // reinit flag
      throw new StubNetworkConnectionException("Simulate a unreachable client exception");
    }
    final StubAsyncClientEndpoint client = clients.get(msg.getSessionId());
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              client.onMessage(data);
            } catch (Throwable t) {
              NativePoolServerUtils.getAsyncNode().onError(msg.getSessionId(), t);
            }
          }
        });
  }

  public void setSimulateConnectionError(Boolean simulateConnectionError) {
    this.simulateConnectionError = simulateConnectionError;
  }

  Integer incrementCountInSession(String sessionId) {
    messageCounts.put(
        sessionId, messageCounts.get(sessionId) == null ? 1 : messageCounts.get(sessionId) + 1);
    return messageCounts.get(sessionId);
  }
}

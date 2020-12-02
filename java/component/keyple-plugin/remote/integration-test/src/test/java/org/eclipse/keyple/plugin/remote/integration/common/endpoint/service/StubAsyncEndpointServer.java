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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.eclipse.keyple.plugin.remote.impl.RemotePluginServerUtils;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.plugin.remote.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remote.integration.common.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulate a async server to test {@link RemotePluginServer}. Send and receive asynchronously
 * serialized {@link MessageDto} with connected {@link StubAsyncEndpointClient}
 */
public class StubAsyncEndpointServer implements AsyncEndpointServer {

  private static final Logger logger = LoggerFactory.getLogger(StubAsyncEndpointServer.class);
  private final Map<String, StubAsyncEndpointClient> clients; // sessionId_client
  private final Map<String, Integer> messageCounts; // sessionId_counts
  private final ExecutorService taskPool;

  private boolean simulateConnectionError;

  public StubAsyncEndpointServer() {
    this.clients = new HashMap<String, StubAsyncEndpointClient>();
    this.messageCounts = new HashMap<String, Integer>();
    this.taskPool = Executors.newCachedThreadPool(new NamedThreadFactory("server-async-pool"));
    this.simulateConnectionError = false;
  }

  /** Simulate a close socket operation */
  void close(String sessionId) {
    messageCounts.remove(sessionId);
    clients.remove(sessionId);
    RemotePluginServerUtils.getAsyncNode().onClose(sessionId);
  }

  /**
   * Simulate data received by the socket
   *
   * @param jsonData incoming json data
   */
  void onData(final String jsonData, final StubAsyncEndpointClient client) {
    final MessageDto message = JacksonParser.fromJson(jsonData);
    clients.put(message.getSessionId(), client);
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            RemotePluginServerUtils.getAsyncNode().onMessage(message);
          }
        });
  }

  @Override
  public void sendMessage(final MessageDto msg) {
    final String data = JacksonParser.toJson(msg);
    logger.trace("Data sent to client {}", data);
    if (incrementCountInSession(msg.getSessionId()) == 2 && simulateConnectionError) {
      simulateConnectionError = false; // reinit flag
      throw new StubNetworkConnectionException("Simulate a unreachable client exception");
    }
    final StubAsyncEndpointClient client = clients.get(msg.getSessionId());
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              client.onMessage(data);
            } catch (Throwable t) {
              RemotePluginServerUtils.getAsyncNode().onError(msg.getSessionId(), t);
            }
          }
        });
  }

  /**
   * Set to true to simulate a connection for the 2nd message received
   *
   * @param simulateConnectionError non nullable Boolean
   */
  public void setSimulateConnectionError(boolean simulateConnectionError) {
    this.simulateConnectionError = simulateConnectionError;
  }

  private Integer incrementCountInSession(String sessionId) {
    messageCounts.put(
        sessionId, messageCounts.get(sessionId) == null ? 1 : messageCounts.get(sessionId) + 1);
    return messageCounts.get(sessionId);
  }
}

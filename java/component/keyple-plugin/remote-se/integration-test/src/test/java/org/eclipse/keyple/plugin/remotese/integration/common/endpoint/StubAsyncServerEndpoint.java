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
package org.eclipse.keyple.plugin.remotese.integration.common.endpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remotese.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulate a async server. Send and receive asynchronously serialized {@link KeypleMessageDto} with
 * connected {@link StubAsyncClientEndpoint}
 */
public class StubAsyncServerEndpoint implements KeypleServerAsync {

private static final Logger logger = LoggerFactory.getLogger(StubAsyncServerEndpoint.class);
  final Map<String, StubAsyncClientEndpoint> clients; // sessionId_client
  final ExecutorService taskPool;
  final String serverNodeId;

  public StubAsyncServerEndpoint() {
    clients = new HashMap<String, StubAsyncClientEndpoint>();
    taskPool = Executors.newCachedThreadPool(new NamedThreadFactory("server-pool"));
    serverNodeId = UUID.randomUUID().toString();
  }

  /** Simulate a open socket operation */
  public void open(String sessionId, StubAsyncClientEndpoint endpoint) {
    clients.put(sessionId, endpoint);
  }

  /** Simulate a close socket operation */
  public void close(String sessionId) {
    clients.remove(sessionId);
    RemoteSeServerUtils.getAsyncNode().onClose(sessionId);
  }

  /**
   * Simulate data received by the socket
   *
   * @param jsonData incoming json data
   */
  public void onData(final String jsonData) {
    final KeypleMessageDto message = JacksonParser.fromJson(jsonData);
    Assert.getInstance().isTrue(clients.containsKey(message.getSessionId()), "Session is not open");
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            RemoteSeServerUtils.getAsyncNode().onMessage(message);
          }
        });
  }

  @Override
  public void sendMessage(final KeypleMessageDto msg) {
    // retrieve socket
    final StubAsyncClientEndpoint client = clients.get(msg.getSessionId());
    msg.setServerNodeId(serverNodeId);
    msg.setClientNodeId(client.getClientNodeId());
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
              try{
                  String data = JacksonParser.toJson(msg);
                  logger.trace("Data sent to client session {} <- {}", msg.getSessionId(), data);
                  client.onMessage(data);
              }catch (Throwable t){
                  RemoteSeServerUtils.getAsyncNode().onError(msg.getSessionId(), t);
              }
          }
        });
  }
}

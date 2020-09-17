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
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerAsyncNode;
import org.eclipse.keyple.plugin.remotese.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerUtils;

public class StubAsyncServerEndpoint implements KeypleServerAsync {

  final Map<String, StubAsyncClientEndpoint> sockets; // socketId_clientId
  final Map<String, String> sessions; //sessionId_socketId
  final ExecutorService taskPool;
  final String serverId;

  public StubAsyncServerEndpoint() {
    sockets = new HashMap<String, StubAsyncClientEndpoint>();
    sessions = new HashMap<String, String>();
    taskPool = Executors.newCachedThreadPool();
    serverId = UUID.randomUUID().toString();
  }

  /**
   * Simulate a open socket operation
   *
   * @return socketId
   */
  public String openSocket(StubAsyncClientEndpoint endpoint) {
    String socketId = UUID.randomUUID().toString();
    sockets.put(socketId, endpoint);
    return socketId;
  }
  /**
   * Simulate a close socket operation
   *
   * @return socketId
   */
  public void closeSocket(String socketId, String sessionId) {
    sockets.remove(socketId);
    sessions.remove(sessionId);
    RemoteSeServerUtils.getAsyncNode().onClose(sessionId);
  }

  /**
   * Simulate data received by the socket
   *
   * @param jsonData incoming json data
   */
  public void onData(final String jsonData, final String socketId) {
    taskPool.submit(
      new Runnable() {
        @Override
        public void run() {
          KeypleMessageDto message = JacksonParser.fromJson(jsonData);
          sessions.put(message.getSessionId(), socketId);
          RemoteSeServerUtils.getAsyncNode().onMessage(message);
        }
      });
  }

  @Override
  public void sendMessage(final KeypleMessageDto msg) {
    // retrieve socket
    final String socketId = sessions.get(msg.getSessionId());
    final StubAsyncClientEndpoint client = sockets.get(socketId);
    msg.setServerNodeId(serverId);
    taskPool.submit(
      new Runnable() {
        @Override
        public void run() {
          client.onMessage(JacksonParser.toJson(msg));
        }
      });
  }
}

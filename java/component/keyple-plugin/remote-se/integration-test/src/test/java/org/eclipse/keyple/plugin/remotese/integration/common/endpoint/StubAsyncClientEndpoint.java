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

import java.util.UUID;

import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remotese.nativese.impl.NativeSeClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubAsyncClientEndpoint implements KeypleClientAsync {

  private static final Logger logger = LoggerFactory.getLogger(StubAsyncClientEndpoint.class);
  final StubAsyncServerEndpoint server;
  private final String clientNodeId;
  private String serverSocketId;

  public StubAsyncClientEndpoint(StubAsyncServerEndpoint server) {
    this.server = server;
    this.clientNodeId = UUID.randomUUID().toString();
  }

  /**
   * Receive serialiezed keyple message dto from the server
   * @param data not null json data
   */
  void onMessage(String data) {
    logger.trace("Data received from server : {}", data);
    KeypleMessageDto message = JacksonParser.fromJson(data);
    message.setClientNodeId(clientNodeId);
    NativeSeClientUtils.getAsyncNode().onMessage(message);
  }

  @Override
  public void openSession(String sessionId) {
    serverSocketId = server.openSocket(this);
    logger.trace("Open session {} to server with socket {}", sessionId, serverSocketId);
    NativeSeClientUtils.getAsyncNode().onOpen(sessionId);
  }

  @Override
  public void sendMessage(KeypleMessageDto msg) {
    msg.setClientNodeId(clientNodeId);
    String data = JacksonParser.toJson(msg);
    logger.trace("Data sent to socket {} <- {}", serverSocketId, data);
    server.onData(data, serverSocketId);
  }

  @Override
  public void closeSession(String sessionId) {
    logger.trace("Close session {} to server with socket {}", sessionId, serverSocketId);
    server.closeSocket(serverSocketId, sessionId);
    serverSocketId = null;
    NativeSeClientUtils.getAsyncNode().onClose(sessionId);
  }
}

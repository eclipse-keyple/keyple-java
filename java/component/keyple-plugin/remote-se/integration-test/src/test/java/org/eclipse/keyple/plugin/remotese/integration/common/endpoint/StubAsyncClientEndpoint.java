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

/**
 * Async client endpoint. Send and receive asynchronously {@link KeypleMessageDto} with {@link StubAsyncServerEndpoint}.
 */
public class StubAsyncClientEndpoint implements KeypleClientAsync {

  private static final Logger logger = LoggerFactory.getLogger(StubAsyncClientEndpoint.class);
  final StubAsyncServerEndpoint server;
  private final String clientNodeId;
  private String currentSessionId;

  public StubAsyncClientEndpoint(StubAsyncServerEndpoint server) {
    this.server = server;
    this.clientNodeId = UUID.randomUUID().toString();
  }

  /**
   * Receive serialized keyple message dto from the server
   * @param data not null json data
   */
  void onMessage(String data) {
    logger.trace("Data received from server : {}", data);
    KeypleMessageDto message = JacksonParser.fromJson(data);
    NativeSeClientUtils.getAsyncNode().onMessage(message);
  }

  @Override
  public void openSession(String sessionId) {
    server.open(sessionId, this);
    this.currentSessionId = sessionId;
    logger.trace("Open session {} to server", currentSessionId);
    NativeSeClientUtils.getAsyncNode().onOpen(currentSessionId);
  }

  @Override
  public void sendMessage(KeypleMessageDto msg) {
    msg.setClientNodeId(clientNodeId);
    String data = JacksonParser.toJson(msg);
    logger.trace("Data sent to server session {} <- {}", currentSessionId, data);
    server.onData(data);
  }

  @Override
  public void closeSession(String sessionId) {
    logger.trace("Close session {} to server", sessionId);
    server.close(sessionId);
    this.currentSessionId = null;
    NativeSeClientUtils.getAsyncNode().onClose(sessionId);
  }

  public String getClientNodeId() {
    return clientNodeId;
  }
}

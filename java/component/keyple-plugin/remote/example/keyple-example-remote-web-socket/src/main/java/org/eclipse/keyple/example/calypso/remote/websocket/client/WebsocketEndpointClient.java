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
package org.eclipse.keyple.example.calypso.remote.websocket.client;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.*;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.example.calypso.remote.websocket.server.WebsocketEndpointServer;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientUtils;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example implementation of a {@link AsyncEndpointClient} based on Web Socket. Interacts with
 * {@link WebsocketEndpointServer}
 */
@ClientEndpoint
public class WebsocketEndpointClient implements AsyncEndpointClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketEndpointClient.class);
  /* opened sessions */
  final Map<String, Session> openSessions;
  /* URI of the server endpoint */
  private final String URI = "http://0.0.0.0:8080/remote-plugin";

  /** Constructor */
  public WebsocketEndpointClient() {
    openSessions = new HashMap<>();
  }

  /**
   * Open a session with the server endpoint
   *
   * @param sessionId id of the session
   */
  @Override
  public void openSession(String sessionId) {
    try {
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      container.connectToServer(this, new URI(URI + "?" + sessionId));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Callback hook for Connection open events.
   *
   * @param session the session which is opened.
   */
  @OnOpen
  public void onOpen(Session session) {
    String sessionId = session.getQueryString();
    LOGGER.trace("Client - Opened socket for sessionId {}", sessionId);
    this.openSessions.put(sessionId, session);
    LocalServiceClientUtils.getAsyncNode().onOpen(sessionId);
  }

  /**
   * Send a keypleMessageDto to the async server endpoint
   *
   * @param keypleMessageDto non nullable instance
   */
  @Override
  public void sendMessage(MessageDto keypleMessageDto) {
    String sessionId = keypleMessageDto.getSessionId();
    this.openSessions
        .get(sessionId)
        .getAsyncRemote()
        .sendText(KeypleJsonParser.getParser().toJson(keypleMessageDto));
  }

  /**
   * Callback hook for Message Events. This method will be invoked when a client send a message.
   *
   * @param data The text message
   */
  @OnMessage
  public void onMessage(String data) {
    LOGGER.trace("Client - Received message {}", data);
    // message received
    MessageDto message = KeypleJsonParser.getParser().fromJson(data, MessageDto.class);
    LocalServiceClientUtils.getAsyncNode().onMessage(message);
  }

  /**
   * Close the session with the server endpoint
   *
   * @param sessionId id of the session to be closed
   */
  @Override
  public void closeSession(String sessionId) {
    try {
      this.openSessions.get(sessionId).close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Callback hook for Connection close events.
   *
   * @param session the session which is getting closed.
   */
  @OnClose
  public void onClose(Session session) {
    String sessionId = session.getQueryString();
    LOGGER.trace("Client - Closed socket for sessionId {}", sessionId);
    openSessions.remove(sessionId);
    LocalServiceClientUtils.getAsyncNode().onClose(sessionId);
  }
}

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
package org.eclipse.keyple.example.calypso.remote.websocket.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.example.calypso.remote.websocket.client.WebsocketClientEndpoint;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remote.virtual.impl.RemoteServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example implementation of {@link KeypleServerAsync} based on Websocket. Interacts with {@link
 * WebsocketClientEndpoint}
 */
@ApplicationScoped
@ServerEndpoint("/remotese-plugin")
public class WebsocketServerEndpoint implements KeypleServerAsync {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketServerEndpoint.class);

  /* opened sessions */
  private final Map<String, Session> openSessions;

  /** constructor */
  public WebsocketServerEndpoint() {
    openSessions = new ConcurrentHashMap<>();
  }

  /**
   * Callback hook for Connection open events.
   *
   * @param session the session which is opened.
   */
  @OnOpen
  public void onOpen(Session session) {
    String sessionId = session.getQueryString();
    LOGGER.trace("Server - Opened socket for sessionId {} : ", sessionId);
    openSessions.put(sessionId, session);
  }

  /**
   * Callback hook for Message Events. This method will be invoked when a client send a message.
   *
   * @param data The text message
   */
  @OnMessage
  public void onMessage(String data) {
    LOGGER.trace("Server - Received message {} : ", data);
    KeypleMessageDto messageDto =
        KeypleJsonParser.getParser().fromJson(data, KeypleMessageDto.class);
    RemoteSeServerUtils.getAsyncNode().onMessage(messageDto);
  }

  /**
   * Callback hook for Connection close events.
   *
   * @param session the session which is getting closed.
   */
  @OnClose
  public void onClose(Session session) {
    String sessionId = session.getQueryString();
    LOGGER.trace("Server - Closed socket for sessionId {} : ", sessionId);
    openSessions.remove(sessionId);
    RemoteSeServerUtils.getAsyncNode().onClose(sessionId);
  }

  /**
   * Send a KeypleMessageDto to the client async endpoint
   *
   * @param keypleMessageDto non nullable instance
   */
  @Override
  public void sendMessage(KeypleMessageDto keypleMessageDto) {
    String sessionId = keypleMessageDto.getSessionId();
    openSessions
        .get(sessionId)
        .getAsyncRemote()
        .sendText(KeypleJsonParser.getParser().toJson(keypleMessageDto));
  }
}

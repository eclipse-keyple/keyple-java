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
package org.eclipse.keyple.example.generic.remote.server.websocket.server;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.example.generic.remote.server.websocket.client.EndpointClient;
import org.eclipse.keyple.plugin.remote.AsyncNodeServer;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.impl.RemotePluginServerUtils;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of a {@link org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer} implementation
 * using Web Sockets.
 *
 * <p>Interacts with the {@link EndpointClient}.
 */
@ApplicationScoped
@ServerEndpoint("/remote-plugin")
public class EndpointServer implements AsyncEndpointServer {

  private static final Logger logger = LoggerFactory.getLogger(EndpointServer.class);

  /** Map of opened sessions by session id */
  private final Map<String, Session> openedSessions;

  /** constructor */
  public EndpointServer() {
    openedSessions = new HashMap<>();
  }

  /**
   * Is invoked by the framework when a server session is opened.
   *
   * @param session the server session which is opened.
   */
  @OnOpen
  public void onOpen(Session session) {

    // Retrieves the session id from the query.
    String sessionId = session.getQueryString();
    logger.trace("Server - Opened socket for sessionId {} : ", sessionId);

    // Associates the server session to its session id.
    openedSessions.put(sessionId, session);
  }

  /** {@inheritDoc} */
  @Override
  public void sendMessage(MessageDto messageDto) {

    // Retrieves the session id from the provided message.
    String sessionId = messageDto.getSessionId();

    // Retrieves the opened server session using the session id.
    Session session = openedSessions.get(sessionId);

    // Serialize the message to send.
    String data = KeypleGsonParser.getParser().toJson(messageDto);

    // Send the message.
    session.getAsyncRemote().sendText(data);
  }

  /**
   * Is invoked by the framework when a message is received from the client.
   *
   * @param data The incoming message.
   */
  @OnMessage
  public void onMessage(String data) {

    logger.trace("Server - Received message {} : ", data);

    // Deserialise the incoming message.
    MessageDto message = KeypleGsonParser.getParser().fromJson(data, MessageDto.class);

    // Retrieves the async node associated to the local service.
    AsyncNodeServer node = RemotePluginServerUtils.getAsyncNode();

    // Forward the message to the node.
    node.onMessage(message);
  }

  /**
   * Is invoked by the framework when the server session is closed.
   *
   * @param session The server session which is getting closed.
   */
  @OnClose
  public void onClose(Session session) {

    // Retrieves the session id from the query.
    String sessionId = session.getQueryString();
    logger.trace("Server - Closed socket for sessionId {}", sessionId);

    // Clean the map of opened sessions.
    openedSessions.remove(sessionId);

    // Retrieves the async node associated to the local service.
    AsyncNodeServer node = RemotePluginServerUtils.getAsyncNode();

    // Forward the event to the node.
    node.onClose(sessionId);
  }

  /**
   * Is invoked by the framework when a session error occurs.
   *
   * @param session The server session which is getting closed.
   * @param error The error.
   */
  @OnError
  public void onError(Session session, Throwable error) {

    // Retrieves the session id from the query.
    String sessionId = session.getQueryString();
    logger.trace("Server - Error socket for sessionId {}", sessionId);

    // Clean the map of opened sessions.
    openedSessions.remove(sessionId);

    // Retrieves the async node associated to the local service.
    AsyncNodeServer node = RemotePluginServerUtils.getAsyncNode();

    // Forward the error to the node.
    node.onError(sessionId, error);
  }
}

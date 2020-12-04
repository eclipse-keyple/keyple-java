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
package org.eclipse.keyple.example.generic.remote.server.websocket.client;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.*;
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.example.generic.remote.server.websocket.server.EndpointServer;
import org.eclipse.keyple.plugin.remote.AsyncNodeClient;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientUtils;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of a {@link org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient} implementation
 * using Web Sockets.
 *
 * <p>Interacts with the {@link EndpointServer}.
 */
@ClientEndpoint
public class EndpointClient implements AsyncEndpointClient {

  private static final Logger logger = LoggerFactory.getLogger(EndpointClient.class);

  /** URI of the endpoint server */
  private static final String URI = "http://0.0.0.0:8080/remote-plugin";

  /** Map of opened sessions by session id */
  private final Map<String, Session> openedSessions;

  /** Constructor */
  public EndpointClient() {
    openedSessions = new HashMap<>();
  }

  /** {@inheritDoc} */
  @Override
  public void openSession(String sessionId) {
    try {
      // Try to open a new session with the server and transmits the provided session id as an URI
      // parameter in order to access it asynchronously on the "onOpen" method.
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      container.connectToServer(this, new URI(URI + "?" + sessionId));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
    logger.trace("Client - Opened socket for sessionId {}", sessionId);

    // Associates the server session to its session id.
    openedSessions.put(sessionId, session);

    // Retrieves the async node associated to the local service.
    AsyncNodeClient node = LocalServiceClientUtils.getAsyncNode();

    // Forward the event to the node.
    node.onOpen(sessionId);
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
   * Is invoked by the framework when a message is received from the server.
   *
   * @param data The incoming message.
   */
  @OnMessage
  public void onMessage(String data) {

    logger.trace("Client - Received message {}", data);

    // Deserialise the incoming message.
    MessageDto message = KeypleGsonParser.getParser().fromJson(data, MessageDto.class);

    // Retrieves the async node associated to the local service.
    AsyncNodeClient node = LocalServiceClientUtils.getAsyncNode();

    // Forward the message to the node.
    node.onMessage(message);
  }

  /** {@inheritDoc} */
  @Override
  public void closeSession(String sessionId) {
    try {
      // Retrieves the opened server session using the session id.
      Session session = openedSessions.get(sessionId);

      // Try to close the server session.
      session.close();

    } catch (IOException e) {
      logger.error("Client - Error while closing the sessionId {}", sessionId, e);
    }
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
    logger.trace("Client - Closed socket for sessionId {}", sessionId);

    // Clean the map of opened sessions.
    openedSessions.remove(sessionId);

    // Retrieves the async node associated to the local service.
    AsyncNodeClient node = LocalServiceClientUtils.getAsyncNode();

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
    logger.trace("Client - Error socket for sessionId {}", sessionId);

    // Clean the map of opened sessions.
    openedSessions.remove(sessionId);

    // Retrieves the async node associated to the local service.
    AsyncNodeClient node = LocalServiceClientUtils.getAsyncNode();

    // Forward the error to the node.
    node.onError(sessionId, error);
  }
}

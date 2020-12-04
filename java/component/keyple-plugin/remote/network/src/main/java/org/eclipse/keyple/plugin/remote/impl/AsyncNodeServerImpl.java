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
package org.eclipse.keyple.plugin.remote.impl;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.AsyncNodeServer;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.NodeCommunicationException;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Async Node Server implementation.
 *
 * @since 1.0
 */
final class AsyncNodeServerImpl extends AbstractNode implements AsyncNodeServer {

  private static final Logger logger = LoggerFactory.getLogger(AsyncNodeServerImpl.class);

  private final AsyncEndpointServer endpoint;
  private final Map<String, SessionManager> sessionManagers;

  /**
   * (package-private)<br>
   *
   * @param handler The associated handler (must be not null).
   * @param endpoint The user server async endpoint (must be not null).
   * @param timeoutInSecond The default timeout (in seconds) to use.
   * @since 1.0
   */
  AsyncNodeServerImpl(
      AbstractMessageHandler handler, AsyncEndpointServer endpoint, int timeoutInSecond) {
    super(handler, timeoutInSecond);
    this.endpoint = endpoint;
    this.sessionManagers = new HashMap<String, SessionManager>();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  void openSession(String sessionId) {
    throw new UnsupportedOperationException("openSession");
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  MessageDto sendRequest(MessageDto msg) {
    msg.setServerNodeId(nodeId);
    SessionManager manager = getManagerForHandler(msg.getSessionId());
    return manager.sendRequest(msg);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  void sendMessage(MessageDto msg) {
    msg.setServerNodeId(nodeId);
    SessionManager manager = getManagerForHandler(msg.getSessionId());
    manager.sendMessage(msg);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  void closeSession(String sessionId) {
    throw new UnsupportedOperationException("closeSession");
  }

  /**
   * (private)<br>
   * Check if the session is active and get the associated session manager.
   *
   * @param sessionId The session id (must be not empty).
   * @return a not null reference.
   * @throws IllegalStateException if the session is not found.
   */
  private SessionManager getManagerForHandler(String sessionId) {
    SessionManager manager = sessionManagers.get(sessionId);
    if (manager == null) {
      throw new IllegalStateException("The node's session [" + sessionId + "] is closed.");
    }
    return manager;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void onMessage(MessageDto msg) {

    Assert.getInstance() //
        .notNull(msg, "msg") //
        .notEmpty(msg.getSessionId(), "sessionId") //
        .notEmpty(msg.getAction(), "action") //
        .notEmpty(msg.getClientNodeId(), "clientNodeId");

    // Get or create a new session manager
    SessionManager manager = sessionManagers.get(msg.getSessionId());
    if (manager == null) {
      manager = new SessionManager(msg.getSessionId());
      sessionManagers.put(msg.getSessionId(), manager);
    }
    manager.onMessage(msg);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void onError(String sessionId, Throwable error) {
    Assert.getInstance().notEmpty(sessionId, "sessionId").notNull(error, "error");
    SessionManager manager = sessionManagers.get(sessionId);
    Assert.getInstance().notNull(manager, "sessionId");
    manager.onError(error);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void onClose(String sessionId) {
    Assert.getInstance().notEmpty(sessionId, "sessionId");
    SessionManager manager = sessionManagers.remove(sessionId);
    Assert.getInstance().notNull(manager, "sessionId");
  }

  /**
   * (private)<br>
   * The inner session manager class.<br>
   * There is one manager by session id.
   */
  private class SessionManager extends AbstractSessionManager {

    /**
     * (private)<br>
     * Constructor
     *
     * @param sessionId The session id to manage.
     */
    private SessionManager(String sessionId) {
      super(sessionId);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    void checkIfExternalErrorOccurred() {
      if (state == SessionManagerState.EXTERNAL_ERROR_OCCURRED) {
        state = SessionManagerState.ABORTED_SESSION;
        throw new NodeCommunicationException(error.getMessage(), error);
      }
    }

    /**
     * (private)<br>
     * Called by the endpoint and notify the awaiting thread if necessary.
     *
     * @param msg The message received from the endpoint.
     * @throws IllegalStateException in case of bad use.
     */
    private synchronized void onMessage(MessageDto msg) {
      checkState(
          SessionManagerState.INITIALIZED, //
          SessionManagerState.ON_MESSAGE, //
          SessionManagerState.SEND_REQUEST_BEGIN, //
          SessionManagerState.SEND_REQUEST_END, //
          SessionManagerState.SEND_MESSAGE);
      if (state == SessionManagerState.SEND_REQUEST_BEGIN) {
        response = msg;
        state = SessionManagerState.SEND_REQUEST_END;
        notify();
      } else {
        state = SessionManagerState.ON_MESSAGE;
        handler.onMessage(msg);
      }
    }

    /**
     * (private)<br>
     * Called by the handler to send a request to the endpoint and await a response.
     *
     * @param msg The message to send.
     * @return The response.
     */
    private synchronized MessageDto sendRequest(MessageDto msg) {
      checkIfExternalErrorOccurred();
      state = SessionManagerState.SEND_REQUEST_BEGIN;
      response = null;
      endpoint.sendMessage(msg);
      waitForState(SessionManagerState.SEND_REQUEST_END);
      return response;
    }

    /**
     * (private)<br>
     * Called by the handler to send a message to the endpoint.
     *
     * @param msg The message to send.
     */
    private synchronized void sendMessage(MessageDto msg) {
      checkIfExternalErrorOccurred();
      state = SessionManagerState.SEND_MESSAGE;
      endpoint.sendMessage(msg);
      checkIfExternalErrorOccurred();
    }

    /**
     * (private)<br>
     * Called by the endpoint in case of endpoint error and notify the awaiting thread if necessary.
     *
     * @throws IllegalStateException in case of bad use.
     */
    private synchronized void onError(Throwable e) {
      checkState(SessionManagerState.SEND_REQUEST_BEGIN, SessionManagerState.SEND_MESSAGE);
      error = e;
      state = SessionManagerState.EXTERNAL_ERROR_OCCURRED;
      notify();
    }
  }
}

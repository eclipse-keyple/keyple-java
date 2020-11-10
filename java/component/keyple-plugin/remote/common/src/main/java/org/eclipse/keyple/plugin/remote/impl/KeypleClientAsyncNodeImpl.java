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

import java.util.*;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.KeypleClientAsync;
import org.eclipse.keyple.plugin.remote.KeypleClientAsyncNode;
import org.eclipse.keyple.plugin.remote.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.exception.KeypleRemoteCommunicationException;
import org.eclipse.keyple.plugin.remote.exception.KeypleTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keyple Client Async Node implementation.
 *
 * <p>This is an internal class an must not be used by the user.
 *
 * @since 1.0
 */
public final class KeypleClientAsyncNodeImpl extends AbstractKeypleNode
    implements KeypleClientAsyncNode {

  private static final Logger logger = LoggerFactory.getLogger(KeypleClientAsyncNodeImpl.class);

  private final KeypleClientAsync endpoint;
  private final Map<String, SessionManager> sessionManagers;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param handler The associated handler (must be not null).
   * @param endpoint The user client async endpoint (must be not null).
   * @param timeoutInSecond The default timeout (in seconds) to use.
   */
  KeypleClientAsyncNodeImpl(
      AbstractKeypleMessageHandler handler, KeypleClientAsync endpoint, int timeoutInSecond) {
    super(handler, timeoutInSecond);
    this.endpoint = endpoint;
    this.sessionManagers = new HashMap<String, SessionManager>();
  }

  /** {@inheritDoc} */
  @Override
  public void openSession(String sessionId) {
    SessionManager manager = new SessionManager(sessionId);
    sessionManagers.put(sessionId, manager);
    manager.openSession();
  }

  /** {@inheritDoc} */
  @Override
  public void onOpen(String sessionId) {
    Assert.getInstance().notEmpty(sessionId, "sessionId");
    SessionManager manager = getManagerForEndpoint(sessionId);
    if (manager != null) {
      manager.onOpen();
    }
  }

  /** {@inheritDoc} */
  @Override
  public KeypleMessageDto sendRequest(KeypleMessageDto msg) {
    msg.setClientNodeId(nodeId);
    SessionManager manager = sessionManagers.get(msg.getSessionId());
    return manager.sendRequest(msg);
  }

  /** {@inheritDoc} */
  @Override
  public void sendMessage(KeypleMessageDto msg) {
    msg.setClientNodeId(nodeId);
    SessionManager manager = sessionManagers.get(msg.getSessionId());
    manager.sendMessage(msg);
  }

  /** {@inheritDoc} */
  @Override
  public void onMessage(KeypleMessageDto msg) {

    Assert.getInstance() //
        .notNull(msg, "msg") //
        .notEmpty(msg.getSessionId(), "sessionId") //
        .notEmpty(msg.getAction(), "action") //
        .notEmpty(msg.getClientNodeId(), "clientNodeId") //
        .notEmpty(msg.getServerNodeId(), "serverNodeId");

    SessionManager manager = getManagerForEndpoint(msg.getSessionId());
    if (manager != null) {
      KeypleMessageDto.Action action = KeypleMessageDto.Action.valueOf(msg.getAction());
      switch (action) {
        case PLUGIN_EVENT:
        case READER_EVENT:
          manager.onEvent(msg);
          break;
        default:
          manager.onResponse(msg);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void closeSession(String sessionId) {
    SessionManager manager = sessionManagers.get(sessionId);
    try {
      manager.closeSession();
    } finally {
      sessionManagers.remove(sessionId);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void onClose(String sessionId) {
    Assert.getInstance().notEmpty(sessionId, "sessionId");
    SessionManager manager = getManagerForEndpoint(sessionId);
    if (manager != null) {
      manager.onClose();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void onError(String sessionId, Throwable error) {
    Assert.getInstance().notEmpty(sessionId, "sessionId").notNull(error, "error");
    SessionManager manager = getManagerForEndpoint(sessionId);
    if (manager != null) {
      manager.onError(error);
    }
  }

  /**
   * (private)<br>
   * Get the manager associated to the provided session id and log a session not found message if
   * manager does not exists.
   *
   * @param sessionId The session id (must be not empty).
   * @return a nullable reference.
   */
  private SessionManager getManagerForEndpoint(String sessionId) {
    SessionManager manager = sessionManagers.get(sessionId);
    if (manager == null) {
      logger.warn(
          "The node's session [{}] is not found. It was maybe closed due to a timeout.", sessionId);
    }
    return manager;
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

    /** {@inheritDoc} */
    @Override
    protected void checkIfExternalErrorOccurred() {
      if (state == SessionManagerState.EXTERNAL_ERROR_OCCURRED) {
        state = SessionManagerState.ABORTED_SESSION;
        throw new KeypleRemoteCommunicationException(error.getMessage(), error);
      }
    }

    /**
     * (private)<br>
     * Called by the handler to open the session by calling the endpoint and awaiting the result.
     *
     * @throws KeypleTimeoutException if a timeout occurs.
     * @throws RuntimeException if an error occurs.
     */
    private synchronized void openSession() {
      state = SessionManagerState.OPEN_SESSION_BEGIN;
      endpoint.openSession(sessionId);
      waitForState(SessionManagerState.OPEN_SESSION_END);
    }

    /**
     * (private)<br>
     * Called by the endpoint and notify the awaiting thread if necessary.
     *
     * @throws IllegalStateException in case of bad use.
     */
    private synchronized void onOpen() {
      checkState(SessionManagerState.OPEN_SESSION_BEGIN);
      state = SessionManagerState.OPEN_SESSION_END;
      notify();
    }

    /**
     * (private)<br>
     * Called by the handler to send a request to the endpoint and await a response.
     *
     * @param msg The message to send.
     * @return The response.
     * @throws KeypleTimeoutException if a timeout occurs.
     * @throws RuntimeException if an error occurs.
     */
    private synchronized KeypleMessageDto sendRequest(KeypleMessageDto msg) {
      checkIfExternalErrorOccurred();
      state = SessionManagerState.SEND_REQUEST_BEGIN;
      response = null;
      endpoint.sendMessage(msg);
      waitForState(SessionManagerState.SEND_REQUEST_END);
      return response;
    }

    /**
     * (private)<br>
     * Called by the endpoint and notify the awaiting thread if necessary.
     *
     * @param msg The response received from the endpoint.
     * @throws IllegalStateException in case of bad use.
     */
    private synchronized void onResponse(KeypleMessageDto msg) {
      checkState(SessionManagerState.SEND_REQUEST_BEGIN);
      response = msg;
      state = SessionManagerState.SEND_REQUEST_END;
      notify();
    }

    /**
     * (private)<br>
     * Called by the endpoint to transmit an event to the handler.
     *
     * @param msg The event received from the endpoint.
     */
    private void onEvent(KeypleMessageDto msg) {
      handler.onMessage(msg);
    }

    /**
     * (private)<br>
     * Called by the handler to send a message to the endpoint.
     *
     * @param msg The message to send.
     * @throws RuntimeException if an error occurs.
     */
    private synchronized void sendMessage(KeypleMessageDto msg) {
      checkIfExternalErrorOccurred();
      state = SessionManagerState.SEND_MESSAGE;
      endpoint.sendMessage(msg);
      checkIfExternalErrorOccurred();
    }

    /**
     * (private)<br>
     * Called by the handler or by the node to close the current session by calling the endpoint and
     * awaiting the result.
     *
     * @throws KeypleTimeoutException if a timeout occurs.
     * @throws RuntimeException if an error occurs.
     */
    private synchronized void closeSession() {
      checkIfExternalErrorOccurred();
      state = SessionManagerState.CLOSE_SESSION_BEGIN;
      endpoint.closeSession(sessionId);
      waitForState(SessionManagerState.CLOSE_SESSION_END);
    }

    /**
     * (private)<br>
     * Called by the endpoint and notify the awaiting thread if necessary.<br>
     *
     * @throws IllegalStateException in case of bad use.
     */
    private synchronized void onClose() {
      checkState(SessionManagerState.CLOSE_SESSION_BEGIN);
      state = SessionManagerState.CLOSE_SESSION_END;
      notify();
    }

    /**
     * (private)<br>
     * Called by the endpoint in case of endpoint error and notify the awaiting thread if necessary.
     *
     * @throws IllegalStateException in case of bad use.
     */
    private synchronized void onError(Throwable e) {
      checkState(
          SessionManagerState.OPEN_SESSION_BEGIN, //
          SessionManagerState.SEND_REQUEST_BEGIN, //
          SessionManagerState.SEND_MESSAGE, //
          SessionManagerState.CLOSE_SESSION_BEGIN);
      error = e;
      state = SessionManagerState.EXTERNAL_ERROR_OCCURRED;
      notify();
    }
  }
}

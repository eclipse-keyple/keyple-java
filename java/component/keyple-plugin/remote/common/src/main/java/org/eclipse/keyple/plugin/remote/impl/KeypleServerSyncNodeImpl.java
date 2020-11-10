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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.*;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.KeypleServerSyncNode;
import org.eclipse.keyple.plugin.remote.exception.KeypleTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keyple Server Sync Node implementation.
 *
 * <p>This is an internal class an must not be used by the user.
 *
 * @since 1.0
 */
public final class KeypleServerSyncNodeImpl extends AbstractKeypleNode
    implements KeypleServerSyncNode {

  private static final Logger logger = LoggerFactory.getLogger(KeypleServerSyncNodeImpl.class);

  private final Map<String, SessionManager> sessionManagers;
  private final Map<String, ServerPushEventManager> pluginManagers;
  private final Map<String, ServerPushEventManager> readerManagers;
  private final JsonParser jsonParser;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param handler The associated handler (must be not null).
   * @param timeoutInSecond The default timeout (in seconds) to use.
   */
  KeypleServerSyncNodeImpl(AbstractKeypleMessageHandler handler, int timeoutInSecond) {
    super(handler, timeoutInSecond);
    jsonParser = new JsonParser();
    this.sessionManagers = new HashMap<String, SessionManager>();
    this.pluginManagers = new HashMap<String, ServerPushEventManager>();
    this.readerManagers = new HashMap<String, ServerPushEventManager>();
  }

  /** {@inheritDoc} */
  @Override
  public void openSession(String sessionId) {
    throw new UnsupportedOperationException("openSession");
  }

  /** {@inheritDoc} */
  @Override
  public List<KeypleMessageDto> onRequest(KeypleMessageDto msg) {

    // Check mandatory fields
    Assert.getInstance() //
        .notNull(msg, "msg") //
        .notEmpty(msg.getSessionId(), "sessionId") //
        .notEmpty(msg.getAction(), "action") //
        .notEmpty(msg.getClientNodeId(), "clientNodeId");

    List<KeypleMessageDto> responses;
    KeypleMessageDto.Action action = KeypleMessageDto.Action.valueOf(msg.getAction());
    switch (action) {
      case CHECK_PLUGIN_EVENT:
        responses = checkEvents(msg, pluginManagers);
        break;
      case CHECK_READER_EVENT:
        responses = checkEvents(msg, readerManagers);
        break;
      default:
        responses = processOnRequest(msg);
    }
    return responses != null ? responses : new ArrayList<KeypleMessageDto>(0);
  }

  /**
   * (private)<br>
   * Check on client request if some events are present in the associated sendbox.
   *
   * @param msg The client message containing all client info (node id, strategy, ...)
   * @param eventManagers The event managers map.
   * @return a null list or a not empty list
   */
  private List<KeypleMessageDto> checkEvents(
      KeypleMessageDto msg, Map<String, ServerPushEventManager> eventManagers) {
    ServerPushEventManager manager = getEventManager(msg, eventManagers);
    return manager.checkEvents(msg);
  }

  /**
   * (private)<br>
   * Processes onRequest for standard transaction call.<br>
   * Create a new session manager if needed.
   *
   * @param msg The message to process (must be not null).
   * @return a nullable list or which contains at most one element.
   * @throws KeypleTimeoutException if a timeout occurs.
   */
  private List<KeypleMessageDto> processOnRequest(KeypleMessageDto msg) {
    SessionManager manager = sessionManagers.get(msg.getSessionId());
    if (manager == null) {
      manager = new SessionManager(msg.getSessionId());
      sessionManagers.put(msg.getSessionId(), manager);
    }
    KeypleMessageDto response = manager.onRequest(msg);
    return response != null ? Collections.singletonList(response) : null;
  }

  /** {@inheritDoc} */
  @Override
  public KeypleMessageDto sendRequest(KeypleMessageDto msg) {
    msg.setServerNodeId(nodeId);
    SessionManager manager = sessionManagers.get(msg.getSessionId());
    try {
      return manager.sendRequest(msg);
    } catch (RuntimeException e) {
      sessionManagers.remove(msg.getSessionId());
      throw e;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void sendMessage(KeypleMessageDto msg) {
    msg.setServerNodeId(nodeId);
    KeypleMessageDto.Action action = KeypleMessageDto.Action.valueOf(msg.getAction());
    switch (action) {
      case PLUGIN_EVENT:
        postEvent(msg, pluginManagers);
        break;
      case READER_EVENT:
        postEvent(msg, readerManagers);
        break;
      default:
        processSendMessage(msg);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void closeSession(String sessionId) {
    throw new UnsupportedOperationException("closeSession");
  }

  /**
   * (private)<br>
   * Post an event into the sendbox, analyse the client strategy, and eventually try to wake up the
   * pending client task in case of long polling strategy.
   *
   * @param msg The message containing the event to post (must be not null).
   * @param eventManagers The event managers map.
   */
  private void postEvent(KeypleMessageDto msg, Map<String, ServerPushEventManager> eventManagers) {
    ServerPushEventManager manager = getEventManager(msg, eventManagers);
    manager.postEvent(msg);
  }

  /**
   * (private)<br>
   * Get or create an event manager associated to a client node id.
   *
   * @param msg The message containing the client's information
   * @param eventManagers The event managers map.
   * @return a not null reference.
   */
  private ServerPushEventManager getEventManager(
      KeypleMessageDto msg, Map<String, ServerPushEventManager> eventManagers) {
    ServerPushEventManager manager = eventManagers.get(msg.getClientNodeId());
    if (manager == null) {
      manager = new ServerPushEventManager(msg.getClientNodeId());
      eventManagers.put(msg.getClientNodeId(), manager);
    }
    return manager;
  }

  /**
   * (private)<br>
   * Processes sendMessage for standard transaction call.<br>
   * Note that the associated session is also closed.
   *
   * @param msg The message to process (must be not null).
   */
  private void processSendMessage(KeypleMessageDto msg) {
    SessionManager manager = sessionManagers.get(msg.getSessionId());
    if (manager == null) {
      throw new IllegalStateException("Session is closed");
    }
    try {
      manager.sendMessage(msg);
    } finally {
      sessionManagers.remove(msg.getSessionId());
    }
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
      // NOP
    }

    /**
     * (private)<br>
     * Called by the endpoint when a new client request is received.
     *
     * @param msg The message to process.
     * @return a not null reference on a message to return to the client.
     * @throws KeypleTimeoutException if a timeout occurs.
     */
    private synchronized KeypleMessageDto onRequest(KeypleMessageDto msg) {
      checkState(SessionManagerState.INITIALIZED, SessionManagerState.SEND_REQUEST_BEGIN);
      if (state == SessionManagerState.INITIALIZED) {
        // Process the message as a client request
        state = SessionManagerState.ON_REQUEST;
        handler.onMessage(msg);
      } else {
        // State is SEND_REQUEST_BEGIN
        // Process the message as a client response
        postMessageAndNotify(msg, SessionManagerState.SEND_REQUEST_END);
      }
      waitForState(SessionManagerState.SEND_MESSAGE, SessionManagerState.SEND_REQUEST_BEGIN);
      return response;
    }

    /**
     * (private)<br>
     * Called by the handler to send a request to the endpoint and await a response.
     *
     * @param msg The message to send.
     * @return The response.
     * @throws KeypleTimeoutException if a timeout occurs.
     */
    private synchronized KeypleMessageDto sendRequest(KeypleMessageDto msg) {
      postMessageAndNotify(msg, SessionManagerState.SEND_REQUEST_BEGIN);
      waitForState(SessionManagerState.SEND_REQUEST_END);
      return response;
    }

    /**
     * (private)<br>
     * Called by the handler to send a message to the endpoint.
     *
     * @param msg The message to send.
     */
    private synchronized void sendMessage(KeypleMessageDto msg) {
      postMessageAndNotify(msg, SessionManagerState.SEND_MESSAGE);
    }

    /**
     * (private)<br>
     * Post a message and try to wake up the waiting task.
     *
     * @param msg The message to post.
     * @param targetState The new state to set before to notify the waiting task.
     */
    private void postMessageAndNotify(KeypleMessageDto msg, SessionManagerState targetState) {
      response = msg;
      state = targetState;
      notify();
    }
  }

  /**
   * (private)<br>
   * This inner class is a manager for server push events.
   */
  private class ServerPushEventManager {

    private final String clientNodeId;

    private volatile List<KeypleMessageDto> events;
    private ServerPushEventStrategy strategy;

    /**
     * (private)<br>
     * Constructor
     *
     * @param clientNodeId The client node id to manage.
     */
    private ServerPushEventManager(String clientNodeId) {
      this.clientNodeId = clientNodeId;
      this.events = null;
      this.strategy = null;
    }

    /**
     * (private)<br>
     * Post an event into the sendbox, analyse the client strategy, and eventually try to wake up
     * the pending client task in case of long polling strategy.
     *
     * @param msg The message containing the event to post (must be not null).
     */
    private synchronized void postEvent(KeypleMessageDto msg) {

      // Post the event
      if (events == null) {
        events = new ArrayList<KeypleMessageDto>(1);
      }
      events.add(msg);

      // Gets the client's strategy
      // If strategy is long polling, then try to wake up the associated awaiting task.
      if (strategy != null && strategy.getType() == ServerPushEventStrategy.Type.LONG_POLLING) {
        notify();
      }
    }

    /**
     * (private)<br>
     * Check on client request if some events are present in the associated sendbox.
     *
     * @param msg The client message containing all client info (node id, strategy, ...)
     * @return a null list or a not empty list
     */
    private synchronized List<KeypleMessageDto> checkEvents(KeypleMessageDto msg) {
      try {
        // We're checking to see if any events are already present
        if (events != null) {
          return events;
        }

        // If none, then gets the client's strategy
        ServerPushEventStrategy strategy = getStrategy(msg);

        // If is a long polling strategy, then await for an event notification.
        if (strategy.getType() == ServerPushEventStrategy.Type.LONG_POLLING) {
          waitAtMost(strategy.getDuration());
        }
        return events;
      } finally {
        events = null;
      }
    }

    /**
     * (private)<br>
     * Gets the client registered strategy or register it in case of first client call.
     *
     * @param msg The client message containing all client info (node id, strategy, ...)
     * @return a not null {@link ServerPushEventStrategy}
     * @throws IllegalArgumentException in case of first client call with bad arguments.
     */
    private ServerPushEventStrategy getStrategy(KeypleMessageDto msg) {

      // Gets the client registered strategy if exists.
      if (strategy == null) {

        // Register the client's strategy
        JsonObject body;
        ServerPushEventStrategy.Type type;
        try {
          body = jsonParser.parse(msg.getBody()).getAsJsonObject();
          type = ServerPushEventStrategy.Type.valueOf(body.get("strategy").getAsString());
        } catch (Exception e) {
          throw new IllegalArgumentException("body", e);
        }

        strategy = new ServerPushEventStrategy(type);

        if (type == ServerPushEventStrategy.Type.LONG_POLLING) {
          try {
            int maxWaitingTime = body.get("duration").getAsInt() * 1000;
            strategy.setDuration(maxWaitingTime);
          } catch (Exception e) {
            throw new IllegalArgumentException("long polling duration", e);
          }
        }
      }
      return strategy;
    }

    /**
     * (private)<br>
     * Wait a most the provided max awaiting time.
     *
     * @param maxAwaitingTime The max awaiting time.
     */
    private void waitAtMost(int maxAwaitingTime) {
      try {
        wait(maxAwaitingTime);
      } catch (InterruptedException e) {
        logger.error(
            "Unexpected interruption of the task associated with the node's id {}",
            clientNodeId,
            e);
        Thread.currentThread().interrupt();
      }
    }
  }
}

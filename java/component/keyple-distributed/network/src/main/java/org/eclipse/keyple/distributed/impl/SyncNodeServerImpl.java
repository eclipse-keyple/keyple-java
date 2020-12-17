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
package org.eclipse.keyple.distributed.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.SyncNodeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Sync Node Server implementation.
 *
 * @since 1.0
 */
final class SyncNodeServerImpl extends AbstractNode implements SyncNodeServer {

  private static final Logger logger = LoggerFactory.getLogger(SyncNodeServerImpl.class);

  private final Map<String, SessionManager> sessionManagers;
  private final Map<String, ServerPushEventManager> pluginManagers;
  private final Map<String, ServerPushEventManager> readerManagers;
  private final JsonParser jsonParser;

  /**
   * (package-private)<br>
   *
   * @param handler The associated handler (must be not null).
   * @param timeoutInSecond The default timeout (in seconds) to use.
   * @since 1.0
   */
  SyncNodeServerImpl(AbstractMessageHandler handler, int timeoutInSecond) {
    super(handler, timeoutInSecond);
    jsonParser = new JsonParser();
    this.sessionManagers = new ConcurrentHashMap<String, SessionManager>();
    this.pluginManagers = new ConcurrentHashMap<String, ServerPushEventManager>();
    this.readerManagers = new ConcurrentHashMap<String, ServerPushEventManager>();
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
  public List<MessageDto> onRequest(MessageDto msg) {

    // Check mandatory fields
    Assert.getInstance() //
        .notNull(msg, "msg") //
        .notEmpty(msg.getSessionId(), "sessionId") //
        .notEmpty(msg.getAction(), "action") //
        .notEmpty(msg.getClientNodeId(), "clientNodeId");

    List<MessageDto> responses;
    MessageDto.Action action = MessageDto.Action.valueOf(msg.getAction());
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
    return responses != null ? responses : new ArrayList<MessageDto>(0);
  }

  /**
   * (private)<br>
   * Check on client request if some events are present in the associated sendbox.
   *
   * @param msg The client message containing all client info (node id, strategy, ...)
   * @param eventManagers The event managers map.
   * @return a null list or a not empty list
   */
  private List<MessageDto> checkEvents(
      MessageDto msg, Map<String, ServerPushEventManager> eventManagers) {
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
   */
  private List<MessageDto> processOnRequest(MessageDto msg) {
    SessionManager manager = sessionManagers.get(msg.getSessionId());
    if (manager == null) {
      manager = new SessionManager(msg.getSessionId());
      sessionManagers.put(msg.getSessionId(), manager);
    }
    MessageDto response = manager.onRequest(msg);
    return response != null ? Collections.singletonList(response) : null;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  MessageDto sendRequest(MessageDto msg) {
    msg.setServerNodeId(nodeId);
    SessionManager manager = sessionManagers.get(msg.getSessionId());
    try {
      return manager.sendRequest(msg);
    } catch (RuntimeException e) {
      sessionManagers.remove(msg.getSessionId());
      throw e;
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  void sendMessage(MessageDto msg) {
    msg.setServerNodeId(nodeId);
    MessageDto.Action action = MessageDto.Action.valueOf(msg.getAction());
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
   * Post an event into the sendbox, analyse the client strategy, and eventually try to wake up the
   * pending client task in case of long polling strategy.
   *
   * @param msg The message containing the event to post (must be not null).
   * @param eventManagers The event managers map.
   */
  private void postEvent(MessageDto msg, Map<String, ServerPushEventManager> eventManagers) {
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
      MessageDto msg, Map<String, ServerPushEventManager> eventManagers) {
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
  private void processSendMessage(MessageDto msg) {
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

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    void checkIfExternalErrorOccurred() {
      // NOP
    }

    /**
     * (private)<br>
     * Called by the endpoint when a new client request is received.
     *
     * @param msg The message to process.
     * @return a not null reference on a message to return to the client.
     */
    private synchronized MessageDto onRequest(MessageDto msg) {
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
     */
    private synchronized MessageDto sendRequest(MessageDto msg) {
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
    private synchronized void sendMessage(MessageDto msg) {
      postMessageAndNotify(msg, SessionManagerState.SEND_MESSAGE);
    }

    /**
     * (private)<br>
     * Post a message and try to wake up the waiting task.
     *
     * @param msg The message to post.
     * @param targetState The new state to set before to notify the waiting task.
     */
    private synchronized void postMessageAndNotify(
        MessageDto msg, SessionManagerState targetState) {
      response = msg;
      state = targetState;
      notifyAll();
    }
  }

  /**
   * (private)<br>
   * This inner class is a manager for server push events.
   */
  private class ServerPushEventManager {

    private final String clientNodeId;

    private List<MessageDto> events;
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
    private synchronized void postEvent(MessageDto msg) {

      // Post the event
      if (events == null) {
        events = new ArrayList<MessageDto>(1);
      }
      events.add(msg);

      // Gets the client's strategy
      // If strategy is long polling, then try to wake up the associated awaiting task.
      if (strategy != null && strategy.getType() == ServerPushEventStrategy.Type.LONG_POLLING) {
        notifyAll();
      }
    }

    /**
     * (private)<br>
     * Check on client request if some events are present in the associated sendbox.
     *
     * @param msg The client message containing all client info (node id, strategy, ...)
     * @return a null list or a not empty list
     */
    private synchronized List<MessageDto> checkEvents(MessageDto msg) {
      try {
        // We're checking to see if any events are already present
        if (events != null) {
          return events;
        }

        // If none, then gets the client's strategy
        registerClientStrategy(msg);

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
     * Registers the client strategy in case of first client invocation.
     *
     * @param msg The client message containing all client info (node id, strategy, ...)
     * @throws IllegalArgumentException in case of first client invocation with bad arguments.
     */
    private void registerClientStrategy(MessageDto msg) {

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
    }

    /**
     * (private)<br>
     * Wait a most the provided max awaiting time.
     *
     * @param maxAwaitingTime The max awaiting time.
     */
    private synchronized void waitAtMost(int maxAwaitingTime) {
      try {
        long deadline = new Date().getTime() + maxAwaitingTime;
        while (events == null && new Date().getTime() < deadline) {
          wait(maxAwaitingTime);
        }
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

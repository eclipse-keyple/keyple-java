/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.core.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerSyncNode;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Keyple Server Sync Node implementation.
 * <p>
 * This is an internal class an must not be used by the user.
 *
 * @since 1.0
 */
public final class KeypleServerSyncNodeImpl extends AbstractKeypleNode
        implements KeypleServerSyncNode {

    private static final Logger logger = LoggerFactory.getLogger(KeypleServerSyncNodeImpl.class);

    // Client elements associated to a session id.
    private final Map<String, Thread> clientTasks;
    private final Map<Thread, KeypleMessageDto> clientSendbox;
    private final Map<String, Long> clientTimeouts;

    // Server elements associated to a session id.
    private final Map<String, Thread> serverTasks;
    private final Map<Thread, KeypleMessageDto> serverSendbox;
    private final Map<String, Long> serverTimeouts;

    // Plugin & Reader server push event managers
    private final ServerPushEventManager pluginManager;
    private final ServerPushEventManager readerManager;

    // JSON Parser
    private final JsonParser jsonParser;

    // Timeout used during awaiting (in milliseconds)
    private final int timeout;

    /**
     * (package-private)<br>
     * Constructor.
     *
     * @param handler The associated handler (must be not null).
     * @param timeoutInSecond The default timeout (in seconds) to use.
     */
    KeypleServerSyncNodeImpl(AbstractKeypleMessageHandler handler, int timeoutInSecond) {
        super(handler);
        clientTasks = new ConcurrentHashMap<String, Thread>();
        clientSendbox = new ConcurrentHashMap<Thread, KeypleMessageDto>();
        clientTimeouts = new ConcurrentTimeoutHashMap();
        serverTasks = new ConcurrentHashMap<String, Thread>();
        serverSendbox = new ConcurrentHashMap<Thread, KeypleMessageDto>();
        serverTimeouts = new ConcurrentTimeoutHashMap();
        pluginManager = new ServerPushEventManager();
        readerManager = new ServerPushEventManager();
        jsonParser = new JsonParser();
        timeout = timeoutInSecond * 1000;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<KeypleMessageDto> onRequest(KeypleMessageDto msg) {

        // Check mandatory fields
        Assert.getInstance()//
                .notNull(msg, "msg")//
                .notEmpty(msg.getSessionId(), "sessionId")//
                .notEmpty(msg.getAction(), "action")//
                .notEmpty(msg.getClientNodeId(), "clientNodeId");

        List<KeypleMessageDto> responses;
        try {
            KeypleMessageDto.Action action = KeypleMessageDto.Action.valueOf(msg.getAction());
            switch (action) {
                case CHECK_PLUGIN_EVENT:
                    responses = pluginManager.checkEvents(msg);
                    break;
                case CHECK_READER_EVENT:
                    responses = readerManager.checkEvents(msg);
                    break;
                default:
                    responses = processOnRequest(msg);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            responses =
                    Arrays.asList(buildErrorMessage(KeypleMessageDto.ErrorCode.UNKNOWN.getCode(),
                            e.getMessage(), msg));
        }
        return responses != null ? responses : new ArrayList<KeypleMessageDto>(0);
    }

    /**
     * (private)<br>
     * Processes onRequest for standard transaction call.
     *
     * @param msg The message to process (must be not null).
     * @return a nullable list or which contains at most one element.
     */
    private List<KeypleMessageDto> processOnRequest(KeypleMessageDto msg) {

        KeypleMessageDto response;

        // Register the current client task
        clientTasks.put(msg.getSessionId(), Thread.currentThread());
        try {
            // Gets the pending server task if it exists
            Thread pendingServerTask = serverTasks.get(msg.getSessionId());
            if (pendingServerTask == null) {
                // If none, then check if a server timeout occurred
                if (serverTimeouts.remove(msg.getSessionId()) != null) {
                    String errorMessage =
                            "Timeout occurs for the server task associated to the session id "
                                    + msg.getSessionId();
                    response = buildErrorMessage(
                            KeypleMessageDto.ErrorCode.TIMEOUT_SERVER_TASK.getCode(), errorMessage,
                            msg);
                } else {
                    // The message is a client request
                    response = processOnRequestAsClientRequest(msg);
                }
            } else {
                // The message is a client response
                response = processOnRequestAsClientResponse(msg, pendingServerTask);
            }
        } finally {
            // Unregister the current client task
            clientTasks.remove(msg.getSessionId());
        }
        return response != null ? Arrays.asList(response) : null;
    }

    /**
     * (private)<br>
     * Processes onRequest as a client request.
     *
     * @param msg The client request (must be not null).
     * @return the next server message to transmit to the client or null if there is nothing to
     *         return.
     */
    private KeypleMessageDto processOnRequestAsClientRequest(KeypleMessageDto msg) {

        // Transmits the message to the handler
        handler.onMessage(msg);

        // Checks whether a response has already been provided
        KeypleMessageDto response = clientSendbox.remove(Thread.currentThread());
        if (response == null) {
            // If none, then await the response
            response = awaitMessage(msg, clientSendbox, clientTimeouts, true);
        }
        return response;
    }

    /**
     * (private)<br>
     * Processes onRequest as a client response.
     *
     * @param msg The client response (must be not null).
     * @return a not null reference to the next server message to transmit to the client.
     */
    private KeypleMessageDto processOnRequestAsClientResponse(KeypleMessageDto msg,
            Thread pendingServerTask) {

        // Post the message for the server task
        serverSendbox.put(pendingServerTask, msg);

        // Wake up the server task
        pendingServerTask.interrupt();

        // Then await the response as a server new request
        return awaitMessage(msg, clientSendbox, clientTimeouts, true);
    }

    /**
     * (private)<br>
     * Builds an error message by copying all main fields from the original message.
     *
     * @param errorCode The error code.
     * @param errorMessage The error message.
     * @param originalMessage The original message (must be not null)
     * @return a not null reference
     */
    private KeypleMessageDto buildErrorMessage(String errorCode, String errorMessage,
            KeypleMessageDto originalMessage) {

        JsonObject body = new JsonObject();
        body.addProperty("code", errorCode);
        body.addProperty("message", errorMessage);

        return new KeypleMessageDto(originalMessage)//
                .setAction(KeypleMessageDto.Action.ERROR.name())//
                .setBody(body.toString());//
    }

    /**
     * (private)<br>
     * Await a new message using sleep strategy until at most for a timeout duration.
     *
     * @param originalMessage The original message (must be not null).
     * @param sendbox The sendbox to check.
     * @param timeouts The timeouts register.
     * @param isForClient Who is awaiting ? Client or Server ?
     * @return a not null reference
     * @throws KeypleTimeoutException if timeout occurs when server is awaiting.
     */
    private KeypleMessageDto awaitMessage(KeypleMessageDto originalMessage,
            Map<Thread, KeypleMessageDto> sendbox, Map<String, Long> timeouts,
            boolean isForClient) {

        KeypleMessageDto message;
        try {
            // Putting on standby
            Thread.sleep(timeout);

            // The maximum waiting time has been reached
            timeouts.put(originalMessage.getSessionId(), new Date().getTime());
            if (isForClient) {
                String errorMessage =
                        "Timeout occurs for the client task associated to the session id "
                                + originalMessage.getSessionId();
                logger.error(errorMessage);
                message =
                        buildErrorMessage(KeypleMessageDto.ErrorCode.TIMEOUT_CLIENT_TASK.getCode(),
                                errorMessage, originalMessage);
            } else {
                String errorMessage =
                        "Timeout occurs for the server task associated to the session id "
                                + originalMessage.getSessionId();
                logger.error(errorMessage);
                throw new KeypleTimeoutException(errorMessage);
            }
        } catch (InterruptedException e) {
            // A message has just been posted
            message = sendbox.remove(Thread.currentThread());
        }
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    KeypleMessageDto sendRequest(KeypleMessageDto msg) {

        KeypleMessageDto response;

        // Register the current server task
        serverTasks.put(msg.getSessionId(), Thread.currentThread());
        try {
            // Gets the pending client task
            Thread pendingClientTask = getPendingClientTask(msg.getSessionId());

            // Post the message for the client task
            clientSendbox.put(pendingClientTask, msg);

            // Wake up the client task
            pendingClientTask.interrupt();

            // Then await the response
            response = awaitMessage(msg, serverSendbox, serverTimeouts, false);
        } finally {
            // Unregister the current server task
            serverTasks.remove(msg.getSessionId());
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void sendMessage(KeypleMessageDto msg) {

        KeypleMessageDto.Action action = KeypleMessageDto.Action.valueOf(msg.getAction());
        switch (action) {
            case PLUGIN_EVENT:
                pluginManager.postEvent(msg);
                break;
            case READER_EVENT:
                readerManager.postEvent(msg);
                break;
            default:
                processSendMessage(msg);
        }
    }

    /**
     * (private)<br>
     * Processes sendMessage for standard transaction call.
     *
     * @param msg The message to process (must be not null).
     */
    private void processSendMessage(KeypleMessageDto msg) {

        // Gets the pending client task
        Thread pendingClientTask = getPendingClientTask(msg.getSessionId());

        // Post the message for the client task
        clientSendbox.put(pendingClientTask, msg);

        // Wake up the client task
        pendingClientTask.interrupt();
    }

    /**
     * (private)<br>
     * Get the pending client task from the session id of the message.
     *
     * @param sessionId The session id (must be not empty).
     * @return a not null reference.
     * @throws KeypleTimeoutException if client task could not be found due to a client timeout.
     * @throws IllegalStateException in case of use in a bad context or if the client timeout is too
     *         old and was unregistered.
     */
    private Thread getPendingClientTask(String sessionId) {

        Thread pendingClientTask = clientTasks.get(sessionId);
        if (pendingClientTask == null) {
            // If none, then check if a client timeout occurred
            String errorMessage = "There is no pending client task for session id " + sessionId;
            if (clientTimeouts.remove(sessionId) != null) {
                throw new KeypleTimeoutException(errorMessage + " due to a client timeout.");
            } else {
                throw new IllegalStateException(
                        errorMessage + " (may possibly be due to a client timeout).");
            }
        }
        return pendingClientTask;
    }

    /**
     * (private)<br>
     * This inner class is a {@link ConcurrentHashMap} of timestamps by session id in which items
     * that are too old are automatically removed.<br>
     * This prevents memory leaks.<br>
     * The cleaning is done when a new item is added.
     */
    private class ConcurrentTimeoutHashMap extends ConcurrentHashMap<String, Long> {

        private long dateOfLastClean = 0L;

        /**
         * {@inheritDoc}
         */
        @Override
        public Long put(String key, Long value) {

            // Put the entry
            Long previousValue = super.put(key, value);

            // Remove old entries
            long now = new Date().getTime();
            long limitDate = now - timeout;
            if (dateOfLastClean < limitDate) {
                dateOfLastClean = now;
                Iterator<Map.Entry<String, Long>> iterator = entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Long> entry = iterator.next();
                    if (entry.getValue().longValue() < limitDate) {
                        iterator.remove();
                    }
                }
            }
            return previousValue;
        }
    }

    /**
     * (private)<br>
     * This inner class is a manager for server push events.
     */
    private class ServerPushEventManager {

        // Plugin event elements associated to a client node id.
        private final Map<String, List<KeypleMessageDto>> eventsSendbox;
        private final Map<String, ServerPushEventStrategy> strategies;
        private final Map<String, Thread> longPollingClientTasks;

        /**
         * (private)<br>
         * Constructor
         */
        private ServerPushEventManager() {
            eventsSendbox = new ConcurrentHashMap<String, List<KeypleMessageDto>>();
            strategies = new ConcurrentHashMap<String, ServerPushEventStrategy>();
            longPollingClientTasks = new ConcurrentHashMap<String, Thread>();
        }

        /**
         * (private)<br>
         * Post an event into the sendbox, analyse the client strategy, and eventually try to wake
         * up the pending client task in case of long polling strategy.<br>
         * Each sendbox is associated to a client node id.
         *
         * @param msg The message containing the event to post (must be not null).
         */
        private void postEvent(KeypleMessageDto msg) {

            // Post the event
            List<KeypleMessageDto> events = eventsSendbox.get(msg.getClientNodeId());
            if (events == null) {
                events = new ArrayList<KeypleMessageDto>(1);
                eventsSendbox.put(msg.getClientNodeId(), events);
            }
            events.add(msg);

            // Gets the client's strategy
            ServerPushEventStrategy strategy = strategies.get(msg.getClientNodeId());

            // If strategy is long polling, then try to wake up the associated awaiting task.
            if (strategy != null
                    && strategy.getType() == ServerPushEventStrategy.Type.LONG_POLLING) {
                Thread pendingClientTask = longPollingClientTasks.get(msg.getClientNodeId());
                if (pendingClientTask != null) {
                    pendingClientTask.interrupt();
                }
            }
        }

        /**
         * (private)<br>
         * Check on client request if some events are present in the associated sendbox using the
         * client node id.
         *
         * @param msg The client message containing all client info (node id, strategy, ...)
         * @return a nullable list
         */
        private List<KeypleMessageDto> checkEvents(KeypleMessageDto msg) {

            // We're checking to see if any events are already present
            List<KeypleMessageDto> events = eventsSendbox.remove(msg.getClientNodeId());
            if (events != null) {
                return events;
            }

            // If none, then gets the client's strategy
            ServerPushEventStrategy strategy = getStrategy(msg);

            // If is a long polling strategy, then await for an event notification.
            if (strategy.getType() == ServerPushEventStrategy.Type.LONG_POLLING) {
                events = awaitEvent(msg.getClientNodeId(), strategy.getDuration());
            }
            return events;
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
            ServerPushEventStrategy strategy = strategies.get(msg.getClientNodeId());
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

                strategies.put(msg.getClientNodeId(), strategy);
            }
            return strategy;
        }

        /**
         * (private)<br>
         * Await an event in case of long polling strategy at most for a max awaiting time.
         *
         * @param clientNodeId The client node id.
         * @param maxAwaitingTime The max awaiting time.
         * @return a nullable list
         */
        private List<KeypleMessageDto> awaitEvent(String clientNodeId, int maxAwaitingTime) {

            List<KeypleMessageDto> events = null;
            try {
                // Register the current client task
                longPollingClientTasks.put(clientNodeId, Thread.currentThread());
                // Then await an event
                Thread.sleep(maxAwaitingTime);
            } catch (InterruptedException e) {
                events = eventsSendbox.remove(clientNodeId);
            } finally {
                // Unregister the current client task
                longPollingClientTasks.remove(clientNodeId);
            }
            return events;
        }
    }
}

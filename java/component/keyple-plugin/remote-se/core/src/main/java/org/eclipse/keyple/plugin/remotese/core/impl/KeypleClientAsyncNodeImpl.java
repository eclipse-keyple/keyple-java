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
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsyncNode;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keyple Client Async Node implementation.
 * <p>
 * This is an internal class an must not be used by the user.
 *
 * @since 1.0
 */
// todo
@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public final class KeypleClientAsyncNodeImpl extends AbstractKeypleNode
        implements KeypleClientAsyncNode {

    private static final Logger logger = LoggerFactory.getLogger(KeypleClientAsyncNodeImpl.class);

    private final KeypleClientAsync endpoint;
    private final Map<String, SessionManager> sessionManagers;

    // Timeout used during awaiting (in milliseconds)
    private final int timeout;

    /**
     * (package-private)<br>
     * Constructor.
     *
     * @param handler The associated handler (must be not null).
     * @param endpoint The user client async endpoint (must be not null).
     * @param timeoutInSecond The default timeout (in seconds) to use.
     */
    KeypleClientAsyncNodeImpl(AbstractKeypleMessageHandler handler, KeypleClientAsync endpoint,
            int timeoutInSecond) {
        super(handler);
        this.endpoint = endpoint;
        this.timeout = timeoutInSecond * 1000;
        this.sessionManagers = new HashMap<String, SessionManager>();
    }

    /**
     * (For internal use only)<br>
     * Create a new session manager, register it using the provided session id, then try to open the
     * session on the endpoint.
     *
     * @param sessionId The session id (must be not empty).
     * @throws RuntimeException if an error occurs.
     * @since 1.0
     */
    public void openSession(String sessionId) {
        SessionManager manager = new SessionManager(sessionId);
        sessionManagers.put(sessionId, manager);
        try {
            manager.openSession();
        } catch (RuntimeException e) {
            closeSessionSilently(sessionId);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(String sessionId) {
        Assert.getInstance().notEmpty(sessionId, "sessionId");
        SessionManager manager = sessionManagers.get(sessionId);
        if (manager != null) {
            manager.onOpen();
        } else {
            logSessionNotFound(sessionId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeypleMessageDto sendRequest(KeypleMessageDto msg) {
        SessionManager manager = sessionManagers.get(msg.getSessionId());
        try {
            return manager.sendRequest(msg);
        } catch (RuntimeException e) {
            closeSessionSilently(msg.getSessionId());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(KeypleMessageDto msg) {
        SessionManager manager = sessionManagers.get(msg.getSessionId());
        try {
            manager.sendMessage(msg);
        } catch (RuntimeException e) {
            closeSessionSilently(msg.getSessionId());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(KeypleMessageDto msg) {

        Assert.getInstance()//
                .notNull(msg, "msg")//
                .notEmpty(msg.getSessionId(), "sessionId")//
                .notEmpty(msg.getAction(), "action")//
                .notEmpty(msg.getClientNodeId(), "clientNodeId")//
                .notEmpty(msg.getServerNodeId(), "serverNodeId");

        SessionManager manager = sessionManagers.get(msg.getSessionId());
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
        } else {
            logSessionNotFound(msg.getSessionId());
        }
    }

    /**
     * (For internal use only)<br>
     * Close the session having the provided session id.
     *
     * @param sessionId The session id (must be not empty).
     * @since 1.0
     */
    public void closeSession(String sessionId) {
        SessionManager manager = sessionManagers.get(sessionId);
        try {
            manager.closeSession();
        } finally {
            sessionManagers.remove(sessionId);
        }
    }

    /**
     * (private)<br>
     * Close the session silently (without throwing exceptions)
     *
     * @param sessionId The session id (must be not empty).
     */
    private void closeSessionSilently(String sessionId) {
        try {
            closeSession(sessionId);
        } catch (RuntimeException e) {
            logger.error("Error during the silent closing of node's session [{}] : {}", sessionId,
                    e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(String sessionId) {
        Assert.getInstance().notEmpty(sessionId, "sessionId");
        SessionManager manager = sessionManagers.get(sessionId);
        if (manager != null) {
            manager.onClose();
        } else {
            logSessionNotFound(sessionId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(String sessionId, Throwable error) {
        Assert.getInstance().notEmpty(sessionId, "sessionId").notNull(error, "error");
        SessionManager manager = sessionManagers.get(sessionId);
        if (manager != null) {
            manager.onError(error);
        } else {
            logSessionNotFound(sessionId);
        }
    }

    /**
     * (private)<br>
     * Log a session not found message.
     *
     * @param sessionId The session id (must be not empty).
     */
    private void logSessionNotFound(String sessionId) {
        logger.warn("The node's session [{}] is not found. It was maybe closed due to a timeout.",
                sessionId);
    }

    /**
     * (private)<br>
     * The session manager state enum
     */
    private enum SessionManagerState {
        INITIALIZED, //
        OPEN_SESSION_BEGIN, //
        OPEN_SESSION_END, //
        SEND_REQUEST_BEGIN, //
        SEND_REQUEST_END, //
        SEND_MESSAGE, //
        CANCEL_SESSION_REQUESTED, //
        CLOSE_SESSION_BEGIN, //
        CLOSE_SESSION_END, //
        ERROR_RECEIVED;
    }

    /**
     * (private)<br>
     * The inner session manager class.<br>
     * There is one manager by session id.
     */
    private class SessionManager {

        private final String sessionId;

        private volatile SessionManagerState state;
        private volatile KeypleMessageDto response;
        private volatile Throwable error;

        /**
         * (private)<br>
         * Constructor
         *
         * @param sessionId The session id to manage.
         */
        private SessionManager(String sessionId) {
            this.sessionId = sessionId;
            this.state = SessionManagerState.INITIALIZED;
            this.response = null;
            this.error = null;
        }

        /**
         * (private)<br>
         * Check if the current state is equal to the target state, else wait until a timeout or to
         * be wake up by another thread.<br>
         *
         * @param targetState The target state.
         * @throws KeypleTimeoutException if a timeout occurs.
         * @throws RuntimeException if an error was received from the endpoint.
         */
        private void waitForState(SessionManagerState targetState) {
            if (state == targetState) {
                return;
            }
            checkError();
            try {
                wait(timeout);
                if (state == targetState) {
                    return;
                }
                checkError();
                timeout();
            } catch (InterruptedException e) {
                logger.error(
                        "Unexpected interruption of the task associated with the node's session {}",
                        sessionId, e);
                Thread.currentThread().interrupt();
            }
        }

        /**
         * (private)<br>
         * Check if the current state is one of the provided target states.
         *
         * @param targetStates The target states to test.
         * @throws IllegalArgumentException if the current state does not match any of the states
         *         provided.
         */
        private void checkState(SessionManagerState... targetStates) {
            for (SessionManagerState targetState : targetStates) {
                if (state == targetState) {
                    return;
                }
            }
            throw new IllegalStateException(
                    "The status of the node's session manager should have been one of "
                            + targetStates + ", but is currently " + state);
        }

        /**
         * (private)<br>
         * Check if an error was received from the endpoint, regardless to the current state, and
         * then request the cancelling of the session and throws an exception.
         *
         * @throws RuntimeException With the original cause if an error exists.
         */
        private void checkError() {
            if (state == SessionManagerState.ERROR_RECEIVED) {
                state = SessionManagerState.CANCEL_SESSION_REQUESTED;
                throw new RuntimeException(error);
            }
        }

        /**
         * (private)<br>
         * The timeout case : request the cancelling of the session and throws an exception.
         *
         * @throws KeypleTimeoutException
         */
        private void timeout() {
            state = SessionManagerState.CANCEL_SESSION_REQUESTED;
            throw new KeypleTimeoutException(
                    "Timeout occurs for the task associated with the node's session [" + sessionId
                            + "]");
        }

        /**
         * (private)<br>
         * Verify if a cancelling was requested or if the current session is closing.
         *
         * @return true if a cancelling was requested or if the current session is closing.
         */
        private boolean isSessionClosing() {
            if (state == SessionManagerState.CANCEL_SESSION_REQUESTED
                    || state == SessionManagerState.CLOSE_SESSION_BEGIN
                    || state == SessionManagerState.CLOSE_SESSION_END) {
                logger.warn("The node's session [{}] is closing", sessionId);
                return true;
            }
            return false;
        }

        /**
         * (private)<br>
         * Verify only if a cancelling was requested.
         *
         * @return true if a cancelling was requested.
         */
        private boolean isSessionCanceling() {
            if (state == SessionManagerState.CANCEL_SESSION_REQUESTED) {
                logger.warn("The node's session [{}] is canceling", sessionId);
                return true;
            }
            return false;
        }

        /**
         * (private)<br>
         * Called by the handler to open the session by calling the endpoint and awaiting the
         * result.
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
         * Called by the endpoint and notify the awaiting thread if necessary.<br>
         * Note that nothing happens if the session is being closed.
         *
         * @throws IllegalArgumentException in case of bad use.
         */
        private synchronized void onOpen() {
            if (isSessionClosing()) {
                return;
            }
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
            checkError();
            state = SessionManagerState.SEND_REQUEST_BEGIN;
            response = null;
            endpoint.sendMessage(msg);
            waitForState(SessionManagerState.SEND_REQUEST_END);
            return response;
        }

        /**
         * (private)<br>
         * Called by the endpoint and notify the awaiting thread if necessary.<br>
         * Note that nothing happens if the session is being closed.
         *
         * @param msg The response received from the endpoint.
         * @throws IllegalArgumentException in case of bad use.
         */
        private synchronized void onResponse(KeypleMessageDto msg) {
            if (isSessionClosing()) {
                return;
            }
            checkState(SessionManagerState.SEND_REQUEST_BEGIN);
            response = msg;
            state = SessionManagerState.SEND_REQUEST_END;
            notify();
        }

        /**
         * (private)<br>
         * Called by the endpoint to transmit an event to the handler<br>
         * Note that nothing happens if the session is being closed.
         *
         * @param msg The event received from the endpoint.
         */
        private void onEvent(KeypleMessageDto msg) {
            if (isSessionClosing()) {
                return;
            }
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
            checkError();
            state = SessionManagerState.SEND_MESSAGE;
            endpoint.sendMessage(msg);
            checkError();
        }

        /**
         * (private)<br>
         * Called by the handler or by the node to close the current session by calling the endpoint
         * and awaiting the result.
         *
         * @throws KeypleTimeoutException if a timeout occurs.
         * @throws RuntimeException if an error occurs.
         */
        private synchronized void closeSession() {
            checkError();
            state = SessionManagerState.CLOSE_SESSION_BEGIN;
            endpoint.closeSession(sessionId);
            waitForState(SessionManagerState.CLOSE_SESSION_END);
        }

        /**
         * (private)<br>
         * Called by the endpoint and notify the awaiting thread if necessary.<br>
         *
         * @throws IllegalArgumentException in case of bad use.
         */
        private synchronized void onClose() {
            checkState(SessionManagerState.CLOSE_SESSION_BEGIN);
            state = SessionManagerState.CLOSE_SESSION_END;
            notify();
        }

        /**
         * (private)<br>
         * Called by the endpoint in case of endpoint error and notify the awaiting thread if
         * necessary.<br>
         * Note that nothing happens if the session is being closed.
         *
         * @throws IllegalArgumentException in case of bad use.
         */
        private synchronized void onError(Throwable e) {
            if (isSessionCanceling()) {
                logger.error("Endpoint error for the node's session [{}] : {}", sessionId,
                        e.getMessage(), e);
                return;
            }
            checkState(SessionManagerState.OPEN_SESSION_BEGIN, //
                    SessionManagerState.SEND_REQUEST_BEGIN, //
                    SessionManagerState.SEND_MESSAGE, //
                    SessionManagerState.CLOSE_SESSION_BEGIN);
            error = e;
            state = SessionManagerState.ERROR_RECEIVED;
            notify();
        }
    }
}

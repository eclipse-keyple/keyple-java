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

import java.util.Arrays;
import java.util.UUID;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleRemoteCommunicationException;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Keyple Node.
 * <p>
 * This is an internal class an must not be used by the user.
 *
 * @since 1.0
 */
public abstract class AbstractKeypleNode {

    private static final Logger logger = LoggerFactory.getLogger(AbstractKeypleNode.class);

    /**
     * (protected)<br>
     * The node id.
     */
    protected final String nodeId;

    /**
     * (protected)<br>
     * The associated handler.
     */
    protected final AbstractKeypleMessageHandler handler;

    /**
     * (private)<br>
     * Timeout used during awaiting (in milliseconds)
     */
    private final int timeout;

    /**
     * (package-private)<br>
     * Constructor.
     *
     * @param handler The associated handler (must be not null).
     * @param timeoutInSecond The default timeout (in seconds) to use.
     */
    AbstractKeypleNode(AbstractKeypleMessageHandler handler, int timeoutInSecond) {
        this.nodeId = UUID.randomUUID().toString();
        this.handler = handler;
        this.timeout = timeoutInSecond * 1000;
    }

    /**
     * Open a new session on the endpoint (for internal use only).
     *
     * @param sessionId The session id (must be not empty).
     * @since 1.0
     */
    public abstract void openSession(String sessionId);

    /**
     * Send a request and return a response (for internal use only).
     *
     * @param msg The message to send (must be not null).
     * @return null if there is no response.
     * @since 1.0
     */
    public abstract KeypleMessageDto sendRequest(KeypleMessageDto msg);

    /**
     * Send a message (for internal use only).
     *
     * @param msg The message to send (must be not null).
     * @since 1.0
     */
    public abstract void sendMessage(KeypleMessageDto msg);

    /**
     * Close the session having the provided session id (for internal use only).
     *
     * @param sessionId The session id (must be not empty).
     * @since 1.0
     */
    public abstract void closeSession(String sessionId);

    /**
     * Close the session silently (without throwing exceptions)
     *
     * @param sessionId The session id (must be not empty).
     * @since 1.0
     */
    public void closeSessionSilently(String sessionId) {
        try {
            closeSession(sessionId);
        } catch (RuntimeException e) {
            logger.error("Error during the silent closing of node's session [{}] : {}", sessionId,
                    e.getMessage(), e);
        }
    }

    /**
     * (protected)<br>
     * The session manager state enum.
     */
    protected enum SessionManagerState {
        INITIALIZED, //
        OPEN_SESSION_BEGIN, //
        OPEN_SESSION_END, //
        ON_REQUEST, //
        ON_MESSAGE, //
        SEND_REQUEST_BEGIN, //
        SEND_REQUEST_END, //
        SEND_MESSAGE, //
        EXTERNAL_ERROR_OCCURRED, //
        CLOSE_SESSION_BEGIN, //
        CLOSE_SESSION_END, //
        ABORTED_SESSION
    }

    /**
     * (protected)<br>
     * The inner session manager abstract class.<br>
     * There is one manager by session id.
     */
    protected abstract class AbstractSessionManager {

        protected final String sessionId;

        protected volatile SessionManagerState state;
        protected volatile KeypleMessageDto response;
        protected volatile Throwable error;

        /**
         * (protected)<br>
         * Constructor
         *
         * @param sessionId The session id to manage.
         */
        protected AbstractSessionManager(String sessionId) {
            this.sessionId = sessionId;
            this.state = SessionManagerState.INITIALIZED;
            this.response = null;
            this.error = null;
        }

        /**
         * (protected)<br>
         * Check if the current state is equal to the target state, else wait until a timeout or to
         * be wake up by another thread.<br>
         *
         * @param targetStates The target states.
         * @throws KeypleTimeoutException if a timeout occurs.
         */
        protected void waitForState(SessionManagerState... targetStates) {
            for (SessionManagerState targetState : targetStates) {
                if (state == targetState) {
                    return;
                }
            }
            checkIfExternalErrorOccurred();
            try {
                wait(timeout);
                for (SessionManagerState targetState : targetStates) {
                    if (state == targetState) {
                        return;
                    }
                }
                checkIfExternalErrorOccurred();
                timeoutOccurred();
            } catch (InterruptedException e) {
                logger.error(
                        "Unexpected interruption of the task associated with the node's session {}",
                        sessionId, e);
                Thread.currentThread().interrupt();
            }
        }

        /**
         * (protected)<br>
         * Check if an external error was received from the endpoint or the handler, regardless to
         * the current state, and then request the cancelling of the session and throws an
         * exception.
         *
         * @throws KeypleRemoteCommunicationException with the original cause if an error exists.
         */
        protected abstract void checkIfExternalErrorOccurred();

        /**
         * (protected)<br>
         * Check if the current state is one of the provided target states.
         *
         * @param targetStates The target states to test.
         * @throws IllegalStateException if the current state does not match any of the states
         *         provided.
         */
        protected void checkState(SessionManagerState... targetStates) {
            for (SessionManagerState targetState : targetStates) {
                if (state == targetState) {
                    return;
                }
            }
            throw new IllegalStateException("The status of the node's session manager [" + sessionId
                    + "] should have been one of " + Arrays.toString(targetStates)
                    + ", but is currently " + state);
        }

        /**
         * (private)<br>
         * The timeout case : request the cancelling of the session and throws an exception.
         *
         * @throws KeypleTimeoutException The thrown exception.
         */
        private void timeoutOccurred() {
            state = SessionManagerState.ABORTED_SESSION;
            throw new KeypleTimeoutException(
                    "Timeout occurs for the task associated with the node's session [" + sessionId
                            + "]");
        }
    }
}

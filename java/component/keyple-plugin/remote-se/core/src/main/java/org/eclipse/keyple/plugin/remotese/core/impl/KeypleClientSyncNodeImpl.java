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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSyncNode;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

/**
 * Keyple Client Sync Node implementation.
 * <p>
 * This is an internal class an must not be used by the user.
 *
 * @since 1.0
 */
public final class KeypleClientSyncNodeImpl extends AbstractKeypleNode
        implements KeypleClientSyncNode {

    private static final Logger logger = LoggerFactory.getLogger(KeypleClientSyncNodeImpl.class);

    private final KeypleClientSync endpoint;

    /**
     * (package-private)<br>
     * Constructor.
     *
     * @param handler The associated handler (must be not null).
     * @param endpoint The user client sync endpoint (must be not null).
     * @param pluginObservationStrategy The server push event strategy associated to the plugin
     *        observation (null if must not be activate).<br>
     *        This parameter can be used only for <b>Remote SE Client Plugin</b> use case.
     * @param readerObservationStrategy The server push event strategy associated to the reader
     *        observation (null if must not be activate).<br>
     */
    KeypleClientSyncNodeImpl(AbstractKeypleMessageHandler handler, KeypleClientSync endpoint,
            ServerPushEventStrategy pluginObservationStrategy,
            ServerPushEventStrategy readerObservationStrategy) {

        super(handler, 0);
        this.endpoint = endpoint;

        if (pluginObservationStrategy != null) {
            EventObserver pluginEventObserver = new EventObserver(pluginObservationStrategy,
                    KeypleMessageDto.Action.CHECK_PLUGIN_EVENT);
            pluginEventObserver.start();
        }
        if (readerObservationStrategy != null) {
            EventObserver readerEventObserver = new EventObserver(readerObservationStrategy,
                    KeypleMessageDto.Action.CHECK_READER_EVENT);
            readerEventObserver.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openSession(String sessionId) {
        // NOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeypleMessageDto sendRequest(KeypleMessageDto msg) {

        msg.setClientNodeId(nodeId);
        List<KeypleMessageDto> responses = endpoint.sendRequest(msg);

        if (responses == null || responses.isEmpty()) {
            return null;
        } else if (responses.size() == 1) {
            KeypleMessageDto response = responses.get(0);
            Assert.getInstance()//
                    .notNull(response, "msg")//
                    .notEmpty(response.getSessionId(), "sessionId")//
                    .notEmpty(response.getAction(), "action")//
                    .notEmpty(response.getClientNodeId(), "clientNodeId")//
                    .notEmpty(response.getServerNodeId(), "serverNodeId");
            return response;
        } else {
            throw new IllegalStateException(
                    "The list returned by the client endpoint should have contained a single element but contains "
                            + responses.size() + " elements.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(KeypleMessageDto msg) {
        msg.setClientNodeId(nodeId);
        endpoint.sendRequest(msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeSession(String sessionId) {
        // NOP
    }

    /**
     * (private)<br>
     * Event Observer inner class.<br>
     * This class can be used only for <b>Remote SE Client Plugin</b> use case.
     */
    private class EventObserver {

        private final ServerPushEventStrategy strategy;
        private final KeypleMessageDto.Action action;
        private final KeypleMessageDto msg;
        private final Thread thread;

        /**
         * (private)<br>
         * Constructor.
         *
         * @param strategy The server push event strategy (must not be null).
         * @param action The action to perform (must not be null).
         */
        private EventObserver(ServerPushEventStrategy strategy, KeypleMessageDto.Action action) {
            this.strategy = strategy;
            this.action = action;
            this.msg = buildMessage();
            if (strategy.getType() == ServerPushEventStrategy.Type.POLLING) {
                this.thread = new PollingEventObserver();
            } else {
                this.thread = new LongPollingEventObserver();
            }
            thread.setUncaughtExceptionHandler(new EventObserverUncaughtExceptionHandler());
            thread.setName(action.name());
        }

        /**
         * (private)<br>
         * Builds the message to send to server for event observation.
         *
         * @return a not null reference
         */
        private KeypleMessageDto buildMessage() {
            JsonObject body = new JsonObject();
            body.addProperty("strategy", strategy.getType().name());
            if (strategy.getType() == ServerPushEventStrategy.Type.LONG_POLLING) {
                body.addProperty("duration", strategy.getDuration());
            }
            return new KeypleMessageDto()//
                    .setSessionId(UUID.randomUUID().toString())//
                    .setAction(action.name())//
                    .setClientNodeId(nodeId)//
                    .setBody(body.toString());
        }

        /**
         * (private)<br>
         * Polling Event Observer inner thread.
         */
        private class PollingEventObserver extends Thread {
            @Override
            public void run() {
                int requestFrequencyInMillis = strategy.getDuration() * 1000;
                while (!Thread.currentThread().isInterrupted()) {
                    checkForEvents();
                    try {
                        Thread.sleep(requestFrequencyInMillis);
                    } catch (InterruptedException e) {
                        logger.error("Unexpected interruption of thread {}", getName(), e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        /**
         * (private)<br>
         * Long Polling Event Observer inner thread.
         */
        private class LongPollingEventObserver extends Thread {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    checkForEvents();
                }
            }
        }

        /**
         * (private)<br>
         * Event Observer Uncaught Exception Handler inner class.
         */
        private class EventObserverUncaughtExceptionHandler
                implements Thread.UncaughtExceptionHandler {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.error("Interruption of thread {} caused by an unhandled exception",
                        t.getName(), e);
            }
        }

        /**
         * (private)<br>
         * Check if server has some events to push.<br>
         * If so, then forward the events to the handler.
         */
        private void checkForEvents() {
            List<KeypleMessageDto> responses;
            try {
                responses = endpoint.sendRequest(msg);
            } catch (Exception e) {
                logger.error("Server connection error", e);
                responses = retryRequest();
            }
            if (responses != null && !responses.isEmpty()) {
                for (KeypleMessageDto event : responses) {
                    handler.onMessage(event);
                }
            }
        }

        /**
         * (private)<br>
         * Retry to send the request to the server in case of server connection error until the
         * server communication is reestablished or the thread is interrupted.<br>
         * The timing of the attempts is based on the Fibonacci sequence.
         *
         * @return a not null list.
         */
        private List<KeypleMessageDto> retryRequest() {
            List<KeypleMessageDto> responses;
            int timer1 = 0;
            int timer2 = 1000;
            int timer;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    timer = timer1 + timer2;
                    Thread.sleep(timer);
                    try {
                        logger.info("Retry to send request after {} seconds...", timer / 1000);
                        responses = endpoint.sendRequest(msg);
                        logger.info("Server connection retrieved");
                        return responses;
                    } catch (Exception e) {
                        timer1 = timer2;
                        timer2 = timer;
                    }
                } catch (InterruptedException e) {
                    logger.error("Unexpected interruption of thread {}",
                            Thread.currentThread().getName(), e);
                    Thread.currentThread().interrupt();
                }
            }
            return new ArrayList<KeypleMessageDto>();
        }

        /**
         * (private)<br>
         * Starts the thread.
         */
        private void start() {
            thread.start();
        }

    }

}

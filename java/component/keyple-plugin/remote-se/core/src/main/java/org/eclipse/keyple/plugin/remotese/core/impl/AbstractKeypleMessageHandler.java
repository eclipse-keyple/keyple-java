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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Keyple Message Handler.
 * <p>
 * This is an internal class an must not be used by the user.
 * 
 * @since 1.0
 */
public abstract class AbstractKeypleMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractKeypleMessageHandler.class);

    /**
     * (protected)<br>
     * The bounded node.
     */
    protected AbstractKeypleNode node;

    /**
     * (protected)<br>
     * Is handler bound to async node ?
     */
    protected boolean isBoundToAsyncNode;

    /**
     * (protected)<br>
     * Constructor.
     */
    protected AbstractKeypleMessageHandler() {}

    /**
     * (protected)<br>
     * This method processes an incoming message.<br>
     * It should be called by a node following the reception of a {@link KeypleMessageDto}.
     *
     * @param msg The message to process.
     */
    protected abstract void onMessage(KeypleMessageDto msg);

    /**
     * This method builds and bind a {@link KeypleClientAsyncNode} with the handler.<br>
     * It must be called by the factory during the initialization phase.
     *
     * @param endpoint The {@link KeypleClientAsync} endpoint.
     * @since 1.0
     */
    public void bindClientAsyncNode(KeypleClientAsync endpoint) {
        node = new KeypleClientAsyncNodeImpl(this, endpoint, 20);
        isBoundToAsyncNode = true;
    }

    /**
     * This method builds and bind a {@link KeypleServerAsyncNode} with the handler.<br>
     * It must be called by the factory during the initialization phase.
     *
     * @param endpoint The {@link KeypleServerAsync} endpoint.
     * @since 1.0
     */
    public void bindServerAsyncNode(KeypleServerAsync endpoint) {
        // TODO KEYP-296
        isBoundToAsyncNode = true;
    }

    /**
     * This method builds and bind a {@link KeypleClientSyncNode} with the handler.<br>
     * It must be called by the factory during the initialization phase.
     *
     * @param endpoint The {@link KeypleClientSync} endpoint.
     * @param pluginObservationStrategy The {@link ServerPushEventStrategy} associated to the plugin
     *        (null if observation is not activated).
     * @param readerObservationStrategy The {@link ServerPushEventStrategy} associated to the reader
     *        (null if observation is not activated).
     * @since 1.0
     */
    public void bindClientSyncNode(KeypleClientSync endpoint,
            ServerPushEventStrategy pluginObservationStrategy,
            ServerPushEventStrategy readerObservationStrategy) {
        node = new KeypleClientSyncNodeImpl(this, endpoint, pluginObservationStrategy,
                readerObservationStrategy);
        isBoundToAsyncNode = false;
    }

    /**
     * This method builds and bind a {@link KeypleServerSyncNode} with the handler.<br>
     * It must be called by the factory during the initialization phase.
     *
     * @since 1.0
     */
    public void bindServerSyncNode() {
        node = new KeypleServerSyncNodeImpl(this, 20);
        isBoundToAsyncNode = false;
    }

    /**
     * If message contains an error, throws the embedded exception.
     *
     * @param message not null instance
     */
    protected void checkError(KeypleMessageDto message) {
        // throw exception if message is ERROR
        if (message.getAction().equals(KeypleMessageDto.Action.ERROR.name())) {
            Gson parser = KeypleJsonParser.getParser();
            JsonObject body = parser.fromJson(message.getBody(), JsonObject.class);
            if(body.has("code")){
                String classname = body.get("code").getAsString();
                try {
                    RuntimeException exception = (RuntimeException) parser.fromJson(body, Class.forName(classname));
                    logger.error("KeypleDto contains exception : {}", exception);
                    throw exception;
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Unable to parse exception from keypleDto " + message.toString());
                }
            }else{
                throw new IllegalArgumentException("Unable to parse exception from keypleDto " + message.toString());
            }
        }
    }
}

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

import org.eclipse.keyple.plugin.remotese.core.*;

/**
 * Abstract Keyple Message Handler.
 * 
 * @since 1.0
 */
public abstract class AbstractKeypleMessageHandler {

    /**
     * (protected)<br>
     * Indicates whether the handler is bound to a synchronous node.
     */
    protected boolean isBoundToSyncNode;

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
        // TODO KEYP-259
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
        // TODO KEYP-257
    }

    /**
     * This method builds and bind a {@link KeypleServerSyncNode} with the handler.<br>
     * It must be called by the factory during the initialization phase.
     *
     * @since 1.0
     */
    public void bindServerSyncNode() {
        // TODO KEYP-258
    }
}

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

import java.util.UUID;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;

/**
 * Abstract Keyple Node.
 * <p>
 * This is an internal class an must not be used by the user.
 *
 * @since 1.0
 */
public abstract class AbstractKeypleNode {

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
     * (package-private)<br>
     * Constructor.
     *
     * @param handler The associated handler (must be not null).
     */
    AbstractKeypleNode(AbstractKeypleMessageHandler handler) {
        this.nodeId = UUID.randomUUID().toString();
        this.handler = handler;
    }

    /**
     * Send a request and return a response.
     *
     * @param msg The message to send (must be not null).
     * @return a not null response.
     * @since 1.0
     */
    public abstract KeypleMessageDto sendRequest(KeypleMessageDto msg);

    /**
     * Send a message.
     *
     * @param msg The message to send (must be not null).
     * @since 1.0
     */
    public abstract void sendMessage(KeypleMessageDto msg);
}

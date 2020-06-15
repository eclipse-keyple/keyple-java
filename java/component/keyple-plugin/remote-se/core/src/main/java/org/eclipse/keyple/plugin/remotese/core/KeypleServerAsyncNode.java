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
package org.eclipse.keyple.plugin.remotese.core;

/**
 * <b>Keyple Server Async Node</b> API.
 * <p>
 * This kind of node must be bind on the server's side if you want to use a full duplex
 * communication protocol, such as Web Sockets for example.
 * <p>
 * Then, you must provide an implementation of the {@link KeypleServerAsync} interface in order to
 * interact with this node.
 * <p>
 * Keyple provides its own implementations of this interface and manages their lifecycle.<br>
 * This kind of node can be bind to a all <b>server</b> Remote SE plugins and services :
 * <ul>
 * <li>{@code RemoteSeServerPlugin}</li>
 * <li>{@code NativeSeServerService}</li>
 * <li>{@code NativeSePoolServerService}</li>
 * </ul>
 * To create it, you must only bind an <b>async</b> node during the initialization process.<br>
 * Then, you can access it everywhere on the server's side using one of the following utility
 * methods, depending on your use case :
 * <ul>
 * <li>{@code RemoteSeServerUtils.getAsyncNode()}</li>
 * <li>{@code NativeSeServerUtils.getAsyncNode()}</li>
 * <li>{@code NativeSePoolServerUtils.getAsyncNode()}</li>
 * </ul>
 *
 * @since 1.0
 */
public interface KeypleServerAsyncNode {

    /**
     * This method must be called by the {@link KeypleServerAsync} endpoint following the reception
     * and deserialization of a {@link KeypleMessageDto} from the client.
     *
     * @param msg The message to process.
     * @since 1.0
     */
    void onMessage(KeypleMessageDto msg);

    /**
     * This method must be called by the {@link KeypleServerAsync} endpoint if a technical error
     * occurs when sending a message to the client.
     *
     * @param sessionId The session id register during the session opening process.
     * @param error The unexpected error.
     * @since 1.0
     */
    void onError(String sessionId, Throwable error);
}

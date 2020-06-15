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
 * <b>Keyple Client Async</b> endpoint API to be implemented by the user.
 * <p>
 * This interface must be implemented by a user client endpoint if you want to use a full duplex
 * communication protocol, such as Web Sockets for example.
 * <p>
 * This endpoint must interact with a {@link KeypleClientAsyncNode} locally and with a
 * {@link KeypleServerAsync} endpoint remotely.
 *
 * @since 1.0
 */
public interface KeypleClientAsync {

    /**
     * This method is called by {@link KeypleClientAsyncNode} to open a communication session with
     * the server.<br>
     * Following the opening of the session you must :
     * <ul>
     * <li>Associate the session with the provided <b>sessionId</b> in order to be able to retrieve
     * the session later.</li>
     * <li>Retrieve the node {@link KeypleClientAsyncNode} using one of the following <b>client</b>
     * utility methods, depending on your use case :
     * <ul>
     * <li>{@code NativeSeClientUtils.getAsyncNode()}</li>
     * <li>{@code RemoteSeClientUtils.getAsyncNode()}</li>
     * <li>{@code RemoteSePoolClientUtils.getAsyncNode()}</li>
     * </ul>
     * </li>
     * <li>Call the method {@link KeypleClientAsyncNode#onOpen(String)} on the node.</li>
     * </ul>
     *
     * @param sessionId The session id.
     * @since 1.0
     */
    void openSession(String sessionId);

    /**
     * This method is called by {@link KeypleClientAsyncNode} to send a {@link KeypleMessageDto} to
     * the server.<br>
     * You have to :
     * <ul>
     * <li>Find the opened session using the accessible <b>sessionId</b> value using the method
     * {@link KeypleMessageDto#getSessionId()} on the provided message to send.</li>
     * <li>Serialize and send the {@link KeypleMessageDto} to the server.</li>
     * </ul>
     *
     * @param msg The message to send.
     * @since 1.0
     */
    void sendMessage(KeypleMessageDto msg);

    /**
     * This method is called by {@link KeypleClientAsyncNode} to close a communication session with
     * the server identified by the provided <b>sessionId</b>.<br>
     * Following the closing of the session you must :
     * <ul>
     * <li>Unregister the session associated to the provided <b>sessionId</b>.</li>
     * <li>Retrieve the node {@link KeypleClientAsyncNode} using the right <b>client</b> utility
     * methods, depending on your use case :
     * <ul>
     * <li>{@code NativeSeClientUtils.getAsyncNode()}</li>
     * <li>{@code RemoteSeClientUtils.getAsyncNode()}</li>
     * <li>{@code RemoteSePoolClientUtils.getAsyncNode()}</li>
     * </ul>
     * </li>
     * <li>Call the method {@link KeypleClientAsyncNode#onClose(String)} on the node.</li>
     * </ul>
     *
     * @param sessionId The session id.
     * @since 1.0
     */
    void closeSession(String sessionId);
}

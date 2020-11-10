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
package org.eclipse.keyple.plugin.remote.spi;

import org.eclipse.keyple.plugin.remote.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.KeypleServerAsyncNode;

/**
 * <b>Server Async Endpoint</b> endpoint API to be implemented by the user.
 *
 * <p>This interface must be implemented by a user server endpoint if you want to use a full duplex
 * communication protocol, such as Web Sockets for example.
 *
 * <p>Following the receive of a message from the client, the endpoint must :
 *
 * <ul>
 *   <li>Associate the session with the the accessible <b>sessionId</b> value using the method
 *       {@link KeypleMessageDto#getSessionId()} on the received message in order to be able to
 *       retrieve the session later.
 *   <li>Retrieve the node {@link KeypleServerAsyncNode} using one of the following <b>server</b>
 *       utility methods, depending on your use case :
 *       <ul>
 *         <li>{@code RemoteServerUtils.getAsyncNode()}
 *         <li>{@code NativeSeServerUtils.getAsyncNode()}
 *         <li>{@code NativeSePoolServerUtils.getAsyncNode()}
 *       </ul>
 *   <li>Call the method {@link KeypleServerAsyncNode#onMessage(KeypleMessageDto)} on the node.
 *   <li>Call the method {@link KeypleServerAsyncNode#onClose(String)} on the node after the session
 *       closing.
 * </ul>
 *
 * <p>This endpoint must interact with a {@link KeypleServerAsyncNode} locally and with a {@link
 * AsyncEndpointClient} endpoint remotely.
 *
 * @since 1.0
 */
public interface AsyncEndpointServer {

  /**
   * This method is called by {@link KeypleServerAsyncNode} to send a {@link KeypleMessageDto} to
   * the client.<br>
   * You have to :
   *
   * <ul>
   *   <li>Find the opened session using the accessible <b>sessionId</b> value using the method
   *       {@link KeypleMessageDto#getSessionId()} on the provided message to send.
   *   <li>Serialize and send the {@link KeypleMessageDto} to the client.
   * </ul>
   *
   * @param msg The message to send.
   * @since 1.0
   */
  void sendMessage(KeypleMessageDto msg);
}

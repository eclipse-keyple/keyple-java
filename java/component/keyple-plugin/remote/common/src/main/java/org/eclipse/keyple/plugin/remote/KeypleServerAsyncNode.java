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
package org.eclipse.keyple.plugin.remote;

import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;

/**
 * <b>Keyple Server Async Node</b> API.
 *
 * <p>This kind of node must be bind on the server's side if you want to use a full duplex
 * communication protocol, such as Web Sockets for example.
 *
 * <p>Then, you must provide an implementation of the {@link AsyncEndpointServer} interface in order
 * to interact with this node.
 *
 * <p>Keyple provides its own implementations of this interface and manages their lifecycle.<br>
 * This kind of node can be bind to a all <b>server</b> Remote plugins and services :
 *
 * <ul>
 *   <li>{@code RemoteServerPlugin}
 *   <li>{@code NativeServerService}
 *   <li>{@code NativePoolServerService}
 * </ul>
 *
 * To create it, you must only bind an <b>async</b> node during the initialization process.<br>
 * Then, you can access it everywhere on the server's side using one of the following utility
 * methods, depending on your use case :
 *
 * <ul>
 *   <li>{@code RemoteServerUtils.getAsyncNode()}
 *   <li>{@code NativeServerUtils.getAsyncNode()}
 *   <li>{@code NativePoolServerUtils.getAsyncNode()}
 * </ul>
 *
 * @since 1.0
 */
public interface KeypleServerAsyncNode {

  /**
   * This method must be called by the {@link AsyncEndpointServer} endpoint following the reception
   * and deserialization of a {@link KeypleMessageDto} from the client.
   *
   * @param msg The message to process.
   * @since 1.0
   */
  void onMessage(KeypleMessageDto msg);

  /**
   * This method should be called by the {@link AsyncEndpointServer} endpoint following the closing of
   * a communication session with the client.
   *
   * @param sessionId The session id registered during the session opening process.
   * @since 1.0
   */
  void onClose(String sessionId);

  /**
   * This method must be called by the {@link AsyncEndpointServer} endpoint if a technical error
   * occurs when sending a message to the client.
   *
   * @param sessionId The session id register during the session opening process.
   * @param error The unexpected error.
   * @since 1.0
   */
  void onError(String sessionId, Throwable error);
}

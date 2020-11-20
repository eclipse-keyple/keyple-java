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
 * API of the <b>Node</b> associated to a <b>server endpoint</b> using an <b>asynchronous</b>
 * network protocol.
 *
 * <p>You must bind this kind of node on the server's side if you plan to use a full duplex
 * communication protocol, such as Web Sockets for example.
 *
 * <p>Then, you must provide an implementation of the {@link AsyncEndpointServer} SPI in order to
 * interact with this node.
 *
 * <p>Keyple provides its own implementations of this interface and manages their lifecycle.<br>
 * This kind of node can be bind to a all <b>server</b> remote plugins and local services :
 *
 * <ul>
 *   <li>{@code RemotePluginServer}
 *   <li>{@code LocalServiceServer}
 *   <li>{@code PoolLocalServiceServer}
 * </ul>
 *
 * To create it, you must only bind an <b>async</b> node during the initialization process.<br>
 * Then, you can access it everywhere on the server's side using one of the following utility
 * methods, depending on your use case :
 *
 * <ul>
 *   <li>{@code RemotePluginServerUtils.getAsyncNode()}
 *   <li>{@code LocalServiceServerUtils.getAsyncNode()}
 *   <li>{@code PoolLocalServiceServerUtils.getAsyncNode()}
 * </ul>
 *
 * @since 1.0
 */
public interface AsyncNodeServer {

  /**
   * This method must be invoked by the {@link AsyncEndpointServer} endpoint following the reception
   * and deserialization of a {@link MessageDto} from the client.
   *
   * @param msg The message to process.
   * @since 1.0
   */
  void onMessage(MessageDto msg);

  /**
   * This method must be invoked by the {@link AsyncEndpointServer} endpoint following the closing
   * of a communication session with the client.
   *
   * @param sessionId The session id registered during the session opening process.
   * @since 1.0
   */
  void onClose(String sessionId);

  /**
   * This method must be invoked by the {@link AsyncEndpointServer} endpoint if a technical error
   * occurs when sending a message to the client.
   *
   * @param sessionId The session id register during the session opening process.
   * @param error The unexpected error.
   * @since 1.0
   */
  void onError(String sessionId, Throwable error);
}

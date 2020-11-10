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

import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;

/**
 * <b>Client Async Node</b> API.
 *
 * <p>This kind of node should be bind on the client's side if you want to use a full duplex
 * communication protocol, such as Web Sockets for example.
 *
 * <p>Then, you should provide an implementation of the {@link AsyncEndpointClient} interface in order
 * to interact with this node.
 *
 * <p>Keyple provides its own implementations of this interface and manages their lifecycle.<br>
 * This kind of node can be bind to a all <b>client</b> Remote plugins and services :
 *
 * <ul>
 *   <li>{@code NativeSeClientService}
 *   <li>{@code RemoteClientPlugin}
 *   <li>{@code RemotePoolClientPlugin}
 * </ul>
 *
 * To create it, you should only bind an <b>async</b> node during the initialization process.<br>
 * Then, you can access it everywhere on the client's side using one of the following utility
 * methods, depending on your use case :
 *
 * <ul>
 *   <li>{@code NativeSeClientUtils.getAsyncNode()}
 *   <li>{@code RemoteClientUtils.getAsyncNode()}
 *   <li>{@code RemotePoolClientUtils.getAsyncNode()}
 * </ul>
 *
 * @since 1.0
 */
public interface AsyncNodeClient {

  /**
   * This method should be called by the {@link AsyncEndpointClient} endpoint following the opening of
   * a new communication session with the server.
   *
   * @param sessionId The session id previously transmitted to the {@link AsyncEndpointClient}
   *     endpoint to open a session.
   * @since 1.0
   */
  void onOpen(String sessionId);

  /**
   * This method should be called by the {@link AsyncEndpointClient} endpoint following the reception
   * and deserialization of a {@link KeypleMessageDto} from the server.
   *
   * @param msg The message to process.
   * @since 1.0
   */
  void onMessage(KeypleMessageDto msg);

  /**
   * This method should be called by the {@link AsyncEndpointClient} endpoint following the closing of
   * a communication session with the server.
   *
   * @param sessionId The session id registered during the session opening process.
   * @since 1.0
   */
  void onClose(String sessionId);

  /**
   * This method should be called by the {@link AsyncEndpointClient} endpoint if a technical error
   * occurs when sending a message to the server.
   *
   * @param sessionId The session id registered during the session opening process.
   * @param error The unexpected error.
   * @since 1.0
   */
  void onError(String sessionId, Throwable error);
}

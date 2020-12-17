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
package org.eclipse.keyple.distributed.spi;

import org.eclipse.keyple.distributed.AsyncNodeServer;
import org.eclipse.keyple.distributed.MessageDto;

/**
 * SPI of the <b>server endpoint</b> using an <b>asynchronous</b> network protocol.
 *
 * <p>You must provide an implementation of this interface if you plan to use a full duplex
 * communication protocol, such as Web Sockets for example.
 *
 * <p>Following the receive of a message from the client, the endpoint must :
 *
 * <ul>
 *   <li>Associate the session with the the accessible <b>sessionId</b> value using the method
 *       {@link MessageDto#getSessionId()} on the received message in order to be able to retrieve
 *       the session later.
 *   <li>Retrieve the {@link AsyncNodeServer} node using one of the following <b>server</b> utility
 *       methods, depending on your use case :
 *       <ul>
 *         <li>{@code RemotePluginServerUtils.getAsyncNode()}
 *         <li>{@code LocalServiceServerUtils.getAsyncNode()}
 *         <li>{@code PoolLocalServiceServerUtils.getAsyncNode()}
 *       </ul>
 *   <li>Invoke the method {@link AsyncNodeServer#onMessage(MessageDto)} on the node.
 *   <li>Invoke the method {@link AsyncNodeServer#onClose(String)} on the node after the session
 *       closing.
 * </ul>
 *
 * <p>This endpoint interacts locally with a {@link AsyncNodeServer} node and remotely with a {@link
 * AsyncEndpointClient} endpoint.
 *
 * @since 1.0
 */
public interface AsyncEndpointServer {

  /**
   * Is invoked by the {@link AsyncNodeServer} node to send a {@link MessageDto} to the client. <br>
   * You have to :
   *
   * <ul>
   *   <li>Find the opened session using the accessible <b>sessionId</b> value using the method
   *       {@link MessageDto#getSessionId()} on the provided message to send.
   *   <li>Serialize and send the {@link MessageDto} to the client.
   * </ul>
   *
   * @param msg The message to send.
   * @since 1.0
   */
  void sendMessage(MessageDto msg);
}

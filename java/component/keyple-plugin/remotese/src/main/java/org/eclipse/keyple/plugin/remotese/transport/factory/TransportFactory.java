/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.transport.factory;

import java.io.IOException;

/**
 * Factory helper for Client/Server protocol implementation. This class holds a configuration and
 * create a server and clients based on this configuration.
 *
 * <p>See {@link org.eclipse.keyple.plugin.remotese.transport.impl.java.LocalTransportFactory} for
 * an example of implementation
 */
public abstract class TransportFactory {

  /**
   * Return a (new) client for this configuration
   *
   * @param clientNodeId unique id for this client
   * @return new ClientNode
   */
  public abstract ClientNode getClient(String clientNodeId);

  /**
   * Return the server of this configuration
   *
   * @return ServerNode
   * @throws IOException if the server could not startup
   */
  public abstract ServerNode getServer() throws IOException;

  /**
   * Return the server nodeId
   *
   * @return serverNodeId
   */
  public abstract String getServerNodeId();
}

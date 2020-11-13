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

import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;

/**
 * <b>Sync Node Client</b> API.
 *
 * <p>This kind of node must be bind on the client's side if you want to use a Client-Server
 * communication protocol, such as standard HTTP for example.
 *
 * <p>Then, you must provide an implementation of the {@link SyncEndpointClient} interface in order
 * to be able to send requests to the server.
 *
 * <p>Keyple provides its own implementations of this interface and manages their lifecycle.<br>
 * This kind of node can be bind to a all <b>client</b> Remote plugins and services :
 *
 * <ul>
 *   <li>{@code LocalServiceClient}
 *   <li>{@code RemotePluginClient}
 *   <li>{@code PoolRemotePluginClient}
 * </ul>
 *
 * To create it, you must only bind a <b>sync</b> node during the initialization process and you
 * never have to access it.
 *
 * @since 1.0
 */
public interface SyncNodeClient {}

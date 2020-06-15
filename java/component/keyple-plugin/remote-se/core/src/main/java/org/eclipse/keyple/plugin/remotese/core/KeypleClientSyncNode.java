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
 * <p>
 * <b>Keyple Client Sync Node</b> API.
 * </p>
 * <p>
 * This kind of node must be bind on the client's side if you want to use a Client-Server
 * communication protocol, such as standard HTTP for example.
 * </p>
 * <p>
 * Then, you must provide an implementation of the {@link KeypleClientSync} interface in order to be
 * able to send requests to the server.
 * </p>
 * <p>
 * Keyple provides its own implementations of this interface and manages their lifecycle.<br>
 * This kind of node can be bind to a all <b>client</b> Remote SE plugins and services :
 * <ul>
 * <li>{@code NativeSeClientService}</li>
 * <li>{@code RemoteSeClientPlugin}</li>
 * <li>{@code RemoteSePoolClientPlugin}</li>
 * </ul>
 * To create it, you must only bind a <b>sync</b> node during the initialization process and you
 * never have to access it.
 * </p>
 *
 * @since 1.0
 */
public interface KeypleClientSyncNode {
}

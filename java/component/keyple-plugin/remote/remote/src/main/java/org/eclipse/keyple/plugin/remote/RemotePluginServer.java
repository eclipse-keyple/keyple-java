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

import java.util.Map;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.plugin.remote.impl.RemotePluginServerFactory;
import org.eclipse.keyple.plugin.remote.impl.RemotePluginServerUtils;

/**
 * API of the <b>Remote Plugin Server</b> associated to the <b>Local Service Client</b>.
 *
 * <p>This plugin must be registered by the application installed on a <b>Server</b> not having
 * local access to the smart card reader and that wishes to control the reader remotely :
 *
 * <ul>
 *   <li>To <b>register</b> the plugin, use the method {@link
 *       SmartCardService#registerPlugin(PluginFactory)} using the factory {@link
 *       RemotePluginServerFactory}.
 *   <li>To <b>access</b> the plugin, use the utility method {@link
 *       RemotePluginServerUtils#getRemotePlugin()}.
 *   <li>To <b>unregister</b> the plugin, use the method {@link
 *       SmartCardService#unregisterPlugin(String)} using the plugin name.
 * </ul>
 *
 * <p>This plugin behaves like an {@link ObservablePlugin} but exposes additional services and
 * contains only {@link RemoteReaderServer} and {@link ObservableRemoteReaderServer} readers.
 *
 * <p>Please note that <b>this plugin is observable only to trigger ticketing services</b> on the
 * server side, but does not allow observation on the local plugin (reader insertion, etc...).
 *
 * <p><u>How to use it ?</u><br>
 *
 * <ol>
 *   <li>Register the plugin.
 *   <li>Subscribe to plugin observation by implementing the interface {@link
 *       ObservablePlugin.PluginObserver} and using the plugin method {@link
 *       ObservablePlugin#addObserver(PluginObserver)}
 *   <li>Waiting to be notified of a plugin event of type {@link
 *       PluginEvent.EventType#READER_CONNECTED}.
 *   <li>Retrieve the name of the first reader contained in the event readers list using the method
 *       {@link PluginEvent#getReaderNames()}.
 *   <li>Retrieve the remote reader from the plugin using the method {@link
 *       RemotePluginServer#getReader(String)}.
 *   <li>If you have activated the observation of the reader events, then you can cast the reader
 *       into an {@link ObservableRemoteReaderServer}.
 *   <li>Retrieve the service id from the reader using the method {@link
 *       RemoteReaderServer#getServiceId()}.
 *   <li>Execute the ticketing service identified by the service id.
 *   <li>During the ticketing service execution, you can retrieve from the reader the user input
 *       data using the method {@link RemoteReaderServer#getUserInputData(Class)} and/or the initial
 *       smart card content transmitted by the client using the method {@link
 *       RemoteReaderServer#getInitialCardContent(Class)}.
 *   <li>To terminate the remote ticketing service, invoke on the plugin the method {@link
 *       RemotePluginServer#terminateService(String, Object)} by providing the associated reader
 *       name and optionally a user output data.
 * </ol>
 *
 * @since 1.0
 */
public interface RemotePluginServer extends ObservablePlugin {

  /**
   * {@inheritDoc}
   *
   * @return a not null sorted set of {@link Reader} but you can cast them to {@link
   *     RemoteReaderServer} if needed.
   * @since 1.0
   */
  @Override
  Map<String, Reader> getReaders();

  /**
   * {@inheritDoc}
   *
   * @return a not null reference.
   * @since 1.0
   */
  @Override
  RemoteReaderServer getReader(String name);

  /**
   * Must be invoked to terminates the remote ticketing service associated to the provided remote
   * reader name and returns to the client the provided user output data.
   *
   * @param remoteReaderName The remote reader name.
   * @param userOutputData The object containing user output data.
   * @since 1.0
   */
  void terminateService(String remoteReaderName, Object userOutputData);
}

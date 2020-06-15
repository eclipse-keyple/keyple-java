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
package org.eclipse.keyple.plugin.remotese.virtualse;

import java.util.SortedSet;
import org.eclipse.keyple.core.seproxy.AbstractPluginFactory;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;

/**
 * <p>
 * <b>Remote SE Server Plugin</b> API.
 * </p>
 * <p>
 * This plugin must be used in the use case of the <b>Remote SE Server Plugin</b>.
 * </p>
 * <p>
 * It must be register by a <b>server</b> application installed on the terminal not having local
 * access to the SE card reader and that wishes to control it remotely :
 * <ul>
 * <li>To <b>register</b> the plugin, use the Keyple service method
 * {@link SeProxyService#registerPlugin(AbstractPluginFactory)} using the factory
 * {@link RemoteSeServerPluginFactory} for the plugin creation.</li>
 * <li>To access the plugin, use one of the following utility methods :
 * <ul>
 * <li>For <b>Async</b> node configuration : {@link RemoteSeServerUtils#getAsyncPlugin()}</li>
 * <li>For <b>Sync</b> node configuration : {@link RemoteSeServerUtils#getSyncPlugin()}</li>
 * </ul>
 * </li>
 * <li>To <b>unregister</b> the plugin, use the Keyple service method
 * {@link SeProxyService#unregisterPlugin(String)} using the plugin name.</li>
 * </ul>
 * </p>
 * <p>
 * This plugin behaves like an {@link ObservablePlugin} but exposes additional services and contains
 * only {@link RemoteSeServerReader} and {@link RemoteSeServerObservableReader} readers.
 * </p>
 * <p>
 * <u>How to use it ?</u><br>
 * <ol>
 * <li>Register the plugin.</li>
 * <li>Subscribe to plugin observation by implementing the interface
 * {@link ObservablePlugin.PluginObserver} and using the plugin method
 * {@link ObservablePlugin#addObserver(PluginObserver)}</li>
 * <li>Waiting to be notified of a plugin event of type
 * {@link PluginEvent.EventType#READER_CONNECTED}.</li>
 * <li>Retrieve the name of the first reader contained in the event readers list using the method
 * {@link PluginEvent#getReaderNames()}.</li>
 * <li>Retrieve the virtual reader from the plugin using the method
 * {@link RemoteSeServerPlugin#getReader(String)}.</li>
 * <li>Retrieve the service id from the reader using the method
 * {@link RemoteSeServerReader#getServiceId()}.</li>
 * <li>Execute the ticketing service identified by the service id.</li>
 * <li>During the ticketing service execution, you can retrieve from the reader the user input data
 * and/or the initial SE content transmitted by the client.</li>
 * <li>To terminate the remote ticketing service, call on the plugin the method
 * {@link RemoteSeServerPlugin#terminateService(String, Object)} by providing the associated reader
 * name and optionally a user output data.</li>
 * </ol>
 * </p>
 *
 * @since 1.0
 */
public interface RemoteSeServerPlugin extends ObservablePlugin {

    /**
     * {@inheritDoc}
     *
     * @return a not null sorted set of {@link SeReader} but you can cast them to
     *         {@link RemoteSeServerReader}.
     * @since 1.0
     */
    @Override
    SortedSet<SeReader> getReaders();

    /**
     * {@inheritDoc}
     *
     * @return a not null reference.
     * @since 1.0
     */
    @Override
    RemoteSeServerReader getReader(String name) throws KeypleReaderNotFoundException;

    /**
     * This method terminates the remote ticketing service associated to the provided virtual reader
     * name and returns to the client the user output data provided.
     *
     * @param virtualReaderName The virtual reader name.
     * @param userOutputData The user output data.
     * @since 1.0
     */
    void terminateService(String virtualReaderName, Object userOutputData);
}

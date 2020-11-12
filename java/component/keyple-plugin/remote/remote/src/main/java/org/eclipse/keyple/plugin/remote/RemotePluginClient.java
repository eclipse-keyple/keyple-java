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

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.SmartCardService;

/**
 * <b>Remote Plugin Client</b> API.
 *
 * <p>This plugin must be used in the use case of the <b>Remote Plugin Client</b> configured
 * <b>without plugin observation</b>.
 *
 * <p>It must be register by a <b>client</b> application installed on the terminal not having local
 * access to the reader and that wishes to control the card remotely :
 *
 * <ul>
 *   <li>To <b>register</b> the plugin, use the Keyple service method {@link
 *       SmartCardService#registerPlugin(PluginFactory)} using the factory {link
 *       RemoteClientPluginFactory} and <b>do not activate the plugin observation</b>.
 *   <li>To access the plugin, use one of the following utility methods :
 *       <ul>
 *         <li>For <b>Async</b> node configuration : {link RemotePluginClientUtils#getAsyncPlugin()}
 *         <li>For <b>Sync</b> node configuration : {link RemotePluginClientUtils#getSyncPlugin()}
 *       </ul>
 *   <li>To <b>unregister</b> the plugin, use the Keyple service method {@link
 *       SmartCardService#unregisterPlugin(String)} using the plugin name.
 * </ul>
 *
 * <p>This plugin behaves like a {@link Plugin}.
 *
 * @since 1.0
 */
public interface RemotePluginClient extends Plugin {}

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
package org.eclipse.keyple.plugin.remote.virtual;

import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.plugin.remote.virtual.impl.RemotePoolClientPluginFactory;
import org.eclipse.keyple.plugin.remote.virtual.impl.RemotePoolClientUtils;

/**
 * <b>Remote Pool Client Plugin</b> API.
 *
 * <p>This plugin must be used in the use case of the <b>Remote Pool Client Plugin</b>.
 *
 * <p>It must be register by a <b>client</b> application installed on the terminal not having local
 * access to the pool cards reader and that wishes to control the card remotely :
 *
 * <ul>
 *   <li>To <b>register</b> the plugin, use the Keyple service method {@link
 *       SeProxyService#registerPlugin(PluginFactory)} using the factory {@link RemotePoolClientPluginFactory}.
 *   <li>To access the plugin, use one of the following utility methods :
 *       <ul>
 *         <li>For <b>Async</b> node configuration : {@link RemotePoolClientUtils#getAsyncPlugin()}
 *         <li>For <b>Sync</b> node configuration : {@link RemotePoolClientUtils#getSyncPlugin()}
 *       </ul>
 *   <li>To <b>unregister</b> the plugin, use the Keyple service method {@link
 *       SeProxyService#unregisterPlugin(String)} using the plugin name.
 * </ul>
 *
 * <p>This plugin behaves like a {@link ReaderPoolPlugin}.
 *
 * @since 1.0
 */
public interface RemotePoolClientPlugin extends ReaderPoolPlugin {}

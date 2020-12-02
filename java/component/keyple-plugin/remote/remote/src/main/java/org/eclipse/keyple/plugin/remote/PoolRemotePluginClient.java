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

import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.PoolPlugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.plugin.remote.impl.PoolRemotePluginClientFactory;
import org.eclipse.keyple.plugin.remote.impl.PoolRemotePluginClientUtils;

/**
 * API of the <b>Pool Remote Plugin Client</b> associated to the <b>Pool Local Service Server</b>.
 *
 * <p>This plugin must be registered by the application installed on a <b>Client</b> not having
 * local access to the pool of smart card readers and that wishes to control the reader remotely :
 *
 * <ul>
 *   <li>To <b>register</b> the plugin, use the method {@link
 *       SmartCardService#registerPlugin(PluginFactory)} using the factory {@link
 *       PoolRemotePluginClientFactory}.
 *   <li>To <b>access</b> the plugin, use the utility method {@link
 *       PoolRemotePluginClientUtils#getRemotePlugin(String)}.
 *   <li>To <b>unregister</b> the plugin, use the method {@link
 *       SmartCardService#unregisterPlugin(String)} using the plugin name.
 * </ul>
 *
 * <p>This plugin behaves like a {@link PoolPlugin}.
 *
 * @since 1.0
 */
public interface PoolRemotePluginClient extends PoolPlugin {}

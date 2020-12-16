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
package org.eclipse.keyple.distributed;

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.SmartCardService;

/**
 * API of the <b>Remote Plugin Client</b> associated to the <b>Local Service Server</b>.
 *
 * <p>This plugin must be used when you do not intend to observe remotely the plugin events.
 *
 * <p>It must be registered by the application installed on a <b>Client</b> not having local access
 * to the smart card reader and that wishes to control the reader remotely :
 *
 * <ul>
 *   <li>To <b>register</b> the plugin, use the method {@link
 *       SmartCardService#registerPlugin(PluginFactory)} using the factory {link
 *       RemotePluginClientFactory} and <b>do not activate the plugin observation</b>.
 *   <li>To <b>access</b> the plugin, use the utility method {link
 *       RemotePluginClientUtils#getRemotePlugin()}.
 *   <li>To <b>unregister</b> the plugin, use the method {@link
 *       SmartCardService#unregisterPlugin(String)} using the plugin name.
 * </ul>
 *
 * <p>This plugin behaves like a {@link Plugin}.
 *
 * @since 1.0
 */
public interface RemotePluginClient extends Plugin {}

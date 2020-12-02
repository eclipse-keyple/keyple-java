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
package org.eclipse.keyple.plugin.remote.impl;

import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.AsyncNodeClient;
import org.eclipse.keyple.plugin.remote.PoolRemotePluginClient;

/**
 * Utility class of the {@link PoolRemotePluginClient}.
 *
 * @since 1.0
 */
public final class PoolRemotePluginClientUtils {

  /**
   * Gets a PoolRemotePluginClient plugin by its name.
   *
   * @param pluginName plugin name
   * @return a not null reference
   * @throws KeyplePluginNotFoundException if the plugin is not registered.
   * @since 1.0
   */
  public static PoolRemotePluginClient getRemotePlugin(String pluginName) {
    Assert.getInstance().notNull(pluginName, "plugin name");
    return (PoolRemotePluginClient) SmartCardService.getInstance().getPlugin(pluginName);
  }

  /**
   * Gets the {@link AsyncNodeClient} node associated with a {@link PoolRemotePluginClient} plugin..
   *
   * @param pluginName name of the plugin associated with the SyncNodeServer.
   * @return a not null reference
   * @throws KeyplePluginNotFoundException if the plugin is not registered.
   * @throws IllegalStateException if the plugin is not configured with a {@link AsyncNodeClient}
   *     node.
   * @since 1.0
   */
  public static AsyncNodeClient getAsyncNode(String pluginName) {
    Assert.getInstance().notNull(pluginName, "plugin name");
    PoolRemotePluginClientImpl plugin = (PoolRemotePluginClientImpl) getRemotePlugin(pluginName);
    if (plugin.node instanceof AsyncNodeClient) {
      return (AsyncNodeClient) plugin.node;
    }
    throw new IllegalStateException(
        "The PoolRemotePluginClient is not configured with a AsyncNodeClient");
  }
}

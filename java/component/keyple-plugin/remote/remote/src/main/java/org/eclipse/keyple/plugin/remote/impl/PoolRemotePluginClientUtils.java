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
   * Gets the remote plugin having the default name.
   *
   * @return A not null reference.
   * @throws KeyplePluginNotFoundException If the plugin is not registered.
   * @since 1.0
   */
  public static PoolRemotePluginClient getRemotePlugin() {
    return (PoolRemotePluginClient)
        SmartCardService.getInstance().getPlugin(PoolRemotePluginClientFactory.DEFAULT_PLUGIN_NAME);
  }

  /**
   * Gets the remote plugin having the provided name.
   *
   * @param pluginName The plugin name.
   * @return A not null reference.
   * @throws IllegalArgumentException If the plugin name is null.
   * @throws KeyplePluginNotFoundException If the plugin is not registered.
   * @since 1.0
   */
  public static PoolRemotePluginClient getRemotePlugin(String pluginName) {
    Assert.getInstance().notNull(pluginName, "pluginName");
    return (PoolRemotePluginClient) SmartCardService.getInstance().getPlugin(pluginName);
  }

  /**
   * Gets the {@link AsyncNodeClient} node associated to the remote plugin having the default name.
   *
   * @return A not null reference.
   * @throws KeyplePluginNotFoundException If the plugin is not registered.
   * @throws IllegalStateException If the plugin is not configured with a {@link AsyncNodeClient}
   *     node.
   * @since 1.0
   */
  public static AsyncNodeClient getAsyncNode() {
    return getAsyncNode(PoolRemotePluginClientFactory.DEFAULT_PLUGIN_NAME);
  }

  /**
   * Gets the {@link AsyncNodeClient} node associated to the remote plugin having the provided name.
   *
   * @param pluginName The name of the remote plugin.
   * @return A not null reference.
   * @throws IllegalArgumentException If the plugin name is null.
   * @throws KeyplePluginNotFoundException If the plugin is not registered.
   * @throws IllegalStateException If the plugin is not configured with a {@link AsyncNodeClient}
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

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
import org.eclipse.keyple.plugin.remote.AsyncNodeServer;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.eclipse.keyple.plugin.remote.SyncNodeServer;

/**
 * Utility class of the {@link RemotePluginServer}.
 *
 * @since 1.0
 */
public final class RemotePluginServerUtils {

  private static final String PLUGIN_NAME = "pluginName";

  private RemotePluginServerUtils() {}

  /**
   * Gets the remote plugin having the default name.
   *
   * @return A not null reference.
   * @throws KeyplePluginNotFoundException If the plugin is not registered.
   * @since 1.0
   */
  public static RemotePluginServer getRemotePlugin() {
    return getRemotePlugin(RemotePluginServerFactory.DEFAULT_PLUGIN_NAME);
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
  public static RemotePluginServer getRemotePlugin(String pluginName) {
    Assert.getInstance().notNull(pluginName, PLUGIN_NAME);
    return (RemotePluginServer) SmartCardService.getInstance().getPlugin(pluginName);
  }

  /**
   * Gets the {@link AsyncNodeServer} node associated to the remote plugin having the default name.
   *
   * @return A not null reference.
   * @throws KeyplePluginNotFoundException If the plugin is not registered.
   * @throws IllegalStateException If the plugin is not configured with a {@link AsyncNodeServer}
   *     node.
   * @since 1.0
   */
  public static AsyncNodeServer getAsyncNode() {
    return getAsyncNode(RemotePluginServerFactory.DEFAULT_PLUGIN_NAME);
  }

  /**
   * Gets the {@link AsyncNodeServer} node associated to the remote plugin having the provided name.
   *
   * @param pluginName The name of the remote plugin.
   * @return A not null reference.
   * @throws IllegalArgumentException If the plugin name is null.
   * @throws KeyplePluginNotFoundException If the plugin is not registered.
   * @throws IllegalStateException If the plugin is not configured with a {@link AsyncNodeServer}
   *     node.
   * @since 1.0
   */
  public static AsyncNodeServer getAsyncNode(String pluginName) {
    Assert.getInstance().notNull(pluginName, PLUGIN_NAME);
    RemotePluginServerImpl plugin = (RemotePluginServerImpl) getRemotePlugin(pluginName);
    if (plugin.node instanceof AsyncNodeServer) {
      return (AsyncNodeServer) plugin.node;
    }
    throw new IllegalStateException(
        "The RemotePluginServer is not configured with a AsyncNodeServer");
  }

  /**
   * Gets the {@link SyncNodeServer} node associated to the remote plugin having the default name.
   *
   * @return A not null reference.
   * @throws KeyplePluginNotFoundException If the plugin is not registered.
   * @throws IllegalStateException If the plugin is not configured with a {@link SyncNodeServer}
   *     node.
   * @since 1.0
   */
  public static SyncNodeServer getSyncNode() {
    return getSyncNode(RemotePluginServerFactory.DEFAULT_PLUGIN_NAME);
  }

  /**
   * Gets the {@link SyncNodeServer} node associated to the remote plugin having the provided name.
   *
   * @param pluginName The name of the remote plugin.
   * @return A not null reference.
   * @throws IllegalArgumentException If the plugin name is null.
   * @throws KeyplePluginNotFoundException If the plugin is not registered.
   * @throws IllegalStateException If the plugin is not configured with a {@link SyncNodeServer}
   *     node.
   * @since 1.0
   */
  public static SyncNodeServer getSyncNode(String pluginName) {
    Assert.getInstance().notNull(pluginName, PLUGIN_NAME);
    RemotePluginServerImpl plugin = (RemotePluginServerImpl) getRemotePlugin(pluginName);
    if (plugin.node instanceof SyncNodeServer) {
      return (SyncNodeServer) plugin.node;
    }
    throw new IllegalStateException(
        "The RemotePluginServer is not configured with a SyncNodeServer");
  }
}

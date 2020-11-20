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
import org.eclipse.keyple.plugin.remote.AsyncNodeServer;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.eclipse.keyple.plugin.remote.SyncNodeServer;

/**
 * Utility class of the {@link RemotePluginServer}
 *
 * @since 1.0
 */
public final class RemotePluginServerUtils {

  /**
   * Gets the plugin having the default name.
   *
   * @return a not null reference
   * @throws KeyplePluginNotFoundException if the plugin is not registered.
   * @since 1.0
   */
  public static RemotePluginServer getRemotePlugin() {
    return (RemotePluginServer)
        SmartCardService.getInstance().getPlugin(RemotePluginServerFactory.DEFAULT_PLUGIN_NAME);
  }

  /**
   * Gets the {@link AsyncNodeServer} node associated to the plugin having the default name.
   *
   * @return a not null reference
   * @throws KeyplePluginNotFoundException if the plugin is not registered.
   * @throws IllegalStateException if the plugin is not configured with a {@link AsyncNodeServer}
   *     node.
   * @since 1.0
   */
  public static AsyncNodeServer getAsyncNode() {
    RemotePluginServerImpl plugin = (RemotePluginServerImpl) getRemotePlugin();
    if (plugin.node instanceof AsyncNodeServer) {
      return (AsyncNodeServer) plugin.node;
    }
    throw new IllegalStateException(
        "The RemotePluginServer is not configured with a AsyncNodeServer");
  }

  /**
   * Gets the {@link SyncNodeServer} node associated to the plugin having the default name.
   *
   * @return a not null reference
   * @throws KeyplePluginNotFoundException if the plugin is not registered.
   * @throws IllegalStateException if the plugin is not configured with a {@link SyncNodeServer}
   *     node.
   * @since 1.0
   */
  public static SyncNodeServer getSyncNode() {
    RemotePluginServerImpl plugin = (RemotePluginServerImpl) getRemotePlugin();
    if (plugin.node instanceof SyncNodeServer) {
      return (SyncNodeServer) plugin.node;
    }
    throw new IllegalStateException(
        "The RemotePluginServer is not configured with a SyncNodeServer");
  }
}

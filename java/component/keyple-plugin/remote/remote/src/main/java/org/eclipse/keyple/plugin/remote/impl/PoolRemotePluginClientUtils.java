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
import org.eclipse.keyple.plugin.remote.AsyncNodeClient;
import org.eclipse.keyple.plugin.remote.PoolRemotePluginClient;

/**
 * Utility class of the {@link PoolRemotePluginClient}.
 *
 * @since 1.0
 */
public final class PoolRemotePluginClientUtils {

  /**
   * Gets the plugin having the default name.
   *
   * @return a not null reference
   * @throws KeyplePluginNotFoundException if the plugin is not registered.
   * @since 1.0
   */
  public static PoolRemotePluginClient getRemotePlugin() {
    return (PoolRemotePluginClient)
        SmartCardService.getInstance().getPlugin(PoolRemotePluginClientFactory.DEFAULT_PLUGIN_NAME);
  }

  /**
   * Gets the {@link AsyncNodeClient} node associated to the plugin having the default name.
   *
   * @return a not null reference
   * @throws KeyplePluginNotFoundException if the plugin is not registered.
   * @throws IllegalStateException if the plugin is not configured with a {@link AsyncNodeClient}
   *     node.
   * @since 1.0
   */
  public static AsyncNodeClient getAsyncNode() {
    PoolRemotePluginClientImpl plugin = (PoolRemotePluginClientImpl) getRemotePlugin();
    if (plugin.node instanceof AsyncNodeClient) {
      return (AsyncNodeClient) plugin.node;
    }
    throw new IllegalStateException(
        "The PoolRemotePluginClient is not configured with a AsyncNodeClient");
  }
}

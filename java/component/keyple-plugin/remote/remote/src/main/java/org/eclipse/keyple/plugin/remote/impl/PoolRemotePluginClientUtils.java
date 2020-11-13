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

/** Use this class to access the registered {@link PoolRemotePluginClient} */
public class PoolRemotePluginClientUtils {

  /**
   * Retrieve the async node used in the PoolRemotePluginClient
   *
   * @return non nullable instance of AsyncNodeClient
   * @since 1.0
   */
  public static AsyncNodeClient getAsyncNode() {
    return (AsyncNodeClient) ((PoolRemotePluginClientImpl) getRemotePlugin()).node;
  }

  /**
   * Access the registered PoolRemotePluginClient
   *
   * @return a registered instance of the PoolRemotePluginClient
   * @throws KeyplePluginNotFoundException if no PoolRemotePluginClient is registered
   * @since 1.0
   */
  public static PoolRemotePluginClient getRemotePlugin() {
    return (PoolRemotePluginClient)
        SmartCardService.getInstance().getPlugin(PoolRemotePluginClientFactory.DEFAULT_PLUGIN_NAME);
  }
}

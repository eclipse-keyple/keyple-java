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

/** Use this class to access the registered {@link RemotePluginServer} */
public class RemotePluginServerUtils {

  /**
   * Access the registered RemotePluginServer
   *
   * @return a registered instance of the RemotePluginServer
   * @throws KeyplePluginNotFoundException if no RemotePluginServer is registered
   * @since 1.0
   */
  public static RemotePluginServer getRemotePlugin() {
    return (RemotePluginServer)
        SmartCardService.getInstance().getPlugin(RemotePluginServerFactory.DEFAULT_PLUGIN_NAME);
  }

  /**
   * Retrieve the async node used in the RemotePluginServer
   *
   * @return non nullable instance of AsyncNodeServer
   * @since 1.0
   */
  public static AsyncNodeServer getAsyncNode() {
    return (AsyncNodeServer) ((RemotePluginServerImpl) getRemotePlugin()).node;
  }

  /**
   * Retrieve the sync node used in the RemotePluginServer
   *
   * @return non nullable instance of SyncNodeServer
   * @since 1.0
   */
  public static SyncNodeServer getSyncNode() {
    return (SyncNodeServer) ((RemotePluginServerImpl) getRemotePlugin()).node;
  }
}

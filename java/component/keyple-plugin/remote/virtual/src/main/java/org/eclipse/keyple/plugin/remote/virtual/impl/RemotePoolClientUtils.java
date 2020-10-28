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
package org.eclipse.keyple.plugin.remote.virtual.impl;

import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.plugin.remote.core.KeypleClientAsyncNode;
import org.eclipse.keyple.plugin.remote.core.KeypleClientSyncNode;
import org.eclipse.keyple.plugin.remote.virtual.RemotePoolClientPlugin;

/** Use this class to access the registered {@link RemotePoolClientPlugin} */
public class RemotePoolClientUtils {

  /**
   * Access the registered RemotePoolClientPlugin with an async Node
   *
   * @return a registered instance of the RemotePoolClientPlugin
   * @throws KeyplePluginNotFoundException if no RemotePoolClientPlugin is registered
   * @since 1.0
   */
  public static RemotePoolClientPlugin getAsyncPlugin() {
    return (RemotePoolClientPlugin)
        SeProxyService.getInstance().getPlugin(RemotePoolClientPluginFactory.PLUGIN_NAME_ASYNC);
  }

  /**
   * Retrieve the async node used in the RemotePoolClientPlugin
   *
   * @return non nullable instance of KeypleClientAsyncNode
   * @since 1.0
   */
  public static KeypleClientAsyncNode getAsyncNode() {
    return (KeypleClientAsyncNode) ((RemotePoolClientPluginImpl) getAsyncPlugin()).getNode();
  }

  /**
   * Access the registered RemotePoolClientPlugin with a sync Node
   *
   * @return a registered instance of the RemotePoolClientPlugin
   * @throws KeyplePluginNotFoundException if no RemotePoolClientPlugin is registered
   * @since 1.0
   */
  public static RemotePoolClientPlugin getSyncPlugin() {
    return (RemotePoolClientPlugin)
        SeProxyService.getInstance().getPlugin(RemotePoolClientPluginFactory.PLUGIN_NAME_SYNC);
  }

  /**
   * Retrieve the sync node used in the RemotePoolClientPlugin
   *
   * @return non nullable instance of KeypleClientSyncNode
   * @since 1.0
   */
  public static KeypleClientSyncNode getSyncNode() {
    return (KeypleClientSyncNode) ((RemotePoolClientPluginImpl) getSyncPlugin()).getNode();
  }
}

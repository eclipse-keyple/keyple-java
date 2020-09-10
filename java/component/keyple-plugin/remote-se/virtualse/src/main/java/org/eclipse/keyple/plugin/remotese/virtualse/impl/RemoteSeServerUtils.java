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
package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerAsyncNode;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerSyncNode;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerPlugin;

/** Utils class to access the @{@link RemoteSeServerPlugin} */
public class RemoteSeServerUtils {

  /**
   * Access the registered RemoteSeServerPlugin with an async Node
   *
   * @return a registered instance of the RemoteSeServerPlugin
   * @throws KeyplePluginNotFoundException if no RemoteSeServerPlugin is registered
   */
  public static RemoteSeServerPlugin getAsyncPlugin() {
    return (RemoteSeServerPlugin)
        SeProxyService.getInstance().getPlugin(RemoteSeServerPluginFactory.PLUGIN_NAME_ASYNC);
  }

  /**
   * Retrieve the async node used in the RemoteSeServerPlugin
   *
   * @return non nullable instance of KeypleServerSyncNode
   */
  public static KeypleServerAsyncNode getAsyncNode() {
    return (KeypleServerAsyncNode) ((RemoteSeServerPluginImpl) getAsyncPlugin()).getNode();
  }

  /**
   * Access the registered RemoteSeServerPlugin with a sync Node
   *
   * @return a registered instance of the RemoteSeServerPlugin
   * @throws KeyplePluginNotFoundException if no RemoteSeServerPlugin is registered
   */
  public static RemoteSeServerPlugin getSyncPlugin() {
    return (RemoteSeServerPlugin)
        SeProxyService.getInstance().getPlugin(RemoteSeServerPluginFactory.PLUGIN_NAME_SYNC);
  }

  /**
   * Retrieve the sync node used in the RemoteSeServerPlugin
   *
   * @return non nullable instance of KeypleServerSyncNode
   */
  public static KeypleServerSyncNode getSyncNode() {
    return (KeypleServerSyncNode) ((RemoteSeServerPluginImpl) getSyncPlugin()).getNode();
  }
}

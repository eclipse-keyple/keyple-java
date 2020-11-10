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

import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.plugin.remote.core.KeypleClientAsyncNode;
import org.eclipse.keyple.plugin.remote.virtual.RemotePoolClientPlugin;

/** Use this class to access the registered {@link RemotePoolClientPlugin} */
public class RemotePoolClientUtils {

  /**
   * Retrieve the async node used in the RemotePoolClientPlugin
   *
   * @return non nullable instance of KeypleClientAsyncNode
   * @since 1.0
   */
  public static KeypleClientAsyncNode getAsyncNode() {
    return (KeypleClientAsyncNode) ((RemotePoolClientPluginImpl) getRemotePlugin()).getNode();
  }

  /**
   * Access the registered RemotePoolClientPlugin
   *
   * @return a registered instance of the RemotePoolClientPlugin
   * @throws KeyplePluginNotFoundException if no RemotePoolClientPlugin is registered
   * @since 1.0
   */
  public static RemotePoolClientPlugin getRemotePlugin() {
    return (RemotePoolClientPlugin)
        SmartCardService.getInstance().getPlugin(RemotePoolClientPluginFactory.DEFAULT_PLUGIN_NAME);
  }
}

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
package org.eclipse.keyple.example.calypso.remote.websocket.server;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.plugin.remote.impl.RemotePluginServerFactory;
import org.eclipse.keyple.remote.example.app.RemotePluginObserver;

/** Example of a server side app */
@ApplicationScoped
public class ServerApp {

  RemotePluginObserver pluginObserver;

  @Inject WebsocketEndpointServer websocketEndpointServer;

  /**
   * Initialize the Remote Plugin with an async endpoint {@link WebsocketEndpointServer} and attach
   * an observer to the plugin {@link RemotePluginObserver} that contains all the business logic
   */
  public void init() {

    pluginObserver = new RemotePluginObserver();

    SmartCardService.getInstance()
        .registerPlugin(
            RemotePluginServerFactory.builder()
                .withAsyncNode(websocketEndpointServer)
                .withPluginObserver(pluginObserver)
                .usingDefaultEventNotificationPool()
                .build());
  }
}

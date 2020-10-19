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
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.plugin.remote.virtual.impl.RemoteServerPluginFactory;
import org.eclipse.keyple.remotese.example.app.RemotePluginObserver;

/** Example of a server side app */
@ApplicationScoped
public class ServerApp {

  RemotePluginObserver pluginObserver;

  @Inject WebsocketServerEndpoint websocketServerEndpoint;

  /**
   * Initialize the Remote Plugin with an async endpoint {@link WebsocketServerEndpoint} and attach
   * an observer to the plugin {@link RemotePluginObserver} that contains all the business logic
   */
  public void init() {

    pluginObserver = new RemotePluginObserver();

    SeProxyService.getInstance()
        .registerPlugin(
            RemoteServerPluginFactory.builder()
                .withAsyncNode(websocketServerEndpoint)
                .withPluginObserver(pluginObserver)
                .usingDefaultEventNotificationPool()
                .build());
  }
}

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
package org.eclipse.keyple.example.generic.remote.server.websocket.server;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.plugin.remote.impl.RemotePluginServerFactory;

/** Example of a server side application. */
@ApplicationScoped
public class AppServer {

  /** The endpoint server */
  @Inject EndpointServer endpointServer;

  /**
   * Initialize the server components :
   *
   * <ul>
   *   <li>A {@link org.eclipse.keyple.plugin.remote.RemotePluginServer} with an async node bind to
   *       a {@link org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer} endpoint and attach an
   *       observer that contains all the business logic.
   * </ul>
   */
  public void init() {

    // Init the remote plugin observer.
    RemotePluginServerObserver pluginObserver = new RemotePluginServerObserver();

    // Init the remote plugin factory.
    RemotePluginServerFactory factory =
        RemotePluginServerFactory.builder()
            .withAsyncNode(endpointServer)
            .withPluginObserver(pluginObserver)
            .usingDefaultEventNotificationPool()
            .build();

    // Register the remote plugin to the smart card service using the factory.
    SmartCardService.getInstance().registerPlugin(factory);
  }
}

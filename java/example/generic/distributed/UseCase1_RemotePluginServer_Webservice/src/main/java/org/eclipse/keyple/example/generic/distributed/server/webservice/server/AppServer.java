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
package org.eclipse.keyple.example.generic.distributed.server.webservice.server;

import java.util.concurrent.Executors;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.distributed.impl.RemotePluginServerFactory;

/** Example of a server side application. */
@ApplicationScoped
public class AppServer {

  /**
   * Initialize the server components :
   *
   * <ul>
   *   <li>A {@link org.eclipse.keyple.distributed.RemotePluginServer} with a sync node and attach
   *       an observer that contains all the business logic.
   * </ul>
   */
  public void init() {

    // Init the remote plugin observer.
    RemotePluginServerObserver pluginObserver = new RemotePluginServerObserver();

    // Init the remote plugin factory.
    RemotePluginServerFactory factory =
        RemotePluginServerFactory.builder()
            .withDefaultPluginName()
            .withSyncNode()
            .withPluginObserver(pluginObserver)
            .usingEventNotificationPool(
                Executors.newCachedThreadPool(r -> new Thread(r, "server-pool")))
            .build();

    // Register the remote plugin to the smart card service using the factory.
    SmartCardService.getInstance().registerPlugin(factory);
  }
}

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
package org.eclipse.keyple.example.calypso.remotese.webservice.server;

import java.util.concurrent.Executors;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerPluginFactory;
import org.eclipse.keyple.remotese.example.app.RemoteSePluginObserver;

/** Example of a server side app */
@ApplicationScoped
public class ServerApp {

  RemoteSePluginObserver pluginObserver;

  /**
   * Initialize the Remote SE Plugin wit a sync node and attach an observer to the plugin {@link
   * RemoteSePluginObserver} that contains all the business logic
   */
  public void init() {

    pluginObserver = new RemoteSePluginObserver();

    SeProxyService.getInstance()
        .registerPlugin(
            RemoteSeServerPluginFactory.builder()
                .withSyncNode()
                .withPluginObserver(pluginObserver)
                .usingEventNotificationPool(
                    Executors.newCachedThreadPool(new NamedThreadFactory("server-pool")))
                .build());
  }
}

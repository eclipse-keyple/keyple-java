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
package org.eclipse.keyple.example.calypso.remote.websocket;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import javax.inject.Inject;
import org.eclipse.keyple.example.calypso.remote.websocket.client.ClientApp;
import org.eclipse.keyple.example.calypso.remote.websocket.server.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class ServerStartup {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerStartup.class);

  public static void main(String... args) {
    Quarkus.run(RemoteWebsocketExample.class, args);
  }

  /*
   * Main class of the server application.
   */
  public static class RemoteWebsocketExample implements QuarkusApplication {

    @Inject
    ServerConfiguration serverConfiguration;

    @Inject ClientApp clientApp;

    @Override
    public int run(String... args) throws Exception {

      LOGGER.info("Server app init ...");

      serverConfiguration.init();

      LOGGER.info("Client init...");

      clientApp.init();

      LOGGER.info("Launch client scenario...");

      Boolean isSuccessful = clientApp.launchScenario();

      LOGGER.info("Is scenario successful - {}", isSuccessful);

      // Quarkus.waitForExit(); close jvm after scenario
      return 0;
    }
  }
}

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
package org.eclipse.keyple.example.generic.distributed.server.websocket;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import javax.inject.Inject;
import org.eclipse.keyple.example.generic.distributed.server.websocket.client.AppClient;
import org.eclipse.keyple.example.generic.distributed.server.websocket.server.AppServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(String... args) {
    Quarkus.run(ExampleApp.class, args);
  }

  /** Main class of the server application. */
  public static class ExampleApp implements QuarkusApplication {

    /** The server application */
    @Inject private AppServer appServer;

    /** The client application */
    @Inject private AppClient appClient;

    @Override
    public int run(String... args) throws Exception {

      LOGGER.info("Server app init...");
      appServer.init();

      LOGGER.info("Client app init...");
      appClient.init();

      LOGGER.info("Launch client scenario...");
      Boolean isSuccessful = appClient.launchScenario();

      LOGGER.info("Is scenario successful ? {}", isSuccessful);
      return 0;
    }
  }
}

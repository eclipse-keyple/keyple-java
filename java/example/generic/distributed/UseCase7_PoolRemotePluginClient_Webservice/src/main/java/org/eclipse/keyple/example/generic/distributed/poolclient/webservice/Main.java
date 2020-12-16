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
package org.eclipse.keyple.example.generic.distributed.poolclient.webservice;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import javax.inject.Inject;
import org.eclipse.keyple.example.generic.distributed.poolclient.webservice.client.AppClient;
import org.eclipse.keyple.example.generic.distributed.poolclient.webservice.server.AppServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Main class of the Quarkus server */
@QuarkusMain
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String... args) {
    Quarkus.run(ExampleApp.class, args);
  }

  /** Main class of the server application. */
  public static class ExampleApp implements QuarkusApplication {

    /** The server application */
    @Inject AppServer appServer;

    /** The client application */
    @Inject AppClient appClient;

    /** {@inheritDoc} */
    @Override
    public int run(String... args) {

      logger.info("Server app init...");
      appServer.init();

      logger.info("Client app init...");
      appClient.init();

      logger.info("Launch client scenario...");
      Boolean isSuccessful = appClient.launchScenario();

      logger.info("Is scenario successful ? {}", isSuccessful);
      return 0;
    }
  }
}

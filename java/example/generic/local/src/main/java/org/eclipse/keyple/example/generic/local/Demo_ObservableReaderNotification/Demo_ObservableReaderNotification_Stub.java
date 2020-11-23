/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.example.generic.local.Demo_ObservableReaderNotification;

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.example.generic.local.common.StubSmartCard1;
import org.eclipse.keyple.example.generic.local.common.StubSmartCard2;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSmartCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_ObservableReaderNotification_Stub {
  private static final Logger logger =
      LoggerFactory.getLogger(Demo_ObservableReaderNotification_Stub.class);

  public static void main(String[] args) throws Exception {

    // Set Stub plugin
    SmartCardService smartCardService = SmartCardService.getInstance();

    final String STUB_PLUGIN_NAME = "stub1";
    final String READER1_NAME = "Reader1";
    final String READER2_NAME = "Reader2";

    /* Create a Exception Handler for plugin and reader observation */
    ExceptionHandlerImpl exceptionHandler = new ExceptionHandlerImpl();

    // Register Stub plugin in the platform
    Plugin plugin =
        smartCardService.registerPlugin(
            new StubPluginFactory(STUB_PLUGIN_NAME, exceptionHandler, exceptionHandler));

    // Set observers
    logger.info("Set plugin observer.");
    /* start detection for all already present readers */
    for (Reader reader : plugin.getReaders().values()) {
      ((ObservableReader) reader).startCardDetection(ObservableReader.PollingMode.REPEATING);
    }

    logger.info("Add observer PLUGINNAME = {}", plugin.getName());
    ((ObservablePlugin) plugin).addObserver(new PluginObserver());
    logger.info("Wait a little to see the \"no reader available message\".");
    Thread.sleep(200);

    logger.info("Plug reader 1.");
    ((StubPlugin) plugin).plugStubReader(READER1_NAME, true);

    Thread.sleep(100);

    logger.info("Plug reader 2.");
    ((StubPlugin) plugin).plugStubReader(READER2_NAME, true);

    Thread.sleep(1000);

    StubReader reader1 = (StubReader) (plugin.getReader(READER1_NAME));

    StubReader reader2 = (StubReader) (plugin.getReader(READER2_NAME));

    // Create 'virtual' Hoplink and SAM card
    StubSmartCard se1 = new StubSmartCard1();
    StubSmartCard se2 = new StubSmartCard2();

    logger.info("Insert card into reader 1.");
    reader1.insertCard(se1);

    Thread.sleep(100);

    logger.info("Insert card into reader 2.");
    reader2.insertCard(se2);

    Thread.sleep(100);

    logger.info("Remove card from reader 1.");
    reader1.removeCard();

    Thread.sleep(100);

    logger.info("Remove card from reader 2.");
    reader2.removeCard();

    Thread.sleep(100);

    logger.info("Plug reader 1 again (twice).");
    ((StubPlugin) plugin).plugStubReader(READER1_NAME, true);

    logger.info("Unplug reader 1.");
    ((StubPlugin) plugin).unplugStubReader(READER1_NAME, true);

    Thread.sleep(100);

    logger.info("Plug reader 1 again.");
    ((StubPlugin) plugin).plugStubReader(READER1_NAME, true);

    Thread.sleep(100);

    logger.info("Unplug reader 1.");
    ((StubPlugin) plugin).unplugStubReader(READER1_NAME, true);

    Thread.sleep(100);

    logger.info("Unplug reader 2.");
    ((StubPlugin) plugin).unplugStubReader(READER2_NAME, true);

    logger.info("END.");
    // unregister plugin
    smartCardService.unregisterPlugin(plugin.getName());

    logger.info("Exit program.");

    System.exit(0);
  }

  private static class ExceptionHandlerImpl
      implements PluginObservationExceptionHandler, ReaderObservationExceptionHandler {
    final Logger logger = LoggerFactory.getLogger(ExceptionHandlerImpl.class);

    @Override
    public void onPluginObservationError(String pluginName, Throwable throwable) {
      logger.error("An unexpected plugin error occurred: {}", pluginName, throwable);
    }

    @Override
    public void onReaderObservationError(
        String pluginName, String readerName, Throwable throwable) {
      logger.error("An unexpected reader error occurred: {}:{}", pluginName, readerName, throwable);
    }
  }
}

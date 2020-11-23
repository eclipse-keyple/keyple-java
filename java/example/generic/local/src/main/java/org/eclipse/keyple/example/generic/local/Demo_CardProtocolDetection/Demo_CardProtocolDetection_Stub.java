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
package org.eclipse.keyple.example.generic.local.Demo_CardProtocolDetection;

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeyplePluginInstantiationException;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.generic.local.common.StubMifareClassic;
import org.eclipse.keyple.example.generic.local.common.StubMifareDesfire;
import org.eclipse.keyple.example.generic.local.common.StubMifareUL;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSupportedProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This code demonstrates the multi-protocols capability of the Keyple SmartCardService
 *
 * <ul>
 *   <li>instantiates a PC/SC plugin for a reader which name matches the regular expression provided
 *       by poReaderName.
 *   <li>uses the observable mechanism to handle card insertion/detection
 *   <li>expects card with various protocols (technologies)
 *   <li>shows the identified protocol when a card is detected
 *   <li>executes a simple Hoplink reading when a Hoplink card is identified
 * </ul>
 */
public class Demo_CardProtocolDetection_Stub {

  private static final Logger logger =
      LoggerFactory.getLogger(Demo_CardProtocolDetection_Stub.class);

  /**
   * Application entry
   *
   * @param args the program arguments
   * @throws IllegalArgumentException in case of a bad argument
   * @throws InterruptedException if thread error occurs
   */
  public static void main(String[] args)
      throws InterruptedException, KeyplePluginNotFoundException,
          KeyplePluginInstantiationException {
    // get the SmartCardService instance
    SmartCardService smartCardService = SmartCardService.getInstance();

    final String STUB_PLUGIN_NAME = "stub1";
    /* Create a Exception Handler for plugin and reader observation */
    ExceptionHandlerImpl exceptionHandler = new ExceptionHandlerImpl();

    // Register Stub plugin in the platform
    Plugin plugin =
        smartCardService.registerPlugin(
            new StubPluginFactory(STUB_PLUGIN_NAME, exceptionHandler, exceptionHandler));

    // create an observer class to handle the card operations
    org.eclipse.keyple.core.service.event.ObservableReader.ReaderObserver observer =
        new CardReaderObserver();

    // Plug PO reader.
    ((StubPlugin) plugin).plugStubReader("poReader", true);

    Thread.sleep(200);

    StubReader poReader = null;
    poReader = (StubReader) (plugin.getReader("poReader"));

    /* Activate protocols */
    poReader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());
    poReader.activateProtocol(StubSupportedProtocols.MIFARE_CLASSIC.name(), "MIFARE_CLASSIC");
    poReader.activateProtocol(StubSupportedProtocols.MEMORY_ST25.name(), "MEMORY_ST25");

    // Set terminal as Observer of the first reader
    poReader.addObserver(observer);

    Thread.sleep(300);

    poReader.removeCard();

    Thread.sleep(100);

    poReader.insertCard(new StubMifareClassic());

    Thread.sleep(300);

    poReader.removeCard();

    Thread.sleep(100);

    // insert Mifare UltraLight
    poReader.insertCard(new StubMifareUL());

    Thread.sleep(300);

    poReader.removeCard();

    Thread.sleep(100);

    // insert Mifare Desfire
    poReader.insertCard(new StubMifareDesfire());

    Thread.sleep(300);

    poReader.removeCard();

    Thread.sleep(100);

    // unregister plugin
    smartCardService.unregisterPlugin(plugin.getName());

    logger.info("Exit program.");
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

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
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ObservableReader.ReaderObserver;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeyplePluginInstantiationException;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.generic.local.common.StubCalypsoClassic;
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
 *   <li>instantiates a stub plugin to simulates card insertion.
 *   <li>uses the observable mechanism to handle card insertion/detection
 *   <li>expects card with various protocols (technologies)
 *   <li>shows the identified protocol when a card is detected
 * </ul>
 */
public class Main_CardProtocolDetection_Stub {

  private static final Logger logger =
      LoggerFactory.getLogger(Main_CardProtocolDetection_Stub.class);

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
            new StubPluginFactory(STUB_PLUGIN_NAME, null, exceptionHandler));

    // Plug PO reader.
    ((StubPlugin) plugin).plugReader("poReader", true);

    Thread.sleep(200);

    StubReader poReader = (StubReader) (plugin.getReader("poReader"));

    /* Activate protocols */
    poReader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());
    poReader.activateProtocol(StubSupportedProtocols.MIFARE_CLASSIC.name(), "MIFARE_CLASSIC");
    poReader.activateProtocol(StubSupportedProtocols.MEMORY_ST25.name(), "MEMORY_ST25");

    // create an observer class to handle the card operations
    ReaderObserver observer = new CardReaderObserver();

    // Set Default selection
    ((ObservableReader) poReader)
        .setDefaultSelectionRequest(
            CardSelectionConfig.getDefaultSelection().getDefaultSelectionsRequest(),
            ObservableReader.NotificationMode.ALWAYS,
            ObservableReader.PollingMode.REPEATING);

    // Set terminal as Observer of the first reader
    poReader.addObserver(observer);

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

    // insert Mifare Desfire
    poReader.insertCard(new StubCalypsoClassic());

    poReader.removeCard();

    Thread.sleep(100);

    // unregister plugin
    smartCardService.unregisterPlugin(plugin.getName());

    logger.info("Exit program.");

    System.exit(0);
  }

  private static class ExceptionHandlerImpl implements ReaderObservationExceptionHandler {
    @Override
    public void onReaderObservationError(
        String pluginName, String readerName, Throwable throwable) {
      logger.error("An unexpected reader error occurred: {}:{}", pluginName, readerName, throwable);
    }
  }
}

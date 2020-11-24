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
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main_ObservableReaderNotification_Pcsc {
  private static final Logger logger =
      LoggerFactory.getLogger(Main_ObservableReaderNotification_Pcsc.class);

  public static void main(String[] args) throws Exception {

    // Get the instance of the SmartCardService (Singleton pattern)
    final SmartCardService smartCardService = SmartCardService.getInstance();

    /* Create a Exception Handler for plugin and reader observation */
    ExceptionHandlerImpl exceptionHandlerImpl = new ExceptionHandlerImpl();

    // Assign PcscPlugin to the SmartCardService
    final Plugin plugin =
        smartCardService.registerPlugin(
            new PcscPluginFactory(exceptionHandlerImpl, exceptionHandlerImpl));

    /*
     * We add an observer to each plugin (only one in this example) the readers observers will
     * be added dynamically upon plugin notification (see SpecificPluginObserver.update)
     */

    /* start detection for all already present readers */
    for (Reader reader : plugin.getReaders().values()) {
      ((ObservableReader) reader).startCardDetection(ObservableReader.PollingMode.REPEATING);
    }

    logger.info("Add observer PLUGINNAME = {}", plugin.getName());
    ((ObservablePlugin) plugin).addObserver(new PluginObserver());

    logger.info("Wait for reader or card insertion/removal");

    // Wait indefinitely. CTRL-C to exit.
    synchronized (waitForEnd) {
      waitForEnd.wait();
    }

    // unregister plugin
    smartCardService.unregisterPlugin(plugin.getName());

    logger.info("Exit program.");
  }

  /**
   * This object is used to freeze the main thread while card operations are handle through the
   * observers callbacks. A call to the notify() method would end the program (not demonstrated
   * here).
   */
  private static final Object waitForEnd = new Object();

  private static class ExceptionHandlerImpl
      implements PluginObservationExceptionHandler, ReaderObservationExceptionHandler {
    final Logger logger = LoggerFactory.getLogger(ExceptionHandlerImpl.class);

    @Override
    public void onPluginObservationError(String pluginName, Throwable throwable) {
      logger.error("An unexpected plugin error occurred: {}", pluginName, throwable);
      synchronized (waitForEnd) {
        waitForEnd.notifyAll();
      }
    }

    @Override
    public void onReaderObservationError(
        String pluginName, String readerName, Throwable throwable) {
      logger.error("An unexpected reader error occurred: {}:{}", pluginName, readerName, throwable);
      synchronized (waitForEnd) {
        waitForEnd.notifyAll();
      }
    }
  }
}

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
package org.eclipse.keyple.example.generic.pc.Demo_ObservableReaderNotification;

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_ObservableReaderNotification_Pcsc {
  private static final Logger logger =
      LoggerFactory.getLogger(Demo_ObservableReaderNotification_Pcsc.class);
  public static final Object waitBeforeEnd = new Object();

  public static void main(String[] args) throws Exception {

    // Get the instance of the SmartCardService (Singleton pattern)
    final SmartCardService smartCardService = SmartCardService.getInstance();
    final Plugin plugin;

    ReaderObserver readerObserver = new ReaderObserver();
    PluginObserver pluginObserver = new PluginObserver(readerObserver);

    // Assign PcscPlugin to the SmartCardService
    plugin = smartCardService.registerPlugin(new PcscPluginFactory(pluginObserver, readerObserver));

    /*
     * We add an observer to each plugin (only one in this example) the readers observers will
     * be added dynamically upon plugin notification (see SpecificPluginObserver.update)
     */

    /* start detection for all already present readers */
    for (Reader reader : plugin.getReaders().values()) {
      ((ObservableReader) reader).startCardDetection(ObservableReader.PollingMode.REPEATING);
    }

    logger.info("Add observer PLUGINNAME = {}", plugin.getName());
    ((ObservablePlugin) plugin).addObserver(pluginObserver);

    logger.info("Wait for reader or card insertion/removal");

    // Wait indefinitely. CTRL-C to exit.
    synchronized (waitBeforeEnd) {
      waitBeforeEnd.wait();
    }

    // unregister plugin
    smartCardService.unregisterPlugin(plugin.getName());

    logger.info("Exit program.");
  }
}

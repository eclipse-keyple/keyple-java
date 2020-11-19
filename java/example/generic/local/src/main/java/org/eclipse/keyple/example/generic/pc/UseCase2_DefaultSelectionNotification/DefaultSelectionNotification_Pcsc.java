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
package org.eclipse.keyple.example.generic.pc.UseCase2_DefaultSelectionNotification;

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.example.generic.pc.common.PcscReaderUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>Use Case ‘generic 2’ – Default Selection Notification (PC/SC)</h1>
 *
 * <ul>
 *   <li>
 *       <h2>Scenario:</h2>
 *       <ul>
 *         <li>Define a default selection of ISO 14443-4 (here a Calypso PO) and set it to an
 *             observable reader, on card detection in case the selection is successful, notify the
 *             terminal application with the card information.
 *         <li><code>
 * Default Selection Notification
 * </code> means that the card processing is automatically started when detected.
 *         <li>PO messages:
 *             <ul>
 *               <li>A single card message handled at Reader level
 *             </ul>
 *       </ul>
 * </ul>
 */
public class DefaultSelectionNotification_Pcsc {
  private static final Logger logger =
      LoggerFactory.getLogger(DefaultSelectionNotification_Pcsc.class);
  static String cardAid = "A0000004040125090101";
  /**
   * This object is used to freeze the main thread while card operations are handle through the
   * observers callbacks. A call to the notify() method would end the program (not demonstrated
   * here).
   */
  private static final Object waitForEnd = new Object();

  class ExceptionHandlerImpl implements ReaderObservationExceptionHandler {

    @Override
    public void onReaderObservationError(String pluginName, String readerName, Throwable e) {
      logger.error("An unexpected error occurred: {}:{}", pluginName, readerName, e);
      synchronized (waitForEnd) {
        waitForEnd.notifyAll();
      }
    }
  }

  public DefaultSelectionNotification_Pcsc() throws InterruptedException {
    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    ExceptionHandlerImpl errorHandler = new ExceptionHandlerImpl();

    // Register the PcscPlugin with SmartCardService, get the corresponding generic Plugin in
    // return
    Plugin plugin = smartCardService.registerPlugin(new PcscPluginFactory(null, errorHandler));

    // Get and configure the PO reader
    Reader reader = plugin.getReader(PcscReaderUtilities.getContactlessReaderName());
    ((PcscReader) reader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    logger.info(
        "=============== UseCase Generic #2: AID based default selection ===================");
    logger.info("= Card reader  NAME = {}", reader.getName());

    // Provide the Reader with the selection operation to be processed when a card is inserted.
    ((ObservableReader) reader)
        .setDefaultSelectionRequest(
            ReaderConfig.getCardSelection().getSelectionOperation(),
            ObservableReader.NotificationMode.MATCHED_ONLY,
            ObservableReader.PollingMode.REPEATING);

    // Set the current class as Observer of the first reader
    ((ObservableReader) reader).addObserver(ReaderConfig.getObserver());

    logger.info(
        "= #### Wait for a card. The default AID based selection to be processed as soon as the card is detected.");

    // Wait for ever (exit with CTRL-C)
    synchronized (waitForEnd) {
      waitForEnd.wait();
    }
  }

  /** main program entry */
  public static void main(String[] args) throws InterruptedException, KeypleException {
    // Create the observable object to handle the card processing
    new DefaultSelectionNotification_Pcsc();
  }
}

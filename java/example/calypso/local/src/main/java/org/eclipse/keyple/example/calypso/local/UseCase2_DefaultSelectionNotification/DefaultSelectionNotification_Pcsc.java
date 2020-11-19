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
package org.eclipse.keyple.example.calypso.local.UseCase2_DefaultSelectionNotification;

import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.example.calypso.local.common.PcscReaderUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>Use Case ‘Calypso 2’ – Default Selection Notification (PC/SC)</h1>
 *
 * <ul>
 *   <li>
 *       <h2>Scenario:</h2>
 *       <ul>
 *         <li>Define a default selection of ISO 14443-4 Calypso PO and set it to an observable
 *             reader, on card detection in case the Calypso selection is successful, notify the
 *             terminal application with the PO information, then the terminal follows by operating
 *             a simple Calypso PO transaction.
 *         <li><code>
 * Default Selection Notification
 * </code> means that the card processing is automatically started when detected.
 *         <li>PO messages:
 *             <ul>
 *               <li>A first card message to notify about the selected Calypso PO
 *               <li>A second card message to operate the simple Calypso transaction
 *             </ul>
 *       </ul>
 * </ul>
 */
public class DefaultSelectionNotification_Pcsc {
  private static final Logger logger =
      LoggerFactory.getLogger(DefaultSelectionNotification_Pcsc.class);

  // This object is used to freeze the main thread while card operations are handle through the
  // observers callbacks. A call to the notify() method would end the program (not demonstrated
  // here).
  private static final Object waitForEnd = new Object();

  static class ExceptionHandlerImpl implements ReaderObservationExceptionHandler {
    final Logger logger = LoggerFactory.getLogger(ExceptionHandlerImpl.class);

    @Override
    public void onReaderObservationError(
        String pluginName, String readerName, Throwable throwable) {
      logger.error("An unexpected reader error occurred: {}:{}", pluginName, readerName, throwable);
      synchronized (waitForEnd) {
        waitForEnd.notify();
      }
    }
  }

  public DefaultSelectionNotification_Pcsc() throws InterruptedException {
    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    ExceptionHandlerImpl exceptionHandler = new ExceptionHandlerImpl();

    // Register the PcscPlugin with SmartCardService, get the corresponding generic Plugin in
    // return
    Plugin plugin = smartCardService.registerPlugin(new PcscPluginFactory(null, exceptionHandler));

    // Get and configure the PO reader
    PcscReader poReader =
        (PcscReader) plugin.getReader(PcscReaderUtilities.getContactlessReaderName());
    poReader.setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    logger.info(
        "=============== UseCase Calypso #2: AID based default selection ===================");
    logger.info("= PO Reader  NAME = {}", poReader.getName());

    CardSelection cardSelection = ReaderConfiguration.getCardSelection();

    // Provide the Reader with the selection operation to be processed when a PO is inserted.
    poReader.setDefaultSelectionRequest(
        cardSelection.getSelectionOperation(),
        ObservableReader.NotificationMode.MATCHED_ONLY,
        ObservableReader.PollingMode.REPEATING);

    // Set the current class as Observer of the first reader
    poReader.addObserver(ReaderConfiguration.getObserver());

    logger.info(
        "= #### Wait for a PO. The default AID based selection with reading of Environment");
    logger.info("= #### file is ready to be processed as soon as the PO is detected.");

    // Wait for ever (exit with CTRL-C)
    synchronized (waitForEnd) {
      waitForEnd.wait();
    }
  }

  /** main program entry */
  public static void main(String[] args) throws InterruptedException {
    // Create the observable object to handle the PO processing
    new DefaultSelectionNotification_Pcsc();
  }
}

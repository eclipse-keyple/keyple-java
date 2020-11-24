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
package org.eclipse.keyple.example.calypso.UseCase2_DefaultSelectionNotification;

import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.example.calypso.common.StubCalypsoClassic;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSmartCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>Use Case ‘Calypso 2’ – Default Selection Notification (Stub)</h1>
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
public class Main_DefaultSelectionNotification_Stub {
  private static final Logger logger =
      LoggerFactory.getLogger(Main_DefaultSelectionNotification_Stub.class);

  /** main program entry */
  public static void main(String[] args) throws InterruptedException {
    /* Get the instance of the SmartCardService (Singleton pattern) */
    SmartCardService smartCardService = SmartCardService.getInstance();

    final String STUB_PLUGIN_NAME = "stub1";

    /* Register Stub plugin in the platform */
    Plugin stubPlugin =
        smartCardService.registerPlugin(
            new StubPluginFactory(STUB_PLUGIN_NAME, null, new ExceptionHandlerImpl()));

    /* Plug the PO stub reader. */
    ((StubPlugin) stubPlugin).plugStubReader("poReader", true);

    /*
     * Get a PO reader ready to work with Calypso PO.
     */
    Reader poReader = stubPlugin.getReader("poReader");

    /* Check if the reader exists */
    if (poReader == null) {
      throw new IllegalStateException("Bad PO reader setup");
    }

    logger.info(
        "=============== UseCase Calypso #2: AID based default selection ===================");
    logger.info("= PO Reader  NAME = {}", poReader.getName());

    /*
     * Prepare a Calypso PO selection
     */
    CardSelection cardSelection = CardSelectionConfig.getCardSelection();
    /*
     * Provide the Reader with the selection operation to be processed when a PO is inserted.
     */
    ((ObservableReader) poReader)
        .setDefaultSelectionRequest(
            cardSelection.getSelectionOperation(),
            ObservableReader.NotificationMode.MATCHED_ONLY,
            ObservableReader.PollingMode.REPEATING);

    /* Set a CardSelectionConfig that contains the ticketing logic for the reader */
    ((ObservableReader) poReader).addObserver(new CardReaderObserver());

    logger.info(
        "= #### Wait for a PO. The default AID based selection with reading of Environment");
    logger.info("= #### file is ready to be processed as soon as the PO is detected.");

    /* Create 'virtual' Calypso PO */
    StubSmartCard calypsoStubCard = new StubCalypsoClassic();

    /* Wait a while. */
    Thread.sleep(100);

    logger.info("Insert stub PO.");
    ((StubReader) poReader).insertCard(calypsoStubCard);

    /* Wait a while. */
    Thread.sleep(1000);

    logger.info("Remove stub PO.");
    ((StubReader) poReader).removeCard();

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

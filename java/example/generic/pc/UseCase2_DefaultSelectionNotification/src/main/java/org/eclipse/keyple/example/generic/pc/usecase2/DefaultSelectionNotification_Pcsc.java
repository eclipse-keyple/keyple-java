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
package org.eclipse.keyple.example.generic.pc.usecase2;

import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.CardSelector;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SmartCardService;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader.ReaderObserver;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.reader.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.example.common.generic.GenericSeSelectionRequest;
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
public class DefaultSelectionNotification_Pcsc implements ReaderObserver {
  private static final Logger logger =
      LoggerFactory.getLogger(DefaultSelectionNotification_Pcsc.class);
  private String seAid = "A0000004040125090101";
  private SeSelection seSelection;
  /**
   * This object is used to freeze the main thread while card operations are handle through the
   * observers callbacks. A call to the notify() method would end the program (not demonstrated
   * here).
   */
  private static final Object waitForEnd = new Object();

  public DefaultSelectionNotification_Pcsc() throws InterruptedException {
    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    // Register the PcscPlugin with SmartCardService, get the corresponding generic ReaderPlugin in
    // return
    ReaderPlugin readerPlugin = smartCardService.registerPlugin(new PcscPluginFactory());

    // Get and configure the PO reader
    Reader reader = readerPlugin.getReader(ReaderUtilities.getContactlessReaderName());
    ((PcscReader) reader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    logger.info(
        "=============== UseCase Generic #2: AID based default selection ===================");
    logger.info("= Card reader  NAME = {}", reader.getName());

    // Prepare a card selection
    seSelection = new SeSelection();

    // Setting of an AID based selection
    //
    // Select the first application matching the selection AID whatever the card communication
    // protocol keep the logical channel open after the selection

    // Generic selection: configures a CardSelector with all the desired attributes to make the
    // selection
    GenericSeSelectionRequest cardSelector =
        new GenericSeSelectionRequest(
            CardSelector.builder()
                .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(seAid).build())
                .build());

    // Add the selection case to the current selection (we could have added other cases here)
    seSelection.prepareSelection(cardSelector);

    // Provide the Reader with the selection operation to be processed when a card is inserted.
    ((ObservableReader) reader)
        .setDefaultSelectionRequest(
            seSelection.getSelectionOperation(),
            ObservableReader.NotificationMode.MATCHED_ONLY,
            ObservableReader.PollingMode.REPEATING);

    // Set the current class as Observer of the first reader
    ((ObservableReader) reader).addObserver(this);

    logger.info(
        "= #### Wait for a card. The default AID based selection to be processed as soon as the card is detected.");

    // Wait for ever (exit with CTRL-C)
    synchronized (waitForEnd) {
      waitForEnd.wait();
    }
  }

  /**
   * Method invoked in the case of a reader event
   *
   * @param event the reader event
   */
  @Override
  public void update(ReaderEvent event) {
    switch (event.getEventType()) {
      case SE_MATCHED:
        // the selection has one target, get the result at index 0
        AbstractMatchingSe selectedSe = null;
        try {
          selectedSe =
              seSelection
                  .processDefaultSelection(event.getDefaultSelectionsResponse())
                  .getActiveMatchingSe();
        } catch (KeypleException e) {
          logger.error("Exception: {}", e.getMessage());
          try {
            ((ObservableReader) (event.getReader())).finalizeSeProcessing();
          } catch (KeypleReaderNotFoundException ex) {
            logger.error("Reader not found exception: {}", ex.getMessage());
          } catch (KeyplePluginNotFoundException ex) {
            logger.error("Plugin not found exception: {}", ex.getMessage());
          }
        }

        if (selectedSe != null) {
          logger.info("Observer notification: the selection of the card has succeeded.");

          logger.info("= #### End of the card processing.");
        } else {
          logger.error(
              "The selection of the card has failed. Should not have occurred due to the MATCHED_ONLY selection mode.");
        }
        break;
      case SE_INSERTED:
        logger.error(
            "SE_INSERTED event: should not have occurred due to the MATCHED_ONLY selection mode.");
        break;
      case SE_REMOVED:
        logger.info("There is no PO inserted anymore. Return to the waiting state...");
        break;
      default:
        break;
    }
    if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED
        || event.getEventType() == ReaderEvent.EventType.SE_MATCHED) {
      // Informs the underlying layer of the end of the card processing, in order to manage the
      // removal sequence.
      try {
        ((ObservableReader) (event.getReader())).finalizeSeProcessing();
      } catch (KeypleReaderNotFoundException e) {
        logger.error("Reader not found exception: {}", e.getMessage());
      } catch (KeyplePluginNotFoundException e) {
        logger.error("Plugin not found exception: {}", e.getMessage());
      }
    }
  }

  /** main program entry */
  public static void main(String[] args) throws InterruptedException, KeypleException {
    // Create the observable object to handle the card processing
    new DefaultSelectionNotification_Pcsc();
  }
}

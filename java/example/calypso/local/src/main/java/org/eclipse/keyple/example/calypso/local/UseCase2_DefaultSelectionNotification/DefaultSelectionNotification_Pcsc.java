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

import static org.eclipse.keyple.calypso.transaction.PoSelector.*;

import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.ElementaryFile;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionException;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ObservableReader.ReaderObserver;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.local.common.CalypsoClassicInfo;
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
public class DefaultSelectionNotification_Pcsc implements ReaderObserver {
  private static final Logger logger =
      LoggerFactory.getLogger(DefaultSelectionNotification_Pcsc.class);
  private final CardSelection cardSelection;

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
    Reader poReader = plugin.getReader(PcscReaderUtilities.getContactlessReaderName());
    ((PcscReader) poReader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    logger.info(
        "=============== UseCase Calypso #2: AID based default selection ===================");
    logger.info("= PO Reader  NAME = {}", poReader.getName());

    // Prepare a Calypso PO selection
    cardSelection = new CardSelection();

    // Setting of an AID based selection of a Calypso REV3 PO
    // // Select the first application matching the selection AID whatever the card communication
    // protocol keep the logical channel open after the selection

    // Calypso selection: configures a PoSelectionRequest with all the desired attributes to
    // make the selection and read additional information afterwards
    PoSelectionRequest poSelectionRequest =
        new PoSelectionRequest(
            PoSelector.builder()
                .aidSelector(AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                .invalidatedPo(InvalidatedPo.REJECT)
                .build());

    // Prepare the reading.
    poSelectionRequest.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Add the selection case to the current selection (we could have added other cases here)
    cardSelection.prepareSelection(poSelectionRequest);

    // Provide the Reader with the selection operation to be processed when a PO is inserted.
    ((ObservableReader) poReader)
        .setDefaultSelectionRequest(
            cardSelection.getSelectionOperation(),
            ObservableReader.NotificationMode.MATCHED_ONLY,
            ObservableReader.PollingMode.REPEATING);

    // Set the current class as Observer of the first reader
    ((ObservableReader) poReader).addObserver(this);

    logger.info(
        "= #### Wait for a PO. The default AID based selection with reading of Environment");
    logger.info("= #### file is ready to be processed as soon as the PO is detected.");

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
      case CARD_MATCHED:
        boolean transactionComplete = false;
        CalypsoPo calypsoPo = null;
        Reader poReader = null;
        try {
          calypsoPo =
              (CalypsoPo)
                  cardSelection
                      .processDefaultSelection(event.getDefaultSelectionsResponse())
                      .getActiveSmartCard();

          poReader =
              SmartCardService.getInstance()
                  .getPlugin(event.getPluginName())
                  .getReader(event.getReaderName());
        } catch (KeypleReaderNotFoundException e) {
          logger.error("Reader not found! {}", e.getMessage());
        } catch (KeyplePluginNotFoundException e) {
          logger.error("Plugin not found! {}", e.getMessage());
        } catch (KeypleException e) {
          logger.error("The selection process failed! {}", e.getMessage());
        }

        logger.info("Observer notification: the selection of the PO has succeeded.");

        // Retrieve the data read from the CalyspoPo updated during the transaction process
        ElementaryFile efEnvironmentAndHolder =
            calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EnvironmentAndHolder);
        String environmentAndHolder =
            ByteArrayUtil.toHex(efEnvironmentAndHolder.getData().getContent());

        // Log the result
        logger.info("EnvironmentAndHolder file data: {}", environmentAndHolder);

        // Go on with the reading of the first record of the EventLog file
        logger.info("= #### 2nd PO exchange: reading transaction of the EventLog file.");

        PoTransaction poTransaction =
            new PoTransaction(new CardResource<CalypsoPo>(poReader, calypsoPo));

        // Prepare the reading order and keep the associated parser for later use once the
        // transaction has been processed.
        poTransaction.prepareReadRecordFile(
            CalypsoClassicInfo.SFI_EventLog, CalypsoClassicInfo.RECORD_NUMBER_1);

        // Actual PO communication: send the prepared read order, then close the channel
        // with the PO
        try {
          poTransaction.prepareReleasePoChannel();
          poTransaction.processPoCommands();

          logger.info("The reading of the EventLog has succeeded.");

          // Retrieve the data read from the CalyspoPo updated during the transaction
          // process
          ElementaryFile efEventLog = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EventLog);
          byte[] eventLog = efEventLog.getData().getContent();

          // Log the result
          logger.info("EventLog file data: {}", ByteArrayUtil.toHex(eventLog));

          transactionComplete = true;
        } catch (CalypsoPoTransactionException e) {
          logger.error("CalypsoPoTransactionException: {}", e.getMessage());
        } catch (CalypsoPoCommandException e) {
          logger.error(
              "PO command {} failed with the status code 0x{}. {}",
              e.getCommand(),
              Integer.toHexString(e.getStatusCode() & 0xFFFF).toUpperCase(),
              e.getMessage());
        }
        if (!transactionComplete) {
          // Informs the underlying layer of the end of the card processing, in order to
          // manage the
          // removal sequence.
          try {
            ((ObservableReader) (event.getReader())).finalizeCardProcessing();
          } catch (KeypleReaderNotFoundException e) {
            logger.error("Reader not found! {}", e.getMessage());
          } catch (KeyplePluginNotFoundException e) {
            logger.error("Plugin not found! {}", e.getMessage());
          }
        }
        logger.info("= #### End of the Calypso PO processing.");
        break;
      case CARD_INSERTED:
        logger.error(
            "CARD_INSERTED event: should not have occurred due to the MATCHED_ONLY selection mode.");
        break;
      case CARD_REMOVED:
        logger.info("There is no PO inserted anymore. Return to the waiting state...");
        break;
      default:
        break;
    }
  }

  /** main program entry */
  public static void main(String[] args) throws InterruptedException {
    // Create the observable object to handle the PO processing
    new DefaultSelectionNotification_Pcsc();
  }
}

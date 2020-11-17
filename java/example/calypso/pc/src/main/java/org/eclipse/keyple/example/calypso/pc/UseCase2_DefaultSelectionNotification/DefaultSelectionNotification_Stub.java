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
package org.eclipse.keyple.example.calypso.pc.UseCase2_DefaultSelectionNotification;

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
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.pc.common.CalypsoClassicInfo;
import org.eclipse.keyple.example.calypso.pc.common.StubCalypsoClassic;
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
public class DefaultSelectionNotification_Stub implements ReaderObserver {
  private static final Logger logger =
      LoggerFactory.getLogger(DefaultSelectionNotification_Stub.class);
  private final CardSelection cardSelection;

  public DefaultSelectionNotification_Stub() throws InterruptedException {

    /* Get the instance of the SmartCardService (Singleton pattern) */
    SmartCardService smartCardService = SmartCardService.getInstance();

    final String STUB_PLUGIN_NAME = "stub1";

    /* Register Stub plugin in the platform */
    Plugin stubPlugin =
        smartCardService.registerPlugin(new StubPluginFactory(STUB_PLUGIN_NAME, null, null));

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
    cardSelection = new CardSelection();

    /*
     * Setting of an AID based selection of a Calypso REV3 PO
     *
     * Select the first application matching the selection AID whatever the card communication
     * protocol keep the logical channel open after the selection
     */

    /*
     * Calypso selection: configures a PoSelectionRequest with all the desired attributes to
     * make the selection and read additional information afterwards
     */
    PoSelectionRequest poSelectionRequest =
        new PoSelectionRequest(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                .invalidatedPo(InvalidatedPo.REJECT)
                .build());

    /*
     * Prepare the reading order.
     */
    poSelectionRequest.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

    /*
     * Add the selection case to the current selection (we could have added other cases here)
     */
    cardSelection.prepareSelection(poSelectionRequest);

    /*
     * Provide the Reader with the selection operation to be processed when a PO is inserted.
     */
    ((ObservableReader) poReader)
        .setDefaultSelectionRequest(
            cardSelection.getSelectionOperation(),
            ObservableReader.NotificationMode.MATCHED_ONLY,
            ObservableReader.PollingMode.REPEATING);

    /* Set the current class as Observer of the first reader */
    ((ObservableReader) poReader).addObserver(this);

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
        } catch (KeyplePluginNotFoundException e) {
          logger.error("Plugin not found! {}", e.getMessage());
        } catch (KeypleReaderNotFoundException e) {
          logger.error("Reader not found! {}", e.getMessage());
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
          String eventLog = ByteArrayUtil.toHex(efEventLog.getData().getContent());

          // Log the result
          logger.info("EventLog file data: {}", eventLog);

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
    new DefaultSelectionNotification_Stub();
  }
}

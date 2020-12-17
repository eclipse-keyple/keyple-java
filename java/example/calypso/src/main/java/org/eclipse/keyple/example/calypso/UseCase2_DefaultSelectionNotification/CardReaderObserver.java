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
package org.eclipse.keyple.example.calypso.UseCase2_DefaultSelectionNotification;

import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.ElementaryFile;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionException;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.common.CalypsoClassicInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CardReaderObserver implements ObservableReader.ReaderObserver {
  private final Logger logger = LoggerFactory.getLogger(CardReaderObserver.class);

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
        CardSelectionsService cardSelectionsService = CardSelectionConfig.getCardSelection();

        calypsoPo =
            (CalypsoPo)
                cardSelectionsService
                    .processDefaultSelectionsResponse(event.getDefaultSelectionsResponse())
                    .getActiveSmartCard();

        poReader =
            SmartCardService.getInstance()
                .getPlugin(event.getPluginName())
                .getReader(event.getReaderName());

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

          ((ObservableReader) (event.getReader())).finalizeCardProcessing();
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
}

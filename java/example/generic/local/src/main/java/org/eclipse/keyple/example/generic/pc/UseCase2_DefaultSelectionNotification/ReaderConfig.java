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
package org.eclipse.keyple.example.generic.pc.UseCase2_DefaultSelectionNotification;

import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.generic.pc.common.GenericCardSelectionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReaderConfig {

  private static CardSelection cardSelection;

  private static final Logger logger = LoggerFactory.getLogger(ReaderConfig.class);

  static CardSelection getDefaultSelection() {
    if (cardSelection != null) {
      return cardSelection;
    }
    // Prepare a card selection
    cardSelection = new CardSelection();

    // Setting of an AID based selection
    //
    // Select the first application matching the selection AID whatever the card communication
    // protocol keep the logical channel open after the selection

    // Generic selection: configures a CardSelector with all the desired attributes to make the
    // selection
    GenericCardSelectionRequest cardSelector =
        new GenericCardSelectionRequest(
            CardSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(
                    CardSelector.AidSelector.builder()
                        .aidToSelect(DefaultSelectionNotification_Pcsc.cardAid)
                        .build())
                .build());

    // Add the selection case to the current selection (we could have added other cases here)
    cardSelection.prepareSelection(cardSelector);

    return cardSelection;
  }

  static ObservableReader.ReaderObserver getObserver() {
    return new ObservableReader.ReaderObserver() {
      /**
       * Method invoked in the case of a reader event
       *
       * @param event the reader event
       */
      @Override
      public void update(ReaderEvent event) {
        switch (event.getEventType()) {
          case CARD_MATCHED:
            // the selection has one target, get the result at index 0
            AbstractSmartCard selectedCard = null;
            try {
              selectedCard =
                  getDefaultSelection()
                      .processDefaultSelection(event.getDefaultSelectionsResponse())
                      .getActiveSmartCard();
            } catch (KeypleException e) {
              logger.error("Exception: {}", e.getMessage());
              try {
                ((ObservableReader) (event.getReader())).finalizeCardProcessing();
              } catch (KeypleReaderNotFoundException ex) {
                logger.error("Reader not found exception: {}", ex.getMessage());
              } catch (KeyplePluginNotFoundException ex) {
                logger.error("Plugin not found exception: {}", ex.getMessage());
              }
            }

            if (selectedCard != null) {
              logger.info("Observer notification: the selection of the card has succeeded.");

              logger.info("= #### End of the card processing.");
            } else {
              logger.error(
                  "The selection of the card has failed. Should not have occurred due to the MATCHED_ONLY selection mode.");
            }
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
        if (event.getEventType() == ReaderEvent.EventType.CARD_INSERTED
            || event.getEventType() == ReaderEvent.EventType.CARD_MATCHED) {
          // Informs the underlying layer of the end of the card processing, in order to manage the
          // removal sequence.
          try {
            ((ObservableReader) (event.getReader())).finalizeCardProcessing();
          } catch (KeypleReaderNotFoundException e) {
            logger.error("Reader not found exception: {}", e.getMessage());
          } catch (KeyplePluginNotFoundException e) {
            logger.error("Plugin not found exception: {}", e.getMessage());
          }
        }
      }
    };
  }
}

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
package org.eclipse.keyple.example.generic.local.Demo_CardProtocolDetection;

import static org.eclipse.keyple.example.generic.local.Demo_CardProtocolDetection.CardSelectionConfig.getDefaultSelection;

import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.card.selection.CardSelectionsResult;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A reader Observer handles card event such as CARD_INSERTED, CARD_MATCHED, CARD_REMOVED */
class CardReaderObserver implements ObservableReader.ReaderObserver {

  private static final Logger logger = LoggerFactory.getLogger(CardReaderObserver.class);

  /**
   * Implementation of the {@link ObservableReader.ReaderObserver#update(ReaderEvent)} method. <br>
   * Note: in the case of CARD_MATCHED, the received event also carries the response to the default
   * selection.
   *
   * @param event the reader event, either CARD_MATCHED, CARD_INSERTED or CARD_REMOVED
   */
  public final void update(final ReaderEvent event) {
    logger.info("New card event: {} - {}", event.getReaderName(), event.getEventType().toString());
    switch (event.getEventType()) {
      case CARD_INSERTED:
        logger.trace("Unexpected card insertion event");
        break;
      case CARD_MATCHED:
        CardSelectionsService cardSelectionsService = getDefaultSelection();
        /* get the card that matches one of the two selection targets */
        CardSelectionsResult cardSelectionsResult =
            cardSelectionsService.processDefaultSelectionsResponse(
                event.getDefaultSelectionsResponse());
        if (cardSelectionsResult.hasActiveSelection()) {
          AbstractSmartCard selectedCard = cardSelectionsResult.getActiveSmartCard();
          logger.info(
              "Inserted card matched with ATR {}", ByteArrayUtil.toHex(selectedCard.getAtrBytes()));
        }
        break;
      case CARD_REMOVED:
        logger.trace("Card removal event");
        break;
      case UNREGISTERED:
        logger.trace("The reader is no more registered in the SmartCardService.");
        break;
    }
  }
}

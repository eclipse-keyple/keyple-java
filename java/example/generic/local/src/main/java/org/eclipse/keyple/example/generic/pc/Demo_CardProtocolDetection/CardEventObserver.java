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
package org.eclipse.keyple.example.generic.pc.Demo_CardProtocolDetection;

import static org.eclipse.keyple.example.generic.pc.Demo_CardProtocolDetection.CardSelectionConfig.getDefaultSelection;

import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CardEventObserver implements ObservableReader.ReaderObserver {

  private static final Logger logger = LoggerFactory.getLogger(CardEventObserver.class);

  /**
   * Implementation of the {@link ObservableReader.ReaderObserver#update(ReaderEvent)} method. <br>
   * Note: in the case of CARD_MATCHED, the received event also carries the response to the default
   * selection.
   *
   * @param event the reader event, either CARD_MATCHED, CARD_INSERTED or CARD_REMOVED
   */
  public final void update(final ReaderEvent event) {
    logger.info("New reader event: {}", event.getReaderName());
    switch (event.getEventType()) {
      case CARD_INSERTED:
        logger.warn("Unexpected card insertion event");
        break;
      case CARD_MATCHED:
        CardSelection cardSelection = getDefaultSelection();
        /* get the card that matches one of the two selection targets */
        if (cardSelection
            .processDefaultSelection(event.getDefaultSelectionsResponse())
            .hasActiveSelection()) {
          AbstractSmartCard selectedCard =
              cardSelection
                  .processDefaultSelection(event.getDefaultSelectionsResponse())
                  .getActiveSmartCard();
        }
        break;
      case CARD_REMOVED:
        logger.warn("Unexpected card removal event");
        break;
      case UNREGISTERED:
        logger.error("Unexpected error: the reader is no more registered in the SmartcardService.");
        break;
    }
  }
}

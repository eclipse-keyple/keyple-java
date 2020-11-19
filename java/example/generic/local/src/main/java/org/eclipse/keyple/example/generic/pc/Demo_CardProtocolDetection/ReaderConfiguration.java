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

import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.generic.pc.common.GenericCardSelectionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReaderConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(ReaderConfiguration.class);

  static CardSelection cardSelection;

  static CardSelection getCardSelection() {
    if (cardSelection != null) {
      return cardSelection;
    }

    cardSelection = new CardSelection();

    // process SDK defined protocols
    for (ContactlessCardCommonProtocols protocol : ContactlessCardCommonProtocols.values()) {
      switch (protocol) {
        case ISO_14443_4:
          /* Add a Hoplink selector */
          String HoplinkAID = "A000000291A000000191";
          PoSelectionRequest poSelectionRequest =
              new PoSelectionRequest(
                  PoSelector.builder()
                      .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                      .aidSelector(
                          CardSelector.AidSelector.builder().aidToSelect(HoplinkAID).build())
                      .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                      .build());

          cardSelection.prepareSelection(poSelectionRequest);
          break;
        case NFC_A_ISO_14443_3A:
        case NFC_B_ISO_14443_3B:
          // not handled in this demo code
          break;
        case INNOVATRON_B_PRIME_CARD:
          // intentionally ignored for demo purpose
          break;
        default:
          /* Add a generic selector */
          cardSelection.prepareSelection(
              new GenericCardSelectionRequest(
                  CardSelector.builder()
                      .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                      .atrFilter(new CardSelector.AtrFilter(".*"))
                      .build()));
          break;
      }
    }
    return cardSelection;
  }

  static ObservableReader.ReaderObserver getObserver() {
    return new ObservableReader.ReaderObserver() {

      /**
       * Implementation of the {@link ObservableReader.ReaderObserver#update(ReaderEvent)} method.
       * <br>
       * Note: in the case of CARD_MATCHED, the received event also carries the response to the
       * default selection.
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
            CardSelection cardSelection = getCardSelection();
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
            logger.error(
                "Unexpected error: the reader is no more registered in the SmartcardService.");
            break;
        }
      }
    };
  }
}

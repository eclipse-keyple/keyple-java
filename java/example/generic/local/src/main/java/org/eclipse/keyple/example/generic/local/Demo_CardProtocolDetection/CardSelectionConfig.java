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

import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.generic.local.common.GenericCardSelectionRequest;

/** Card Selection Configuration */
class CardSelectionConfig {

  private static CardSelection cardSelection;
  private static String HoplinkAID = "A000000291A000000191";

  /**
   * Define a default card selection configuration for multiple protocols
   *
   * @return card selection object
   */
  static CardSelection getDefaultSelection() {
    if (cardSelection != null) {
      return cardSelection;
    }

    cardSelection = new CardSelection();

    // process SDK defined protocols
    for (ContactlessCardCommonProtocols protocol : ContactlessCardCommonProtocols.values()) {
      switch (protocol) {
        case ISO_14443_4:
          /* Add a Hoplink selector */
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
}

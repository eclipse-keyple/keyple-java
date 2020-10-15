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
package org.eclipse.keyple.example.generic.pc.Demo_SeProtocolDetection;

import static org.eclipse.keyple.calypso.transaction.PoSelector.*;

import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.CardSelection;
import org.eclipse.keyple.core.seproxy.CardSelector;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.plugin.reader.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.common.generic.AbstractReaderObserverAsynchronousEngine;
import org.eclipse.keyple.example.common.generic.GenericCardSelectionRequest;

/**
 * This code demonstrates the multi-protocols capability of the Keyple SeProxy
 *
 * <ul>
 *   <li>instantiates a PC/SC plugin for a reader which name matches the regular expression provided
 *       by poReaderName.
 *   <li>uses the observable mechanism to handle card insertion/detection
 *   <li>expects card with various protocols (technologies)
 *   <li>shows the identified protocol when a card is detected
 *   <li>executes a simple Hoplink reading when a Hoplink card is identified
 * </ul>
 *
 * The program spends most of its time waiting for a Enter key before exit. The actual card
 * processing is mainly event driven through the observability.
 */
public class SeProtocolDetectionEngine extends AbstractReaderObserverAsynchronousEngine {
  private Reader reader;
  private CardSelection cardSelection;

  public SeProtocolDetectionEngine() {
    super();
  }

  /* Assign reader to the transaction engine */
  public void setReader(Reader poReader) {
    this.reader = poReader;
  }

  public AbstractDefaultSelectionsRequest prepareCardSelection() {

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
                      .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                      .aidSelector(AidSelector.builder().aidToSelect(HoplinkAID).build())
                      .invalidatedPo(InvalidatedPo.REJECT)
                      .build());

          cardSelection.prepareSelection(poSelectionRequest);
          break;
        case NFC_A_ISO_14443_3A:
        case NFC_B_ISO_14443_3B:
          // not handled in this demo code
          break;
        case CALYPSO_OLD_CARD_PRIME:
          // intentionally ignored for demo purpose
          break;
        default:
          /* Add a generic selector */
          cardSelection.prepareSelection(
              new GenericCardSelectionRequest(
                  CardSelector.builder()
                      .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                      .atrFilter(new CardSelector.AtrFilter(".*"))
                      .build()));
          break;
      }
    }
    return cardSelection.getSelectionOperation();
  }

  /**
   * This method is called when a card is inserted (or presented to the reader's antenna). It
   * executes a {@link AbstractDefaultSelectionsResponse} and processes the {@link
   * AbstractDefaultSelectionsResponse} showing the APDUs exchanges
   */
  @Override
  public void processSeMatch(AbstractDefaultSelectionsResponse defaultSelectionsResponse) {
    /* get the card that matches one of the two selection targets */
    if (cardSelection.processDefaultSelection(defaultSelectionsResponse).hasActiveSelection()) {
      AbstractMatchingSe selectedSe =
          cardSelection.processDefaultSelection(defaultSelectionsResponse).getActiveMatchingSe();
    } else {
      // TODO check this. Shouldn't an exception have been raised before?
      System.out.println("No selection matched!");
    }
  }

  @Override
  public void processSeInserted() {
    System.out.println("Unexpected card insertion event");
  }

  @Override
  public void processSeRemoved() {
    System.out.println("Card removal event");
  }

  @Override
  public void processUnexpectedSeRemoval() {
    System.out.println("Unexpected card removal event");
  }
}

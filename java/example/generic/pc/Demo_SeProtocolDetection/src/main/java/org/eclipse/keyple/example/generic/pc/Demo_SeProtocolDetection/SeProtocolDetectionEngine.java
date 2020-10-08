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
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.common.generic.AbstractReaderObserverAsynchronousEngine;
import org.eclipse.keyple.example.common.generic.GenericSeSelectionRequest;

/**
 * This code demonstrates the multi-protocols capability of the Keyple SeProxy
 *
 * <ul>
 *   <li>instantiates a PC/SC plugin for a reader which name matches the regular expression provided
 *       by poReaderName.
 *   <li>uses the observable mechanism to handle SE insertion/detection
 *   <li>expects SE with various protocols (technologies)
 *   <li>shows the identified protocol when a SE is detected
 *   <li>executes a simple Hoplink reading when a Hoplink SE is identified
 * </ul>
 *
 * The program spends most of its time waiting for a Enter key before exit. The actual SE processing
 * is mainly event driven through the observability.
 */
public class SeProtocolDetectionEngine extends AbstractReaderObserverAsynchronousEngine {
  private SeReader seReader;
  private SeSelection seSelection;

  public SeProtocolDetectionEngine() {
    super();
  }

  /* Assign reader to the transaction engine */
  public void setReader(SeReader poReader) {
    this.seReader = poReader;
  }

  public AbstractDefaultSelectionsRequest prepareSeSelection() {

    seSelection = new SeSelection();

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

          seSelection.prepareSelection(poSelectionRequest);
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
          seSelection.prepareSelection(
              new GenericSeSelectionRequest(
                  SeSelector.builder()
                      .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                      .atrFilter(new SeSelector.AtrFilter(".*"))
                      .build()));
          break;
      }
    }
    return seSelection.getSelectionOperation();
  }

  /**
   * This method is called when a SE is inserted (or presented to the reader's antenna). It executes
   * a {@link AbstractDefaultSelectionsResponse} and processes the {@link
   * AbstractDefaultSelectionsResponse} showing the APDUs exchanges
   */
  @Override
  public void processSeMatch(AbstractDefaultSelectionsResponse defaultSelectionsResponse) {
    /* get the SE that matches one of the two selection targets */
    if (seSelection.processDefaultSelection(defaultSelectionsResponse).hasActiveSelection()) {
      AbstractMatchingSe selectedSe =
          seSelection.processDefaultSelection(defaultSelectionsResponse).getActiveMatchingSe();
    } else {
      // TODO check this. Shouldn't an exception have been raised before?
      System.out.println("No selection matched!");
    }
  }

  @Override
  public void processSeInserted() {
    System.out.println("Unexpected SE insertion event");
  }

  @Override
  public void processSeRemoved() {
    System.out.println("SE removal event");
  }

  @Override
  public void processUnexpectedSeRemoval() {
    System.out.println("Unexpected SE removal event");
  }
}

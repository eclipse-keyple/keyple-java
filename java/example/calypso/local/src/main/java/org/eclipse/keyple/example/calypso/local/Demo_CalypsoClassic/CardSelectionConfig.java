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
package org.eclipse.keyple.example.calypso.local.Demo_CalypsoClassic;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.C1;

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.calypso.local.common.CalypsoClassicInfo;

/** Card Selection Configuration */
class CardSelectionConfig {
  private static CardSelection poCardSelection;

  /**
   * Define the card selection configuration for the Calypso PO
   *
   * @return card selection object
   */
  static CardSelection getPoDefaultCardSelection() {
    if (poCardSelection != null) {
      return poCardSelection;
    }
    /*
     * Initialize the selection process
     */
    poCardSelection = new CardSelection();

    /* operate multiple PO selections */
    String poFakeAid1 = "AABBCCDDEE"; // fake AID 1
    String poFakeAid2 = "EEDDCCBBAA"; // fake AID 2

    /*
     * Add selection case 1: Fake AID1, protocol ISO, target rev 3
     */
    poCardSelection.prepareSelection(
        new PoSelectionRequest(
            PoSelector.builder()
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poFakeAid1).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build()));

    /*
     * Add selection case 2: Calypso application, protocol ISO, target rev 2 or 3
     *
     * addition of read commands to execute following the selection
     */
    PoSelectionRequest poSelectionRequestCalypsoAid =
        new PoSelectionRequest(
            PoSelector.builder()
                .aidSelector(
                    CardSelector.AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                .invalidatedPo(PoSelector.InvalidatedPo.ACCEPT)
                .build());

    poSelectionRequestCalypsoAid.prepareSelectFile(CalypsoClassicInfo.LID_DF_RT);

    poSelectionRequestCalypsoAid.prepareSelectFile(CalypsoClassicInfo.LID_EventLog);

    poSelectionRequestCalypsoAid.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EventLog, CalypsoClassicInfo.RECORD_NUMBER_1);

    poCardSelection.prepareSelection(poSelectionRequestCalypsoAid);

    /*
     * Add selection case 3: Fake AID2, unspecified protocol, target rev 2 or 3
     */
    poCardSelection.prepareSelection(
        new PoSelectionRequest(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name())
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poFakeAid2).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build()));

    /*
     * Add selection case 4: ATR selection, rev 1 atrregex
     */
    poCardSelection.prepareSelection(
        new PoSelectionRequest(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name())
                .atrFilter(new CardSelector.AtrFilter(CalypsoClassicInfo.ATR_REV1_REGEX))
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build()));

    return poCardSelection;
  }

  /**
   * Operate the SAM selection
   *
   * @param samReader the reader where to operate the SAM selection
   * @return a CalypsoSam object if the selection succeed
   * @throws IllegalStateException if the selection fails
   */
  static CalypsoSam selectSam(Reader samReader) {
    // Create a SAM resource after selecting the SAM
    CardSelection samSelection = new CardSelection();

    SamSelector samSelector = SamSelector.builder().samRevision(C1).serialNumber(".*").build();

    // Prepare selector
    samSelection.prepareSelection(new SamSelectionRequest(samSelector));
    CalypsoSam calypsoSam;
    if (!samReader.isCardPresent()) {
      throw new IllegalStateException("No SAM is present in the reader " + samReader.getName());
    }
    SelectionsResult selectionsResult = samSelection.processExplicitSelection(samReader);
    if (!selectionsResult.hasActiveSelection()) {
      throw new IllegalStateException("Unable to open a logical channel for SAM!");
    }
    calypsoSam = (CalypsoSam) selectionsResult.getActiveSmartCard();

    return calypsoSam;
  }
}

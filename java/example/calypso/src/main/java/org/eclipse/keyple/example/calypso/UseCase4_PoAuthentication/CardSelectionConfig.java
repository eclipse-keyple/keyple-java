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
package org.eclipse.keyple.example.calypso.UseCase4_PoAuthentication;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.C1;

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.calypso.common.CalypsoClassicInfo;

/** Card Selection Configuration */
class CardSelectionConfig {

  /**
   * Operate the PO selection
   *
   * @param poReader the reader where to operate the PO selection
   * @return a CalypsoPo object if the selection succeed
   * @throws IllegalStateException if the selection fails
   */
  static CalypsoPo selectPo(Reader poReader) {
    // Check if a PO is present in the reader
    if (!poReader.isCardPresent()) {
      throw new IllegalStateException("No PO is present in the reader " + poReader.getName());
    }

    // Prepare a Calypso PO selection
    CardSelection cardSelection = CardSelectionConfig.getPoCardSelection();

    // Actual PO communication: operate through a single request the Calypso PO selection
    // and the file read
    CalypsoPo calypsoPo =
        (CalypsoPo) cardSelection.processExplicitSelection(poReader).getActiveSmartCard();

    return calypsoPo;
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
    CardSelection samSelection = CardSelectionConfig.getSamCardSelection();

    if (!samReader.isCardPresent()) {
      throw new IllegalStateException("No SAM is present in the reader " + samReader.getName());
    }

    SelectionsResult selectionsResult = samSelection.processExplicitSelection(samReader);

    if (!selectionsResult.hasActiveSelection()) {
      throw new IllegalStateException("Unable to open a logical channel for SAM!");
    }

    CalypsoSam calypsoSam = (CalypsoSam) selectionsResult.getActiveSmartCard();

    return calypsoSam;
  }

  private static CardSelection getPoCardSelection() {
    // Prepare a Calypso PO selection
    CardSelection cardSelection = new CardSelection();

    // Setting of an AID based selection of a Calypso REV3 PO
    // Select the first application matching the selection AID whatever the card communication
    // protocol keep the logical channel open after the selection

    // Calypso selection: configures a PoSelectionRequest with all the desired attributes to
    // make the selection and read additional information afterwards
    PoSelectionRequest poSelectionRequest =
        new PoSelectionRequest(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(
                    CardSelector.AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    // Prepare the reading of the Environment and Holder file.
    poSelectionRequest.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Add the selection case to the current selection
    // (we could have added other cases here)
    cardSelection.prepareSelection(poSelectionRequest);

    return cardSelection;
  }

  private static CardSelection getSamCardSelection() {
    // Create a SAM resource after selecting the SAM
    CardSelection samSelection = new CardSelection();

    SamSelector samSelector = SamSelector.builder().samRevision(C1).serialNumber(".*").build();

    // Prepare selector
    samSelection.prepareSelection(new SamSelectionRequest(samSelector));

    return samSelection;
  }
}

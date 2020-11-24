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
package org.eclipse.keyple.example.calypso.local.UseCase6_VerifyPin;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.C1;

import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.SamSelectionRequest;
import org.eclipse.keyple.calypso.transaction.SamSelector;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.example.calypso.local.common.CalypsoClassicInfo;

/** Card Selection Configuration */
class CardSelectionConfig {

  private static CardSelection poCardSelection;

  /**
   * Define the card selection configuration for the Calypso PO
   *
   * @return card selection object
   */
  static CardSelection getPoCardSelection() {
    if (poCardSelection != null) {
      return poCardSelection;
    }
    // Prepare a Calypso PO selection
    poCardSelection = new CardSelection();

    // Setting of an AID based selection of a Calypso REV3 PO
    //
    // Select the first application matching the selection AID whatever the card communication
    // protocol keep the logical channel open after the selection

    // Calypso selection: configures a PoSelectionRequest with all the desired attributes to
    // make the selection and read additional information afterwards
    PoSelectionRequest poSelectionRequest =
        new PoSelectionRequest(
            PoSelector.builder()
                .aidSelector(
                    CardSelector.AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    // Prepare the reading of the Environment and Holder file.
    poSelectionRequest.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Add the selection case to the current selection
    //
    // (we could have added other cases here)
    poCardSelection.prepareSelection(poSelectionRequest);

    return poCardSelection;
  }

  /**
   * Define the card selection configuration for the Calypso SAM
   *
   * @return card selection object
   */
  static CardSelection getSamCardSelection() {
    // Create a SAM resource after selecting the SAM
    CardSelection samSelection = new CardSelection();

    SamSelector samSelector = SamSelector.builder().samRevision(C1).serialNumber(".*").build();

    // Prepare selector
    samSelection.prepareSelection(new SamSelectionRequest(samSelector));

    return samSelection;
  }
}

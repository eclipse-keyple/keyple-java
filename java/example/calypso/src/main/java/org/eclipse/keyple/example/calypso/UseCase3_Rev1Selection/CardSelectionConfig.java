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
package org.eclipse.keyple.example.calypso.UseCase3_Rev1Selection;

import org.eclipse.keyple.calypso.transaction.PoSelection;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.common.CalypsoClassicInfo;

/** Card Selection Configuration */
class CardSelectionConfig {

  private static final String PO_ATR_REGEX = ".*";
  private static final String PO_DF_RT_PATH = "2000";

  /**
   * Define the card selection configuration for the Calypso PO
   *
   * @return card selection object
   */
  static CardSelectionsService getPoCardSelection() {
    // Select the first application matching the selection.
    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    // Calypso selection: configures a PoSelection with all the desired attributes to
    // make the selection and read additional information afterwards
    PoSelection poSelection =
        new PoSelection(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name())
                .atrFilter(new CardSelector.AtrFilter(PO_ATR_REGEX))
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    // Prepare the selection of the DF RT.
    poSelection.prepareSelectFile(ByteArrayUtil.fromHex(PO_DF_RT_PATH));

    // Prepare the reading order.
    poSelection.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Add the selection case to the current selection (we could have added other cases
    // here)
    cardSelectionsService.prepareSelection(poSelection);

    return cardSelectionsService;
  }
}

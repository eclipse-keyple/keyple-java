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
package org.eclipse.keyple.example.calypso.local.UseCase3_Rev1Selection;

import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.local.common.CalypsoClassicInfo;

class CardSelectionConfig {

  private static final String PO_ATR_REGEX = ".*";
  private static final String PO_DF_RT_PATH = "2000";

  static CardSelection getPoCardSelection() {
    // Select the first application matching the selection.
    CardSelection cardSelection = new CardSelection();

    // Calypso selection: configures a PoSelectionRequest with all the desired attributes to
    // make the selection and read additional information afterwards
    PoSelectionRequest poSelectionRequest =
        new PoSelectionRequest(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name())
                .atrFilter(new CardSelector.AtrFilter(PO_ATR_REGEX))
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    // Prepare the selection of the DF RT.
    poSelectionRequest.prepareSelectFile(ByteArrayUtil.fromHex(PO_DF_RT_PATH));

    // Prepare the reading order.
    poSelectionRequest.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Add the selection case to the current selection (we could have added other cases
    // here)
    cardSelection.prepareSelection(poSelectionRequest);

    return cardSelection;
  }
}

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
package org.eclipse.keyple.plugin.remote.integration.common.util;

import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.AccessLevel;

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;

public final class CalypsoUtils {

  public static PoSecuritySettings getSecuritySettings(CardResource<CalypsoSam> samResource) {

    // The default KIF values for personalization, loading and debiting
    final byte DEFAULT_KIF_PERSO = (byte) 0x21;
    final byte DEFAULT_KIF_LOAD = (byte) 0x27;
    final byte DEFAULT_KIF_DEBIT = (byte) 0x30;
    // The default key record number values for personalization, loading and debiting
    // The actual value should be adjusted.
    final byte DEFAULT_KEY_RECORD_NUMBER_PERSO = (byte) 0x01;
    final byte DEFAULT_KEY_RECORD_NUMBER_LOAD = (byte) 0x02;
    final byte DEFAULT_KEY_RECORD_NUMBER_DEBIT = (byte) 0x03;
    /* define the security parameters to provide when creating PoTransaction */
    return new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
        .sessionDefaultKif(AccessLevel.SESSION_LVL_PERSO, DEFAULT_KIF_PERSO)
        .sessionDefaultKif(AccessLevel.SESSION_LVL_LOAD, DEFAULT_KIF_LOAD)
        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)
        .sessionDefaultKeyRecordNumber(
            AccessLevel.SESSION_LVL_PERSO, DEFAULT_KEY_RECORD_NUMBER_PERSO)
        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_LOAD, DEFAULT_KEY_RECORD_NUMBER_LOAD)
        .sessionDefaultKeyRecordNumber(
            AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
        .build();
  }

  public static CardSelectionsService getCardSelection() {
    // Prepare PO Selection
    CardSelectionsService cardSelection = new CardSelectionsService();

    // Calypso selection
    PoSelection poSelection =
        new PoSelection(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(
                    CardSelector.AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    // Prepare the reading order.
    poSelection.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Add the selection case to the current selection
    cardSelection.prepareSelection(poSelection);

    return cardSelection;
  }

  public static String readEventLog(CalypsoPo calypsoPo, Reader reader, Logger logger) {
    // execute calypso session from a card selection
    logger.info(
        "Initial PO Content, atr : {}, sn : {}",
        calypsoPo.getAtr(),
        calypsoPo.getApplicationSerialNumber());

    // Retrieve the data read from the CalyspoPo updated during the transaction process
    ElementaryFile efEnvironmentAndHolder =
        calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EnvironmentAndHolder);
    String environmentAndHolder =
        ByteArrayUtil.toHex(efEnvironmentAndHolder.getData().getContent());

    // Log the result
    logger.info("EnvironmentAndHolder file data: {}", environmentAndHolder);

    // Go on with the reading of the first record of the EventLog file
    logger.info("= #### reading transaction of the EventLog file.");

    PoTransaction poTransaction = new PoTransaction(new CardResource<CalypsoPo>(reader, calypsoPo));

    // Prepare the reading order and keep the associated parser for later use once the
    // transaction has been processed.
    poTransaction.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EventLog, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Actual PO communication: send the prepared read order, then close the channel with
    // the PO
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
    logger.info("The reading of the EventLog has succeeded.");

    // Retrieve the data read from the CalyspoPo updated during the transaction process
    ElementaryFile efEventLog = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EventLog);
    String eventLog = ByteArrayUtil.toHex(efEventLog.getData().getContent());

    // Log the result
    logger.info("EventLog file data: {}", eventLog);

    return eventLog;
  }
}

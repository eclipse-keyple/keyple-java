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
package org.eclipse.keyple.example.calypso.local.UseCase6_VerifyPin;

import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoPinException;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.example.calypso.local.common.PcscReaderUtils;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>Use Case ‘Calypso 6’ – Verify Pin (outside and inside secure session) (PC/SC)</h1>
 *
 * <br>
 * The example shows 4 successive presentations of PIN codes:
 *
 * <ul>
 *   <li>Outside session, transmission in plain
 *   <li>Outside session, transmission encrypted
 *   <li>Inside session, incorrect PIN
 *   <li>Inside session, correct PIN
 * </ul>
 *
 * The remaining attempt counter is logged after each operation.
 */
public class VerifyPin_Pcsc {
  private static final Logger logger = LoggerFactory.getLogger(VerifyPin_Pcsc.class);
  private static Reader poReader;
  private static CalypsoPo calypsoPo;

  public static void main(String[] args) {

    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    // Register the PcscPlugin with SmartCardService, get the corresponding generic Plugin in return
    // This example does not use observation, no exception handler is defined.
    Plugin plugin = smartCardService.registerPlugin(new PcscPluginFactory(null, null));

    // Get and configure the PO reader
    poReader = plugin.getReader(PcscReaderUtils.getContactlessReaderName());
    ((PcscReader) poReader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    // Get and configure the SAM reader
    Reader samReader = plugin.getReader(PcscReaderUtils.getContactReaderName());
    ((PcscReader) samReader).setContactless(false).setIsoProtocol(PcscReader.IsoProtocol.T0);

    // Create a SAM resource after selecting the SAM
    CardSelection samSelection = CardSelectionConfig.getSamCardSelection();

    if (samReader.isCardPresent()) {
      throw new IllegalStateException("No SAM is present in the reader " + samReader.getName());
    }

    // process explicit selection
    SelectionsResult selectionsResult = samSelection.processExplicitSelection(samReader);

    if (!selectionsResult.hasActiveSelection()) {
      throw new IllegalStateException("Unable to open a logical channel for SAM!");
    }

    CalypsoSam calypsoSam = (CalypsoSam) selectionsResult.getActiveSmartCard();

    CardResource<CalypsoSam> samResource = new CardResource<CalypsoSam>(samReader, calypsoSam);

    // display basic information about the readers and SAM
    logger.info("=============== UseCase Calypso #6: Verify PIN  ==================");
    logger.info("= PO Reader  NAME = {}", poReader.getName());
    logger.info("= SAM Reader  NAME = {}", samResource.getReader().getName());

    logger.info("= ##### 1st PO exchange: AID based selection with reading of Environment file.");

    // Prepare a Calypso PO selection
    CardSelection cardSelection = CardSelectionConfig.getPoCardSelection();

    // Actual PO communication: operate through a single request the Calypso PO selection
    // and the file read
    calypsoPo = (CalypsoPo) cardSelection.processExplicitSelection(poReader).getActiveSmartCard();

    // Security settings
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .pinCipheringKey((byte) 0x30, (byte) 0x79)
            .build();

    // Create the PO resource
    CardResource<CalypsoPo> poResource;
    poResource = new CardResource<CalypsoPo>(poReader, calypsoPo);
    String pinOk = "0000";
    String pinKo = "0001";

    // create an unsecured PoTransaction
    PoTransaction poTransactionUnsecured = new PoTransaction(poResource);

    ////////////////////////////
    // Verification of the PIN (correct) out of a secure session in plain mode
    poTransactionUnsecured.processVerifyPin(pinOk);
    logger.info("Remaining attempts #1: {}", calypsoPo.getPinAttemptRemaining());

    // create a secured PoTransaction
    PoTransaction poTransaction = new PoTransaction(poResource, poSecuritySettings);

    ////////////////////////////
    // Verification of the PIN (correct) out of a secure session in encrypted mode
    poTransaction.processVerifyPin(pinOk);
    // log the current counter value (should be 3)
    logger.info("Remaining attempts #2: {}", calypsoPo.getPinAttemptRemaining());

    ////////////////////////////
    // Verification of the PIN (incorrect) inside a secure session
    poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);
    try {
      poTransaction.processVerifyPin(pinKo);
    } catch (CalypsoPoPinException ex) {
      logger.error("PIN Exception: {}", ex.getMessage());
    }
    poTransaction.processCancel();
    // log the current counter value (should be 2)
    logger.error("Remaining attempts #3: {}", calypsoPo.getPinAttemptRemaining());

    ////////////////////////////
    // Verification of the PIN (correct) inside a secure session with reading of the counter
    //////////////////////////// before
    poTransaction.prepareCheckPinStatus();
    poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);
    // log the current counter value (should be 2)
    logger.info("Remaining attempts #4: {}", calypsoPo.getPinAttemptRemaining());
    poTransaction.processVerifyPin(pinOk);
    poTransaction.prepareReleasePoChannel();
    poTransaction.processClosing();
    // log the current counter value (should be 3)
    logger.info("Remaining attempts #5: {}", calypsoPo.getPinAttemptRemaining());

    System.exit(0);
  }
}

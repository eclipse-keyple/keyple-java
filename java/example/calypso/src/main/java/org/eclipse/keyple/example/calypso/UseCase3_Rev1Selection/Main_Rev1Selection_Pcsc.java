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
package org.eclipse.keyple.example.calypso.UseCase3_Rev1Selection;

import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.ElementaryFile;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.common.CalypsoClassicInfo;
import org.eclipse.keyple.example.calypso.common.PcscReaderUtils;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactlessProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>Use Case ‘Calypso 3’ – Rev1 Selection Atr (PC/SC)</h1>
 *
 * <ul>
 *   <li>
 *       <h2>Scenario:</h2>
 *       <ul>
 *         <li>Check if a B' protocol card is in the reader, select a Calypso PO Rev1 (ATR
 *             selection), select the DF RT (ticketing), operate a simple Calypso PO transaction
 *             (simple plain read, not involving a Calypso SAM).
 *         <li><code>
 * Explicit Selection
 * </code> means that it is the terminal application which start the card processing.
 *         <li>PO messages:
 *             <ul>
 *               <li>A first card message to do an ATR based selection and DF selection of the card
 *                   in the reader
 *               <li>A second card message to operate the simple Calypso transaction
 *             </ul>
 *       </ul>
 * </ul>
 */
public class Main_Rev1Selection_Pcsc {
  private static final Logger logger = LoggerFactory.getLogger(Main_Rev1Selection_Pcsc.class);

  /** main program entry */
  public static void main(String[] args) {

    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    // Register the PcscPlugin with SmartCardService, get the corresponding generic Plugin in return
    // This example does not use observation, no exception handler is defined.
    Plugin plugin = smartCardService.registerPlugin(new PcscPluginFactory(null, null));

    // Get and configure the PO reader
    Reader poReader = plugin.getReader(PcscReaderUtils.getContactlessReaderName());
    ((PcscReader) poReader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    /* Activate protocols */
    poReader.activateProtocol(
        PcscSupportedContactlessProtocols.INNOVATRON_B_PRIME_CARD.name(),
        ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name());

    logger.info(
        "=============== UseCase Calypso #1: ATR based explicit selection (PO Rev1) ===========");
    logger.info("= PO Reader  NAME = {}", poReader.getName());

    // Check if a PO is present in the reader
    if (!poReader.isCardPresent()) {
      logger.error("No PO is present in the reader");
    }

    logger.info("= #### 1st PO exchange: ATR based selection with reading of Environment file.");

    // Prepare a Calypso PO selection Setting up a selection of a Calypso REV1 PO (B Prime)
    // based on ATR
    //
    // Select the first application matching the selection.
    CardSelection cardSelection = CardSelectionConfig.getPoCardSelection();

    // Actual PO communication: operate through a single request the Calypso PO selection
    // and the file read
    CalypsoPo calypsoPo =
        (CalypsoPo) cardSelection.processExplicitSelection(poReader).getActiveSmartCard();
    logger.info("The selection of the PO has succeeded.");

    // Retrieve the data read from the CalyspoPo updated during the transaction process
    ElementaryFile efEnvironmentAndHolder =
        calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EnvironmentAndHolder);
    String environmentAndHolder =
        ByteArrayUtil.toHex(efEnvironmentAndHolder.getData().getContent());

    // Log the result
    logger.info("EnvironmentAndHolder file data: {}", environmentAndHolder);

    // Go on with the reading of the first record of the EventLog file
    logger.info("= #### 2nd PO exchange: reading transaction of the EventLog file.");

    PoTransaction poTransaction =
        new PoTransaction(new CardResource<CalypsoPo>(poReader, calypsoPo));

    // Prepare the reading order and keep the associated parser for later use once the
    // transaction has been processed. We provide the expected record length since the REV1
    // PO need it.
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

    logger.info("= #### End of the Calypso PO processing.");

    System.exit(0);
  }
}

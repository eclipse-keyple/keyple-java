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
package org.eclipse.keyple.example.calypso.UseCase1_ExplicitSelectionAid;

import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.ElementaryFile;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.common.CalypsoClassicInfo;
import org.eclipse.keyple.example.calypso.common.StubCalypsoClassic;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>Use Case ‘Calypso 1’ – Explicit Selection Aid (Stub)</h1>
 *
 * <ul>
 *   <li>
 *       <h2>Scenario:</h2>
 *       <ul>
 *         <li>Check if a ISO 14443-4 card is in the reader, select a Calypso PO, operate a simple
 *             Calypso PO transaction (simple plain read, not involving a Calypso SAM).
 *         <li><code>
 * Explicit Selection
 * </code> means that it is the terminal application which start the card processing.
 *         <li>PO messages:
 *             <ul>
 *               <li>A first card message to select the application in the reader
 *               <li>A second card message to operate the simple Calypso transaction
 *             </ul>
 *       </ul>
 * </ul>
 */
public class Main_ExplicitSelectionAid_Stub {
  private static final Logger logger =
      LoggerFactory.getLogger(Main_ExplicitSelectionAid_Stub.class);

  public static void main(String[] args) {

    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    final String STUB_PLUGIN_NAME = "stub1";

    // Register Stub plugin in the platform
    // This example does not use observation, no exception handler is defined.
    Plugin stubPlugin =
        smartCardService.registerPlugin(new StubPluginFactory(STUB_PLUGIN_NAME, null, null));

    // Plug the PO stub reader.
    ((StubPlugin) stubPlugin).plugReader("poReader", true);

    // Get a PO reader ready to work with Calypso PO.
    StubReader poReader = (StubReader) (stubPlugin.getReader("poReader"));

    /* Activate ISO_14443_4 Protocol */
    poReader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // Check if the reader exists
    if (poReader == null) {
      throw new IllegalStateException("Bad PO reader setup");
    }

    // Create 'virtual' Calypso PO
    StubSmartCard calypsoStubCard = new StubCalypsoClassic();

    logger.info("Insert stub PO.");
    poReader.insertCard(calypsoStubCard);

    logger.info(
        "=============== UseCase Calypso #1: AID based explicit selection ==================");
    logger.info("= PO Reader  NAME = {}", poReader.getName());

    // Check if a PO is present in the reader
    if (!poReader.isCardPresent()) {
      logger.error("No PO is detected.");
    }

    logger.info("= #### 1st PO exchange: AID based selection with reading of Environment file.");

    // Prepare a Calypso PO selection
    CardSelectionsService cardSelectionsService = CardSelectionConfig.getPoCardSelection();

    // Actual PO communication: operate through a single request the Calypso PO selection
    // and the file read
    CalypsoPo calypsoPo =
        (CalypsoPo) cardSelectionsService.processExplicitSelections(poReader).getActiveSmartCard();
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

    logger.info("= #### End of the Calypso PO processing.");

    logger.info("Remove stub PO.");
    poReader.removeCard();

    System.exit(0);
  }
}

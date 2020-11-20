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
package org.eclipse.keyple.example.calypso.local.UseCase4_PoAuthentication;

import static org.eclipse.keyple.example.calypso.local.UseCase4_PoAuthentication.CardSelectionConfig.selectPo;
import static org.eclipse.keyple.example.calypso.local.UseCase4_PoAuthentication.CardSelectionConfig.selectSam;

import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.ElementaryFile;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.local.common.*;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSmartCard;
import org.eclipse.keyple.plugin.stub.StubSupportedProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>Use Case ‘Calypso 4’ – PO Authentication (Stub)</h1>
 *
 * <ul>
 *   <li>
 *       <h2>Scenario:</h2>
 *       <ul>
 *         <li>Initialize two stub readers (PO and SAM), insert a stub PO and a stub SAM.
 *         <li>Check if a ISO 14443-4 card is in the reader, select a Calypso PO, operate a simple
 *             Calypso PO authentication (open and close a secure session performed with the debit
 *             key).
 *             <p>The SAM messages are handled transparently by the Calypso transaction API.
 *         <li><code>
 * Explicit Selection
 * </code> means that it is the terminal application which start the card processing.
 *         <li>4 PO messages:
 *             <ul>
 *               <li>1 - card message to explicitly select the application in the reader
 *               <li>2 - transaction card message to operate the session opening and a PO read
 *               <li>3 - transaction card message to operate the reading of a file
 *               <li>4 - transaction card message to operate the closing opening
 *             </ul>
 *       </ul>
 * </ul>
 */
public class PoAuthentication_Stub {
  private static final Logger logger = LoggerFactory.getLogger(PoAuthentication_Stub.class);

  private static StubPlugin plugin;

  public static void main(String[] args) {

    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    final String STUB_PLUGIN_NAME = "stub1";

    // Register Stub plugin in the platform
    plugin = (StubPlugin) smartCardService.registerPlugin(new StubPluginFactory(STUB_PLUGIN_NAME));

    Reader poReader = initPoReader();

    Reader samReader = initSamReader();

    CalypsoSam calypsoSam = selectSam(samReader);

    CardResource<CalypsoSam> samResource = new CardResource<CalypsoSam>(samReader, calypsoSam);

    logger.info("=============== UseCase Calypso #4: Po Authentication ==================");
    logger.info("= PO Reader  NAME = {}", poReader.getName());
    logger.info("= SAM Reader  NAME = {}", samReader.getName());
    logger.info(
        "= SAM Reader  NAME = {}, SERIAL NUMBER = {}",
        samReader.getName(),
        ByteArrayUtil.toHex(calypsoSam.getSerialNumber()));
    logger.info("= ##### 1st PO exchange: AID based selection with reading of Environment file.");

    CalypsoPo calypsoPo = selectPo(poReader);

    logger.info("The selection of the PO has succeeded.");

    // All data collected from the PO are available in CalypsoPo
    // Get the Environment and Holder data
    ElementaryFile efEnvironmentAndHolder =
        calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EnvironmentAndHolder);

    String environmentAndHolder =
        ByteArrayUtil.toHex(efEnvironmentAndHolder.getData().getContent());
    logger.info("File Environment and Holder: {}", environmentAndHolder);

    // Go on with the reading of the first record of the EventLog file
    logger.info(
        "= ##### 2nd PO exchange: open and close a secure session to perform authentication.");

    PoTransaction poTransaction =
        new PoTransaction(
            new CardResource<CalypsoPo>(poReader, calypsoPo),
            CalypsoUtils.getSecuritySettings(samResource));

    // Read the EventLog file at the Session Opening
    poTransaction.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EventLog, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Open Session for the debit key
    poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);

    // Get the EventLog data
    ElementaryFile efEventLog = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EventLog);

    String eventLog = ByteArrayUtil.toHex(efEventLog.getData().getContent());
    logger.info("File Event log: {}", eventLog);

    if (!calypsoPo.isDfRatified()) {
      logger.info("========= Previous Secure Session was not ratified. =====================");
    }

    // Read the ContractList file inside the Secure Session
    poTransaction.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_ContractList, CalypsoClassicInfo.RECORD_NUMBER_1);

    poTransaction.processPoCommands();

    // Get the ContractList data
    ElementaryFile efContractList = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_ContractList);

    String contractList = ByteArrayUtil.toHex(efContractList.getData().getContent());
    logger.info("File Contract List: {}", contractList);

    // Append a new record to EventLog.
    // Does not change the content as in the PC/SC example due to the Stub limitations
    poTransaction.prepareAppendRecord(
        CalypsoClassicInfo.SFI_EventLog, efEventLog.getData().getContent());

    // Execute Append Record and close the Secure Session.
    logger.info("========= PO Calypso session ======= Closing ============================");

    // A ratification command will be sent (CONTACTLESS_MODE).
    poTransaction.prepareReleasePoChannel();
    poTransaction.processClosing();

    logger.info("The Calypso session ended successfully.");

    logger.info("= ##### End of the Calypso PO processing.");

    System.exit(0);
  }

  private static Reader initPoReader() {
    // Plug PO
    plugin.plugStubReader("poReader", true);

    Reader poReader = plugin.getReader("poReader");

    // activate protocols
    poReader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // Create 'virtual' Calypso PO
    StubSmartCard calypsoStubCard = new StubCalypsoClassic();

    logger.info("Insert stub PO.");
    ((StubReader) poReader).insertCard(calypsoStubCard);

    return poReader;
  }

  private static Reader initSamReader() {
    // Plug a SAM stub reader.
    ((StubPlugin) plugin).plugStubReader("samReader", true);
    Reader samReader = plugin.getReader("samReader");

    // activate protocols
    samReader.activateProtocol(
        StubSupportedProtocols.ISO_7816_3.name(), ContactCardCommonProtocols.ISO_7816_3.name());

    // Create 'virtual' Calypso SAM
    StubSmartCard calypsoSamStubCard = new StubSamCalypsoClassic();

    logger.info("Insert stub SAM.");
    ((StubReader) samReader).insertCard(calypsoSamStubCard);

    return samReader;
  }
}

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
package org.eclipse.keyple.example.calypso.local.UseCase8_StoredValue_DebitInSession;

import static org.eclipse.keyple.calypso.transaction.PoTransaction.SvSettings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.ElementaryFile;
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.local.common.CalypsoClassicInfo;
import org.eclipse.keyple.example.calypso.local.common.PcscReaderUtils;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>Use Case ‘Calypso 8’ – Stored Value Debit in Session (PC/SC)</h1>
 *
 * <br>
 * This example illustrates an SV debit within a secure session.<br>
 */
public class StoredValue_DebitInSession_Pcsc {
  private static final Logger logger =
      LoggerFactory.getLogger(StoredValue_DebitInSession_Pcsc.class);

  /**
   * Main program entry
   *
   * <p>Any error will be notified by a runtime exception (not captured in this example).
   *
   * @param args not used
   */
  public static void main(String[] args) {

    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    // Register the PcscPlugin with SmartCardService, get the corresponding generic Plugin in return
    // This example does not use observation, no exception handler is defined.
    Plugin plugin = smartCardService.registerPlugin(new PcscPluginFactory(null, null));

    // Get and configure the PO reader
    Reader poReader = plugin.getReader(PcscReaderUtils.getContactlessReaderName());
    ((PcscReader) poReader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    // Get and configure the SAM reader
    Reader samReader = plugin.getReader(PcscReaderUtils.getContactReaderName());
    ((PcscReader) samReader).setContactless(false).setIsoProtocol(PcscReader.IsoProtocol.T0);

    if (samReader.isCardPresent()) {
      throw new IllegalStateException("No SAM is present in the reader " + samReader.getName());
    }

    SelectionsResult selectionsResult =
        CardSelectionConfiguration.getSamCardSelection().processExplicitSelection(samReader);

    if (!selectionsResult.hasActiveSelection()) {
      throw new IllegalStateException("Unable to open a logical channel for SAM!");
    }

    CalypsoSam calypsoSam = (CalypsoSam) selectionsResult.getActiveSmartCard();

    CardResource<CalypsoSam> samResource = new CardResource<CalypsoSam>(samReader, calypsoSam);

    // display basic information about the readers and SAM
    logger.info("=============== UseCase Calypso #8: Stored Value Debit in Session ==");
    logger.info("= PO Reader  NAME = {}", poReader.getName());
    logger.info("= SAM Reader  NAME = {}", samResource.getReader().getName());

    CalypsoPo calypsoPo =
        (CalypsoPo)
            CardSelectionConfiguration.getCardSelection()
                .processExplicitSelection(poReader)
                .getActiveSmartCard();

    // Security settings
    // Both Reload and Debit SV logs are requested
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
            .svGetLogReadMode(SvSettings.LogRead.ALL)
            .build();

    // Create the PO resource
    CardResource<CalypsoPo> poResource;
    poResource = new CardResource<CalypsoPo>(poReader, calypsoPo);

    PoTransaction poTransaction = new PoTransaction(poResource, poSecuritySettings);

    // Read the EventLog file at the Session Opening
    poTransaction.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EventLog, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Open a secure session (DEBIT level) and execute the prepared command
    poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);

    // Get and display the EventLog data
    ElementaryFile efEventLog = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EventLog);

    String eventLog = ByteArrayUtil.toHex(efEventLog.getData().getContent());
    logger.info("File Event log: {}", eventLog);

    // Prepare a SV Debit (this command could also have been placed before processOpening
    // since it is not followed by any other command)
    poTransaction.prepareSvGet(SvSettings.Operation.DEBIT, SvSettings.Action.DO);

    // Execute the prepared command
    poTransaction.processPoCommands();

    // Display the current SV status
    logger.info("Current SV status (SV Get for DEBIT):");
    logger.info(". Balance = {}", calypsoPo.getSvBalance());
    logger.info(". Last Transaction Number = {}", calypsoPo.getSvLastTNum());

    // To easily display the content of the logs, we use here the toString method which
    // exports the data in JSON format.
    String loadLogRecordJson = prettyPrintJson(calypsoPo.getSvLoadLogRecord().toString());
    String debitLogRecordJson = prettyPrintJson(calypsoPo.getSvDebitLogLastRecord().toString());
    logger.info(". Load log record = {}", loadLogRecordJson);
    logger.info(". Debit log record = {}", debitLogRecordJson);

    // Prepare an SV Debit of 2 units
    poTransaction.prepareSvDebit(2);

    // Prepare to append a new record to EventLog. Just increment the first byte.
    byte[] log = efEventLog.getData().getContent();
    log[0] = (byte) (log[0] + 1);
    poTransaction.prepareAppendRecord(CalypsoClassicInfo.SFI_EventLog, log);

    // Execute the 2 prepared commands, close the secure session, verify the SV signature
    // and close the communication after
    poTransaction.prepareReleasePoChannel();
    poTransaction.processClosing();

    System.exit(0);
  }

  /**
   * Help method for formatting a JSON data string
   *
   * @param uglyJSONString the string to format
   * @return the formatted string
   */
  private static String prettyPrintJson(String uglyJSONString) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    JsonParser jp = new JsonParser();
    JsonElement je = jp.parse(uglyJSONString);
    return gson.toJson(je);
  }
}

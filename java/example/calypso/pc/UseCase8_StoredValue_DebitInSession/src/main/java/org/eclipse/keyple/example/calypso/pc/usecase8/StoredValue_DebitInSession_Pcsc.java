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
package org.eclipse.keyple.example.calypso.pc.usecase8;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.C1;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SvSettings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.ElementaryFile;
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.calypso.transaction.SamSelectionRequest;
import org.eclipse.keyple.calypso.transaction.SamSelector;
import org.eclipse.keyple.core.selection.CardResource;
import org.eclipse.keyple.core.selection.CardSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.CardSelector;
import org.eclipse.keyple.core.seproxy.Plugin;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.SmartCardService;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.example.common.calypso.postructure.CalypsoClassicInfo;
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
  private static Reader poReader;
  private static CalypsoPo calypsoPo;

  /**
   * Selects the PO
   *
   * @return true if the PO is selected
   * @throws KeypleReaderException in case of reader communication failure
   */
  private static boolean selectPo() {
    /* Check if a PO is present in the reader */
    if (poReader.isSePresent()) {
      logger.info("= ##### 1st PO exchange: AID based selection with reading of Environment file.");

      // Prepare a Calypso PO selection
      CardSelection cardSelection = new CardSelection();

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
                      CardSelector.AidSelector.builder()
                          .aidToSelect(CalypsoClassicInfo.AID)
                          .build())
                  .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                  .build());

      // Prepare the reading of the Environment and Holder file.
      poSelectionRequest.prepareReadRecordFile(
          CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

      // Add the selection case to the current selection
      //
      // (we could have added other cases here)
      cardSelection.prepareSelection(poSelectionRequest);

      // Actual PO communication: operate through a single request the Calypso PO selection
      // and the file read
      calypsoPo = (CalypsoPo) cardSelection.processExplicitSelection(poReader).getActiveSmartCard();
      return true;
    } else {
      logger.error("No PO were detected.");
    }
    return false;
  }

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

    // Register the PcscPlugin with SmartCardService, get the corresponding generic Plugin in
    // return
    Plugin plugin = smartCardService.registerPlugin(new PcscPluginFactory());

    // Get and configure the PO reader
    poReader = plugin.getReader(ReaderUtilities.getContactlessReaderName());
    ((PcscReader) poReader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    // Get and configure the SAM reader
    Reader samReader = plugin.getReader(ReaderUtilities.getContactReaderName());
    ((PcscReader) samReader).setContactless(false).setIsoProtocol(PcscReader.IsoProtocol.T0);

    // Create a SAM resource after selecting the SAM
    CardSelection samSelection = new CardSelection();

    SamSelector samSelector = SamSelector.builder().samRevision(C1).serialNumber(".*").build();

    // Prepare selector
    samSelection.prepareSelection(new SamSelectionRequest(samSelector));
    CalypsoSam calypsoSam;
    try {
      if (samReader.isSePresent()) {
        SelectionsResult selectionsResult = samSelection.processExplicitSelection(samReader);
        if (selectionsResult.hasActiveSelection()) {
          calypsoSam = (CalypsoSam) selectionsResult.getActiveSmartCard();
        } else {
          throw new IllegalStateException("Unable to open a logical channel for SAM!");
        }
      } else {
        throw new IllegalStateException("No SAM is present in the reader " + samReader.getName());
      }
    } catch (KeypleReaderException e) {
      throw new IllegalStateException("Reader exception: " + e.getMessage());
    } catch (KeypleException e) {
      throw new IllegalStateException("Reader exception: " + e.getMessage());
    }
    CardResource<CalypsoSam> samResource = new CardResource<CalypsoSam>(samReader, calypsoSam);

    // display basic information about the readers and SAM
    logger.info("=============== UseCase Calypso #8: Stored Value Debit in Session ==");
    logger.info("= PO Reader  NAME = {}", poReader.getName());
    logger.info("= SAM Reader  NAME = {}", samResource.getReader().getName());

    if (selectPo()) {
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
    } else {
      logger.error("The PO selection failed");
    }

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

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
package org.eclipse.keyple.example.calypso.local.UseCase7_StoredValue_SimpleReload;

import static org.eclipse.keyple.calypso.transaction.PoTransaction.SvSettings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
 * <h1>Use Case ‘Calypso 7’ – Stored Value Simple Reload (PC/SC)</h1>
 *
 * <br>
 * This example illustrates an out of secure session SV reload (the code wouldn't be very different
 * different with a secure session.).<br>
 * Both logs (reload and debit) are read.
 */
public class StoredValue_SimpleReload_Pcsc {
  private static final Logger logger = LoggerFactory.getLogger(StoredValue_SimpleReload_Pcsc.class);

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

    CardSelection samSelection = CardSelectionConfig.getSamCardSelection();

    if (samReader.isCardPresent()) {
      throw new IllegalStateException("No SAM is present in the reader " + samReader.getName());
    }
    SelectionsResult selectionsResult = samSelection.processExplicitSelection(samReader);

    if (!selectionsResult.hasActiveSelection()) {
      throw new IllegalStateException("Unable to open a logical channel for SAM!");
    }

    CalypsoSam calypsoSam = (CalypsoSam) selectionsResult.getActiveSmartCard();

    CardResource<CalypsoSam> samResource = new CardResource<CalypsoSam>(samReader, calypsoSam);

    // display basic information about the readers and SAM
    logger.info("=============== UseCase Calypso #7: Stored Value Simple Reload =====");
    logger.info("= PO Reader  NAME = {}", poReader.getName());
    logger.info("= SAM Reader  NAME = {}", samResource.getReader().getName());

    logger.info("= ##### 1st PO exchange: AID based selection with reading of Environment file.");

    // Actual PO communication: operate through a single request the Calypso PO selection
    // and the file read
    CalypsoPo calypsoPo =
        (CalypsoPo)
            CardSelectionConfig.getCardSelection()
                .processExplicitSelection(poReader)
                .getActiveSmartCard(); // Security settings

    // Keep the default setting for SV logs reading (only the reload log will be read here)
    PoSecuritySettings poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(samResource).build();

    // Create the PO resource
    CardResource<CalypsoPo> poResource;
    poResource = new CardResource<CalypsoPo>(poReader, calypsoPo);

    // Create a secured PoTransaction
    PoTransaction poTransaction = new PoTransaction(poResource, poSecuritySettings);

    // Prepare the command to retrieve the SV status with the two debit and reload logs.
    poTransaction.prepareSvGet(SvSettings.Operation.RELOAD, SvSettings.Action.DO);

    // Execute the command
    poTransaction.processPoCommands();

    // Display the current SV status
    logger.info("Current SV status (SV Get for RELOAD):");
    logger.info(". Balance = {}", calypsoPo.getSvBalance());
    logger.info(". Last Transaction Number = {}", calypsoPo.getSvLastTNum());

    // To easily display the content of the log, we use here the toString method which
    // exports the data in JSON format.
    String loadLogRecordJson = prettyPrintJson(calypsoPo.getSvLoadLogRecord().toString());
    logger.info(". Debit log record = {}", loadLogRecordJson);

    // Reload with 2 units
    poTransaction.prepareSvReload(2);

    // Execute the command and close the communication after
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();

    logger.info("The balance of the PO has been recharged by 2 units");

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

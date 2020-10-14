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
package org.eclipse.keyple.example.calypso.pc.usecase7;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.C1;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SvSettings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.calypso.transaction.SamSelectionRequest;
import org.eclipse.keyple.calypso.transaction.SamSelector;
import org.eclipse.keyple.core.selection.SeResource;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.CardSelector;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.example.common.calypso.postructure.CalypsoClassicInfo;
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
      SeSelection seSelection = new SeSelection();

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

      // Add the selection case to the current selection
      //
      // (we could have added other cases here)
      seSelection.prepareSelection(poSelectionRequest);

      // Actual PO communication: operate through a single request the Calypso PO selection
      // and the file read
      calypsoPo = (CalypsoPo) seSelection.processExplicitSelection(poReader).getActiveMatchingSe();
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

    // Get the instance of the SeProxyService (Singleton pattern)
    SeProxyService seProxyService = SeProxyService.getInstance();

    // Register the PcscPlugin with SeProxyService, get the corresponding generic ReaderPlugin in
    // return
    ReaderPlugin readerPlugin = seProxyService.registerPlugin(new PcscPluginFactory());

    // Get and configure the PO reader
    poReader = readerPlugin.getReader(ReaderUtilities.getContactlessReaderName());
    ((PcscReader) poReader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    // Get and configure the SAM reader
    Reader samReader = readerPlugin.getReader(ReaderUtilities.getContactReaderName());
    ((PcscReader) samReader).setContactless(false).setIsoProtocol(PcscReader.IsoProtocol.T0);

    // Create a SAM resource after selecting the SAM
    SeSelection samSelection = new SeSelection();

    SamSelector samSelector = SamSelector.builder().samRevision(C1).serialNumber(".*").build();

    // Prepare selector
    samSelection.prepareSelection(new SamSelectionRequest(samSelector));
    CalypsoSam calypsoSam;
    try {
      if (samReader.isSePresent()) {
        SelectionsResult selectionsResult = samSelection.processExplicitSelection(samReader);
        if (selectionsResult.hasActiveSelection()) {
          calypsoSam = (CalypsoSam) selectionsResult.getActiveMatchingSe();
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
    SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);

    // display basic information about the readers and SAM
    logger.info("=============== UseCase Calypso #7: Stored Value Simple Reload =====");
    logger.info("= PO Reader  NAME = {}", poReader.getName());
    logger.info("= SAM Reader  NAME = {}", samResource.getReader().getName());

    if (selectPo()) {
      // Security settings
      // Keep the default setting for SV logs reading (only the reload log will be read here)
      PoSecuritySettings poSecuritySettings =
          new PoSecuritySettings.PoSecuritySettingsBuilder(samResource).build();

      // Create the PO resource
      SeResource<CalypsoPo> poResource;
      poResource = new SeResource<CalypsoPo>(poReader, calypsoPo);

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

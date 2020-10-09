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
package org.eclipse.keyple.example.generic.pc.usecase1;

import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.example.common.generic.GenericSeSelectionRequest;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>Use Case ‘generic 1’ – Explicit Selection Aid (PC/SC)</h1>
 *
 * <ul>
 *   <li>
 *       <h2>Scenario:</h2>
 *       <ul>
 *         <li>Check if a ISO 14443-4 SE is in the reader, select a SE (here a Calypso PO).
 *         <li><code>
 * Explicit Selection
 * </code> means that it is the terminal application which start the SE processing.
 *         <li>SE messages:
 *             <ul>
 *               <li>A single SE message to select the application in the reader
 *             </ul>
 *       </ul>
 * </ul>
 */
public class ExplicitSelectionAid_Pcsc {
  private static final Logger logger = LoggerFactory.getLogger(ExplicitSelectionAid_Pcsc.class);
  private static final String seAid = "A000000291A000000191"; /* Here a Hoplink AID */

  public static void main(String[] args) {

    // Get the instance of the SeProxyService (Singleton pattern)
    SeProxyService seProxyService = SeProxyService.getInstance();

    // Register the PcscPlugin with SeProxyService, get the corresponding generic ReaderPlugin in
    // return
    ReaderPlugin readerPlugin = seProxyService.registerPlugin(new PcscPluginFactory());

    // Get and configure the SE reader
    SeReader seReader = readerPlugin.getReader(ReaderUtilities.getContactlessReaderName());
    ((PcscReader) seReader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    logger.info(
        "=============== UseCase Generic #1: AID based explicit selection ==================");
    logger.info("= SE Reader  NAME = {}", seReader.getName());

    // Check if a SE is present in the reader
    if (seReader.isSePresent()) {

      logger.info("= #### AID based selection.");

      // Prepare the SE selection
      SeSelection seSelection = new SeSelection();

      // Setting of an AID based selection (in this example a Calypso REV3 PO)
      //
      // Select the first application matching the selection AID whatever the SE communication
      // protocol keep the logical channel open after the selection

      // Generic selection: configures a SeSelector with all the desired attributes to make
      // the selection and read additional information afterwards
      GenericSeSelectionRequest genericSeSelectionRequest =
          new GenericSeSelectionRequest(
              SeSelector.builder()
                  .aidSelector(SeSelector.AidSelector.builder().aidToSelect(seAid).build())
                  .build());

      // Add the selection case to the current selection (we could have added other cases
      // here)
      seSelection.prepareSelection(genericSeSelectionRequest);

      // Actual SE communication: operate through a single request the SE selection
      SelectionsResult selectionsResult = seSelection.processExplicitSelection(seReader);
      if (selectionsResult.hasActiveSelection()) {
        AbstractMatchingSe matchingSe = selectionsResult.getActiveMatchingSe();
        logger.info("The selection of the SE has succeeded.");
        if (matchingSe.hasFci()) {
          String fci = ByteArrayUtil.toHex(matchingSe.getFciBytes());
          logger.info("Application FCI = {}", fci);
        }
        if (matchingSe.hasAtr()) {
          String atr = ByteArrayUtil.toHex(matchingSe.getAtrBytes());
          logger.info("Secure Element ATR = {}", atr);
        }
      } else {
        logger.info("The selection of the application " + seAid + " failed.");
      }

      logger.info("= #### End of the generic SE processing.");
    } else {
      logger.error("The selection of the SE has failed.");
    }
    System.exit(0);
  }
}

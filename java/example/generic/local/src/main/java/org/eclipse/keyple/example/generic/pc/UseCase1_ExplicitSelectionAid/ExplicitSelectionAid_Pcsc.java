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
package org.eclipse.keyple.example.generic.pc.UseCase1_ExplicitSelectionAid;

import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.generic.pc.common.GenericCardSelectionRequest;
import org.eclipse.keyple.example.generic.pc.common.PcscReaderUtilities;
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
 *         <li>Check if a ISO 14443-4 card is in the reader, select a card (here a Calypso PO).
 *         <li><code>
 * Explicit Selection
 * </code> means that it is the terminal application which start the card processing.
 *         <li>card messages:
 *             <ul>
 *               <li>A single card message to select the application in the reader
 *             </ul>
 *       </ul>
 * </ul>
 */
public class ExplicitSelectionAid_Pcsc {
  private static final Logger logger = LoggerFactory.getLogger(ExplicitSelectionAid_Pcsc.class);
  private static final String cardAid = "A000000291A000000191"; /* Here a Hoplink AID */

  public static void main(String[] args) {

    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    // Register the PcscPlugin with SmartCardService, get the corresponding generic Plugin in return
    // This example does not use observation, no exception handler is defined.
    Plugin plugin = smartCardService.registerPlugin(new PcscPluginFactory(null, null));

    // Get and configure the card reader
    Reader reader = plugin.getReader(PcscReaderUtilities.getContactlessReaderName());
    ((PcscReader) reader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    logger.info(
        "=============== UseCase Generic #1: AID based explicit selection ==================");
    logger.info("= Card Reader  NAME = {}", reader.getName());

    // Check if a card is present in the reader
    if (reader.isCardPresent()) {

      logger.info("= #### AID based selection.");

      // Prepare the card selection
      CardSelection cardSelection = new CardSelection();

      // Setting of an AID based selection (in this example a Calypso REV3 PO)
      //
      // Select the first application matching the selection AID whatever the card communication
      // protocol keep the logical channel open after the selection

      // Generic selection: configures a CardSelector with all the desired attributes to make
      // the selection and read additional information afterwards
      GenericCardSelectionRequest genericCardSelectionRequest =
          new GenericCardSelectionRequest(
              CardSelector.builder()
                  .aidSelector(CardSelector.AidSelector.builder().aidToSelect(cardAid).build())
                  .build());

      // Add the selection case to the current selection (we could have added other cases
      // here)
      cardSelection.prepareSelection(genericCardSelectionRequest);

      // Actual card communication: operate through a single request the card selection
      SelectionsResult selectionsResult = cardSelection.processExplicitSelection(reader);
      if (selectionsResult.hasActiveSelection()) {
        AbstractSmartCard smartCard = selectionsResult.getActiveSmartCard();
        logger.info("The selection of the card has succeeded.");
        if (smartCard.hasFci()) {
          String fci = ByteArrayUtil.toHex(smartCard.getFciBytes());
          logger.info("Application FCI = {}", fci);
        }
        if (smartCard.hasAtr()) {
          String atr = ByteArrayUtil.toHex(smartCard.getAtrBytes());
          logger.info("Card ATR = {}", atr);
        }
      } else {
        logger.info("The selection of the application " + cardAid + " failed.");
      }

      logger.info("= #### End of the generic card processing.");
    } else {
      logger.error("The selection of the card has failed.");
    }
    System.exit(0);
  }
}

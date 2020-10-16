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
package org.eclipse.keyple.example.generic.pc.usecase3;

import java.util.Map;
import org.eclipse.keyple.core.selection.AbstractSmartCard;
import org.eclipse.keyple.core.selection.CardSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.CardSelector;
import org.eclipse.keyple.core.seproxy.MultiSelectionProcessing;
import org.eclipse.keyple.core.seproxy.Plugin;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.SmartCardService;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.example.common.generic.GenericCardSelectionRequest;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The UseCase_Generic3_GroupedMultiSelection_Pcsc class illustrates the use of the select next
 * mechanism
 */
public class GroupedMultiSelection_Pcsc {
  private static final Logger logger = LoggerFactory.getLogger(GroupedMultiSelection_Pcsc.class);

  public static void main(String[] args) {

    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    // Register the PcscPlugin with SmartCardService, get the corresponding generic Plugin in
    // return
    Plugin plugin = smartCardService.registerPlugin(new PcscPluginFactory());

    // Get and configure the PO reader
    Reader reader = plugin.getReader(ReaderUtilities.getContactlessReaderName());
    ((PcscReader) reader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    logger.info(
        "=============== UseCase Generic #3: AID based grouped explicit multiple selection ==================");
    logger.info("= Card Reader  NAME = {}", reader.getName());

    // Check if a card is present in the reader
    if (reader.isCardPresent()) {

      CardSelection cardSelection = new CardSelection(MultiSelectionProcessing.PROCESS_ALL);

      // operate the card selection (change the AID here to adapt it to the card used for the test)
      String cardAidPrefix = "A000000404012509";

      // AID based selection (1st selection, later indexed 0)
      cardSelection.prepareSelection(
          new GenericCardSelectionRequest(
              CardSelector.builder()
                  .aidSelector(
                      CardSelector.AidSelector.builder()
                          .aidToSelect(cardAidPrefix)
                          .fileOccurrence(CardSelector.AidSelector.FileOccurrence.FIRST)
                          .fileControlInformation(
                              CardSelector.AidSelector.FileControlInformation.FCI)
                          .build())
                  .build()));

      // next selection (2nd selection, later indexed 1)
      cardSelection.prepareSelection(
          new GenericCardSelectionRequest(
              CardSelector.builder()
                  .aidSelector(
                      CardSelector.AidSelector.builder()
                          .aidToSelect(cardAidPrefix)
                          .fileOccurrence(CardSelector.AidSelector.FileOccurrence.NEXT)
                          .fileControlInformation(
                              CardSelector.AidSelector.FileControlInformation.FCI)
                          .build())
                  .build()));

      // next selection (3rd selection, later indexed 2)
      cardSelection.prepareSelection(
          new GenericCardSelectionRequest(
              CardSelector.builder()
                  .aidSelector(
                      CardSelector.AidSelector.builder()
                          .aidToSelect(cardAidPrefix)
                          .fileOccurrence(CardSelector.AidSelector.FileOccurrence.NEXT)
                          .fileControlInformation(
                              CardSelector.AidSelector.FileControlInformation.FCI)
                          .build())
                  .build()));

      // close the channel after the selection to force the selection of all applications
      cardSelection.prepareReleaseChannel();

      // Actual card communication: operate through a single request the card selection
      SelectionsResult selectionsResult = cardSelection.processExplicitSelection(reader);

      if (selectionsResult.getSmartCards().size() > 0) {
        for (Map.Entry<Integer, AbstractSmartCard> entry :
            selectionsResult.getSmartCards().entrySet()) {
          AbstractSmartCard smartCard = entry.getValue();
          String atr = smartCard.hasAtr() ? ByteArrayUtil.toHex(smartCard.getAtrBytes()) : "no ATR";
          String fci = smartCard.hasFci() ? ByteArrayUtil.toHex(smartCard.getFciBytes()) : "no FCI";
          logger.info(
              "Selection status for selection (indexed {}): \n\t\tATR: {}\n\t\tFCI: {}",
              entry.getKey(),
              atr,
              fci);
        }
      } else {
        logger.error("No cards matched the selection.");
      }
    } else {
      logger.error("No cards were detected.");
    }
    System.exit(0);
  }
}

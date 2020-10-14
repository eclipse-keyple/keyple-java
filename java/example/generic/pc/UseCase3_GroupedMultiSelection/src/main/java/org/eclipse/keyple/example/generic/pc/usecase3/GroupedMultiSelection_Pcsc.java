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
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.CardSelector;
import org.eclipse.keyple.core.seproxy.MultiSelectionProcessing;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SmartCardService;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.example.common.generic.GenericSeSelectionRequest;
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

    // Register the PcscPlugin with SmartCardService, get the corresponding generic ReaderPlugin in
    // return
    ReaderPlugin readerPlugin = smartCardService.registerPlugin(new PcscPluginFactory());

    // Get and configure the PO reader
    Reader reader = readerPlugin.getReader(ReaderUtilities.getContactlessReaderName());
    ((PcscReader) reader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    logger.info(
        "=============== UseCase Generic #3: AID based grouped explicit multiple selection ==================");
    logger.info("= Card Reader  NAME = {}", reader.getName());

    // Check if a card is present in the reader
    if (reader.isSePresent()) {

      SeSelection seSelection = new SeSelection(MultiSelectionProcessing.PROCESS_ALL);

      // operate the card selection (change the AID here to adapt it to the card used for the test)
      String seAidPrefix = "A000000404012509";

      // AID based selection (1st selection, later indexed 0)
      seSelection.prepareSelection(
          new GenericSeSelectionRequest(
              CardSelector.builder()
                  .aidSelector(
                      CardSelector.AidSelector.builder()
                          .aidToSelect(seAidPrefix)
                          .fileOccurrence(CardSelector.AidSelector.FileOccurrence.FIRST)
                          .fileControlInformation(
                              CardSelector.AidSelector.FileControlInformation.FCI)
                          .build())
                  .build()));

      // next selection (2nd selection, later indexed 1)
      seSelection.prepareSelection(
          new GenericSeSelectionRequest(
              CardSelector.builder()
                  .aidSelector(
                      CardSelector.AidSelector.builder()
                          .aidToSelect(seAidPrefix)
                          .fileOccurrence(CardSelector.AidSelector.FileOccurrence.NEXT)
                          .fileControlInformation(
                              CardSelector.AidSelector.FileControlInformation.FCI)
                          .build())
                  .build()));

      // next selection (3rd selection, later indexed 2)
      seSelection.prepareSelection(
          new GenericSeSelectionRequest(
              CardSelector.builder()
                  .aidSelector(
                      CardSelector.AidSelector.builder()
                          .aidToSelect(seAidPrefix)
                          .fileOccurrence(CardSelector.AidSelector.FileOccurrence.NEXT)
                          .fileControlInformation(
                              CardSelector.AidSelector.FileControlInformation.FCI)
                          .build())
                  .build()));

      // close the channel after the selection to force the selection of all applications
      seSelection.prepareReleaseSeChannel();

      // Actual card communication: operate through a single request the card selection
      SelectionsResult selectionsResult = seSelection.processExplicitSelection(reader);

      if (selectionsResult.getMatchingSelections().size() > 0) {
        for (Map.Entry<Integer, AbstractMatchingSe> entry :
            selectionsResult.getMatchingSelections().entrySet()) {
          AbstractMatchingSe matchingSe = entry.getValue();
          String atr =
              matchingSe.hasAtr() ? ByteArrayUtil.toHex(matchingSe.getAtrBytes()) : "no ATR";
          String fci =
              matchingSe.hasFci() ? ByteArrayUtil.toHex(matchingSe.getFciBytes()) : "no FCI";
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

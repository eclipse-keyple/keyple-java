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
package org.eclipse.keyple.example.generic.pc.usecase4;

import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.CardSelector;
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
public class SequentialMultiSelection_Pcsc {
  private static final Logger logger = LoggerFactory.getLogger(SequentialMultiSelection_Pcsc.class);

  private static void doAndAnalyseSelection(Reader reader, SeSelection seSelection, int index) {
    SelectionsResult selectionsResult = seSelection.processExplicitSelection(reader);
    if (selectionsResult.hasActiveSelection()) {
      AbstractMatchingSe matchingSe = selectionsResult.getActiveMatchingSe();
      logger.info("The card matched the selection {}.", index);
      String atr = matchingSe.hasAtr() ? ByteArrayUtil.toHex(matchingSe.getAtrBytes()) : "no ATR";
      String fci = matchingSe.hasFci() ? ByteArrayUtil.toHex(matchingSe.getFciBytes()) : "no FCI";
      logger.info("Selection status for case {}: \n\t\tATR: {}\n\t\tFCI: {}", index, atr, fci);
    } else {
      logger.info("The selection did not match for case {}.", index);
    }
  }

  public static void main(String[] args) {

    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    // Register the PcscPlugin with SmartCardService, get the corresponding generic ReaderPlugin in
    // return
    ReaderPlugin readerPlugin = smartCardService.registerPlugin(new PcscPluginFactory());

    // Get and configure the PO reader
    Reader reader = readerPlugin.getReader(ReaderUtilities.getContactlessReaderName());
    ((PcscReader) reader).setContactless(true);
    ((PcscReader) reader).setIsoProtocol(PcscReader.IsoProtocol.T1);

    logger.info(
        "=============== UseCase Generic #4: AID based sequential explicit multiple selection "
            + "==================");
    logger.info("= Card reader  NAME = {}", reader.getName());

    // Check if a card is present in the reader
    if (reader.isSePresent()) {

      SeSelection seSelection;

      // operate card AID selection (change the AID prefix here to adapt it to the card used for
      // the test [the card should have at least two applications matching the AID prefix])
      String seAidPrefix = "315449432E494341";

      // First selection case
      seSelection = new SeSelection();

      // AID based selection: get the first application occurrence matching the AID, keep the
      // physical channel open
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

      // Do the selection and display the result
      doAndAnalyseSelection(reader, seSelection, 1);

      // New selection: get the next application occurrence matching the same AID, close the
      // physical channel after
      seSelection = new SeSelection();

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

      // close the channel after the selection
      seSelection.prepareReleaseSeChannel();

      // Do the selection and display the result
      doAndAnalyseSelection(reader, seSelection, 2);

    } else {

      logger.error("No cards were detected.");
    }
    System.exit(0);
  }
}

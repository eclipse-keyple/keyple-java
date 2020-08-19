/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.generic.pc.usecase4;


import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.example.common.generic.GenericSeSelectionRequest;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The UseCase_Generic3_GroupedMultiSelection_Pcsc class illustrates the use of the select next
 * mechanism
 */
public class SequentialMultiSelection_Pcsc {
    private static final Logger logger =
            LoggerFactory.getLogger(SequentialMultiSelection_Pcsc.class);

    private static void doAndAnalyseSelection(SeReader seReader, SeSelection seSelection,
            int index) {
        SelectionsResult selectionsResult = seSelection.processExplicitSelection(seReader);
        if (selectionsResult.hasActiveSelection()) {
            AbstractMatchingSe matchingSe = selectionsResult.getActiveMatchingSe();
            logger.info("The SE matched the selection {}.", index);
            String atr =
                    matchingSe.hasAtr() ? ByteArrayUtil.toHex(matchingSe.getAtrBytes()) : "no ATR";
            String fci =
                    matchingSe.hasFci() ? ByteArrayUtil.toHex(matchingSe.getFciBytes()) : "no FCI";
            logger.info("Selection status for case {}: \n\t\tATR: {}\n\t\tFCI: {}", index, atr,
                    fci);
        } else {
            logger.info("The selection did not match for case {}.", index);
        }
    }

    public static void main(String[] args) {

        // Get the instance of the SeProxyService (Singleton pattern)
        SeProxyService seProxyService = SeProxyService.getInstance();

        // Assign PcscPlugin to the SeProxyService
        seProxyService.registerPlugin(new PcscPluginFactory());

        // Get a SE reader ready to work with generic SE. Use the getReader helper method from the
        // ReaderUtilities class.
        SeReader seReader = ReaderUtilities.getDefaultContactLessSeReader();

        logger.info(
                "=============== UseCase Generic #4: AID based sequential explicit multiple selection "
                        + "==================");
        logger.info("= SE Reader  NAME = {}", seReader.getName());

        // Check if a SE is present in the reader
        if (seReader.isSePresent()) {

            SeSelection seSelection;

            // operate SE AID selection (change the AID prefix here to adapt it to the SE used for
            // the test [the SE should have at least two applications matching the AID prefix])
            String seAidPrefix = "315449432E494341";

            // First selection case
            seSelection = new SeSelection();

            // AID based selection: get the first application occurrence matching the AID, keep the
            // physical channel open
            seSelection.prepareSelection(new GenericSeSelectionRequest(SeSelector.builder()
                    .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                    .aidSelector(SeSelector.AidSelector.builder().aidToSelect(seAidPrefix)
                            .fileOccurrence(SeSelector.AidSelector.FileOccurrence.FIRST)
                            .fileControlInformation(
                                    SeSelector.AidSelector.FileControlInformation.FCI)
                            .build())
                    .build()));

            // Do the selection and display the result
            doAndAnalyseSelection(seReader, seSelection, 1);

            // New selection: get the next application occurrence matching the same AID, close the
            // physical channel after
            seSelection = new SeSelection();

            seSelection.prepareSelection(new GenericSeSelectionRequest(SeSelector.builder()
                    .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                    .aidSelector(SeSelector.AidSelector.builder().aidToSelect(seAidPrefix)
                            .fileOccurrence(SeSelector.AidSelector.FileOccurrence.NEXT)
                            .fileControlInformation(
                                    SeSelector.AidSelector.FileControlInformation.FCI)
                            .build())
                    .build()));

            // close the channel after the selection
            seSelection.prepareReleaseSeChannel();

            // Do the selection and display the result
            doAndAnalyseSelection(seReader, seSelection, 2);

        } else {

            logger.error("No SE were detected.");
        }
        System.exit(0);
    }
}

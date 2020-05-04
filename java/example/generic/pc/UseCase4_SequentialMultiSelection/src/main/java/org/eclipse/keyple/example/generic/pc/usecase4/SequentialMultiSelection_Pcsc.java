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

import java.io.IOException;
import org.eclipse.keyple.core.selection.*;
import org.eclipse.keyple.core.seproxy.*;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
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
    protected static final Logger logger =
            LoggerFactory.getLogger(SequentialMultiSelection_Pcsc.class);

    private static void doAndAnalyseSelection(SeReader seReader, SeSelection seSelection, int index)
            throws KeypleException {
        SelectionsResult selectionsResult = seSelection.processExplicitSelection(seReader);
        if (selectionsResult.hasActiveSelection()) {
            AbstractMatchingSe matchingSe = selectionsResult.getActiveMatchingSe();
            logger.info("The SE matched the selection {}.", index);
            logger.info("Selection status for case {}: \n\t\tATR: {}\n\t\tFCI: {}", index,
                    ByteArrayUtil.toHex(matchingSe.getSelectionStatus().getAtr().getBytes()),
                    ByteArrayUtil.toHex(matchingSe.getSelectionStatus().getFci().getDataOut()));
        } else {
            logger.info("The selection did not match for case {}.", index);
        }
    }

    public static void main(String[] args)
            throws KeypleException, InterruptedException, IOException {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.registerPlugin(new PcscPluginFactory());

        /*
         * Get a SE reader ready to work with generic SE. Use the getReader helper method from the
         * ReaderUtilities class.
         */
        SeReader seReader = ReaderUtilities.getDefaultContactLessSeReader();

        /* Check if the reader exists */
        if (seReader == null) {
            throw new IllegalStateException("Bad SE reader setup");
        }

        logger.info(
                "=============== UseCase Generic #4: AID based sequential explicit multiple selection "
                        + "==================");
        logger.info("= SE Reader  NAME = {}", seReader.getName());

        AbstractMatchingSe matchingSe;

        /* Check if a SE is present in the reader */
        if (seReader.isSePresent()) {

            SeSelection seSelection;

            /*
             * operate SE AID selection (change the AID prefix here to adapt it to the SE used for
             * the test [the SE should have at least two applications matching the AID prefix])
             */
            String seAidPrefix = "315449432E494341";

            /* First selection case */
            seSelection =
                    new SeSelection(MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN);

            /*
             * AID based selection: get the first application occurrence matching the AID, keep the
             * physical channel open
             */
            seSelection.prepareSelection(new GenericSeSelectionRequest(new SeSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                    new SeSelector.AidSelector(
                            new SeSelector.AidSelector.IsoAid(ByteArrayUtil.fromHex(seAidPrefix)),
                            null, SeSelector.AidSelector.FileOccurrence.FIRST,
                            SeSelector.AidSelector.FileControlInformation.FCI))));

            /* Do the selection and display the result */
            doAndAnalyseSelection(seReader, seSelection, 1);

            /*
             * New selection: get the next application occurrence matching the same AID, close the
             * physical channel after
             */
            seSelection = new SeSelection(MultiSeRequestProcessing.FIRST_MATCH,
                    ChannelControl.CLOSE_AFTER);

            seSelection.prepareSelection(new GenericSeSelectionRequest(new SeSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                    new SeSelector.AidSelector(
                            new SeSelector.AidSelector.IsoAid(ByteArrayUtil.fromHex(seAidPrefix)),
                            null, SeSelector.AidSelector.FileOccurrence.NEXT,
                            SeSelector.AidSelector.FileControlInformation.FCI))));

            /* Do the selection and display the result */
            doAndAnalyseSelection(seReader, seSelection, 2);

        } else {
            logger.error("No SE were detected.");
        }
        System.exit(0);
    }
}

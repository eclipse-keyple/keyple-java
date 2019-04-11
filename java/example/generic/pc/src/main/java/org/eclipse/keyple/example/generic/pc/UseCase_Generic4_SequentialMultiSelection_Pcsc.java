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
package org.eclipse.keyple.example.generic.pc;

import java.io.IOException;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.transaction.*;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The UseCase_Generic3_GroupedMultiSelection_Pcsc class illustrates the use of the select next
 * mechanism
 */
public class UseCase_Generic4_SequentialMultiSelection_Pcsc {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_Generic1_ExplicitSelectionAid_Pcsc.class);

    private static void doAndAnalyseSelection(SeReader seReader, SeSelection seSelection, int index)
            throws KeypleReaderException {
        MatchingSe matchingSe = seSelection.processExplicitSelection(seReader)
                .getMatchingSelection(index).getMatchingSe();
        if (matchingSe != null && matchingSe.getSelectionStatus().hasMatched()) {
            logger.info("The SE matched the selection {}.", index);

            logger.info("Selection status for case {}: \n\t\tATR: {}\n\t\tFCI: {}", index,
                    ByteArrayUtils.toHex(matchingSe.getSelectionStatus().getAtr().getBytes()),
                    ByteArrayUtils.toHex(matchingSe.getSelectionStatus().getFci().getDataOut()));
        } else {
            logger.info("The selection did not match for case {}.", index);
        }
    }

    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        /*
         * Get a SE reader ready to work with generic SE. Use the getReader helper method from the
         * ReaderUtilities class.
         */
        SeReader seReader = ReaderUtilities.getDefaultContactLessSeReader(seProxyService);

        /* Check if the reader exists */
        if (seReader == null) {
            throw new IllegalStateException("Bad SE reader setup");
        }

        logger.info(
                "=============== UseCase Generic #4: AID based sequential explicit multiple selection "
                        + "==================");
        logger.info("= SE Reader  NAME = {}", seReader.getName());

        MatchingSe matchingSe;

        /* Check if a SE is present in the reader */
        if (seReader.isSePresent()) {

            SeSelection seSelection;

            seSelection = new SeSelection();

            /* operate SE selection (change the AID here to adapt it to the SE used for the test) */
            String seAidPrefix = "A000000404012509";

            /* AID based selection */
            seSelection.prepareSelection(new SeSelectionRequest(
                    new SeSelector(
                            new SeSelector.AidSelector(ByteArrayUtils.fromHex(seAidPrefix), null,
                                    SeSelector.AidSelector.FileOccurrence.FIRST,
                                    SeSelector.AidSelector.FileControlInformation.FCI),
                            null, "Initial selection #1"),
                    ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4));

            seSelection = new SeSelection();

            doAndAnalyseSelection(seReader, seSelection, 1);

            /* next selection */
            seSelection.prepareSelection(new SeSelectionRequest(
                    new SeSelector(
                            new SeSelector.AidSelector(ByteArrayUtils.fromHex(seAidPrefix), null,
                                    SeSelector.AidSelector.FileOccurrence.NEXT,
                                    SeSelector.AidSelector.FileControlInformation.FCI),
                            null, "Next selection #2"),
                    ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4));

            seSelection = new SeSelection();

            doAndAnalyseSelection(seReader, seSelection, 2);

            /* next selection */
            seSelection.prepareSelection(new SeSelectionRequest(
                    new SeSelector(
                            new SeSelector.AidSelector(ByteArrayUtils.fromHex(seAidPrefix), null,
                                    SeSelector.AidSelector.FileOccurrence.NEXT,
                                    SeSelector.AidSelector.FileControlInformation.FCI),
                            null, "Next selection #3"),
                    ChannelState.CLOSE_AFTER, ContactlessProtocols.PROTOCOL_ISO14443_4));

            doAndAnalyseSelection(seReader, seSelection, 3);

        } else {
            logger.error("No SE were detected.");
        }
        System.exit(0);
    }
}

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
import org.eclipse.keyple.core.selection.*;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.generic.common.GenericSeSelectionRequest;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The UseCase_Generic3_GroupedMultiSelection_Pcsc class illustrates the use of the select next
 * mechanism
 */
public class UseCase_Generic3_GroupedMultiSelection_Pcsc {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_Generic1_ExplicitSelectionAid_Pcsc.class);

    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance and assign the PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(PcscPluginFactory.getInstance().getPluginInstance());

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
                "=============== UseCase Generic #3: AID based grouped explicit multiple selection ==================");
        logger.info("= SE Reader  NAME = {}", seReader.getName());

        /* Check if a SE is present in the reader */
        if (seReader.isSePresent()) {

            /* CLOSE_AFTER pour assurer la sélection de toutes les applications */
            SeSelection seSelection = new SeSelection();

            /* operate SE selection (change the AID here to adapt it to the SE used for the test) */
            String seAidPrefix = "A000000404012509";

            /* AID based selection (1st selection, later indexed 0) */
            seSelection.prepareSelection(new GenericSeSelectionRequest(new SeSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                    new SeSelector.AidSelector(new SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                            SeSelector.AidSelector.FileOccurrence.FIRST,
                            SeSelector.AidSelector.FileControlInformation.FCI),
                    "Initial selection #1"), ChannelState.CLOSE_AFTER));

            /* next selection (2nd selection, later indexed 1) */
            seSelection.prepareSelection(new GenericSeSelectionRequest(new SeSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                    new SeSelector.AidSelector(new SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                            SeSelector.AidSelector.FileOccurrence.NEXT,
                            SeSelector.AidSelector.FileControlInformation.FCI),
                    "Next selection #2"), ChannelState.CLOSE_AFTER));

            /* next selection (3rd selection, later indexed 2) */
            seSelection.prepareSelection(new GenericSeSelectionRequest(new SeSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                    new SeSelector.AidSelector(new SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                            SeSelector.AidSelector.FileOccurrence.NEXT,
                            SeSelector.AidSelector.FileControlInformation.FCI),
                    "Next selection #3"), ChannelState.CLOSE_AFTER));
            /*
             * Actual SE communication: operate through a single request the SE selection
             */

            SelectionsResult selectionsResult = seSelection.processExplicitSelection(seReader);

            if (selectionsResult.getMatchingSelections().size() > 0) {
                for (MatchingSelection matchingSelection : selectionsResult
                        .getMatchingSelections()) {
                    AbstractMatchingSe matchingSe = matchingSelection.getMatchingSe();
                    logger.info(
                            "Selection status for selection \"{}\" (indexed {}): \n\t\tATR: {}\n\t\tFCI: {}",
                            matchingSelection.getExtraInfo(), matchingSelection.getSelectionIndex(),
                            ByteArrayUtil
                                    .toHex(matchingSe.getSelectionStatus().getAtr().getBytes()),
                            ByteArrayUtil
                                    .toHex(matchingSe.getSelectionStatus().getFci().getDataOut()));
                }
            } else {
                logger.error("No SE matched the selection.");
            }
        } else {
            logger.error("No SE were detected.");
        }
        System.exit(0);
    }
}

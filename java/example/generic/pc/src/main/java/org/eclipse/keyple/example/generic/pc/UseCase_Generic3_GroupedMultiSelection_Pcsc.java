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
public class UseCase_Generic3_GroupedMultiSelection_Pcsc {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_Generic1_ExplicitSelectionAid_Pcsc.class);

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
                "=============== UseCase Generic #3: AID based grouped explicit multiple selection ==================");
        logger.info("= SE Reader  NAME = {}", seReader.getName());

        MatchingSe matchingSeTable[] = new MatchingSe[3];

        /* Check if a SE is present in the reader */
        if (seReader.isSePresent()) {

            /* CLOSE_AFTER pour assurer la sélection de toutes les applications */
            SeSelection seSelection = new SeSelection(seReader);

            /* operate SE selection (change the AID here to adapt it to the SE used for the test) */
            String seAidPrefix = "A000000404012509";

            /* AID based selection */
            matchingSeTable[0] =
                    seSelection
                            .prepareSelection(new SeSelectionRequest(
                                    new SeSelector(
                                            new SeSelector.AidSelector(
                                                    ByteArrayUtils.fromHex(seAidPrefix), null),
                                            null, "Initial selection #1"),
                                    ChannelState.CLOSE_AFTER,
                                    ContactlessProtocols.PROTOCOL_ISO14443_4));
            /* next selection */
            matchingSeTable[1] =
                    seSelection
                            .prepareSelection(new SeSelectionRequest(
                                    new SeSelector(
                                            new SeSelector.AidSelector(
                                                    ByteArrayUtils.fromHex(seAidPrefix), null),
                                            null, "Next selection #2"),
                                    ChannelState.CLOSE_AFTER,
                                    ContactlessProtocols.PROTOCOL_ISO14443_4));
            /* next selection */
            matchingSeTable[2] =
                    seSelection
                            .prepareSelection(new SeSelectionRequest(
                                    new SeSelector(
                                            new SeSelector.AidSelector(
                                                    ByteArrayUtils.fromHex(seAidPrefix), null),
                                            null, "Next selection #3"),
                                    ChannelState.CLOSE_AFTER,
                                    ContactlessProtocols.PROTOCOL_ISO14443_4));
            /*
             * Actual SE communication: operate through a single request the SE selection
             */
            if (seSelection.processExplicitSelection()) {

                int matchedSelection = 0;
                /* Count the number of SE that matched the selection */
                for (int i = 0; i < matchingSeTable.length; i++) {
                    if (matchingSeTable[i].getSelectionSeResponse() != null) {
                        matchedSelection++;
                    }
                }
                logger.info("The SE matched {} time(s) the selection.", matchedSelection);

                for (int i = 0; i < matchingSeTable.length; i++) {

                    if (matchingSeTable[i].getSelectionSeResponse() != null) {
                        logger.info("Selection status for case {}: \n\t\tATR: {}\n\t\tFCI: {}",
                                i + 1,
                                ByteArrayUtils.toHex(matchingSeTable[i].getSelectionSeResponse()
                                        .getSelectionStatus().getAtr().getBytes()),
                                ByteArrayUtils.toHex(matchingSeTable[i].getSelectionSeResponse()
                                        .getSelectionStatus().getFci().getDataOut()));
                    }
                }
            } else {
                logger.info("The selection process did not return any selected SE.");
            }
        } else {
            logger.error("No SE were detected.");
        }
        System.exit(0);
    }
}

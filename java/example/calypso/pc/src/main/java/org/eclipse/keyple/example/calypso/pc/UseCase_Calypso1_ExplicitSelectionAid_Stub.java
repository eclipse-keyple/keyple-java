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
package org.eclipse.keyple.example.calypso.pc;


import java.io.IOException;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.example.calypso.common.stub.se.StubCalypsoClassic;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubProtocolSetting;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.transaction.MatchingSelection;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SelectionsResult;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Use Case ‘Calypso 1’ – Explicit Selection Aid (Stub)</h1>
 * <ul>
 * <li>
 * <h2>Scenario:</h2>
 * <ul>
 * <li>Check if a ISO 14443-4 SE is in the reader, select a Calypso PO, operate a simple Calypso PO
 * transaction (simple plain read, not involving a Calypso SAM).</li>
 * <li><code>
 Explicit Selection
 </code> means that it is the terminal application which start the SE processing.</li>
 * <li>PO messages:
 * <ul>
 * <li>A first SE message to select the application in the reader</li>
 * <li>A second SE message to operate the simple Calypso transaction</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class UseCase_Calypso1_ExplicitSelectionAid_Stub {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_Calypso1_ExplicitSelectionAid_Stub.class);

    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the Stub plugin */
        StubPlugin stubPlugin = StubPlugin.getInstance();

        /* Assign StubPlugin to the SeProxyService */
        seProxyService.addPlugin(stubPlugin);

        /* Plug the PO stub reader. */
        stubPlugin.plugStubReader("poReader", true);

        /*
         * Get a PO reader ready to work with Calypso PO.
         */
        StubReader poReader = (StubReader) (stubPlugin.getReader("poReader"));

        /* Check if the reader exists */
        if (poReader == null) {
            throw new IllegalStateException("Bad PO reader setup");
        }

        poReader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        /* Create 'virtual' Calypso PO */
        StubSecureElement calypsoStubSe = new StubCalypsoClassic();

        logger.info("Insert stub PO.");
        poReader.insertSe(calypsoStubSe);

        logger.info(
                "=============== UseCase Calypso #1: AID based explicit selection ==================");
        logger.info("= PO Reader  NAME = {}", poReader.getName());

        /* Check if a PO is present in the reader */
        if (poReader.isSePresent()) {

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= 1st PO exchange: AID based selection with reading of Environment file.         =");
            logger.info(
                    "==================================================================================");

            /*
             * Prepare a Calypso PO selection
             */
            SeSelection seSelection = new SeSelection();

            /*
             * Setting of an AID based selection of a Calypso REV3 PO
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */

            /*
             * Calypso selection: configures a PoSelectionRequest with all the desired attributes to
             * make the selection and read additional information afterwards
             */
            PoSelectionRequest poSelectionRequest = new PoSelectionRequest(new PoSelector(
                    new PoSelector.PoAidSelector(ByteArrayUtils.fromHex(CalypsoClassicInfo.AID),
                            PoSelector.InvalidatedPo.REJECT),
                    null, "AID: " + CalypsoClassicInfo.AID), ChannelState.KEEP_OPEN,
                    ContactlessProtocols.PROTOCOL_ISO14443_4);

            /*
             * Prepare the reading order and keep the associated parser for later use once the
             * selection has been made.
             */
            int readEnvironmentParserIndex = poSelectionRequest.prepareReadRecordsCmd(
                    CalypsoClassicInfo.SFI_EnvironmentAndHolder,
                    ReadDataStructure.SINGLE_RECORD_DATA, CalypsoClassicInfo.RECORD_NUMBER_1,
                    String.format("EnvironmentAndHolder (SFI=%02X))",
                            CalypsoClassicInfo.SFI_EnvironmentAndHolder));

            /*
             * Add the selection case to the current selection (we could have added other cases
             * here)
             */
            seSelection.prepareSelection(poSelectionRequest);

            /*
             * Actual PO communication: operate through a single request the Calypso PO selection
             * and the file read
             */

            SelectionsResult selectionsResult = seSelection.processExplicitSelection(poReader);

            if (selectionsResult.hasActiveSelection()) {
                MatchingSelection matchingSelection = selectionsResult.getActiveSelection();

                CalypsoPo calypsoPo = (CalypsoPo) matchingSelection.getMatchingSe();
                logger.info("The selection of the PO has succeeded.");

                /* Retrieve the parser and the data read from the selection processed */
                ReadRecordsRespPars readEnvironmentParser = (ReadRecordsRespPars) matchingSelection
                        .getResponseParser(readEnvironmentParserIndex);

                byte environmentAndHolder[] = (readEnvironmentParser.getRecords())
                        .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                /* Log the result */
                logger.info("Environment file data: {}",
                        ByteArrayUtils.toHex(environmentAndHolder));

                /* Go on with the reading of the first record of the EventLog file */
                logger.info(
                        "==================================================================================");
                logger.info(
                        "= 2nd PO exchange: reading transaction of the EventLog file.                     =");
                logger.info(
                        "==================================================================================");

                PoTransaction poTransaction = new PoTransaction(poReader, calypsoPo);

                /*
                 * Prepare the reading order and keep the associated parser for later use once the
                 * transaction has been processed.
                 */
                int readEventLogParserIndex = poTransaction.prepareReadRecordsCmd(
                        CalypsoClassicInfo.SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA,
                        CalypsoClassicInfo.RECORD_NUMBER_1,
                        String.format("EventLog (SFI=%02X, recnbr=%d))",
                                CalypsoClassicInfo.SFI_EventLog,
                                CalypsoClassicInfo.RECORD_NUMBER_1));

                /*
                 * Actual PO communication: send the prepared read order, then close the channel
                 * with the PO
                 */
                if (poTransaction.processPoCommands(ChannelState.CLOSE_AFTER)) {
                    logger.info("The reading of the EventLog has succeeded.");

                    /*
                     * Retrieve the data read from the parser updated during the transaction process
                     */
                    byte eventLog[] = (((ReadRecordsRespPars) poTransaction
                            .getResponseParser(readEventLogParserIndex)).getRecords())
                                    .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                    /* Log the result */
                    logger.info("EventLog file data: {}", ByteArrayUtils.toHex(eventLog));
                }
                logger.info(
                        "==================================================================================");
                logger.info(
                        "= End of the Calypso PO processing.                                              =");
                logger.info(
                        "==================================================================================");
            } else {
                logger.error("The selection of the PO has failed.");
            }
        } else {
            logger.error("No PO were detected.");
        }

        logger.info("Remove stub PO.");
        poReader.removeSe();

        System.exit(0);
    }
}

/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.integration.tools.calypso;

import static org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild.SelectControl.CURRENT_DF;
import static org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild.SelectControl.FIRST;
import static org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild.SelectControl.NEXT;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.calypso.transaction.exception.KeypleCalypsoSecureSessionException;
import org.eclipse.keyple.integration.example.pc.calypso.DemoUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SelectionsResult;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tool_AnalyzePoFileStructure {

    private static final Logger logger = LoggerFactory.getLogger(Tool_AnalyzePoFileStructure.class);

    // private PoStructure poStructure = null;

    private static String getEfTypeName(int inEfType) {

        switch (inEfType) {
            case SelectFileRespPars.EF_TYPE_BINARY: {
                return "Bin ";
            }

            case SelectFileRespPars.EF_TYPE_LINEAR: {
                return "Lin ";
            }

            case SelectFileRespPars.EF_TYPE_CYCLIC: {
                return "Cycl";
            }

            case SelectFileRespPars.EF_TYPE_SIMULATED_COUNTERS: {
                return "SimC";
            }

            case SelectFileRespPars.EF_TYPE_COUNTERS: {
                return "Cnt ";
            }
        }

        return "--";
    }

    private static String getAcName(byte inAcValue, byte inKeyLevel) {

        switch (inAcValue) {

            case 0x1F: {
                return "AA";
            }

            case 0x00: {
                return "NN";
            }

            case 0x10: {
                return "S" + inKeyLevel;
            }

            case 0x01: {
                return "PN";
            }
        }

        return "--";
    }

    private static void printFileInformation(SelectFileRespPars inFileInformation) {


        logger.info("{}",
                String.format("|%04X | %s | %02X  | %2d | %4d | %s | %s | %s | %s |",
                        inFileInformation.getLid(), getEfTypeName(inFileInformation.getEfType()),
                        inFileInformation.getSfi(), inFileInformation.getNumRec(),
                        inFileInformation.getRecSize(),
                        getAcName(inFileInformation.getAccessConditions()[0],
                                inFileInformation.getKeyIndexes()[0]),
                        getAcName(inFileInformation.getAccessConditions()[1],
                                inFileInformation.getKeyIndexes()[1]),
                        getAcName(inFileInformation.getAccessConditions()[2],
                                inFileInformation.getKeyIndexes()[2]),
                        getAcName(inFileInformation.getAccessConditions()[3],
                                inFileInformation.getKeyIndexes()[3])));
    }

    private static void printApplicationInformation(SeReader poReader, CalypsoPo curApp) {

        try {
            PoTransaction poTransaction = new PoTransaction(poReader, curApp);

            int selectCurrentDfIndex = poTransaction.prepareSelectFileCmd(CURRENT_DF, "CurrentDF");

            poTransaction.processPoCommands(ChannelState.KEEP_OPEN);

            SelectFileRespPars selectCurrentDf =
                    (SelectFileRespPars) poTransaction.getResponseParser(selectCurrentDfIndex);

            if (!selectCurrentDf.isSelectionSuccessful()) {
                return;
            }
            logger.info(
                    "===================================================================================");
            logger.info(
                    "| AID                             | LID  | KVC1 | KVC2 | KVC3 | G0 | G1 | G2 | G3 |");
            logger.info("{}",
                    String.format("|%32s | %04X | %02X | %02X| %02X",
                            ByteArrayUtils.toHex(curApp.getDfName()), selectCurrentDf.getLid(),
                            selectCurrentDf.getKvcInfo()[0], selectCurrentDf.getKvcInfo()[1],
                            selectCurrentDf.getKvcInfo()[2]));


            logger.info(
                    "===================================================================================");
        } catch (KeypleCalypsoSecureSessionException e) {
            // SW1SW2 is in:: e.getResponses().get(0).getStatusCode();
        } catch (Exception e) {
            logger.error("Exception: " + e.getCause());
        }
    }


    protected static void getApplicationFileData(SeReader poReader, CalypsoPo curApp) {

        try {
            PoTransaction poTransaction = new PoTransaction(poReader, curApp);
            int currentFile;

            int selectFileParserFirstIndex = poTransaction.prepareSelectFileCmd(FIRST, "First EF");

            poTransaction.processPoCommands(ChannelState.KEEP_OPEN);

            SelectFileRespPars selectFileParserFirst = (SelectFileRespPars) poTransaction
                    .getResponseParser(selectFileParserFirstIndex);
            if (!selectFileParserFirst.isSelectionSuccessful()) {
                return;
            }

            logger.info("|LID  | Type | SID | #R | Size | G0 | G1 | G2 | G3 |");
            logger.info("----------------------------------------------------");

            printFileInformation(selectFileParserFirst);
            currentFile = selectFileParserFirst.getLid();

            int selectFileParserNextIndex = poTransaction.prepareSelectFileCmd(NEXT, "Next EF");
            poTransaction.processPoCommands(ChannelState.KEEP_OPEN);

            SelectFileRespPars selectFileParserNext =
                    (SelectFileRespPars) poTransaction.getResponseParser(selectFileParserNextIndex);
            if (!selectFileParserNext.isSelectionSuccessful()) {
                return;
            }

            printFileInformation(selectFileParserNext);

            while (selectFileParserNext.isSelectionSuccessful()
                    && selectFileParserNext.getLid() != currentFile) {

                currentFile = selectFileParserNext.getLid();

                selectFileParserNextIndex = poTransaction.prepareSelectFileCmd(NEXT, "Next EF");
                poTransaction.processPoCommands(ChannelState.KEEP_OPEN);

                selectFileParserNext = (SelectFileRespPars) poTransaction
                        .getResponseParser(selectFileParserNextIndex);

                if (!selectFileParserNext.isSelectionSuccessful()) {
                    return;
                }

                printFileInformation(selectFileParserNext);
            }

        } catch (KeypleCalypsoSecureSessionException e) {
            // SW1SW2 is in:: e.getResponses().get(0).getStatusCode();
        } catch (Exception e) {
            logger.error("Exception: " + e.getCause());
        } finally {
            logger.info("----------------------------------------------------");
        }
    }


    protected static void getApplicationData(String aid, SeReader poReader) {

        try {
            SeSelection seSelection = new SeSelection();


            PoSelectionRequest poSelectionRequest1 = new PoSelectionRequest(
                    new SeSelector(new SeSelector.AidSelector(ByteArrayUtils.fromHex(aid), null),
                            null, "firstApplication"),
                    ChannelState.KEEP_OPEN, Protocol.ANY);

            int firstApplicationIndex = seSelection.prepareSelection(poSelectionRequest1);

            SelectionsResult selectionsResult = seSelection.processExplicitSelection(poReader);

            if (!selectionsResult.hasActiveSelection()) {
                // logger.info("1st application not found.");
                return;
            }

            CalypsoPo firstApplication =
                    (CalypsoPo) selectionsResult.getActiveSelection().getMatchingSe();
            printApplicationInformation(poReader, firstApplication);

            // additional selection
            getApplicationFileData(poReader, firstApplication);

            // logger.info("Searching for 2nd application with AID::" + aid);

            seSelection = new SeSelection();

            PoSelectionRequest poSelectionRequest2 =
                    new PoSelectionRequest(
                            new SeSelector(
                                    new SeSelector.AidSelector(ByteArrayUtils.fromHex(aid), null,
                                            SeSelector.AidSelector.FileOccurrence.NEXT,
                                            SeSelector.AidSelector.FileControlInformation.FCI),
                                    null, "secondApplication"),
                            ChannelState.KEEP_OPEN, Protocol.ANY);

            int secondApplicationIndex = seSelection.prepareSelection(poSelectionRequest2);

            selectionsResult = seSelection.processExplicitSelection(poReader);

            if (!selectionsResult.hasActiveSelection()) {
                // logger.info("2nd application not found.");
                return;
            }

            CalypsoPo secondApplication =
                    (CalypsoPo) selectionsResult.getActiveSelection().getMatchingSe();

            logger.info(
                    "==================================================================================");
            logger.info("Selected application with AID:: "
                    + ByteArrayUtils.toHex(secondApplication.getDfName()));

            // additional selection
            getApplicationFileData(poReader, firstApplication);

        } catch (Exception e) {
            logger.error("Exception: " + e.getCause());
        }
    }

    public static void main(String[] args) throws KeypleBaseException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        SeReader poReader =
                DemoUtilities.getReader(seProxyService, DemoUtilities.PO_READER_NAME_REGEX);

        /* Check if the reader exists */
        if (poReader == null) {
            throw new IllegalStateException("Bad PO reader setup");
        }

        logger.info("= PO Reader  NAME = {}", poReader.getName());
        /* Check if a PO is present in the reader */
        if (poReader.isSePresent()) {

            /* Supported Base AID */
            String poMasterFileAid = "334D54522E";
            String poTransportFileAid = "315449432E";
            String poHoplinkAid = "A000000291";
            String poStoredValueAid = "304554502E";
            String nfcNdefAid = "D276000085";

            getApplicationData(poMasterFileAid, poReader);

            getApplicationData(poTransportFileAid, poReader);

            getApplicationData(poHoplinkAid, poReader);

            getApplicationData(poStoredValueAid, poReader);

            getApplicationData(nfcNdefAid, poReader);

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= End of the Calypso PO Analysis.                                                =");
            logger.info(
                    "==================================================================================");
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}

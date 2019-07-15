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
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.builder.GetDataTraceCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.PoGetChallengeCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.GetDataTraceRespPars;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.calypso.command.po.parser.security.PoGetChallengeRespPars;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.integration.IntegrationUtils;
import org.eclipse.keyple.integration.poData.*;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.*;

public class Tool_AnalyzePoFileStructure {

    private static final Logger logger = LoggerFactory.getLogger(Tool_AnalyzePoFileStructure.class);

    private static PoStructureData poStructureData = null;

    private static long getTransactionCounter(SeReader poReader, CalypsoPo calypsoPo) {

        try {

            PoResource poResource = new PoResource(poReader, calypsoPo);

            // create an apdu requests list to handle PO and SAM commands
            List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

            // get the challenge from the PO
            apduRequests.add(new PoGetChallengeCmdBuild(poResource.getMatchingSe().getPoClass())
                    .getApduRequest());

            SeRequest seRequest = new SeRequest(apduRequests, ChannelState.KEEP_OPEN);

            SeResponse seResponse = ((ProxyReader) poResource.getSeReader()).transmit(seRequest);

            if (seResponse == null || !seResponse.getApduResponses().get(0).isSuccessful()) {
                throw new IllegalStateException("PO get challenge command failed.");
            }

            PoGetChallengeRespPars poGetChallengeRespPars =
                    new PoGetChallengeRespPars(seResponse.getApduResponses().get(0));

            byte[] poChallenge = poGetChallengeRespPars.getPoChallenge();
            return IntegrationUtils.bytesToLong(poChallenge, 3);

        } catch (Exception e) {
            logger.error("Exception: " + e.getCause());
            return 0;
        }
    }


    private static void getFileData(PoTransaction poTransaction,
            SelectFileRespPars inFileInformation) {

        try {
            PoFileData fileData = new PoFileData(inFileInformation);

            if (inFileInformation.getFileType() == SelectFileRespPars.FILE_TYPE_EF
                    && inFileInformation.getEfType() != SelectFileRespPars.EF_TYPE_BINARY
                    && inFileInformation.getAccessConditions()[0] != 0x01) {

                for (int i = 0; i < inFileInformation.getNumRec(); i++) {

                    int readRecordParserIndex = poTransaction.prepareReadRecordsCmd(
                            inFileInformation.getSfi(), ReadDataStructure.SINGLE_RECORD_DATA,
                            (byte) (i + 1), inFileInformation.getRecSize(), "");

                    poTransaction.processPoCommands(ChannelState.KEEP_OPEN);

                    fileData.getRecordData()
                            .add((new RecordData(i + 1,
                                    ((ReadRecordsRespPars) poTransaction
                                            .getResponseParser(readRecordParserIndex)).getRecords()
                                                    .get(i + 1))));
                }
            }

            PoApplicationData applicationData = poStructureData.getApplicationList()
                    .get(poStructureData.getApplicationList().size() - 1);
            applicationData.getFileList().add(fileData);

        } catch (Exception e) {
            logger.error("Exception: " + e.getCause());
        }

    }


    private static void getApplicationData(SeReader poReader, CalypsoPo curApp) {

        try {

            // Get and fill the Application file information
            PoTransaction poTransaction = new PoTransaction(new PoResource(poReader, curApp));

            int selectCurrentDfIndex = poTransaction.prepareSelectFileCmd(CURRENT_DF, "CurrentDF");

            poTransaction.processPoCommands(ChannelState.KEEP_OPEN);

            SelectFileRespPars selectCurrentDf =
                    (SelectFileRespPars) poTransaction.getResponseParser(selectCurrentDfIndex);

            if (!selectCurrentDf.isSelectionSuccessful()) {
                return;
            }

            if (poStructureData == null) {

                PoResource poResource = new PoResource(poReader, curApp);
                List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

                apduRequests.add(new GetDataTraceCmdBuild(poResource.getMatchingSe().getPoClass())
                        .getApduRequest());
                SeRequest seRequest = new SeRequest(apduRequests, ChannelState.KEEP_OPEN);

                SeResponse seResponse =
                        ((ProxyReader) poResource.getSeReader()).transmit(seRequest);

                GetDataTraceRespPars traceInfo =
                        new GetDataTraceRespPars(seResponse.getApduResponses().get(0));

                poStructureData = new PoStructureData(traceInfo.getApduResponse().getDataOut(),
                        "AnalyzePoFileStructure", new Date(), 1, "Keyple");

            }

            long transactionCounter = getTransactionCounter(poReader, curApp);

            PoApplicationData appToAdd =
                    new PoApplicationData(selectCurrentDf, curApp, transactionCounter);

            poStructureData.getApplicationList().add(appToAdd);

            // Iterate on all the files present in the application
            int currentFile;

            int selectFileParserFirstIndex = poTransaction.prepareSelectFileCmd(FIRST, "First EF");

            poTransaction.processPoCommands(ChannelState.KEEP_OPEN);

            SelectFileRespPars selectFileParserFirst = (SelectFileRespPars) poTransaction
                    .getResponseParser(selectFileParserFirstIndex);
            if (!selectFileParserFirst.isSelectionSuccessful()) {
                return;
            }

            getFileData(poTransaction, selectFileParserFirst);
            currentFile = selectFileParserFirst.getLid();

            int selectFileParserNextIndex = poTransaction.prepareSelectFileCmd(NEXT, "Next EF");
            poTransaction.processPoCommands(ChannelState.KEEP_OPEN);

            SelectFileRespPars selectFileParserNext =
                    (SelectFileRespPars) poTransaction.getResponseParser(selectFileParserNextIndex);
            if (!selectFileParserNext.isSelectionSuccessful()) {
                return;
            }

            getFileData(poTransaction, selectFileParserNext);

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

                getFileData(poTransaction, selectFileParserNext);
            }

        } catch (Exception e) {
            // Do nothing because when we reach the end of the list we always generate an exception
        }
    }


    protected static void getApplicationsData(String aid, SeReader poReader) {

        try {
            SeSelection seSelection = new SeSelection();

            PoSelectionRequest poSelectionRequest1 = new PoSelectionRequest(new PoSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                    new PoSelector.PoAidSelector(new SeSelector.AidSelector.IsoAid(aid), null),
                    "firstApplication"), ChannelState.KEEP_OPEN);

            int firstApplicationIndex = seSelection.prepareSelection(poSelectionRequest1);

            SelectionsResult selectionsResult = seSelection.processExplicitSelection(poReader);

            if (!selectionsResult.hasActiveSelection()) {
                // logger.info("1st application not found.");
                return;
            }

            CalypsoPo firstApplication =
                    (CalypsoPo) selectionsResult.getActiveSelection().getMatchingSe();

            getApplicationData(poReader, firstApplication);

            seSelection = new SeSelection();

            PoSelectionRequest poSelectionRequest2 = new PoSelectionRequest(
                    new PoSelector(SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                            new PoSelector.PoAidSelector(new SeSelector.AidSelector.IsoAid(aid),
                                    null, SeSelector.AidSelector.FileOccurrence.NEXT,
                                    SeSelector.AidSelector.FileControlInformation.FCI),
                            "secondApplication"),
                    ChannelState.KEEP_OPEN);

            int secondApplicationIndex = seSelection.prepareSelection(poSelectionRequest2);

            selectionsResult = seSelection.processExplicitSelection(poReader);

            if (!selectionsResult.hasActiveSelection()) {
                // logger.info("2nd application not found.");
                return;
            }

            CalypsoPo secondApplication =
                    (CalypsoPo) selectionsResult.getActiveSelection().getMatchingSe();

            getApplicationData(poReader, secondApplication);


        } catch (Exception e) {
            logger.error("Exception(getApplicationsData): " + e.getCause());
        }
    }


    public static void main(String[] args) throws KeypleBaseException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        poStructureData = null;

        SeReader poReader =
                IntegrationUtils.getReader(seProxyService, IntegrationUtils.PO_READER_NAME_REGEX);

        poReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                PcscProtocolSetting.PCSC_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

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

            getApplicationsData(poMasterFileAid, poReader);

            getApplicationsData(poTransportFileAid, poReader);

            getApplicationsData(poHoplinkAid, poReader);

            getApplicationsData(poStoredValueAid, poReader);

            getApplicationsData(nfcNdefAid, poReader);

            try {
                Gson gson =
                        new GsonBuilder()
                                .registerTypeHierarchyAdapter(byte[].class,
                                        new IntegrationUtils.HexTypeAdapter())
                                .setPrettyPrinting().create();

                String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());

                String fileName = new String(dateString + "_PoData_"
                        + poStructureData.getApplicationList().get(0).getCsnDec() + ".json");

                poStructureData.setId(fileName);

                String jsonToPrint = gson.toJson(poStructureData);

                FileWriter fw = new FileWriter(fileName);
                fw.write(jsonToPrint);
                fw.close();

            } catch (Exception e) {
                logger.error("Exception while writing the report: " + e.getCause());
            }

            poStructureData.print(logger);

            logger.info(
                    "========================================================================================================");
            logger.info(
                    "= End of the Calypso PO Analysis.                                                                      =");
            logger.info(
                    "========================================================================================================");
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}

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
package org.eclipse.keyple.integration.tools.calypso.sam;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.security.SamReadCeilingsCmdBuild;
import org.eclipse.keyple.calypso.command.sam.builder.security.SamReadCeilingsCmdBuild.*;
import org.eclipse.keyple.calypso.command.sam.builder.security.SamReadEventCounterCmdBuild;
import org.eclipse.keyple.calypso.command.sam.builder.security.SamReadEventCounterCmdBuild.*;
import org.eclipse.keyple.calypso.command.sam.builder.security.SamReadKeyParametersCmdBuild;
import org.eclipse.keyple.calypso.command.sam.builder.security.SamReadKeyParametersCmdBuild.*;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.SamResource;
import org.eclipse.keyple.calypso.transaction.SamSelectionRequest;
import org.eclipse.keyple.calypso.transaction.SamSelector;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.integration.IntegrationUtils;
import org.eclipse.keyple.integration.samData.SamCounterData;
import org.eclipse.keyple.integration.samData.SamKeyData;
import org.eclipse.keyple.integration.samData.SamStructureData;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tool_SAM_GetKeyParams {

    private static final Logger logger = LoggerFactory.getLogger(Tool_SAM_GetKeyParams.class);


    private static SamKeyData getKeyInfo(SamResource samResource, SourceRef keyType, int keyIndex)
            throws KeypleReaderException {

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

        apduRequests.add(new SamReadKeyParametersCmdBuild(SamRevision.C1, keyType, keyIndex)
                .getApduRequest());

        SeRequest seRequest = new SeRequest(apduRequests);

        SeResponse seResponse = ((ProxyReader) samResource.getSeReader()).transmit(seRequest);

        if (seResponse == null) {
            throw new IllegalStateException(
                    "SAM Read Key Parameters command command failed. Null response");
        }

        if (seResponse.getApduResponses().get(0).getDataOut().length > 0) {
            return new SamKeyData(keyIndex, seResponse.getApduResponses().get(0).getDataOut());
        } else {
            return null;
        }

    }


    private static byte[] getCounterRecord(SamResource samResource, int recordIndex)
            throws KeypleReaderException {

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

        apduRequests.add(new SamReadEventCounterCmdBuild(SamRevision.C1,
                SamEventCounterOperationType.COUNTER_RECORD, recordIndex).getApduRequest());

        SeRequest seRequest = new SeRequest(apduRequests);

        SeResponse seResponse = ((ProxyReader) samResource.getSeReader()).transmit(seRequest);

        if (seResponse == null) {
            throw new IllegalStateException(
                    "SAM Read Event Counter command command failed. Null response");
        }

        if (seResponse.getApduResponses().get(0).isSuccessful()) {
            return seResponse.getApduResponses().get(0).getDataOut();
        } else {
            return null;
        }
    }


    private static byte[] getCeilingsRecord(SamResource samResource, int recordIndex)
            throws KeypleReaderException {

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

        apduRequests.add(new SamReadCeilingsCmdBuild(SamRevision.C1,
                CeilingsOperationType.CEILING_RECORD, recordIndex).getApduRequest());

        SeRequest seRequest = new SeRequest(apduRequests);

        SeResponse seResponse = ((ProxyReader) samResource.getSeReader()).transmit(seRequest);

        if (seResponse == null) {
            throw new IllegalStateException(
                    "SAM Read Ceilings command command failed. Null response");
        }

        if (seResponse.getApduResponses().get(0).isSuccessful()) {
            return seResponse.getApduResponses().get(0).getDataOut();
        } else {
            return null;
        }
    }


    public static void main(String[] args) throws KeypleBaseException {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.registerPlugin(new PcscPluginFactory());

        ProxyReader samReader = (ProxyReader) IntegrationUtils.getReader(seProxyService,
                IntegrationUtils.SAM_READER_NAME_REGEX);

        /* Check if the readers exist */
        if (samReader == null) {
            throw new IllegalStateException("Bad SAM reader setup");
        }

        logger.info("= SAM Reader  NAME = {}", samReader.getName());

        samReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO7816_3,
                PcscProtocolSetting.PCSC_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO7816_3));

        // do the SAM selection to open the logical channel
        final String SAM_ATR_REGEX = "3B3F9600805A[0-9a-fA-F]{2}80[0-9a-fA-F]{16}829000";

        SeSelection samSelection = new SeSelection();

        SamSelectionRequest samSelectionRequest =
                new SamSelectionRequest(new SamSelector(SamRevision.C1, null, "SAM Selection"));

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelection(samSelectionRequest);

        SelectionsResult samSelectionsResult;
        try {
            samSelectionsResult = samSelection.processExplicitSelection(samReader);
            if (!samSelectionsResult.hasActiveSelection()) {
                System.out.println("Unable to open a logical channel for SAM!");
                throw new IllegalStateException("SAM channel opening failure");
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());
        }

        CalypsoSam calypsoSam =
                (CalypsoSam) samSelectionsResult.getActiveSelection().getMatchingSe();

        SamStructureData samStructureData = new SamStructureData(calypsoSam);

        SamResource samResource = new SamResource(samReader, calypsoSam);

        // Get System Keys Parameter Data
        for (int i = 1; i < 5; i++) {

            try {
                SamKeyData systemKeyData = getKeyInfo(samResource, SourceRef.SYSTEM_KEY, i);

                if (systemKeyData != null) {
                    samStructureData.getSystemKeyList().add(systemKeyData);
                }

            } catch (KeypleReaderException e) {
                throw new IllegalStateException("Reader exception: " + e.getMessage());
            }
        }

        // Get Work Keys Parameter Data
        for (int i = 1; i <= SamReadKeyParametersCmdBuild.MAX_WORK_KEY_REC_NUMB; i++) {

            try {
                SamKeyData workKeyData = getKeyInfo(samResource, SourceRef.WORK_KEY, i);

                if (workKeyData != null) {
                    samStructureData.getWorkKeyList().add(workKeyData);
                }

            } catch (KeypleReaderException e) {
                throw new IllegalStateException("Reader exception: " + e.getMessage());
            }
        }

        for (int i = 1; i <= SamReadEventCounterCmdBuild.MAX_COUNTER_REC_NUMB; i++) {

            try {
                byte[] counterRecordData = getCounterRecord(samResource, i);
                byte[] ceilingsRecordData = getCeilingsRecord(samResource, i);

                if (counterRecordData != null) {

                    byte[] counterData = new byte[3];
                    byte[] ceilingData = new byte[3];

                    for (int f = 0; f < 9; f++) {
                        System.arraycopy(counterRecordData, 8 + (f * 3), counterData, 0, 3);
                        System.arraycopy(ceilingsRecordData, 8 + (f * 3), ceilingData, 0, 3);

                        SamCounterData newCounter =
                                new SamCounterData((((i - 1) * 9) + f), counterData, ceilingData);
                        samStructureData.getCounterDataList().add(newCounter);

                    }

                }

            } catch (KeypleReaderException e) {
                throw new IllegalStateException("Reader exception: " + e.getMessage());
            }

        }

        try {
            Gson gson =
                    new GsonBuilder()
                            .registerTypeHierarchyAdapter(byte[].class,
                                    new IntegrationUtils.HexTypeAdapter())
                            .setPrettyPrinting().create();

            String dateString = new SimpleDateFormat("yyyyMMdd").format(new Date());

            String fileName = new String(
                    dateString + "_SamData_" + samStructureData.getSerialNumber() + ".json");

            String jsonToPrint = gson.toJson(samStructureData);

            FileWriter fw = new FileWriter(fileName);
            fw.write(jsonToPrint);
            fw.close();

        } catch (Exception e) {
            logger.error("Exception while writing the report: " + e.getCause());
        }

        samStructureData.print(logger);
    }
}

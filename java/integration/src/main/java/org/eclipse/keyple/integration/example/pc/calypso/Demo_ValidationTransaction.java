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
package org.eclipse.keyple.integration.example.pc.calypso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.calypso.command.po.parser.AppendRecordRespPars;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.command.po.parser.UpdateRecordRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.integration.calypso.PoFileStructureInfo;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.*;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelectionRequest;
import org.eclipse.keyple.util.ByteArrayUtils;

@SuppressWarnings("PMD.VariableNamingConventions")
public class Demo_ValidationTransaction implements ObservableReader.ReaderObserver {

    private SeReader poReader, samReader;

    @Override
    public void update(ReaderEvent event) {
        switch (event.getEventType()) {
            case SE_INSERTED:
                System.out.println(
                        "\n==================================================================================");
                System.out.println("Found a Calypso PO! Validating...\n");
                detectAndHandlePO();
                break;
            case SE_REMOVAL:
                System.out.println("\nWaiting for new Calypso PO...");
                break;
            default:
                System.out.println("IO Error");
        }
    }

    public static byte[] longToBytes(long lg) {
        byte[] result = new byte[8];
        long l = lg;
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public static int getCounterValueFromByteArray(byte[] inByteArray, int inCounterIndex) {
        int counterValue = 0;

        for (int i = 0; i < 3; i++) {
            counterValue <<= 8;
            counterValue |= (inByteArray[i + (3 * (inCounterIndex - 1))] & 0xFF);
        }

        return counterValue;
    }

    public static byte[] getByteArrayFromCounterValue(int inCounterValue) {

        byte[] result = new byte[3];

        int counter = inCounterValue;
        for (int i = 2; i >= 0; i--) {
            result[i] = (byte) (inCounterValue & 0xFF);
            counter >>= 8;
        }

        return result;
    }

    // Not optimized for online/remote operation
    private void validateAuditC0(PoTransaction poTransaction) throws KeypleReaderException {

        byte eventSfi = 0x08;
        byte contractListSfi = 0x1E;
        byte environmentSfi = 0x07;


        ReadRecordsRespPars readEventParser = poTransaction.prepareReadRecordsCmd(eventSfi,
                ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, "Event");
        ReadRecordsRespPars readContractListParser = poTransaction.prepareReadRecordsCmd(
                contractListSfi, ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, "ContractList");

        // Open Session with debit key #3 and reading the Environment at SFI 07h
        // Files to read during the beginning of the session: Event (SFI 0x08) and ContractList (SFI
        // 0x1E)
        poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, environmentSfi, (byte) 0x01);

        byte contractIndex = readContractListParser.getRecords().get(1)[0];
        byte[] eventTimestampData = Arrays.copyOfRange(readEventParser.getRecords().get(1), 1,
                (Long.SIZE / Byte.SIZE) + 1);

        String timeStampString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new Date(bytesToLong(eventTimestampData)));

        System.out.println(
                "\t------------------------------------------------------------------------------");
        String nameInPO = new String(poTransaction.getOpenRecordDataRead());
        System.out.println("\tName in PO:: " + nameInPO);
        System.out.println(
                "\t------------------------------------------------------------------------------");
        System.out.println("\tPrevious Event Information");
        System.out.println(
                "\t- Index of Validated Contract:: " + (contractIndex == 0 ? 4 : contractIndex));
        System.out.println("\t- Contract Type:: Season Pass");
        System.out.println("\t- Event DateTime:: " + timeStampString);
        System.out.println(
                "\t------------------------------------------------------------------------------\n");

        ReadRecordsRespPars readContractParser = poTransaction.prepareReadRecordsCmd((byte) 0x29,
                ReadDataStructure.SINGLE_RECORD_DATA, (byte) (contractIndex + 1), (byte) 0x1D,
                "Contract");

        poTransaction.processPoCommandsInSession();

        System.out
                .println("Reading contract #" + (contractIndex + 1) + " for current validation...");

        /*
         * System.out.println("Contract#" + (contractIndex+1) + ": " +
         * ByteArrayUtils.toHex(dataReadInSession.getApduResponses().get(0).getDataOut()) +
         * ", SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(0).getStatusCode() &
         * 0xFFFF));
         */

        byte[] newEventData = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] newContractListData =
                new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        if (contractIndex < 3) {
            newContractListData[0] = (byte) (contractIndex + 1);
        }

        newEventData[0] = (byte) (contractIndex + 1);

        byte[] dateToInsert = longToBytes(new Date().getTime());
        System.arraycopy(dateToInsert, 0, newEventData, 1, (Long.SIZE / Byte.SIZE));

        UpdateRecordRespPars updateContractListParser = poTransaction.prepareUpdateRecordCmd(
                contractListSfi, (byte) 0x01, newContractListData, "ContractList");
        AppendRecordRespPars appendEventPars =
                poTransaction.prepareAppendRecordCmd(eventSfi, newEventData, "Event");

        poTransaction.processPoCommandsInSession();

        poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);

        System.out.println("\nValidation Successful!");
        System.out.println(
                "==================================================================================");
    }


    // Optimised for online/remote operation
    private void validateClap(CalypsoPo detectedPO) throws KeypleReaderException {

        byte eventSfi = 0x08;
        byte countersSfi = 0x1B;
        byte environmentSfi = 0x14;
        byte contractsSfi = 0x29;

        SeResponse dataReadInSession;
        PoTransaction poTransaction = new PoTransaction(poReader, detectedPO, samReader, null);

        ReadRecordsRespPars readEventParser = poTransaction.prepareReadRecordsCmd(eventSfi,
                ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, "Event");
        ReadRecordsRespPars readCountersParser = poTransaction.prepareReadRecordsCmd(countersSfi,
                ReadDataStructure.SINGLE_COUNTER, (byte) 0x01, "Counters");
        poTransaction.prepareReadRecordsCmd(contractsSfi, ReadDataStructure.MULTIPLE_RECORD_DATA,
                (byte) 0x01, "Contracts");

        // Open Session with debit key #3 and reading the Environment at SFI 07h
        // Files to read during the beginning of the session: Event (SFI 0x08), Counters (SFI 0x1B)
        // and all records of the Contracts (SFI 0x29)
        poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, environmentSfi, (byte) 0x01);

        byte[] eventTimestampData = Arrays.copyOfRange(readEventParser.getRecords().get(1), 1,
                (Long.SIZE / Byte.SIZE) + 1);

        String timeStampString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new Date(bytesToLong(eventTimestampData)));

        int counterValue = readCountersParser.getCounters().get(0);

        System.out.println(
                "\t------------------------------------------------------------------------------");
        String nameInPO = new String(poTransaction.getOpenRecordDataRead());
        System.out.println("\tName in PO:: " + nameInPO);
        System.out.println(
                "\t------------------------------------------------------------------------------");
        System.out.println("\tPrevious Event Information");
        System.out.println("\t- Index of Validated Contract:: 1");
        System.out.println("\t- Contract Type:: MultiTrip Ticket");
        System.out.println("\t- Counter Value:: " + counterValue);
        System.out.println("\t- Event DateTime:: " + timeStampString);
        System.out.println(
                "\t------------------------------------------------------------------------------\n");

        System.out.println("All contracts read during the beginning of the current transaction...");

        // Perform automatic top-up when the value is 0 by closing the current session and opening a
        // new one with a loading key
        if (counterValue == 0) {

            System.out.println("No value present in the card. Initiating auto top-up...");

            poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);

            poTransaction = new PoTransaction(poReader, detectedPO, samReader, null);

            poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                    PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD, (byte) 0x00, (byte) 0x00);

            byte[] newCounterData = new byte[] {0x00, 0x00, 0x05, 0x00, 0x00, 0x00};

            poTransaction.prepareUpdateRecordCmd(countersSfi, (byte) 0x01, newCounterData,
                    "Counter");
            counterValue = 5;
        }

        /*
         * System.out.println("Contract#" + (contractIndex+1) + ": " +
         * ByteArrayUtils.toHex(dataReadInSession.getApduResponses().get(0).getDataOut()) +
         * ", SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(0).getStatusCode() &
         * 0xFFFF));
         */

        byte[] newEventData = new byte[] {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        byte[] dateToInsert = longToBytes(new Date().getTime());
        System.arraycopy(dateToInsert, 0, newEventData, 1, (Long.SIZE / Byte.SIZE));

        poTransaction.prepareAppendRecordCmd(eventSfi, newEventData, "Event");

        poTransaction.prepareDecreaseCmd(countersSfi, (byte) 0x01, 1, "Counter decval=1");

        poTransaction.processPoCommandsInSession();

        poTransaction.processClosing(TransmissionMode.CONTACTLESS, ChannelState.KEEP_OPEN);

        System.out.println("\nValidation Successful!");
        System.out.println(
                "==================================================================================");
    }


    private void detectAndHandlePO() {

        try {
            // operate PO multiselection
            String poAuditC0Aid = "315449432E4943414C54"; // AID of the PO with Audit C0 profile
            String clapAid = "315449432E494341D62010029101"; // AID of the CLAP product being tested
            String cdLightAid = "315449432E494341"; // AID of the Rev2.4 PO emulating CDLight

            SeSelection seSelection = new SeSelection(poReader);

            // Add Audit C0 AID to the list
            CalypsoPo auditC0Se =
                    (CalypsoPo) seSelection
                            .prepareSelection(
                                    new PoSelectionRequest(
                                            new SeSelector(new SeSelector.AidSelector(
                                                    ByteArrayUtils.fromHex(
                                                            PoFileStructureInfo.poAuditC0Aid),
                                                    null), null, "Audit C0"),
                                            ChannelState.KEEP_OPEN, Protocol.ANY));

            // Add CLAP AID to the list
            CalypsoPo clapSe =
                    (CalypsoPo) seSelection
                            .prepareSelection(
                                    new PoSelectionRequest(
                                            new SeSelector(
                                                    new SeSelector.AidSelector(
                                                            ByteArrayUtils.fromHex(
                                                                    PoFileStructureInfo.clapAid),
                                                            null),
                                                    null, "CLAP"),
                                            ChannelState.KEEP_OPEN, Protocol.ANY));

            // Add cdLight AID to the list
            CalypsoPo cdLightSe =
                    (CalypsoPo) seSelection
                            .prepareSelection(new PoSelectionRequest(
                                    new SeSelector(
                                            new SeSelector.AidSelector(ByteArrayUtils
                                                    .fromHex(PoFileStructureInfo.cdLightAid), null),
                                            null, "CDLight"),
                                    ChannelState.KEEP_OPEN, Protocol.ANY));

            if (!seSelection.processExplicitSelection()) {
                throw new IllegalArgumentException("No recognizable PO detected.");
            }


            // Depending on the PO detected perform either a Season Pass validation or a MultiTrip
            // validation
            if (auditC0Se.isSelected()) {

                PoTransaction poTransaction =
                        new PoTransaction(poReader, auditC0Se, samReader, null);
                validateAuditC0(poTransaction);

            } else if (clapSe.isSelected()) {

                PoTransaction poTransaction = new PoTransaction(poReader, clapSe, samReader, null);
                validateClap(clapSe);

            } else if (cdLightSe.isSelected()) {

                PoTransaction poTransaction =
                        new PoTransaction(poReader, cdLightSe, samReader, null);
                validateAuditC0(poTransaction);

            } else {
                System.out.println("No recognizable PO detected.");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static final Object waitForEnd = new Object();

    public static void main(String[] args)
            throws IOException, InterruptedException, KeypleBaseException {

        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReaderPlugin> pluginsSet = new ConcurrentSkipListSet<ReaderPlugin>();
        pluginsSet.add(PcscPlugin.getInstance());
        seProxyService.setPlugins(pluginsSet);

        SeReader poReader =
                DemoUtilities.getReader(seProxyService, DemoUtilities.PO_READER_NAME_REGEX);
        SeReader samReader =
                DemoUtilities.getReader(seProxyService, DemoUtilities.SAM_READER_NAME_REGEX);


        if (poReader == samReader || poReader == null || samReader == null) {
            throw new IllegalStateException("Bad PO/SAM setup");
        }

        System.out.println(
                "\n==================================================================================");
        System.out.println("PO Reader  : " + poReader.getName());
        System.out.println("SAM Reader : " + samReader.getName());
        System.out.println(
                "==================================================================================");

        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);
        samReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        // provide the reader with the map
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        final String SAM_ATR_REGEX = "3B3F9600805A[0-9a-fA-F]{2}80[0-9a-fA-F]{16}829000";

        SeSelection samSelection = new SeSelection(samReader);

        SeSelectionRequest samSelectionRequest = new SeSelectionRequest(
                new SeSelector(null, new SeSelector.AtrFilter(SAM_ATR_REGEX), "SAM Selection"),
                ChannelState.KEEP_OPEN, Protocol.ANY);

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelection(samSelectionRequest);

        try {
            if (!samSelection.processExplicitSelection()) {
                System.out.println("Unable to open a logical channel for SAM!");
                throw new IllegalStateException("SAM channel opening failure");
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());

        }

        // Setting up ourselves as an observer
        Demo_ValidationTransaction observer = new Demo_ValidationTransaction();
        observer.poReader = poReader;
        observer.samReader = samReader;

        System.out.println("\nReady for PO presentation!");

        // Set terminal as Observer of the first reader
        ((ObservableReader) poReader).addObserver(observer);
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }

}

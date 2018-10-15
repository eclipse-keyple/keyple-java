/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.integration.example.pc.calypso;

import static org.eclipse.keyple.calypso.transaction.PoTransaction.CommunicationMode;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.ModificationMode.*;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionAccessLevel.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.transaction.CalypsoPO;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.pc.generic.PcscReadersSettings;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.ByteArrayUtils;

public class Demo_ValidationTransaction implements ObservableReader.ReaderObserver {

    private ProxyReader poReader, csmReader;

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

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
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

        for (int i = 2; i >= 0; i--) {
            result[i] = (byte) (inCounterValue & 0xFF);
            inCounterValue >>= 8;
        }

        return result;
    }

    // Not optimized for online/remote operation
    private void validateAuditC0(PoTransaction poTransaction, ApduResponse fciData)
            throws KeypleReaderException {

        byte eventSfi = 0x08;
        byte contractListSfi = 0x1E;
        byte environmentSfi = 0x07;

        SeResponse dataReadInSession;

        poTransaction.prepareReadRecordsCmd(eventSfi, (byte) 0x01, true, (byte) 0x00, "Event");
        poTransaction.prepareReadRecordsCmd(contractListSfi, (byte) 0x01, true, (byte) 0x00,
                "ContractList");

        // Open Session with debit key #3 and reading the Environment at SFI 07h
        // Files to read during the beginning of the session: Event (SFI 0x08) and ContractList (SFI
        // 0x1E)
        dataReadInSession = poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                SESSION_LVL_DEBIT, environmentSfi, (byte) 0x01);

        /*
         * byte[] sessionData =
         * ByteArrayUtils.subLen(dataReadInSession.getApduResponses().get(0).getDataOut(), 0, 8);
         * byte[] environmentData =
         * ByteArrayUtils.subIndex(dataReadInSession.getApduResponses().get(0).getDataOut(), 8,
         * 29+8);
         * 
         * System.out.println("OpenSession#: " + ByteArrayUtils.toHex(sessionData) +
         * ", Environment#:" + ByteArrayUtils.toHex(environmentData) +", SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(0).getStatusCode() &
         * 0xFFFF));
         * 
         * System.out.println("Event#: " +
         * ByteArrayUtils.toHex(dataReadInSession.getApduResponses().get(1).getDataOut()) +
         * ", SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(1).getStatusCode() &
         * 0xFFFF));
         * 
         * System.out.println("ContractList#: " +
         * ByteArrayUtils.toHex(dataReadInSession.getApduResponses().get(2).getDataOut()) +
         * ", SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(2).getStatusCode() &
         * 0xFFFF));
         */

        byte contractIndex = dataReadInSession.getApduResponses().get(2).getDataOut()[0];
        byte[] eventTimestampData =
                Arrays.copyOfRange(dataReadInSession.getApduResponses().get(1).getDataOut(), 1,
                        (Long.SIZE / Byte.SIZE));

        String timeStampString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new Date(bytesToLong(eventTimestampData)));

        System.out.println(
                "\t------------------------------------------------------------------------------");
        System.out.println("\tPrevious Event Information");
        System.out.println(
                "\t- Index of Validated Contract:: " + (contractIndex == 0 ? 4 : contractIndex));
        System.out.println("\t- Contract Type:: Season Pass");
        System.out.println("\t- Event DateTime:: " + timeStampString);
        System.out.println(
                "\t------------------------------------------------------------------------------\n");

        poTransaction.prepareReadRecordsCmd((byte) 0x29, (byte) (contractIndex + 1), true,
                (byte) 0x1D, "Contract");

        dataReadInSession = poTransaction.processPoCommands();

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

        poTransaction.prepareUpdateRecordCmd(contractListSfi, (byte) 0x01, newContractListData,
                "ContractList");
        poTransaction.prepareAppendRecordCmd(eventSfi, newEventData, "Event");

        poTransaction.processClosing(CommunicationMode.CONTACTLESS_MODE, false);

        System.out.println("\nValidation Successful!");
        System.out.println(
                "==================================================================================");
    }


    // Optimised for online/remote operation
    private void validateClap(PoTransaction poTransaction, ApduResponse fciData)
            throws KeypleReaderException {

        byte eventSfi = 0x08;
        byte countersSfi = 0x1B;
        byte environmentSfi = 0x14;
        byte contractsSfi = 0x29;

        SeResponse dataReadInSession;

        poTransaction.prepareReadRecordsCmd(eventSfi, (byte) 0x01, true, (byte) 0x00, "Event");
        poTransaction.prepareReadRecordsCmd(countersSfi, (byte) 0x01, true, (byte) 0x00,
                "Counters");
        poTransaction.prepareReadRecordsCmd(contractsSfi, (byte) 0x01, false, (byte) 0x00,
                "Contracts");

        // Open Session with debit key #3 and reading the Environment at SFI 07h
        // Files to read during the beginning of the session: Event (SFI 0x08), Counters (SFI 0x1B)
        // and all records of the Contracts (SFI 0x29)
        dataReadInSession = poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                SESSION_LVL_DEBIT, environmentSfi, (byte) 0x01);
        /*
         * byte[] sessionData =
         * ByteArrayUtils.subLen(dataReadInSession.getApduResponses().get(0).getDataOut(), 0, 8);
         * byte[] environmentData =
         * ByteArrayUtils.subIndex(dataReadInSession.getApduResponses().get(0).getDataOut(), 8,
         * 29+8);
         * 
         * System.out.println("OpenSession#: " + ByteArrayUtils.toHex(sessionData) +
         * ", Environment#:" + ByteArrayUtils.toHex(environmentData) +", SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(0).getStatusCode() &
         * 0xFFFF));
         * 
         * System.out.println("Event#: " +
         * ByteArrayUtils.toHex(dataReadInSession.getApduResponses().get(1).getDataOut()) +
         * ", SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(1).getStatusCode() &
         * 0xFFFF));
         * 
         * System.out.println("Counters#: " +
         * ByteArrayUtils.toHex(dataReadInSession.getApduResponses().get(2).getDataOut()) +
         * ", SW1SW2: " +
         * Integer.toHexString(dataReadInSession.getApduResponses().get(2).getStatusCode() &
         * 0xFFFF));
         */
        byte[] eventTimestampData =
                Arrays.copyOfRange(dataReadInSession.getApduResponses().get(1).getDataOut(), 1,
                        (Long.SIZE / Byte.SIZE));

        String timeStampString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new Date(bytesToLong(eventTimestampData)));

        int counterValue = getCounterValueFromByteArray(
                dataReadInSession.getApduResponses().get(2).getDataOut(), 1);

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
        // new one with a
        // loading key
        if (counterValue == 0) {

            System.out.println("No value present in the card. Initiating auto top-up...");

            poTransaction.processClosing(PoTransaction.CommunicationMode.CONTACTLESS_MODE, false);

            poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC, SESSION_LVL_LOAD,
                    (byte) 0x00, (byte) 0x00);

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

        byte[] updatedCounterValue = getByteArrayFromCounterValue(counterValue - 1);

        poTransaction.processClosing(CommunicationMode.CONTACTLESS_MODE, false);

        System.out.println("\nValidation Successful!");
        System.out.println(
                "==================================================================================");
    }


    private void detectAndHandlePO() {

        try {
            // operate PO multiselection
            String poAuditC0Aid = "315449432E4943414C54"; // AID of the PO with Audit C0 profile
            String clapAid = "315449432E494341D62010029101"; // AID of the CLAP product being tested
            String CSM_ATR_REGEX = "3B3F9600805A[0-9a-fA-F]{2}80[0-9a-fA-F]{16}829000";
            String cdLightAid = "315449432E494341"; // AID of the Rev2.4 PO emulating CDLight

            // check the availability of the CSM, open its physical and logical channels and keep it
            // open
            SeRequest csmCheckRequest =
                    new SeRequest(new SeRequest.AtrSelector(CSM_ATR_REGEX), null, true);
            SeResponse csmCheckResponse =
                    csmReader.transmit(new SeRequestSet(csmCheckRequest)).getSingleResponse();

            if (csmCheckResponse == null) {
                System.out.println("Unable to open a logical channel for CSM!");
                throw new IllegalStateException("CSM channel opening failure");
            }

            // Create a SeRequest list
            Set<SeRequest> selectionRequests = new LinkedHashSet<SeRequest>();

            // Add Audit C0 AID to the list
            SeRequest seRequest = new SeRequest(
                    new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAuditC0Aid)), null, false);
            selectionRequests.add(seRequest);

            // Add CLAP AID to the list
            seRequest = new SeRequest(new SeRequest.AidSelector(ByteArrayUtils.fromHex(clapAid)),
                    null, false);
            selectionRequests.add(seRequest);

            // Add CdLight AID to the list
            seRequest = new SeRequest(new SeRequest.AidSelector(ByteArrayUtils.fromHex(cdLightAid)),
                    null, false);
            selectionRequests.add(seRequest);

            List<SeResponse> seResponses =
                    poReader.transmit(new SeRequestSet(selectionRequests)).getResponses();

            // Depending on the PO detected perform either a Season Pass validation or a MultiTrip
            // validation
            if (seResponses.get(0) != null) {

                ApduResponse fciData = seResponses.get(0).getFci();
                PoTransaction poTransaction = new PoTransaction(poReader,
                        new CalypsoPO(seResponses.get(0)), csmReader, null);
                validateAuditC0(poTransaction, fciData);

            } else if (seResponses.get(1) != null) {

                ApduResponse fciData = seResponses.get(1).getFci();
                PoTransaction poTransaction = new PoTransaction(poReader,
                        new CalypsoPO(seResponses.get(1)), csmReader, null);
                validateClap(poTransaction, fciData);

            } else if (seResponses.get(2) != null) {

                ApduResponse fciData = seResponses.get(2).getFci();
                PoTransaction poTransaction = new PoTransaction(poReader,
                        new CalypsoPO(seResponses.get(2)), csmReader, null);
                validateAuditC0(poTransaction, fciData);

            } else {
                System.out.println("No recognizable PO detected.");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Get the terminals with names that match the expected pattern
     *
     * @param seProxyService SE Proxy service
     * @param pattern Pattern
     * @return ProxyReader
     * @throws KeypleReaderException Any error with the card communication
     */
    private static ProxyReader getReader(SeProxyService seProxyService, String pattern)
            throws KeypleReaderException {
        Pattern p = Pattern.compile(pattern);
        for (ReaderPlugin plugin : seProxyService.getPlugins()) {
            for (ProxyReader reader : plugin.getReaders()) {
                if (p.matcher(reader.getName()).matches()) {
                    return reader;
                }
            }
        }
        return null;
    }

    private static final Object waitForEnd = new Object();

    public static void main(String[] args)
            throws IOException, InterruptedException, KeypleBaseException {

        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReaderPlugin> pluginsSet = new ConcurrentSkipListSet<ReaderPlugin>();
        pluginsSet.add(PcscPlugin.getInstance());
        seProxyService.setPlugins(pluginsSet);

        ProxyReader poReader = getReader(seProxyService, PcscReadersSettings.PO_READER_NAME_REGEX);
        ProxyReader csmReader =
                getReader(seProxyService, PcscReadersSettings.CSM_READER_NAME_REGEX);


        if (poReader == csmReader || poReader == null || csmReader == null) {
            throw new IllegalStateException("Bad PO/CSM setup");
        }

        System.out.println(
                "\n==================================================================================");
        System.out.println("PO Reader  : " + poReader.getName());
        System.out.println("CSM Reader : " + csmReader.getName());
        System.out.println(
                "==================================================================================");

        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);
        csmReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        // provide the reader with the map
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // Setting up ourselves as an observer
        Demo_ValidationTransaction observer = new Demo_ValidationTransaction();
        observer.poReader = poReader;
        observer.csmReader = csmReader;

        System.out.println("\nReady for PO presentation!");

        // Set terminal as Observer of the first reader
        ((ObservableReader) poReader).addObserver(observer);
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }

}

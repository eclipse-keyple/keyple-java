/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.pc;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.commands.PoSendableInSession;
import org.eclipse.keyple.calypso.commands.po.builder.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.example.common.HoplinkCommandsSettings;
import org.eclipse.keyple.example.common.HoplinkSampleCommands;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.local.AbstractLocalReader;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.eclipse.keyple.util.Observable;

/**
 * List of treatments 2 readers (match interface) fake aid navigo aid hoplink -> 3 sessions
 */
public class KeypleCalypsoDemo_HoplinkTransaction
        implements AbstractObservableReader.Observer<ReaderEvent> {
    private ProxyReader poReader, csmReader;

    public KeypleCalypsoDemo_HoplinkTransaction() {
        super();
    }

    @Override
    public void update(Observable observable, ReaderEvent event) {
        switch (event) {
            case SE_INSERTED:
                System.out.println("SE INSERTED");
                System.out.println("\nStart processing of a Calypso PO");
                operatePoTransactions();
                break;
            case SE_REMOVAL:
                System.out.println("SE REMOVED");
                System.out.println("\nWait for Calypso PO");
                break;
            default:
                System.out.println("IO Error");
        }
    }

    private void printSelectAppResponseStatus(String message, SeRequest seRequest,
            SeResponse seResponse) {
        int i;
        System.out.println("===== " + message);
        System.out.println("* Request:");
        System.out.println("AID: " + ByteBufferUtils.toHex(seRequest.getAidToSelect()));
        List<ApduRequest> apduRequests = seRequest.getApduRequests();
        i = 0;
        if (apduRequests != null && apduRequests.size() > 0) {
            for (ApduRequest apduRequest : apduRequests) {
                System.out.println(
                        "COMMAND#" + i + ": " + ByteBufferUtils.toHex(apduRequest.getBytes()));
                i++;
            }
        } else {
            System.out.println("No APDU request");
        }
        System.out.println("keepChannelOpen flag: " + seRequest.isKeepChannelOpen());
        System.out.println("protocol flag: " + seRequest.getProtocolFlag());

        System.out.println("* Response:");
        if (seResponse == null) {
            System.out.println("SeResponse is null");
        } else {
            ApduResponse atr, fci;
            atr = seResponse.getAtr();
            fci = seResponse.getFci();
            List<ApduResponse> apduResponses = seResponse.getApduResponses();
            if (atr != null) {
                System.out.println("ATR: " + ByteBufferUtils.toHex(atr.getDataOut()));
            } else {
                System.out.println("ATR: null");
            }
            if (fci != null) {
                System.out.println("FCI: " + ByteBufferUtils.toHex(fci.getDataOut()));
            } else {
                System.out.println("FCI: null");
            }
            if (apduResponses.size() > 0) {
                i = 0;
                for (ApduResponse apduResponse : apduResponses) {
                    System.out.println("RESPONSE#" + i + ": "
                            + ByteBufferUtils.toHex(apduResponse.getDataOut()) + ", SW1SW2: "
                            + Integer.toHexString(apduResponse.getStatusCode() & 0xFFFF));
                    i++;
                }
            }
        }
        // new line
        System.out.println("");
    }

    private void doHoplinkTwoStepAuthentication(PoSecureSession poTransaction, ApduResponse fciData,
            boolean closeSeChannel) throws IOReaderException {
        List<PoSendableInSession> filesToReadInSession = new ArrayList<PoSendableInSession>();
        filesToReadInSession.add(HoplinkSampleCommands.poReadRecordCmd_T2Env);
        filesToReadInSession.add(HoplinkSampleCommands.poReadRecordCmd_T2Usage);

        // Step 1
        System.out.println(
                "\n\n========= PO Hoplink 2-step session ======= Identification =====================");
        poTransaction.processIdentification(fciData);

        // Step 2A
        System.out.println(
                "========= PO Hoplink 2-step session ======= Opening + Closing ====================");
        // Read first record of SFI 08h - for 15h bytes
        byte debitKeyIndex = 0x03;
        // Open Session for the debit key #1 - with read of the first record of the cyclic EF of
        // SFI 0Ah
        AbstractOpenSessionCmdBuild poOpenSession =
                AbstractOpenSessionCmdBuild.create(poTransaction.getRevision(), debitKeyIndex,
                        poTransaction.sessionTerminalChallenge, (byte) 0x1A, (byte) 0x01);

        poTransaction.processOpeningClosing(poOpenSession, filesToReadInSession, null,
                closeSeChannel);

        if (poTransaction.isSuccessful()) {
            System.out.println(
                    "========= PO Hoplink 2-step session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
            System.out.println(
                    "========= PO Hoplink 2-step session ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void doHoplinkThreeStepReadWriteTransaction(PoSecureSession poTransaction,
            ApduResponse fciData, boolean closeSeChannel) throws IOReaderException {


        List<PoSendableInSession> filesToReadInSession = new ArrayList<PoSendableInSession>();
        filesToReadInSession.add(HoplinkSampleCommands.poReadRecordCmd_T2Env);
        filesToReadInSession.add(HoplinkSampleCommands.poReadRecordCmd_T2Usage);
        // filesToReadInSession.add(HoplinkSampleCommands.poUpdateRecordCmd_T2UsageFill);

        // Step 1
        System.out.println(
                "\n\n========= PO Hoplink 3-step session ======= Identification =====================");
        poTransaction.processIdentification(fciData);

        // Step 2
        System.out.println(
                "========= PO Hoplink 3-step session ======= Opening ============================");
        // Read first record of SFI 08h - for 15h bytes
        byte debitKeyIndex = 0x03;
        // Open Session for the debit key #1 - with read of the first record of the cyclic EF of
        // SFI 0Ah
        AbstractOpenSessionCmdBuild poOpenSession =
                AbstractOpenSessionCmdBuild.create(poTransaction.getRevision(), debitKeyIndex,
                        poTransaction.sessionTerminalChallenge, (byte) 0x1A, (byte) 0x01);
        poTransaction.processOpening(poOpenSession, filesToReadInSession);

        // Step 3
        System.out.println(
                "========= PO Hoplink 3-step session ======= Continuation =======================");
        poTransaction.processProceeding(filesToReadInSession);

        // DONE: Find something better
        // poReadRecordCmd_T2EnvR1.getApduRequest().getBytes().position(0);

        // Step 4
        System.out.println(
                "========= PO Hoplink 3-step session ======= Closing ============================");
        poTransaction.processClosing(null, null, HoplinkSampleCommands.poRatificationCommand, true);

        if (poTransaction.isSuccessful()) {
            System.out.println(
                    "========= PO Hoplink 3-step session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
            System.out.println(
                    "========= PO Hoplink 3-step session ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void operateSeveralHoplinkSessions(PoSecureSession poTransaction, ApduResponse fciData)
            throws IOReaderException {
        // execute a two-step Calypso session: processIdentification, processOpeningClosing
        // keep the logical channel opened
        doHoplinkTwoStepAuthentication(poTransaction, fciData, false);

        // execute a three-step Calypso session: processIdentification, processOpening,
        // processClosing
        // close the logical channel opened
        doHoplinkThreeStepReadWriteTransaction(poTransaction, fciData, true);

        // redo the Hoplink PO selection after logical channel closing (may be not needed with some
        // PO
        // for which the application is selected by default)
        SeRequestSet selectionRequest = new SeRequestSet(
                new SeRequest(ByteBufferUtils.fromHex(HoplinkCommandsSettings.AID), null, true));
        fciData = poReader.transmit(selectionRequest).getSingleResponse().getFci();

        // execute a two-step Calypso session: processIdentification, processOpeningClosing
        // close the logical channel opened
        doHoplinkTwoStepAuthentication(poTransaction, fciData, true);
    }

    public void operatePoTransactions() {
        PoSecureSession poTransaction = new PoSecureSession(poReader, csmReader, (byte) 0x00);

        try {
            // operate PO multiselection
            String poFakeAid = "AABBCCDDEE"; //
            String poNavigoAid = "A0000004040125090101"; // Navigo AID
            String poHoplinkAid = HoplinkCommandsSettings.AID; // commands before session, keep true

            // prepare the PO selection SeRequestSet

            // Create a SeRequest list
            Set<SeRequest> selectionRequests = new LinkedHashSet<SeRequest>();

            // fake application seRequest preparation, addition to the list
            SeRequest seRequest = new SeRequest(ByteBufferUtils.fromHex(poFakeAid), null, false);
            selectionRequests.add(seRequest);

            // Navigo application seRequest preparation, addition to the list
            seRequest = new SeRequest(ByteBufferUtils.fromHex(poNavigoAid), null, false);
            selectionRequests.add(seRequest);

            // Hoplink application seRequest preparation, addition to the list
            // read commands before session
            List<ApduRequest> requestToExecuteBeforeSession = new ArrayList<ApduRequest>();
            requestToExecuteBeforeSession
                    .add(HoplinkSampleCommands.poReadRecordCmd_T2Env.getApduRequest());
            seRequest = new SeRequest(ByteBufferUtils.fromHex(poHoplinkAid),
                    requestToExecuteBeforeSession, false,
                    HoplinkCommandsSettings.selectApplicationSuccessfulStatusCodes);
            selectionRequests.add(seRequest);

            List<SeResponse> seResponses =
                    poReader.transmit(new SeRequestSet(selectionRequests)).getResponses();

            Iterator<SeRequest> seReqIterator = selectionRequests.iterator();
            Iterator<SeResponse> seRespIterator = seResponses.iterator();

            // we expect 3 responses
            printSelectAppResponseStatus("Case #1: fake AID", seReqIterator.next(),
                    seRespIterator.next());
            printSelectAppResponseStatus("Case #2: Navigo AID", seReqIterator.next(),
                    seRespIterator.next());
            printSelectAppResponseStatus("Case #3: Hoplink AID", seReqIterator.next(),
                    seRespIterator.next());

            if (seResponses.get(2) != null) {
                ApduResponse fciData = seResponses.get(2).getFci();
                operateSeveralHoplinkSessions(poTransaction, fciData);
            } else {
                System.out.println(
                        "No Hoplink transaction. SeResponse to Hoplink selection was null.");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Get the terminal which names match the expected pattern
     *
     * @param seProxyService SE Proxy service
     * @param pattern Pattern
     * @return ProxyReader
     * @throws IOReaderException Any error with the card communication
     */
    private static ProxyReader getReader(SeProxyService seProxyService, String pattern)
            throws IOReaderException {
        Pattern p = Pattern.compile(pattern);
        for (ReadersPlugin plugin : seProxyService.getPlugins()) {
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
            throws IOException, IOReaderException, InterruptedException {
        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReadersPlugin> pluginsSet = new ConcurrentSkipListSet<ReadersPlugin>();
        pluginsSet.add(PcscPlugin.getInstance());
        seProxyService.setPlugins(pluginsSet);

        ProxyReader poReader =
                getReader(seProxyService, KeypleGenericDemo_ReadersSettings.PO_READER_NAME_REGEX);
        ProxyReader csmReader =
                getReader(seProxyService, KeypleGenericDemo_ReadersSettings.CSM_READER_NAME_REGEX);


        if (poReader == csmReader || poReader == null || csmReader == null) {
            throw new IllegalStateException("Bad PO/CSM setup");
        }

        System.out.println("PO Reader  : " + poReader.getName());
        System.out.println("CSM Reader : " + csmReader.getName());

        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);
        csmReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        // provide the reader with the map
        ((AbstractLocalReader) poReader)
                .addSeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4);

        // Setting up ourself as an observer
        KeypleCalypsoDemo_HoplinkTransaction observer = new KeypleCalypsoDemo_HoplinkTransaction();
        observer.poReader = poReader;
        observer.csmReader = csmReader;

        // Set terminal as Observer of the first reader
        ((AbstractObservableReader) poReader).addObserver(observer);
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }


}

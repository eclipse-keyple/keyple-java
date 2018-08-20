/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.pc.calypso;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.command.PoSendableInSession;
import org.eclipse.keyple.calypso.command.po.builder.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.example.common.HoplinkInfoAndSampleCommands;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * This Calypso demonstration code consists in:
 * <ol>
 * <li>Setting up a two-reader configuration ({@link main}) and adding an observer method (@link
 * update)</li>
 * <li>Starting a card operation when a PO presence is notified ({@link operatePoTransactions})</li>
 * <li>Opening a logical channel with the CSM (C1 CSM is expected) see ({@link CSM_C1_ATR_REGEX})</li>
 * <li>Attempting to open a logical channel with the PO with 3 options:
 * <ul>
 * <li>Selection with a fake AID</li>
 * <li>Selection with a Navigo AID</li>
 * <li>Selection with a Hoplink AID</li>
 * </ul>
 * </li>
 * <li>Display SeRequest/SeResponse data ({@link printSelectAppResponseStatus})</li>
 * <li>If the Hoplink selection succeeded, do 3 Hoplink transactions ({@link
 * operateMultipleHoplinkTransactions}).</li>
 * </ol>
 * <p>
 * The Hoplink transactions demonstrated here illustrate the 2 and 3-step modes and also show the
 * logical channel management.
 * <p>
 * Read the doc of each methods for further details.
 * </ol>
 */
public class Demo_HoplinkTransaction implements ObservableReader.ReaderObserver {
    private ProxyReader poReader, csmReader;

    public Demo_HoplinkTransaction() {
        super();
    }

    @Override
    public void update(ReaderEvent event) {
        switch (event.getEventType()) {
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

    /**
     * Display SeRequest and SeResponse details in the console
     * 
     * @param message
     * @param seRequest
     * @param seResponse
     */
    private void printSelectAppResponseStatus(String message, SeRequest seRequest,
            SeResponse seResponse) {
        int i;
        System.out.println("===== " + message);
        System.out.println("* Request:");
        System.out.println("AID: " + ByteBufferUtils
                .toHex(((SeRequest.AidSelector) seRequest.getSelector()).getAidToSelect()));
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

    /**
     * Do an 2-step Hoplink transaction:
     * <ul>
     * <li>Process identification</li>
     * <li>Process Opening and Closing</li>
     * </ul>
     * <p>
     * File with SFI 1A is read at session opening.
     * <p>
     * T2 Environment and T2 Usage are read in session.
     * 
     * @param poTransaction
     * @param fciData
     * @param closeSeChannel
     * @throws IOReaderException
     */
    private void doHoplinkTwoStepAuthentication(PoSecureSession poTransaction, ApduResponse fciData,
            boolean closeSeChannel) throws IOReaderException {
        List<PoSendableInSession> filesToReadInSession = new ArrayList<PoSendableInSession>();
        filesToReadInSession.add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Env);
        filesToReadInSession.add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Usage);

        // Step 1
        System.out.println(
                "\n\n========= PO Hoplink 2-step transaction ======= Identification =====================");
        poTransaction.processIdentification(fciData);

        // Step 2A
        System.out.println(
                "========= PO Hoplink 2-step transaction ======= Opening + Closing ====================");

        byte debitKeyIndex = 0x03;
        // Open Session for the debit key #3 - with reading of the first record of the cyclic EF of
        // SFI 1Ah
        AbstractOpenSessionCmdBuild poOpenSession =
                AbstractOpenSessionCmdBuild.create(poTransaction.getRevision(), debitKeyIndex,
                        poTransaction.sessionTerminalChallenge, (byte) 0x1A, (byte) 0x01);

        poTransaction.processOpeningClosing(poOpenSession, filesToReadInSession, null,
                closeSeChannel);

        if (poTransaction.isSuccessful()) {
            System.out.println(
                    "========= PO Hoplink 2-step transaction ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
            System.out.println(
                    "========= PO Hoplink 2-step transaction ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    /**
     * Do an 3-step Hoplink transaction:
     * <ul>
     * <li>Process identification</li>
     * <li>Process Opening and Closing</li>
     * </ul>
     * <p>
     * File with SFI 1A is read at session opening.
     * <p>
     * T2 Environment and T2 Usage are read in session.
     * 
     * @param poTransaction
     * @param fciData
     * @param closeSeChannel
     * @throws IOReaderException
     */
    private void doHoplinkThreeStepReadWriteTransaction(PoSecureSession poTransaction,
            ApduResponse fciData, boolean closeSeChannel) throws IOReaderException {


        List<PoSendableInSession> filesToReadInSession = new ArrayList<PoSendableInSession>();
        filesToReadInSession.add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Env);
        filesToReadInSession.add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Usage);
        // filesToReadInSession.add(HoplinkInfoAndSampleCommands.poUpdateRecordCmd_T2UsageFill);

        // Step 1
        System.out.println(
                "\n\n========= PO Hoplink 3-step session ======= Identification =====================");
        poTransaction.processIdentification(fciData);

        // Step 2
        System.out.println(
                "========= PO Hoplink 3-step session ======= Opening ============================");
        byte debitKeyIndex = 0x03;
        // Open Session for the debit key #3 - with reading of the first record of the cyclic EF of
        // SFI 0Ah
        AbstractOpenSessionCmdBuild poOpenSession =
                AbstractOpenSessionCmdBuild.create(poTransaction.getRevision(), debitKeyIndex,
                        poTransaction.sessionTerminalChallenge, (byte) 0x1A, (byte) 0x01);
        poTransaction.processOpening(poOpenSession, filesToReadInSession);

        // Step 3
        System.out.println(
                "========= PO Hoplink 3-step session ======= Proceed =======================");
        poTransaction.processProceeding(filesToReadInSession);

        // Step 4
        System.out.println(
                "========= PO Hoplink 3-step session ======= Closing ============================");
        poTransaction.processClosing(null, null, HoplinkInfoAndSampleCommands.poRatificationCommand,
                false);

        if (poTransaction.isSuccessful()) {
            System.out.println(
                    "========= PO Hoplink 3-step session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
            System.out.println(
                    "========= PO Hoplink 3-step session ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    /**
     * Chain 3 Hoplink transactions: 2-step, 3-step, 2-step (see @link
     * doHoplinkTwoStepAuthentication and @link doHoplinkThreeStepReadWriteTransaction)
     * <p>
     * To illustrate the the logical channel management, it is kept open after the 1st transaction.
     * <p>
     * Closed after the end of the 2nd transaction and reopened before the 3rd transaction.
     * <p>
     * Finally the logical channel is closed at the end of the 3rd transaction.
     * 
     * @param poTransaction
     * @param fciData
     * @throws IOReaderException
     */
    private void operateMultipleHoplinkTransactions(PoSecureSession poTransaction,
            ApduResponse fciData) throws IOReaderException {
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
        SeRequestSet selectionRequest =
                new SeRequestSet(new SeRequest(
                        new SeRequest.AidSelector(
                                ByteBufferUtils.fromHex(HoplinkInfoAndSampleCommands.AID)),
                        null, true));
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
            String poHoplinkAid = HoplinkInfoAndSampleCommands.AID; // commands before session, keep
                                                                    // true
            String csmC1ATRregex = HoplinkInfoAndSampleCommands.CSM_C1_ATR_REGEX; // csm identifier

            // check the availability of the CSM, open its physical and logical channels and keep it
            // open
            SeRequest csmCheckRequest =
                    new SeRequest(new SeRequest.AtrSelector(csmC1ATRregex), null, true);
            SeResponse csmCheckResponse =
                    csmReader.transmit(new SeRequestSet(csmCheckRequest)).getSingleResponse();

            if (csmCheckResponse == null) {
                System.out.println("Unable to open a logical channel for CSM!");
                throw new IllegalStateException("CSM channel opening failure");
            }

            // prepare the PO selection SeRequestSet
            // Create a SeRequest list
            Set<SeRequest> selectionRequests = new LinkedHashSet<SeRequest>();

            // fake application seRequest preparation, addition to the list
            SeRequest seRequest = new SeRequest(
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poFakeAid)), null, false);
            selectionRequests.add(seRequest);

            // Navigo application seRequest preparation, addition to the list
            seRequest = new SeRequest(
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poNavigoAid)), null, false);
            selectionRequests.add(seRequest);

            // Hoplink application seRequest preparation, addition to the list
            // read commands before session
            List<ApduRequest> requestToExecuteBeforeSession = new ArrayList<ApduRequest>();
            requestToExecuteBeforeSession
                    .add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Env.getApduRequest());

            // AID based selection
            seRequest =
                    new SeRequest(new SeRequest.AidSelector(ByteBufferUtils.fromHex(poHoplinkAid)),
                            requestToExecuteBeforeSession, false,
                            HoplinkInfoAndSampleCommands.selectApplicationSuccessfulStatusCodes);

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

            // test if the Hoplink selection succeeded
            if (seResponses.get(2) != null) {
                ApduResponse fciData = seResponses.get(2).getFci();
                operateMultipleHoplinkTransactions(poTransaction, fciData);
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
        for (ReaderPlugin plugin : seProxyService.getPlugins()) {
            for (ProxyReader reader : plugin.getReaders()) {
                if (p.matcher(reader.getName()).matches()) {
                    return reader;
                }
            }
        }
        return null;
    }

    /**
     * This object is used to freeze the main thread while card operations are handle through the
     * observers callbacks. A call to the notify() method would end the program (not demonstrated
     * here).
     */
    private static final Object waitForEnd = new Object();

    public static void main(String[] args)
            throws IOException, IOReaderException, InterruptedException {
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

        System.out.println("PO Reader  : " + poReader.getName());
        System.out.println("CSM Reader : " + csmReader.getName());

        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);
        csmReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        // provide the reader with the map
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // Setting up ourself as an observer
        Demo_HoplinkTransaction observer = new Demo_HoplinkTransaction();
        observer.poReader = poReader;
        observer.csmReader = csmReader;

        // Set terminal as Observer of the first reader
        ((ObservableReader) poReader).addObserver(observer);
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }
}

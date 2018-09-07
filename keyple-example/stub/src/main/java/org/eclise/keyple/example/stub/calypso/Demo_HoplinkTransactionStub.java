/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.stub.calypso;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.example.common.HoplinkInfoAndSampleCommands;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_HoplinkTransactionStub implements ObservableReader.ReaderObserver {

    private static final Logger logger = LoggerFactory.getLogger(Demo_HoplinkTransactionStub.class);

    private ProxyReader poReader, csmReader;

    public Demo_HoplinkTransactionStub() {}

    @Override
    public void update(ReaderEvent event) {
        switch (event.getEventType()) {
            case SE_INSERTED:
                logger.info("SE INSERTED");
                logger.info("\nStart processing of a Calypso PO");
                operatePoTransactions();
                break;
            case SE_REMOVAL:
                logger.info("SE REMOVED");
                logger.info("\nWait for Calypso PO");
                break;
            default:
                logger.error("IO Error");
        }
    }

    /**
     * Display SeRequest and SeResponse details in the console
     *
     * @param message user message
     * @param seRequest current SeRequest
     * @param seResponse current SeResponse (defined as public for purposes of javadoc)
     */
    public void printSelectAppResponseStatus(String message, SeRequest seRequest,
            SeResponse seResponse) {
        int i;
        logger.info("===== " + message);
        logger.info("* Request:");
        logger.info("AID: " + ByteBufferUtils
                .toHex(((SeRequest.AidSelector) seRequest.getSelector()).getAidToSelect()));
        List<ApduRequest> apduRequests = seRequest.getApduRequests();
        i = 0;
        if (apduRequests != null && apduRequests.size() > 0) {
            for (ApduRequest apduRequest : apduRequests) {
                logger.info("COMMAND#" + i + ": " + ByteBufferUtils.toHex(apduRequest.getBytes()));
                i++;
            }
        } else {
            logger.info("No APDU request");
        }
        logger.info("keepChannelOpen flag: " + seRequest.isKeepChannelOpen());
        logger.info("protocol flag: " + seRequest.getProtocolFlag());

        logger.info("* Response:");
        if (seResponse == null) {
            logger.info("SeResponse is null");
        } else {
            ApduResponse atr, fci;
            atr = seResponse.getAtr();
            fci = seResponse.getFci();
            List<ApduResponse> apduResponses = seResponse.getApduResponses();
            if (atr != null) {
                logger.info("ATR: " + ByteBufferUtils.toHex(atr.getDataOut()));
            } else {
                logger.info("ATR: null");
            }
            if (fci != null) {
                logger.info("FCI: " + ByteBufferUtils.toHex(fci.getDataOut()));
            } else {
                logger.info("FCI: null");
            }
            if (apduResponses.size() > 0) {
                i = 0;
                for (ApduResponse apduResponse : apduResponses) {
                    logger.info("RESPONSE#" + i + ": "
                            + ByteBufferUtils.toHex(apduResponse.getDataOut()) + ", SW1SW2: "
                            + Integer.toHexString(apduResponse.getStatusCode() & 0xFFFF));
                    i++;
                }
            }
        }
        // new line
        logger.info("");
    }

    /**
     * Do an Hoplink transaction:
     * <ul>
     * <li>Process opening</li>
     * <li>Process PO commands</li>
     * <li>Process closing</li>
     * </ul>
     * <p>
     * File with SFI 1A is read at session opening.
     * <p>
     * T2 Environment and T2 Usage are read in session.
     *
     * @param poTransaction PoSecureSession object
     * @param fciData FCI data from the selection step
     * @param closeSeChannel flag to ask or not the channel closing at the end of the transaction
     * @throws IOReaderException reader exception (defined as public for purposes of javadoc)
     */
    public void doHoplinkReadWriteTransaction(PoSecureSession poTransaction, ApduResponse fciData,
            boolean closeSeChannel) throws IOReaderException {


        List<PoSendableInSession> filesToReadInSession = new ArrayList<PoSendableInSession>();
        filesToReadInSession.add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Env);
        filesToReadInSession.add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Usage);
        // filesToReadInSession.add(HoplinkInfoAndSampleCommands.poUpdateRecordCmd_T2UsageFill);

        System.out.println(
                "========= PO Hoplink session ======= Opening ============================");
        PoSecureSession.SessionAccessLevel accessLevel =
                PoSecureSession.SessionAccessLevel.SESSION_LVL_DEBIT;

        // Open Session for the debit key - with reading of the first record of the cyclic EF of
        // SFI 0Ah
        poTransaction.processOpening(fciData, accessLevel, (byte) 0x1A, (byte) 0x01,
                filesToReadInSession);

        System.out.println(
                "========= PO Hoplink session ======= Processing of PO commands =======================");
        poTransaction.processPoCommands(filesToReadInSession);

        System.out.println(
                "========= PO Hoplink session ======= Closing ============================");
        poTransaction.processClosing(null, null, HoplinkInfoAndSampleCommands.poRatificationCommand,
                false);

        if (poTransaction.isSuccessful()) {
            System.out.println(
                    "========= PO Hoplink session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
            System.out.println(
                    "========= PO Hoplink session ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    /**
     * Chain 3 Hoplink transactions: 2-step, 3-step, 2-step (see @link
     * doHoplinkTwoStepAuthentication and @link doHoplinkReadWriteTransaction)
     * <p>
     * To illustrate the the logical channel management, it is kept open after the 1st transaction.
     * <p>
     * Closed after the end of the 2nd transaction and reopened before the 3rd transaction.
     * <p>
     * Finally the logical channel is closed at the end of the 3rd transaction.
     *
     * @param poTransaction PoSecureSession object
     * @param fciData FCI data from the selection step
     * @throws IOReaderException reader exception (defined as public for purposes of javadoc)
     */
    public void operateMultipleHoplinkTransactions(PoSecureSession poTransaction,
            ApduResponse fciData) throws IOReaderException {
        // execute a Calypso session: processOpening, processPoCommands, processClosing
        // close the logical channel
        doHoplinkReadWriteTransaction(poTransaction, fciData, true);

        // redo the Hoplink PO selection after logical channel closing (may be not needed with some
        // PO
        // for which the application is selected by default)
        SeRequestSet selectionRequest =
                new SeRequestSet(new SeRequest(
                        new SeRequest.AidSelector(
                                ByteBufferUtils.fromHex(HoplinkInfoAndSampleCommands.AID)),
                        null, true));
        fciData = poReader.transmit(selectionRequest).getSingleResponse().getFci();

        doHoplinkReadWriteTransaction(poTransaction, fciData, true);
    }

    /**
     * Do the PO selection and possibly go on with Hoplink transactions.
     */
    public void operatePoTransactions() {
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

            EnumMap<PoSecureSession.CsmSettings, Byte> csmSetting =
                    new EnumMap<PoSecureSession.CsmSettings, Byte>(
                            PoSecureSession.CsmSettings.class);

            csmSetting.put(PoSecureSession.CsmSettings.CS_DEFAULT_KIF_PERSO,
                    PoSecureSession.DEFAULT_KIF_PERSO);
            csmSetting.put(PoSecureSession.CsmSettings.CS_DEFAULT_KIF_LOAD,
                    PoSecureSession.DEFAULT_KIF_LOAD);
            csmSetting.put(PoSecureSession.CsmSettings.CS_DEFAULT_KIF_DEBIT,
                    PoSecureSession.DEFAULT_KIF_DEBIT);
            csmSetting.put(PoSecureSession.CsmSettings.CS_DEFAULT_KEY_RECORD_NUMBER,
                    PoSecureSession.DEFAULT_KEY_RECORD_NUMER);

            PoSecureSession poTransaction = new PoSecureSession(poReader, csmReader, csmSetting);

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
     * @param pattern regex pattern to select a reader
     * @return ProxyReader
     * @throws IOReaderException Any error with the card communication (defined as public for
     *         purposes of javadoc)
     */
    public ProxyReader getReader(SeProxyService seProxyService, String pattern)
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

    /**
     * main program entry
     *
     * @param args the program arguments
     * @throws IOException setParameter exception
     * @throws IOReaderException reader exception
     * @throws InterruptedException thread exception
     */
    public static void main(String[] args)
            throws IOException, IOReaderException, InterruptedException {

        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReaderPlugin> pluginsSet = new ConcurrentSkipListSet<ReaderPlugin>();
        pluginsSet.add(StubPlugin.getInstance());
        seProxyService.setPlugins(pluginsSet);

        // Setting up ourself as an observer
        Demo_HoplinkTransactionStub observer = new Demo_HoplinkTransactionStub();

        StubReader poReader = StubPlugin.getInstance().plugStubReader("poReader");
        StubReader csmReader = StubPlugin.getInstance().plugStubReader("csmReader");


        logger.info("PO Reader  : " + poReader.getName());
        logger.info("CSM Reader : " + csmReader.getName());


        observer.poReader = poReader;
        observer.csmReader = csmReader;

        // Set terminal as Observer of the first reader
        ((ObservableReader) poReader).addObserver(observer);


        StubSecureElement hoplinkSE = new HoplinkStubSE();
        StubSecureElement csmSE = new CSMStubSE();

        csmReader.insertSe(csmSE);
        poReader.insertSe(hoplinkSE);
        poReader.removeSe();


    }
}

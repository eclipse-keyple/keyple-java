/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.common;

import static org.eclipse.keyple.calypso.transaction.PoSecureSession.*;
import static org.eclipse.keyple.calypso.transaction.PoSecureSession.CsmSettings.*;
import java.util.*;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

/**
 * This Calypso demonstration code consists in:
 *
 * <ol>
 * <li>Setting up a two-reader configuration and adding an observer method ({@link #update update})
 * <li>Starting a card operation when a PO presence is notified ({@link #operatePoTransactions
 * operatePoTransactions})
 * <li>Opening a logical channel with the CSM (C1 CSM is expected) see
 * ({@link org.eclipse.keyple.example.common.HoplinkInfoAndSampleCommands#CSM_C1_ATR_REGEX
 * CSM_C1_ATR_REGEX})
 * <li>Attempting to open a logical channel with the PO with 3 options:
 * <ul>
 * <li>Selection with a fake AID
 * <li>Selection with a Navigo AID
 * <li>Selection with a Hoplink AID
 * </ul>
 * <li>Display SeRequest/SeResponse data ({@link #printSelectAppResponseStatus
 * printSelectAppResponseStatus})
 * <li>If the Hoplink selection succeeded, do 3 Hoplink transactions
 * ({@link #operateMultipleHoplinkTransactions operateMultipleHoplinkTransactions}).
 * </ol>
 *
 * <p>
 * The Hoplink transactions demonstrated here shows the Keyple API in use with Hoplink SE (PO and
 * CSM).
 *
 * <p>
 * Read the doc of each methods for further details.
 */
public class Demo_HoplinkTransactionEngine implements ObservableReader.ReaderObserver {
    private final static Logger logger =
            LoggerFactory.getLogger(Demo_HoplinkTransactionEngine.class);

    /* define the CSM parameters to provide when creating PoSecureSession */
    final static EnumMap<PoSecureSession.CsmSettings, Byte> csmSetting =
            new EnumMap<PoSecureSession.CsmSettings, Byte>(PoSecureSession.CsmSettings.class) {
                {
                    put(CS_DEFAULT_KIF_PERSO, DEFAULT_KIF_PERSO);
                    put(CS_DEFAULT_KIF_LOAD, DEFAULT_KIF_LOAD);
                    put(CS_DEFAULT_KIF_DEBIT, DEFAULT_KIF_DEBIT);
                    put(CS_DEFAULT_KEY_RECORD_NUMBER, DEFAULT_KEY_RECORD_NUMER);
                }
            };;

    ProxyReader poReader, csmReader;

    Profiler profiler;

    /* Constructor */
    public Demo_HoplinkTransactionEngine() {
        super();
    }

    /* Assign readers to the transaction engine */
    public void setReaders(ProxyReader poReader, ProxyReader csmReader) {
        this.poReader = poReader;
        this.csmReader = csmReader;
    }

    /* Check CSM presence and consistency */
    public boolean checkCsm() {
        boolean csmOk;
        /*
         * check the availability of the CSM, open its physical and logical channels and keep it
         * open
         */
        String csmC1ATRregex = HoplinkInfoAndSampleCommands.CSM_C1_ATR_REGEX; // csm identifier

        /* open CSM logical channel */
        SeRequest csmCheckRequest =
                new SeRequest(new SeRequest.AtrSelector(csmC1ATRregex), null, true);
        SeResponse csmCheckResponse = null;
        try {
            csmCheckResponse =
                    csmReader.transmit(new SeRequestSet(csmCheckRequest)).getSingleResponse();
            if (csmCheckResponse == null) {
                logger.error("Unable to open a logical channel for CSM!");
                csmOk = false;
            } else {
                csmOk = true;
            }
        } catch (IOReaderException e) {
            logger.error("Reader exception: CAUSE = {}", e.getMessage());
            csmOk = false;
        }

        return csmOk;
    }

    @Override
    /*
     * This method is called when an reader event occurs according to the Observer pattern
     */
    public void update(ReaderEvent event) {
        switch (event.getEventType()) {
            case SE_INSERTED:
                logger.info("SE INSERTED");
                logger.info("Start processing of a Calypso PO");
                operatePoTransactions();
                break;
            case SE_REMOVAL:
                logger.info("SE REMOVED");
                logger.info("Wait for Calypso PO");
                break;
            default:
                logger.error("IO Error");
        }
    }

    /**
     * Output SeRequest and SeResponse details in the log flow
     * 
     * @param message user message
     * @param seRequest current SeRequest
     * @param seResponse current SeResponse (defined as public for purposes of javadoc)
     */
    public void printSelectAppResponseStatus(String message, SeRequest seRequest,
            SeResponse seResponse) {
        int i;
        logger.info("===== " + message);
        logger.info("* Request: AID = {}, keepChannelOpenFlag = {}, protocolFlag = {}",
                ByteBufferUtils
                        .toHex(((SeRequest.AidSelector) seRequest.getSelector()).getAidToSelect()),
                seRequest.isKeepChannelOpen(), seRequest.getProtocolFlag());
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

        logger.info("* Response:");
        if (seResponse == null) {
            logger.info("SeResponse is null");
        } else {
            ApduResponse atr, fci;
            atr = seResponse.getAtr();
            fci = seResponse.getFci();
            List<ApduResponse> apduResponses = seResponse.getApduResponses();
            logger.info("Atr = {}, Fci = {}",
                    atr == null ? "null" : ByteBufferUtils.toHex(atr.getBytes()),
                    fci == null ? "null" : ByteBufferUtils.toHex(fci.getBytes()));
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
    }

    /**
     * Do an Hoplink transaction:
     * <ul>
     * <li>Process opening</li>
     * <li>Process PO commands</li>
     * <li>Process closing</li>
     * </ul>
     * <p>
     * File with SFI 1A is read on session opening.
     * <p>
     * T2 Environment and T2 Usage are read in session.
     * <p>
     * The PO logical channel is kept open or closed according to the closeSeChannel flag
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

        /*
         * the modification command sent sent on closing is disabled for the moment due to CAAD
         * configuration of the current Hoplink test PO
         */
        // List<PoModificationCommand> poModificationCommands = new
        // ArrayList<PoModificationCommand>();
        // poModificationCommands.add(HoplinkInfoAndSampleCommands.poUpdateRecordCmd_T2UsageFill);

        List<ApduResponse> poAnticipatedResponses = new ArrayList<ApduResponse>();
        poAnticipatedResponses.add(new ApduResponse(ByteBufferUtils.fromHex("9000"), null));

        logger.info("========= PO Hoplink session ======= Opening ============================");
        PoSecureSession.SessionAccessLevel accessLevel =
                PoSecureSession.SessionAccessLevel.SESSION_LVL_DEBIT;

        /*
         * Open Session for the debit key - with reading of the first record of the cyclic EF of SFI
         * 0Ah
         */
        poTransaction.processOpening(fciData, accessLevel, (byte) 0x1A, (byte) 0x01,
                filesToReadInSession);

        logger.info(
                "========= PO Hoplink session ======= Processing of PO commands =======================");
        poTransaction.processPoCommands(filesToReadInSession);

        logger.info("========= PO Hoplink session ======= Closing ============================");
        poTransaction.processClosing(null, null, HoplinkInfoAndSampleCommands.poRatificationCommand,
                false);
        // poTransaction.processClosing(poModificationCommands, poAnticipatedResponses,
        // HoplinkInfoAndSampleCommands.poRatificationCommand, false);

        if (poTransaction.isSuccessful()) {
            logger.info(
                    "========= PO Hoplink session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } else {
            logger.error(
                    "========= PO Hoplink session ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    /**
     * Chain 3 Hoplink transactions with different logical channel management cases. (see @link
     * doHoplinkReadWriteTransaction)
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
        /*
         * execute an Hoplink session: processOpening, processPoCommands, processClosing close the
         * logical channel
         */
        profiler.start("Hoplink1");
        doHoplinkReadWriteTransaction(poTransaction, fciData, true);


        profiler.start("Hoplink2");
        doHoplinkReadWriteTransaction(poTransaction, fciData, false);


        /*
         * redo the Hoplink PO selection after logical channel closing (may be not needed with some
         * PO for which the application is selected by default)
         */
        profiler.start("Re-selection");
        SeRequestSet selectionRequest =
                new SeRequestSet(new SeRequest(
                        new SeRequest.AidSelector(
                                ByteBufferUtils.fromHex(HoplinkInfoAndSampleCommands.AID)),
                        null, true));
        fciData = poReader.transmit(selectionRequest).getSingleResponse().getFci();

        profiler.start("Hoplink3");
        doHoplinkReadWriteTransaction(poTransaction, fciData, false);
    }

    /**
     * Do the PO selection and possibly go on with Hoplink transactions.
     */
    public void operatePoTransactions() {
        try {
            profiler = new Profiler("Entire transaction");

            /* operate multiple PO selections */
            String poFakeAid = "AABBCCDDEE"; // fake AID
            String poNavigoAid = "A0000004040125090101"; // Navigo AID
            String poHoplinkAid = HoplinkInfoAndSampleCommands.AID; // Hoplink AID

            /*
             * true prepare the PO selection SeRequestSet Create a SeRequest list
             */
            Set<SeRequest> selectionRequests = new LinkedHashSet<SeRequest>();

            /* fake application seRequest preparation, addition to the list */
            SeRequest seRequest = new SeRequest(
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poFakeAid)), null, false);
            selectionRequests.add(seRequest);

            /* Navigo application seRequest preparation, addition to the list */
            seRequest = new SeRequest(
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poNavigoAid)), null, false);
            selectionRequests.add(seRequest);

            /*
             * Hoplink application seRequest preparation, addition to the list read commands before
             * session
             */
            List<ApduRequest> requestToExecuteBeforeSession = new ArrayList<ApduRequest>();
            requestToExecuteBeforeSession
                    .add(HoplinkInfoAndSampleCommands.poReadRecordCmd_T2Env.getApduRequest());

            /* AID based selection */
            seRequest =
                    new SeRequest(new SeRequest.AidSelector(ByteBufferUtils.fromHex(poHoplinkAid)),
                            requestToExecuteBeforeSession, false,
                            HoplinkInfoAndSampleCommands.selectApplicationSuccessfulStatusCodes);

            selectionRequests.add(seRequest);

            /* Time measurement */
            profiler.start("Initial selection");

            List<SeResponse> seResponses =
                    poReader.transmit(new SeRequestSet(selectionRequests)).getResponses();

            Iterator<SeRequest> seReqIterator = selectionRequests.iterator();
            Iterator<SeResponse> seRespIterator = seResponses.iterator();

            /* we expect 3 responses */
            printSelectAppResponseStatus("Case #1: fake AID", seReqIterator.next(),
                    seRespIterator.next());
            printSelectAppResponseStatus("Case #2: Navigo AID", seReqIterator.next(),
                    seRespIterator.next());
            printSelectAppResponseStatus("Case #3: Hoplink AID", seReqIterator.next(),
                    seRespIterator.next());

            PoSecureSession poTransaction = new PoSecureSession(poReader, csmReader, csmSetting);

            /* test if the Hoplink selection succeeded */
            if (seResponses.get(2) != null) {
                ApduResponse fciData = seResponses.get(2).getFci();
                operateMultipleHoplinkTransactions(poTransaction, fciData);
            } else {
                logger.info("No Hoplink transaction. SeResponse to Hoplink selection was null.");
            }

            profiler.stop();
            logger.warn(System.getProperty("line.separator") + "{}", profiler);
        } catch (Exception e) {
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
     *         Javadoc purposes)
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
}

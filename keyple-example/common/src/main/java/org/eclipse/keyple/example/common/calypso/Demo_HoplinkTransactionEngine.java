/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.common.calypso;

import static org.eclipse.keyple.calypso.transaction.PoSecureSession.*;
import static org.eclipse.keyple.calypso.transaction.PoSecureSession.CsmSettings.*;
import java.util.*;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
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
 * ({@link HoplinkInfoAndSampleCommands#CSM_C1_ATR_REGEX CSM_C1_ATR_REGEX})
 * <li>Attempting to open a logical channel with the PO with 3 options:
 * <ul>
 * <li>Selection with a fake AID
 * <li>Selection with a Navigo AID
 * <li>Selection with a Hoplink AID
 * </ul>
 * <li>Display SeRequest/SeResponse data ({@link #printSelectAppResponseStatus
 * printSelectAppResponseStatus})
 * <li>If the Hoplink selection succeeded, do an Hoplink transaction
 * ({@link #doHoplinkReadWriteTransaction(PoSecureSession, ApduResponse, boolean)}
 * doHoplinkReadWriteTransaction}).
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

    boolean csmChannelOpen;

    /* Constructor */
    public Demo_HoplinkTransactionEngine() {
        super();
        this.csmChannelOpen = false;
    }

    /* Assign readers to the transaction engine */
    public void setReaders(ProxyReader poReader, ProxyReader csmReader) {
        this.poReader = poReader;
        this.csmReader = csmReader;
    }

    /**
     * Check CSM presence and consistency
     *
     * Throw an exception if the expected CSM is not available
     */
    private void checkCsmAndOpenChannel() {
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
                throw new IllegalStateException("Unable to open a logical channel for CSM!");
            } else {
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());

        }
    }

    @Override
    /*
     * This method is called when an reader event occurs according to the Observer pattern
     */
    public void update(ReaderEvent event) {
        switch (event.getEventType()) {
            case SE_INSERTED:
                if (logger.isInfoEnabled()) {
                    logger.info("SE INSERTED");
                    logger.info("Start processing of a Calypso PO");
                }
                operatePoTransactions();
                break;
            case SE_REMOVAL:
                if (logger.isInfoEnabled()) {
                    logger.info("SE REMOVED");
                    logger.info("Wait for Calypso PO");
                }
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
     * @throws KeypleReaderException reader exception (defined as public for purposes of javadoc)
     */
    public void doHoplinkReadWriteTransaction(PoSecureSession poTransaction, ApduResponse fciData,
            boolean closeSeChannel) throws KeypleReaderException {


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

        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Hoplink session ======= Opening ============================");
        }
        PoSecureSession.SessionAccessLevel accessLevel =
                PoSecureSession.SessionAccessLevel.SESSION_LVL_DEBIT;

        /*
         * Open Session for the debit key - with reading of the first record of the cyclic EF of SFI
         * 0Ah
         */
        poTransaction.processOpening(fciData, accessLevel, (byte) 0x1A, (byte) 0x01,
                filesToReadInSession);

        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Hoplink session ======= Processing of PO commands =======================");
        }
        poTransaction.processPoCommands(filesToReadInSession);

        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Hoplink session ======= Closing ============================");
        }
        poTransaction.processClosing(null, null, CommunicationMode.CONTACTLESS_MODE, false);

        if (poTransaction.isSuccessful()) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        "========= PO Hoplink session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        } else {
            logger.error(
                    "========= PO Hoplink session ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    /**
     * Do the PO selection and possibly go on with Hoplink transactions.
     */
    public void operatePoTransactions() {
        try {
            /* first time: check CSM */
            if (!this.csmChannelOpen) {
                /* the following method will throw an exception if the CSM is not available. */
                checkCsmAndOpenChannel();
                this.csmChannelOpen = true;
            }

            profiler = new Profiler("Entire transaction");

            /* operate multiple PO selections */
            String poFakeAid1 = "AABBCCDDEE"; // fake AID 1
            String poFakeAid2 = "EEDDCCBBAA"; // fake AID 2
            String poHoplinkAid = HoplinkInfoAndSampleCommands.AID; // Hoplink AID

            /*
             * true prepare the PO selection SeRequestSet Create a SeRequest list
             */
            Set<SeRequest> selectionRequests = new LinkedHashSet<SeRequest>();

            /* fake application seRequest preparation, addition to the list */
            SeRequest seRequest = new SeRequest(
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poFakeAid1)), null, true);
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
                            requestToExecuteBeforeSession, true,
                            HoplinkInfoAndSampleCommands.selectApplicationSuccessfulStatusCodes);

            selectionRequests.add(seRequest);

            /* fake application seRequest preparation, addition to the list */
            seRequest = new SeRequest(
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poFakeAid2)), null, true);
            selectionRequests.add(seRequest);


            /* Time measurement */
            profiler.start("Initial selection");

            List<SeResponse> seResponses =
                    poReader.transmit(new SeRequestSet(selectionRequests)).getResponses();

            Iterator<SeRequest> seReqIterator = selectionRequests.iterator();

            int responseIndex = 0;
            /*
             * we expect up to 3 responses, only one should be not null since the selection process
             * stops at the first successful selection
             */
            if (logger.isInfoEnabled()) {
                for (Iterator<SeResponse> seRespIterator = seResponses.iterator(); seRespIterator
                        .hasNext();) {
                    SeResponse seResponse = seRespIterator.next();
                    if (seResponse != null) {
                        printSelectAppResponseStatus(
                                String.format("Selection case #%d", responseIndex),
                                seReqIterator.next(), seResponse);
                    }
                    responseIndex++;
                }
            }

            PoSecureSession poTransaction = new PoSecureSession(poReader, csmReader, csmSetting);
            /*
             * If the Hoplink selection succeeded we should have 2 responses and the 2nd one not
             * null
             */
            if (seResponses.size() == 2 && seResponses.get(1) != null) {
                ApduResponse fciData = seResponses.get(1).getFci();
                profiler.start("Hoplink1");
                doHoplinkReadWriteTransaction(poTransaction, fciData, true);
            } else {
                logger.error("No Hoplink transaction. SeResponse to Hoplink selection was null.");
            }

            profiler.stop();
            logger.warn(System.getProperty("line.separator") + "{}", profiler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

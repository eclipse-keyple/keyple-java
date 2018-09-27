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
import static org.eclipse.keyple.example.common.calypso.CalypsoBasicInfoAndSampleCommands.*;
import java.util.*;
import org.eclipse.keyple.calypso.command.po.PoModificationCommand;
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
 * ({@link CalypsoBasicInfoAndSampleCommands#CSM_C1_ATR_REGEX CSM_C1_ATR_REGEX})
 * <li>Attempting to open a logical channel with the PO with 3 options:
 * <ul>
 * <li>Selection with a fake AID
 * <li>Selection with a Navigo AID
 * <li>Selection with a Hoplink AID
 * </ul>
 * <li>Display SeRequest/SeResponse data ({@link #printSelectAppResponseStatus
 * printSelectAppResponseStatus})
 * <li>If the Calypso selection succeeded, do a Calypso transaction
 * ({@link #doCalypsoReadWriteTransaction(PoSecureSession, ApduResponse, boolean)}
 * doCalypsoReadWriteTransaction}).
 * </ol>
 *
 * <p>
 * The Calypso transactions demonstrated here shows the Keyple API in use with Calypso SE (PO and
 * CSM).
 *
 * <p>
 * Read the doc of each methods for further details.
 */
public class Demo_CalypsoBasicTransactionEngine implements ObservableReader.ReaderObserver {
    private final static Logger logger =
            LoggerFactory.getLogger(Demo_CalypsoBasicTransactionEngine.class);

    /* define the CSM parameters to provide when creating PoSecureSession */
    final static EnumMap<CsmSettings, Byte> csmSetting =
            new EnumMap<CsmSettings, Byte>(CsmSettings.class) {
                {
                    put(CS_DEFAULT_KIF_PERSO, DEFAULT_KIF_PERSO);
                    put(CS_DEFAULT_KIF_LOAD, DEFAULT_KIF_LOAD);
                    put(CS_DEFAULT_KIF_DEBIT, DEFAULT_KIF_DEBIT);
                    put(CS_DEFAULT_KEY_RECORD_NUMBER, DEFAULT_KEY_RECORD_NUMER);
                }
            };;

    private ProxyReader poReader, csmReader;

    Profiler profiler;

    boolean csmChannelOpen;

    /* Constructor */
    public Demo_CalypsoBasicTransactionEngine() {
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
        String csmC1ATRregex = CalypsoBasicInfoAndSampleCommands.CSM_C1_ATR_REGEX; // csm identifier

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
     * Do an Calypso transaction:
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
    public void doCalypsoReadWriteTransaction(PoSecureSession poTransaction, ApduResponse fciData,
            boolean closeSeChannel) throws KeypleReaderException {

        /* SeResponse object to receive the results of PoSecureSession operations. */
        SeResponse seResponse;

        /*
         * Read commands to execute during the opening step: EventLog, ContractList
         */
        List<PoSendableInSession> eventLogContractListFilesReading =
                new ArrayList<PoSendableInSession>();

        eventLogContractListFilesReading.add(poReadRecordCmd_EventLog);
        eventLogContractListFilesReading.add(poReadRecordCmd_ContractList);

        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Calypso session ======= Opening ============================");
        }
        SessionAccessLevel accessLevel = SessionAccessLevel.SESSION_LVL_DEBIT;

        /*
         * Open Session for the debit key - with reading of the first record of the cyclic EF of
         * Environment and Holder file
         */
        seResponse = poTransaction.processOpening(fciData, accessLevel, SFI_EnvironmentAndHolder,
                RECORD_NUMBER_1, eventLogContractListFilesReading);

        /*
         * [------------------------------------]
         *
         * Place to analyze the PO profile available in seResponse: Environment/Holder, EventLog,
         * ContractList.
         *
         * The information available allows the determination of the contract to be read.
         *
         * [------------------------------------]
         */

        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Calypso session ======= Processing of PO commands =======================");
        }

        /* Read contract command (we assume we have determine Contract #1 to be read. */
        List<PoSendableInSession> contractFileReading = new ArrayList<PoSendableInSession>();
        contractFileReading.add(poReadRecordCmd_Contract);

        seResponse = poTransaction.processPoCommands(contractFileReading);

        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Calypso session ======= Closing ============================");
        }

        /*
         * [------------------------------------]
         *
         * Place to analyze the Contract (in seResponse).
         *
         * The content of the contract will grant or not access.
         *
         * In any case, a new record will be added to the EventLog.
         *
         * [------------------------------------]
         */

        /* Create an modification command list (a single command here) */
        List<PoModificationCommand> eventLogAppend = new ArrayList<PoModificationCommand>();
        eventLogAppend.add(poAppendRecordCmd_EventLog);

        /*
         * The successful execution status of the Append Record command is anticipated.
         *
         * A ratification command is provided (short Read Record).
         */
        List<ApduResponse> poAnticipatedResponses = new ArrayList<ApduResponse>();
        poAnticipatedResponses.add(new ApduResponse(ByteBufferUtils.fromHex("9000"), null));

        seResponse = poTransaction.processClosing(eventLogAppend, poAnticipatedResponses,
                CommunicationMode.CONTACTLESS_MODE, false);

        if (poTransaction.isSuccessful()) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        "========= PO Calypso session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        } else {
            logger.error(
                    "========= PO Calypso session ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    /**
     * Do the PO selection and possibly go on with Calypso transactions.
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
            String poCalypsoAid = CalypsoBasicInfoAndSampleCommands.AID; // Calypso AID

            /*
             * Prepare the PO selection SeRequestSet
             *
             * Create a SeRequest list with various selection cases.
             */
            Set<SeRequest> selectionRequests = new LinkedHashSet<SeRequest>();

            /* fake application seRequest preparation, addition to the list */
            SeRequest seRequest = new SeRequest(
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poFakeAid1)), null, true);
            selectionRequests.add(seRequest);

            /*
             * Calypso application seRequest preparation, addition to the list read commands before
             * session
             */
            List<ApduRequest> requestToExecuteBeforeSession = new ArrayList<ApduRequest>();
            requestToExecuteBeforeSession.add(
                    CalypsoBasicInfoAndSampleCommands.poReadRecordCmd_EventLog.getApduRequest());

            /* AID based selection */
            seRequest = new SeRequest(
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poCalypsoAid)),
                    requestToExecuteBeforeSession, true,
                    CalypsoBasicInfoAndSampleCommands.selectApplicationSuccessfulStatusCodes);

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
             * If the Calypso selection succeeded we should have 2 responses and the 2nd one not
             * null
             */
            if (seResponses.size() == 2 && seResponses.get(1) != null) {
                ApduResponse fciData = seResponses.get(1).getFci();
                profiler.start("Calypso1");
                doCalypsoReadWriteTransaction(poTransaction, fciData, true);
            } else {
                logger.error("No Calypso transaction. SeResponse to Calypso selection was null.");
            }

            profiler.stop();
            logger.warn(System.getProperty("line.separator") + "{}", profiler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

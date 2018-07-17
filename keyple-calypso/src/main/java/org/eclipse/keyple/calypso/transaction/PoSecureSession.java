/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.transaction;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.commands.CsmSendableInSession;
import org.eclipse.keyple.calypso.commands.PoSendableInSession;
import org.eclipse.keyple.calypso.commands.SendableInSession;
import org.eclipse.keyple.calypso.commands.csm.CsmRevision;
import org.eclipse.keyple.calypso.commands.csm.builder.*;
import org.eclipse.keyple.calypso.commands.csm.parser.CsmGetChallengeRespPars;
import org.eclipse.keyple.calypso.commands.csm.parser.DigestAuthenticateRespPars;
import org.eclipse.keyple.calypso.commands.csm.parser.DigestCloseRespPars;
import org.eclipse.keyple.calypso.commands.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.commands.po.PoRevision;
import org.eclipse.keyple.calypso.commands.po.builder.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.commands.po.builder.CloseSessionCmdBuild;
import org.eclipse.keyple.calypso.commands.po.parser.AbstractOpenSessionRespPars;
import org.eclipse.keyple.calypso.commands.po.parser.CloseSessionRespPars;
import org.eclipse.keyple.calypso.commands.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.calypso.commands.utils.ApduUtils;
import org.eclipse.keyple.commands.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.InvalidMessageException;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;
import org.eclipse.keyple.util.ByteBufferUtils;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

/*
 * TODO improve the session state machine management (currentState) to force public methods to be
 * called in the right order
 */

/**
 * Portable Object Secure Session.
 *
 * A non-encrypted secure session with a Calypso PO requires the management of two
 * {@link ProxyReader} in order to communicate with both a Calypso PO and a CSM
 *
 * @author Calypso Networks Association
 */
public class PoSecureSession {

    private static final ILogger logger = SLoggerFactory.getLogger(PoSecureSession.class);

    /** The reader for PO. */
    private ProxyReader poReader;
    /** The reader for session CSM. */
    private ProxyReader csmReader;

    /**
     * The PO Transaction State defined with the elements: ‘IOError’, ‘SEInserted’ and ‘SERemoval’.
     */
    public enum SessionState {
        /** Initial state of a PO transaction. */
        PO_NOT_IDENTIFIED,
        /** Calypso application selected. */
        PO_IDENTIFIED,
        /** A secure session is active. */
        SESSION_OPEN,
        /** The transaction is finished. */
        SESSION_CLOSED,
    }

    /** the type of the notified event. */
    private SessionState currentState;

    /** Selected AID of the Calypso PO. */
    private ByteBuffer poCalypsoInstanceAid;
    /** Serial Number of the selected Calypso instance. */
    private ByteBuffer poCalypsoInstanceSerial;

    /** The PO Calypso Revision. */
    public PoRevision poRevision = PoRevision.REV3_1;// AbstractPoCommandBuilder.defaultRevision; //
    // TODO =>
    // add a getter

    /** The CSM default revision. */
    private CsmRevision csmRevision = CsmRevision.S1D;

    /** The default key index for a PO session. */
    private byte defaultKeyIndex;

    public ByteBuffer sessionTerminalChallenge;
    private ByteBuffer sessionCardChallenge;

    ByteBuffer sessionTerminalSignature;
    ByteBuffer sessionCardSignature;

    boolean transactionResult;

    /**
     * Instantiates a new po plain secure session.
     *
     * @param poReader the PO reader
     * @param csmReader the SAM reader
     * @param defaultKeyIndex default KIF index
     */
    // TODO: Ro replace the "defaultKeyIndex" byte parameter by a "csmSetting" Map<String> (or
    // ByteArray, or List<Byte>) parameter
    public PoSecureSession(ProxyReader poReader, ProxyReader csmReader, byte defaultKeyIndex) {
        this.poReader = poReader;
        this.csmReader = csmReader;

        this.defaultKeyIndex = defaultKeyIndex; // TODO => to fix

        currentState = SessionState.PO_NOT_IDENTIFIED;
    }

    /**
     * Secure Session management: step 1 Process identification from the previously selected PO
     * application. No communication is made with the PO One communication is made with the CSM to
     * operate the diversification and obtain a terminal session challenge. If the provided FCI
     * isn't a Calypso PO FCI an exception is thrown.
     * 
     * @param poFciData the po response to the application selection (FCI)
     * @throws IOReaderException the IO reader exception
     */
    public void processIdentification(ApduResponse poFciData) throws IOReaderException {
        // Init CSM ApduRequest List
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();
        // PO & CSM channels to be kept "Open"
        boolean keepChannelOpen = true;

        // Parse PO FCI - to retrieve Calypso Revision, Serial Number, & DF Name (AID)
        GetDataFciRespPars poFciRespPars = new GetDataFciRespPars(poFciData);
        poRevision = computePoRevision(poFciRespPars.getApplicationTypeByte());
        poCalypsoInstanceAid = poFciRespPars.getDfName();
        poCalypsoInstanceSerial = poFciRespPars.getApplicationSerialNumber();

        logger.info("Identification: PO Response", "action", "po_secure_session.ident_po_response",
                "dfName", ByteBufferUtils.toHex(poCalypsoInstanceAid), "serialNumber",
                ByteBufferUtils.toHex(poCalypsoInstanceSerial));

        // Define CSM Select Diversifier command
        AbstractApduCommandBuilder selectDiversifier =
                new SelectDiversifierCmdBuild(this.csmRevision, poCalypsoInstanceSerial);
        csmApduRequestList.add(selectDiversifier.getApduRequest());

        // Define CSM Get Challenge command
        AbstractApduCommandBuilder csmGetChallenge =
                new CsmGetChallengeCmdBuild(this.csmRevision, (byte) 0x04);
        csmApduRequestList.add(csmGetChallenge.getApduRequest());

        logger.info("Identification: CSM Request", "action", "po_secure_session.ident_csm_request",
                "apduReq1", ByteBufferUtils.toHex(selectDiversifier.getApduRequest().getBytes()),
                "apduReq2", ByteBufferUtils.toHex(csmGetChallenge.getApduRequest().getBytes()));

        // Transfert CSM commands
        // create a SeRequestSet (list of SeRequest)
        SeRequestSet csmRequest = new SeRequestSet(new SeRequest(null, csmApduRequestList, true));
        SeResponse csmResponse = csmReader.transmit(csmRequest).getSingleResponse();
        List<ApduResponse> csmApduResponseList = csmResponse.getApduResponses();

        logger.info("Identification: CSM Response", "action",
                "po_secure_session.ident_csm_response", "apduCount", csmApduResponseList.size());

        if (csmApduResponseList.size() == 2) {
            // TODO => check that csmApduResponseList.get(1) has the right length (challenge +
            // status)
            CsmGetChallengeRespPars csmChallengePars =
                    new CsmGetChallengeRespPars(csmApduResponseList.get(1));
            sessionTerminalChallenge = csmChallengePars.getChallenge();
            logger.info("Identification: Done", "action", "po_secure_session.ident_done", "apdu",
                    ByteBufferUtils.toHex(csmChallengePars.getApduResponse().getBytes()),
                    "sessionTerminalChallenge", ByteBufferUtils.toHex(sessionTerminalChallenge));
        } else {
            throw new InvalidMessageException("Invalid message received",
                    InvalidMessageException.Type.CSM, csmApduRequestList, csmApduResponseList);
        }

        currentState = SessionState.PO_IDENTIFIED;
    }

    /**
     * Secure Session management: step 2 Process opening. On poReader, generate a SERequest with the
     * current selected AID, with keepChannelOpen set at true, and apduRequests defined with
     * openCommand and the optional poCommands_InsideSession. Returns the corresponding SeResponse
     * (for openCommand and poCommands_InsideSession). Identifies the session PO keyset. On
     * csmSessionReader, automatically operate the Digest Init and potentially several Digest Update
     * Multiple.
     *
     * @param openCommand the open command
     * @param poCommandsInsideSession the po commands inside session
     * @return the SE response
     * @throws IOReaderException the IO reader exception
     */
    public SeResponse processOpening(AbstractOpenSessionCmdBuild openCommand,
            List<PoSendableInSession> poCommandsInsideSession) throws IOReaderException {

        // Init PO ApduRequest List
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();

        // Init CSM ApduRequest List
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();

        // Add Open Session command to PO ApduRequest list
        poApduRequestList.add(openCommand.getApduRequest());

        // Add list of PoSendableInSession commands to PO ApduRequest
        if ((poCommandsInsideSession != null) && !poCommandsInsideSession.isEmpty()) {
            poApduRequestList.addAll(this.getApduRequestsToSendInSession(
                    (List<SendableInSession>) (List<?>) poCommandsInsideSession));
        }

        // Transfert PO commands
        logger.info("Opening: PO request", "action", "po_secure_session.open_po_request");

        // create a SeRequestSet (list of SeRequest)
        SeRequestSet poRequests =
                new SeRequestSet(new SeRequest(poCalypsoInstanceAid, poApduRequestList, true));

        SeResponse poResponse = poReader.transmit(poRequests).getSingleResponse();
        List<ApduResponse> poApduResponseList = poResponse.getApduResponses();

        // Parse OpenSession Response to get Card Challenge
        if (poApduResponseList.isEmpty()) {
            throw new InvalidMessageException("No response", InvalidMessageException.Type.PO,
                    poApduRequestList, poApduResponseList);
        }
        if (!poApduResponseList.get(0).isSuccessful()) {
            throw new InvalidMessageException("Invalid PO opening response",
                    InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
        }
        // TODO: check that csmApduResponseList.get(1) has the right length (challenge + status)
        logger.info("Opening: PO commands", "action", "po_secure_session.open_po_send");

        // the response to open session is the first item of poApduResponseList
        AbstractOpenSessionRespPars poOpenSessionPars =
                AbstractOpenSessionRespPars.create(poApduResponseList.get(0), poRevision);
        sessionCardChallenge = poOpenSessionPars.getPoChallenge();

        // Build "Digest Init" command from PO Open Session
        byte kif = poOpenSessionPars.getSelectedKif();

        logger.info("Opening: PO response", "action", "po_secure_session.open_po_response",
                "apduResponse",
                ByteBufferUtils.toHex(poOpenSessionPars.getApduResponse().getBytes()),
                "sessionCardChallenge", ByteBufferUtils.toHex(sessionCardChallenge), "poKif",
                String.format("%02X", poOpenSessionPars.getSelectedKif()), "poKvc",
                String.format("%02X", poOpenSessionPars.getSelectedKvc()));

        if (kif == (byte) 0xFF) {
            if (defaultKeyIndex == (byte) 0x01) {
                kif = (byte) 0x21;
            } else if (defaultKeyIndex == (byte) 0x02) {
                kif = (byte) 0x27;
            } else if (defaultKeyIndex == (byte) 0x03) {
                kif = (byte) 0x30;
            }
        }
        AbstractApduCommandBuilder digestInit = new DigestInitCmdBuild(csmRevision, false,
                poRevision.equals(PoRevision.REV3_2), defaultKeyIndex, kif,
                poOpenSessionPars.getSelectedKvc(), poOpenSessionPars.getRecordDataRead());
        logger.info("Opening: CSM Request", "action", "po_secure_session.open_csm_digest_init",
                "apdu", ByteBufferUtils.toHex(digestInit.getApduRequest().getBytes()));

        csmApduRequestList.add(digestInit.getApduRequest());

        // Browse other PO commands to compute CSM digest
        if ((poCommandsInsideSession != null) && !poCommandsInsideSession.isEmpty()
                && (poCommandsInsideSession.size() > 1)) {
            // TODO => rajouter un contrôle afin de vérifier que poApduResponseList a même taille
            // que poApduRequestList
            for (int i = 1; i < poApduRequestList.size(); i++) { // The loop starts after the Open
                /*
                 * Session for the first command send in session Build "Digest Update" command for
                 * each PO APDU Request
                 */
                csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
                        poApduRequestList.get(i).getBytes())).getApduRequest());
                /*
                 * Build "Digest Update" command for each PO APDU Response //TODO => this is the
                 * right command, to fix ApduResponse.getBytes
                 */
                csmApduRequestList.add(((new DigestUpdateCmdBuild(csmRevision, false,
                        poApduResponseList.get(i).getBytes())).getApduRequest())); // HACK
            }
        }

        // Transfert CSM commands
        logger.info("Opening: CSM Request", "action", "po_secure_session.open_csm_request",
                "apduList", csmApduRequestList);

        // create a SeRequestSet (list of SeRequests)
        SeRequestSet csmRequest = new SeRequestSet(new SeRequest(null, csmApduRequestList, true));

        /* TODO Check responses. We do not check responses at the moment, but we should! */
        csmReader.transmit(csmRequest);

        currentState = SessionState.SESSION_OPEN;
        return poResponse;
    }

    /**
     * Secure Session management: step 2A Process opening. On poReader, generate a SERequest with
     * the current selected AID, with keepChannelOpen set at true, and apduRequests defined with
     * openCommand and the optional poCommands_InsideSession. Returns the corresponding SeResponse
     * (for openCommand and poCommands_InsideSession). Identifies the session PO keyset. On
     * csmSessionReader, automatically operate the Digest Init and potentially several Digest Update
     * Multiple.
     *
     * @param openCommand the open command
     * @param poCommandsInsideSession the po commands inside session
     * @return the SE response
     * @throws IOReaderException the IO reader exception
     */
    public SeResponse processOpeningClosing(AbstractOpenSessionCmdBuild openCommand,
            List<PoSendableInSession> poCommandsInsideSession,
            AbstractPoCommandBuilder ratificationCommand, boolean closeSeChannel)
            throws IOReaderException {

        /* First ================================================================= */

        // Init PO ApduRequest List
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();

        // Init CSM ApduRequest List
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();

        // Add Open Session command to PO ApduRequest list
        poApduRequestList.add(openCommand.getApduRequest());

        // Add list of PoSendableInSession commands to PO ApduRequest
        if ((poCommandsInsideSession != null) && !poCommandsInsideSession.isEmpty()) {
            poApduRequestList.addAll(this.getApduRequestsToSendInSession(
                    (List<SendableInSession>) (List<?>) poCommandsInsideSession));
        }

        // Transfert PO commands
        logger.info("Opening: PO request", "action", "po_secure_session.open_po_request");

        // create a SeRequestSet (list of SeRequest)
        SeRequestSet poRequests =
                new SeRequestSet(new SeRequest(poCalypsoInstanceAid, poApduRequestList, true));

        SeResponse poResponse = poReader.transmit(poRequests).getSingleResponse();
        List<ApduResponse> poApduResponseList = poResponse.getApduResponses();

        // Parse OpenSession Response to get Card Challenge
        if (poApduResponseList.isEmpty()) {
            throw new InvalidMessageException("No response", InvalidMessageException.Type.PO,
                    poApduRequestList, poApduResponseList);
        }
        if (!poApduResponseList.get(0).isSuccessful()) {
            throw new InvalidMessageException("Invalid PO opening response",
                    InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
        }
        // TODO: check that csmApduResponseList.get(1) has the right length (challenge + status)
        logger.info("Opening: PO commands", "action", "po_secure_session.open_po_send");

        // the response to open session is the first item of poApduResponseList
        AbstractOpenSessionRespPars poOpenSessionPars =
                AbstractOpenSessionRespPars.create(poApduResponseList.get(0), poRevision);
        sessionCardChallenge = poOpenSessionPars.getPoChallenge();

        // Build "Digest Init" command from PO Open Session
        byte kif = poOpenSessionPars.getSelectedKif();

        logger.info("Opening: PO response", "action", "po_secure_session.open_po_response",
                "apduResponse",
                ByteBufferUtils.toHex(poOpenSessionPars.getApduResponse().getBytes()),
                "sessionCardChallenge", ByteBufferUtils.toHex(sessionCardChallenge), "poKif",
                String.format("%02X", poOpenSessionPars.getSelectedKif()), "poKvc",
                String.format("%02X", poOpenSessionPars.getSelectedKvc()));

        if (kif == (byte) 0xFF) {
            if (defaultKeyIndex == (byte) 0x01) {
                kif = (byte) 0x21;
            } else if (defaultKeyIndex == (byte) 0x02) {
                kif = (byte) 0x27;
            } else if (defaultKeyIndex == (byte) 0x03) {
                kif = (byte) 0x30;
            }
        }
        AbstractApduCommandBuilder digestInit = new DigestInitCmdBuild(csmRevision, false,
                poRevision.equals(PoRevision.REV3_2), defaultKeyIndex, kif,
                poOpenSessionPars.getSelectedKvc(), poOpenSessionPars.getRecordDataRead());
        logger.info("Opening: CSM Request", "action", "po_secure_session.open_csm_digest_init",
                "apdu", ByteBufferUtils.toHex(digestInit.getApduRequest().getBytes()));

        csmApduRequestList.add(digestInit.getApduRequest());

        // Browse other PO commands to compute CSM digest
        if ((poCommandsInsideSession != null) && !poCommandsInsideSession.isEmpty()
                && (poCommandsInsideSession.size() > 1)) {
            // TODO => rajouter un contrôle afin de vérifier que poApduResponseList a même taille
            // que poApduRequestList
            for (int i = 1; i < poApduRequestList.size(); i++) { // The loop starts after the Open
                /*
                 * Session for the first command send in session Build "Digest Update" command for
                 * each PO APDU Request
                 */
                csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
                        poApduRequestList.get(i).getBytes())).getApduRequest());
                /*
                 * Build "Digest Update" command for each PO APDU Response //TODO => this is the
                 * right command, to fix ApduResponse.getBytes
                 */
                csmApduRequestList.add(((new DigestUpdateCmdBuild(csmRevision, false,
                        poApduResponseList.get(i).getBytes())).getApduRequest())); // HACK
            }
        }

        // Transfert CSM commands
        logger.info("Opening: CSM Request", "action", "po_secure_session.open_csm_request",
                "apduList", csmApduRequestList);

        // create a SeRequestSet (list of SeRequests)
        SeRequestSet csmRequest = new SeRequestSet(new SeRequest(null, csmApduRequestList, true));

        /* TODO Check responses. We do not check responses at the moment, but we should! */
        csmReader.transmit(csmRequest);

        /* Second ================================================================= */

        // Build "Digest Close" command
        DigestCloseCmdBuild digestClose = new DigestCloseCmdBuild(csmRevision,
                poRevision.equals(PoRevision.REV3_2) ? (byte) 0x08 : (byte) 0x04);

        csmApduRequestList.clear();
        csmApduRequestList.add(digestClose.getApduRequest());

        // ****FIRST**** transfert of CSM commands
        logger.info("Closing: Sending CSM request", "action", "po_secure_session.close_csm_req",
                "apduList", csmApduRequestList);

        // create a SeRequestSet (list of SeRequests)
        csmRequest = new SeRequestSet(new SeRequest(null, csmApduRequestList, true));

        SeResponse csmResponse_1 = csmReader.transmit(csmRequest).getSingleResponse();
        List<ApduResponse> csmApduResponseList_1 = csmResponse_1.getApduResponses();

        // Get Terminal Signature
        if ((csmApduResponseList_1 != null) && !csmApduResponseList_1.isEmpty()) {
            // T item = csmApduResponseList.get(csmApduResponseList.size()-1);
            DigestCloseRespPars respPars = new DigestCloseRespPars(
                    csmApduResponseList_1.get(csmApduResponseList_1.size() - 1));

            sessionTerminalSignature = respPars.getSignature();
        }

        // a ****SINGLE**** PO exchange - the "LAST" one
        // a last PO Request (channel closing decided by the app)
        // a last CSM Request (channel kept open)

        boolean ratificationAsked = (ratificationCommand != null);

        // Build PO Close Session command
        CloseSessionCmdBuild closeCommand =
                new CloseSessionCmdBuild(poRevision, ratificationAsked, sessionTerminalSignature);

        poApduRequestList.clear();
        poApduRequestList.add(closeCommand.getApduRequest());

        // Build PO Ratification command
        if (ratificationAsked) {
            poApduRequestList.add(ratificationCommand.getApduRequest());
        }

        // Transfert PO commands
        // create a SeRequestSet (list of SeRequests)
        SeRequestSet poRequest = new SeRequestSet(new SeRequest(poCalypsoInstanceAid,
                poApduRequestList, closeSeChannel ? false : true));

        logger.info("Closing: Sending PO request", "action", "po_secure_session.close_po_req",
                "apduList", poRequest.getRequests().iterator().next().getApduRequests());

        poResponse = poReader.transmit(poRequest).getSingleResponse();
        poApduResponseList = poResponse.getApduResponses();

        // TODO => check that PO response is equal to anticipated PO response (that
        // poApduResponseList equals poAnticipatedResponseInsideSession)

        // parse Card Signature
        /*
         * TODO add support of poRevision parameter to CloseSessionRespPars for REV2.4 PO CLAss byte
         */
        // before last if ratification, otherwise last one
        CloseSessionRespPars poCloseSessionPars = new CloseSessionRespPars(
                poApduResponseList.get(poApduResponseList.size() - ((ratificationAsked) ? 2 : 1)));
        if (!poCloseSessionPars.isSuccessful()) {
            throw new InvalidMessageException("Didn't get a signature",
                    InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
        }
        sessionCardSignature = poCloseSessionPars.getSignatureLo();

        // Build CSM Digest Authenticate command
        AbstractApduCommandBuilder digestAuth =
                new DigestAuthenticateCmdBuild(this.csmRevision, sessionCardSignature);
        csmApduRequestList.clear();
        csmApduRequestList.add(digestAuth.getApduRequest());

        // ****SECOND**** transfer of CSM commands, keep CSM channel open
        // TODO find out why it fails when keepChannelOpen is true as wanted!
        SeRequestSet csmRequest_2 =
                new SeRequestSet(new SeRequest(null, csmApduRequestList, false));

        SeResponse csmResponse_2 = csmReader.transmit(csmRequest_2).getSingleResponse();
        List<ApduResponse> csmApduResponseList_2 = csmResponse_2.getApduResponses();

        // Get transaction result
        if ((csmApduResponseList_2 != null) && !csmApduResponseList_2.isEmpty()) {
            DigestAuthenticateRespPars respPars =
                    new DigestAuthenticateRespPars(csmApduResponseList_2.get(0));
            transactionResult = respPars.isSuccessful();
        }

        // TODO => to check:
        // if (!digestCloseRespPars.isSuccessful()) {
        // throw new IllegalArgumentException(digestCloseRespPars.getStatusInformation());
        // }

        currentState = SessionState.SESSION_CLOSED;
        return poResponse;
    }


    /**
     * Change SendableInSession List to ApduRequest List .
     *
     * @param poOrCsmCommandsInsideSession a po or csm commands list to be sent in session
     * @return the ApduRequest list
     */
    private List<ApduRequest> getApduRequestsToSendInSession(
            List<SendableInSession> poOrCsmCommandsInsideSession) {
        List<ApduRequest> apduRequestList = new ArrayList<ApduRequest>();
        if (poOrCsmCommandsInsideSession != null) {
            for (SendableInSession cmd : poOrCsmCommandsInsideSession) {
                apduRequestList.add(((AbstractApduCommandBuilder) cmd).getApduRequest());
            }
        }
        return apduRequestList;
    }


    /**
     * Function used to get the secureSession.
     *
     * @param responsesOpenSession the response open session
     * @return the secure session by SE response and poRevision
     * @throws UnexpectedReaderException the unexpected reader exception
     */
    private AbstractOpenSessionRespPars getSecureSessionBySEResponseAndRevision(
            SeResponseSet responsesOpenSession) throws UnexpectedReaderException {
        SeResponse responseOpenSession = responsesOpenSession.getSingleResponse();
        AbstractOpenSessionRespPars openSessionRespPars = AbstractOpenSessionRespPars
                .create(responseOpenSession.getApduResponses().get(0), poRevision);
        if (!openSessionRespPars.isSuccessful()) {
            throw new UnexpectedReaderException(openSessionRespPars.getStatusInformation());
        }

        return openSessionRespPars;
    }


    /**
     * Secure Session management: step 3 (optional) Process proceeding. On poReader, generate a
     * SERequest with the current selected AID, with keepChannelOpen set at true, and apduRequests
     * defined with the poCommands_InsideSession. Returns the corresponding SeResponse (for
     * poCommands_InsideSession). On csmSessionReader, automatically operate potentially several
     * Digest Update Multiple.
     *
     * @param poCommandsInsideSession the po commands inside session
     * @return a SE Response
     *
     * @throws IOReaderException IO Reader exception
     */
    public SeResponse processProceeding(List<PoSendableInSession> poCommandsInsideSession)
            throws IOReaderException {

        // Get PO ApduRequest List from PoSendableInSession List
        List<ApduRequest> poApduRequestList = this.getApduRequestsToSendInSession(
                (List<SendableInSession>) (List<?>) poCommandsInsideSession);
        // Init CSM ApduRequest List
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();
        // PO & CSM to be kept "Open"
        boolean keepChannelOpen = true;

        // Transfert PO commands
        logger.info("Processing: Sending PO commands", "action",
                "po_secure_session.process_po_request", "apduList", poApduRequestList);
        SeResponse poResponse = poReader
                .transmit(new SeRequestSet(
                        new SeRequest(poCalypsoInstanceAid, poApduRequestList, keepChannelOpen)))
                .getSingleResponse();

        List<ApduResponse> poApduResponseList = poResponse.getApduResponses();
        logger.info("Processing: Receiving PO responses", "action",
                "po_secure_session.process_po_response", "apduList", poApduResponseList);

        // Browse all exchanged PO commands to compute CSM digest
        /*
         * TODO ? => rajouter un contrôle afin de vérifier que poApduResponseList a même taille que
         * poApduRequestList
         */
        for (int i = 0; i < poApduRequestList.size(); i++) {
            // Build "Digest Update" command for each PO APDU Request
            csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
                    poApduRequestList.get(i).getBytes())).getApduRequest());

            // Build "Digest Update" command for each PO APDU Response
            csmApduRequestList.add(((new DigestUpdateCmdBuild(csmRevision, false,
                    poApduResponseList.get(i).getBytes())).getApduRequest())); // HACK
        }

        // Transfert CSM commands
        logger.info("Processing: Sending CSM requests", "action",
                "po_secure_session.process_csm_request", "apduList", csmApduRequestList);
        SeResponse csmResponse = csmReader
                .transmit(
                        new SeRequestSet(new SeRequest(null, csmApduRequestList, keepChannelOpen)))
                .getSingleResponse();
        logger.info("Processing: Receiving CSM response", "action",
                "po_secure_session.process_csm_response", "apduList",
                csmResponse.getApduResponses());
        return poResponse;
    }

    /**
     * Secure Session management: step 4 (final) Process closing. On csmSessionReader, automatically
     * operate potentially several Digest Update Multiple, and the Digest Close. Identifies the
     * terminal signature. On poReader, generate a SERequest with the current selected AID, with
     * keepChannelOpen set at false, and apduRequests defined with poCommands_InsideSession,
     * closeCommand, and ratificationCommand. Identifies the PO signature. On csmSessionReader,
     * automatically operates the Digest Authenticate. Returns the corresponding SeResponse and the
     * boolean status of the authentication.
     *
     * @param poCommandsInsideSession the po commands inside session
     * @param poAnticipatedResponseInsideSession The anticipated PO response in the sessions
     * @param ratificationCommand the ratification command
     * @param closeSeChannel if true the SE channel of the po reader is closed after the last
     *        command
     * @return SeResponse close session response
     * @throws IOReaderException the IO reader exception
     */
    // public SeResponse processClosing(List<PoSendableInSession> poCommandsInsideSession,
    // CloseSessionCmdBuild closeCommand, PoGetChallengeCmdBuild ratificationCommand)
    // TODO - prévoir une variante pour enchainer plusieurs session d'affilée (la commande de
    // ratification étant un nouveau processOpeningClosing)
    public SeResponse processClosing(List<PoSendableInSession> poCommandsInsideSession,
            List<ApduResponse> poAnticipatedResponseInsideSession,
            AbstractPoCommandBuilder ratificationCommand, boolean closeSeChannel)
            throws IOReaderException {

        // Get PO ApduRequest List from PoSendableInSession List - for the first PO exchange
        List<ApduRequest> poApduRequestList = this.getApduRequestsToSendInSession(
                (List<SendableInSession>) (List<?>) poCommandsInsideSession);
        // Init CSM ApduRequest List - for the first CSM exchange
        List<ApduRequest> csmApduRequestList_1 = new ArrayList<ApduRequest>();
        // Init CSM ApduRequest List - for the second CSM exchange
        List<ApduRequest> csmApduRequestList_2 = new ArrayList<ApduRequest>();

        // The CSM channel should stay 'Open' for the first CSM exchange
        // Next PO & CSM channels will be 'Close' for the last exchanges

        // Compute "Anticipated" Digest Update (for optional poCommandsInsideSession)
        if ((poCommandsInsideSession != null) && !poApduRequestList.isEmpty()) {
            if (poApduRequestList.size() == poAnticipatedResponseInsideSession.size()) {

                // Browse "ANTICIPATED" exchanges of PO commands to compute CSM digest
                // TODO => rajouter un contrôle afin de vérifier que poApduResponseList a même
                // taille que poApduRequestList
                for (int i = 0; i < poApduRequestList.size(); i++) {
                    // TODO => optimization with Digest Update Multiple - conditions : (CSM revision
                    // >= 3) && (poApduRequestLength + poApduRequestLength <= 250)

                    // Build "Digest Update" command for each PO APDU Request
                    csmApduRequestList_1.add((new DigestUpdateCmdBuild(csmRevision, false,
                            poApduRequestList.get(i).getBytes())).getApduRequest());
                    // Build "Digest Update" command for each "ANTICIPATED" PO APDU Response
                    // csmApduRequestList_1.add((new DigestUpdateCmdBuild(csmRevision, false,
                    // poApduResponseList_1.get(i).getBytes())).getApduRequest());
                    csmApduRequestList_1.add((new DigestUpdateCmdBuild(csmRevision, false,
                            poAnticipatedResponseInsideSession.get(i).getBytes()))
                                    .getApduRequest());
                }
            } else {
                // TODO => error processing
            }
        }

        // Build "Digest Close" command
        DigestCloseCmdBuild digestClose = new DigestCloseCmdBuild(csmRevision,
                poRevision.equals(PoRevision.REV3_2) ? (byte) 0x08 : (byte) 0x04);

        csmApduRequestList_1.add(digestClose.getApduRequest());

        // ****FIRST**** transfert of CSM commands
        logger.info("Closing: Sending CSM request", "action", "po_secure_session.close_csm_req",
                "apduList", csmApduRequestList_1);

        // create a SeRequestSet (list of SeRequests)
        SeRequestSet csmRequest = new SeRequestSet(new SeRequest(null, csmApduRequestList_1, true));

        SeResponse csmResponse_1 = csmReader.transmit(csmRequest).getSingleResponse();
        List<ApduResponse> csmApduResponseList_1 = csmResponse_1.getApduResponses();

        // Get Terminal Signature
        if ((csmApduResponseList_1 != null) && !csmApduResponseList_1.isEmpty()) {
            // T item = csmApduResponseList.get(csmApduResponseList.size()-1);
            DigestCloseRespPars respPars = new DigestCloseRespPars(
                    csmApduResponseList_1.get(csmApduResponseList_1.size() - 1));

            sessionTerminalSignature = respPars.getSignature();
        }

        // a ****SINGLE**** PO exchange - the "LAST" one
        // a last PO Request (channel closing decided by the app)
        // a last CSM Request (channel kept open)

        boolean ratificationAsked = (ratificationCommand != null);

        // Build PO Close Session command
        CloseSessionCmdBuild closeCommand =
                new CloseSessionCmdBuild(poRevision, ratificationAsked, sessionTerminalSignature);

        poApduRequestList.add(closeCommand.getApduRequest());

        // Build PO Ratification command
        if (ratificationAsked) {
            poApduRequestList.add(ratificationCommand.getApduRequest());
        }

        // Transfert PO commands
        // create a SeRequestSet (list of SeRequests)
        SeRequestSet poRequest = new SeRequestSet(new SeRequest(poCalypsoInstanceAid,
                poApduRequestList, closeSeChannel ? false : true));

        logger.info("Closing: Sending PO request", "action", "po_secure_session.close_po_req",
                "apduList", poRequest.getRequests().iterator().next().getApduRequests());

        SeResponse poResponse = poReader.transmit(poRequest).getSingleResponse();
        List<ApduResponse> poApduResponseList = poResponse.getApduResponses();

        // TODO => check that PO response is equal to anticipated PO response (that
        // poApduResponseList equals poAnticipatedResponseInsideSession)

        // parse Card Signature
        /*
         * TODO add support of poRevision parameter to CloseSessionRespPars for REV2.4 PO CLAss byte
         */
        // before last if ratification, otherwise last one
        CloseSessionRespPars poCloseSessionPars = new CloseSessionRespPars(
                poApduResponseList.get(poApduResponseList.size() - ((ratificationAsked) ? 2 : 1)));
        if (!poCloseSessionPars.isSuccessful()) {
            throw new InvalidMessageException("Didn't get a signature",
                    InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
        }
        sessionCardSignature = poCloseSessionPars.getSignatureLo();

        // Build CSM Digest Authenticate command
        AbstractApduCommandBuilder digestAuth =
                new DigestAuthenticateCmdBuild(this.csmRevision, sessionCardSignature);
        csmApduRequestList_2.add(digestAuth.getApduRequest());

        // ****SECOND**** transfer of CSM commands (keep channel open to avoid unwanted CSM reset)
        // TODO find out why it fails when keepChannelOpen is true as wanted!
        SeRequestSet csmRequest_2 =
                new SeRequestSet(new SeRequest(null, csmApduRequestList_2, false));

        SeResponse csmResponse_2 = csmReader.transmit(csmRequest_2).getSingleResponse();
        List<ApduResponse> csmApduResponseList_2 = csmResponse_2.getApduResponses();

        // Get transaction result
        if ((csmApduResponseList_2 != null) && !csmApduResponseList_2.isEmpty()) {
            DigestAuthenticateRespPars respPars =
                    new DigestAuthenticateRespPars(csmApduResponseList_2.get(0));
            transactionResult = respPars.isSuccessful();
        }

        // TODO => to check:
        // if (!digestCloseRespPars.isSuccessful()) {
        // throw new IllegalArgumentException(digestCloseRespPars.getStatusInformation());
        // }

        currentState = SessionState.SESSION_CLOSED;
        return poResponse;
    }

    /**
     * Process CSM commands. The provided commands have to implements the CsmSendableInSession
     * interface
     * 
     * @param csmSendableInSessions a list of commands to sent to the CSM
     * @return SeResponse csm responses
     * @throws IOReaderException
     */
    public SeResponse processCsmCommands(List<CsmSendableInSession> csmSendableInSessions)
            throws IOReaderException {
        // Init CSM ApduRequest List - for the first CSM exchange
        List<ApduRequest> csmApduRequestList = this.getApduRequestsToSendInSession(
                (List<SendableInSession>) (List<?>) csmSendableInSessions);
        // create a SeRequestSet (list of SeRequest)
        SeRequestSet csmRequests = new SeRequestSet(new SeRequest(null, csmApduRequestList, true));

        return csmReader.transmit(csmRequests).getSingleResponse();
    }

    public static PoRevision computePoRevision(byte applicationTypeByte) {
        PoRevision rev = PoRevision.REV3_1;
        if (applicationTypeByte <= (byte) 0x1F) {
            rev = PoRevision.REV2_4;
        } else if (Byte.valueOf(applicationTypeByte).compareTo((byte) 0x7f) <= 0
                && Byte.valueOf(applicationTypeByte).compareTo((byte) 0x20) >= 0) {
            if (ApduUtils.isBitSet(applicationTypeByte, 3)) {
                rev = PoRevision.REV3_2;
            } else {
                rev = PoRevision.REV3_1;
            }
        }
        return rev;
    }

    /**
     * Gets the PO Revision.
     *
     * @return the PoPlainSecureSession_OLD.poRevision
     */
    public PoRevision getRevision() {
        // TODO checks if poRevision initialized
        return poRevision;
    }

    /**
     * Checks if the PO transaction is successful.
     *
     * @return the PoPlainSecureSession_OLD.transactionResult
     */
    public boolean isSuccessful() {
        // TODO checks if transaction state is "closed"
        return transactionResult;
    }


    /**
     * Gets the poRevision.
     *
     * @return the PO poRevision
     */
    public ByteBuffer getSessionTerminalChallenge() {
        return sessionTerminalChallenge;
    }
}

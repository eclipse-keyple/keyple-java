/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.transaction;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.keyple.calypso.commands.SendableInSession;
import org.keyple.calypso.commands.csm.CsmRevision;
import org.keyple.calypso.commands.csm.builder.*;
import org.keyple.calypso.commands.csm.parser.CsmGetChallengeRespPars;
import org.keyple.calypso.commands.csm.parser.DigestAuthenticateRespPars;
import org.keyple.calypso.commands.csm.parser.DigestCloseRespPars;
import org.keyple.calypso.commands.po.AbstractPoCommandBuilder;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.AbstractOpenSessionCmdBuild;
import org.keyple.calypso.commands.po.builder.CloseSessionCmdBuild;
import org.keyple.calypso.commands.po.parser.AbstractOpenSessionRespPars;
import org.keyple.calypso.commands.po.parser.CloseSessionRespPars;
import org.keyple.calypso.commands.po.parser.GetDataFciRespPars;
import org.keyple.calypso.commands.utils.ApduUtils;
import org.keyple.commands.AbstractApduCommandBuilder;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InvalidMessageException;
import org.keyple.seproxy.exceptions.UnexpectedReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

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
     * Process identification. On poReader, generate a SERequest for the specified PO AID, with
     * keepChannelOpen set at true, and apduRequests defined with the optional
     * poCommands_OutsideSession. Returns the corresponding SEResponseElement. Identifies the serial
     * and the revision of the PO from FCI data. On csmSessionReader, automatically operate the
     * Select Diversifier and the Get Challenge.
     *
     * @param poAid the po AID
     * @param poCommandsOutsideSession the po commands outside session
     * @return the SE response
     * @throws IOReaderException the IO reader exception
     */
    public SeResponse processIdentification(ByteBuffer poAid,
            List<SendableInSession> poCommandsOutsideSession) throws IOReaderException {


        // Get PO ApduRequest List from SendableInSession List
        List<ApduRequest> poApduRequestList =
                this.getApduRequestListFromSendableInSessionListTo(poCommandsOutsideSession);
        // Init CSM ApduRequest List
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();
        // PO & CSM channels to be kept "Open"
        boolean keepChannelOpen = true;

        // Transfert PO commands
        logger.info("Identification: PO requests", "action", "po_secure_session.ident_po_request");
        // System.out.println("\t========= Identification === Transfert PO commands");

        // create a list of SeRequest
        List<SeRequest> poRequestElements = new ArrayList<SeRequest>();
        poRequestElements.add(new SeRequest(poAid, poApduRequestList, keepChannelOpen));
        SeRequestSet poRequest = new SeRequestSet(poRequestElements);

        // SeRequestSet poRequest = new SeRequestSet(poAid, poApduRequestList, keepChannelOpen);
        SeResponseSet poResponse = poReader.transmit(poRequest);
        SeResponse poResponseElement = poResponse.getElements().get(0);

        // Parse PO FCI - to retrieve Calypso Revision, Serial Number, & DF Name (AID)
        GetDataFciRespPars poFciRespPars = new GetDataFciRespPars(poResponseElement.getFci());
        poRevision = computePoRevision(poFciRespPars.getApplicationTypeByte());
        poCalypsoInstanceAid = poFciRespPars.getDfName();
        poCalypsoInstanceSerial = poFciRespPars.getApplicationSerialNumber();
        // System.out.println("\t========= Identification === Selected DF Name : " +
        // ByteBufferUtils.toHex(poCalypsoInstanceAid));
        logger.info("Identification: PO Response", "action", "po_secure_session.ident_po_response",
                "dfName", ByteBufferUtils.toHex(poCalypsoInstanceAid), "serialNumber",
                ByteBufferUtils.toHex(poCalypsoInstanceSerial));

        /*
         * System.out.println("\t========= Identification === Calypso Serial Number : " +
         * ByteBufferUtils.toHex(poCalypsoInstanceSerial));
         */
        // Define CSM Select Diversifier command
        AbstractApduCommandBuilder selectDiversifier =
                new SelectDiversifierCmdBuild(this.csmRevision, poCalypsoInstanceSerial);
        csmApduRequestList.add(selectDiversifier.getApduRequest());

        // Define CSM Get Challenge command
        AbstractApduCommandBuilder csmGetChallenge =
                new CsmGetChallengeCmdBuild(this.csmRevision, (byte) 0x04);
        csmApduRequestList.add(csmGetChallenge.getApduRequest());
        /*
         * System.out.println(
         * "\t========= Identification === Generate CSM cmd request - Get Challenge : " +
         * ByteBufferUtils.toHex(csmGetChallenge.getApduRequest().getBuffer()));
         */
        logger.info("Identification: CSM Request", "action", "po_secure_session.ident_csm_request",
                "apduReq1", ByteBufferUtils.toHex(selectDiversifier.getApduRequest().getBuffer()),
                "apduReq2", ByteBufferUtils.toHex(csmGetChallenge.getApduRequest().getBuffer()));

        // Transfert CSM commands
        // create a list of SeRequest
        List<SeRequest> csmRequestElements = new ArrayList<SeRequest>();
        csmRequestElements.add(new SeRequest(null, csmApduRequestList, keepChannelOpen));
        SeRequestSet csmRequest = new SeRequestSet(csmRequestElements);
        SeResponseSet csmResponse = csmReader.transmit(csmRequest);
        SeResponse csmResponseElement = csmResponse.getElements().get(0);
        List<ApduResponse> csmApduResponseList = csmResponseElement.getApduResponses();

        logger.info("Identification: CSM Response", "action",
                "po_secure_session.ident_csm_response", "apduCount", csmApduResponseList.size());

        if (csmApduResponseList.size() == 2) {
            // TODO => check that csmApduResponseList.get(1) has the right length (challenge +
            // status)
            CsmGetChallengeRespPars csmChallengePars =
                    new CsmGetChallengeRespPars(csmApduResponseList.get(1));
            sessionTerminalChallenge = csmChallengePars.getChallenge();
            logger.info("Identification: Done", "action", "po_secure_session.ident_done", "apdu",
                    ByteBufferUtils.toHex(csmChallengePars.getApduResponse().getBuffer()),
                    "sessionTerminalChallenge", ByteBufferUtils.toHex(sessionTerminalChallenge));
            // System.out.println("\t========= Identification === Parse CSM cmd response - Select
            // Diversifier : " +
            // ByteBufferUtils.toHex(csmChallengePars.getApduResponse().getBuffer()));

            // System.out.println("\t========= Identification === Terminal Challenge : " +
            // ByteBufferUtils.toHex(sessionTerminalChallenge));
        } else {
            throw new InvalidMessageException("Invalid message received",
                    InvalidMessageException.Type.CSM, csmApduRequestList, csmApduResponseList);
        }

        currentState = SessionState.PO_IDENTIFIED;
        return poResponseElement;
    }

    /**
     * Process opening. On poReader, generate a SERequest with the current selected AID, with
     * keepChannelOpen set at true, and apduRequests defined with openCommand and the optional
     * poCommands_InsideSession. Returns the corresponding SEResponseElement (for openCommand and
     * poCommands_InsideSession). Identifies the session PO keyset. On csmSessionReader,
     * automatically operate the Digest Init and potentially several Digest Update Multiple.
     *
     * @param openCommand the open command
     * @param poCommandsInsideSession the po commands inside session
     * @return the SE response
     * @throws IOReaderException the IO reader exception
     */
    // fclariamb(2018-03-02): TODO: Cleanup that mess. There was a lot of commented out code and I
    // added a lot more of it, once this code is tested we should clean it up
    public SeResponse processOpening(AbstractOpenSessionCmdBuild openCommand,
            List<SendableInSession> poCommandsInsideSession) throws IOReaderException {

        // Get PO ApduRequest List from SendableInSession List
        // List<ApduRequest> poApduRequestList =
        // this.getApduRequestListFromSendableInSessionListTo(poCommandsInsideSession);
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();
        // Init CSM ApduRequest List
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();
        // PO & CSM channels to be kept "Open"
        // boolean keepChannelOpen = true;

        // Add Open Session command to PO ApduRequest list
        poApduRequestList.add(openCommand.getApduRequest());
        // Add list of SendableInSession commands to PO ApduRequest
        if ((poCommandsInsideSession != null) && !poCommandsInsideSession.isEmpty()) {
            poApduRequestList.addAll(
                    this.getApduRequestListFromSendableInSessionListTo(poCommandsInsideSession));
        }

        // Transfert PO commands
        logger.info("Opening: PO request", "action", "po_secure_session.open_po_request");
        // System.out.println("\t========= Opening ========== Transfert PO commands");
        // create a list of SeRequest
        List<SeRequest> poRequestElements = new ArrayList<SeRequest>();
        poRequestElements.add(new SeRequest(poCalypsoInstanceAid, poApduRequestList, true));
        SeRequestSet poRequest = new SeRequestSet(poRequestElements);

        SeResponseSet poResponse = poReader.transmit(poRequest);
        SeResponse poResponseElement = poResponse.getElements().get(0);
        List<ApduResponse> poApduResponseList = poResponseElement.getApduResponses();

        // Parse OpenSession Response to get Card Challenge
        if (poApduResponseList.isEmpty()) {
            throw new InvalidMessageException("No response", InvalidMessageException.Type.PO,
                    poApduRequestList, poApduResponseList);
        }
        if (poApduResponseList.get(0).getStatusCode() != 0x9000) {
            throw new InvalidMessageException("Invalid PO opening response",
                    InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
        }
        // TODO: check that csmApduResponseList.get(1) has the right length (challenge + status)
        logger.info("Opening: PO commands", "action", "po_secure_session.open_po_send");
        AbstractOpenSessionRespPars poOpenSessionPars =
                AbstractOpenSessionRespPars.create(poApduResponseList.get(0), poRevision); // first
        // item
        // of
        // poApduResponseList
        // System.out.println("\t========= Opening ========== Parse PO cmd response - Open Session :
        // " + ByteBufferUtils.toHex(poOpenSessionPars.getApduResponse().getBuffer()));
        sessionCardChallenge = poOpenSessionPars.getPoChallenge();
        // System.out.println("\t========= Opening ========== WRONG Card Challenge : " +
        // ByteBufferUtils.toHex(sessionCardChallenge));

        // HACK - AbstractOpenSessionRespPars.getPoChallengeOld() ne retourne pas la bonne valeur de
        // PO
        // challenge
        // TODO - corriger => AbstractOpenSessionRespPars.getPoChallengeOld()
        sessionCardChallenge = ByteBufferUtils.fromHex(ByteBufferUtils
                .toHex(poOpenSessionPars.getApduResponse().getBuffer()).substring(0, 4 * 2)); // HACK

        // System.out.println("\t========= Opening ========== HACKED Card Challenge : " +
        // ByteBufferUtils.toHex(sessionCardChallenge));

        // Build "Digest Init" command from PO Open Session
        byte kif = poOpenSessionPars.getSelectedKif();
        // System.out.println("\t========= Opening ========== PO KIF : "
        // + DatatypeConverter.printHexBinary(new byte[] {(byte) kif}));
        // System.out.println("\t========= Opening ========== PO KVC : " + DatatypeConverter
        // .printHexBinary(new byte[] {(byte) poOpenSessionPars.getSelectedKvc()}));

        logger.info("Opening: PO response", "action", "po_secure_session.open_po_response",
                "apduResponse",
                ByteBufferUtils.toHex(poOpenSessionPars.getApduResponse().getBuffer()),
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
                "apdu", ByteBufferUtils.toHex(digestInit.getApduRequest().getBuffer()));
        // System.out.println("\t========= Opening ========== Generate CSM cmd request - Digest Init
        // : " + ByteBufferUtils.toHex(digestInit.getApduRequest().getBuffer()));
        csmApduRequestList.add(digestInit.getApduRequest());

        // Browse other PO commands to compute CSM digest
        if ((poCommandsInsideSession != null) && !poCommandsInsideSession.isEmpty()
                && (poCommandsInsideSession.size() > 1)) {
            // TODO => rajouter un contr�le afin de v�rifier que poApduResponseList a m�me taille
            // que poApduRequestList
            for (int i = 1; i < poApduRequestList.size(); i++) { // The loop starts after the Open
                // Session for the first command
                // send in session
                // Build "Digest Update" command for each PO APDU Request
                /*
                 * System.out.println(
                 * "\t========= Opening ========== Generate CSM cmd request - Digest Update for PO request : "
                 * + ByteBufferUtils.toHex((new DigestUpdateCmdBuild(csmRevision, false,
                 * poApduRequestList.get(i).getBuffer())) .getApduRequest().getBuffer()));
                 */
                csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
                        poApduRequestList.get(i).getBuffer())).getApduRequest());
                // Build "Digest Update" command for each PO APDU Response
                /*
                 * System.out.println(
                 * "\t========= Opening ==WRONG=== Generate CSM cmd request - Digest Update for PO response : "
                 * + ByteBufferUtils.toHex((new DigestUpdateCmdBuild(csmRevision, false,
                 * poApduResponseList.get(i).getBuffer()) .getApduRequest().getBuffer())));
                 */
                // csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
                // poApduResponseList.get(i).getBytes())).getApduRequest()); //TODO => this is the
                // rigth command, to fix ApduResponse.getBytes
                /*
                 * ByteBuffer additionOfGetBytesAndGetStatusCode =
                 * ArrayUtils.addAll(poApduResponseList.get(i).getBuffer(),
                 * poApduResponseList.get(i).getStatusCodeOld()); //
                 * System.out.println("\t\tDEBUG ##### csmApduResponseList.size() : " + //
                 * DatatypeConverter.printHexBinary(additionOfGetBytesAndGetStatusCode));
                 */
                /*
                 * System.out.println(
                 * "\t========= Opening ==HACK==== Generate CSM cmd request - Digest Update for PO response : "
                 * + ByteBufferUtils.toHex((new DigestUpdateCmdBuild(csmRevision, false,
                 * poApduResponseList.get(i).getBuffer())) .getApduRequest().getBuffer()));
                 */
                csmApduRequestList.add(((new DigestUpdateCmdBuild(csmRevision, false,
                        poApduResponseList.get(i).getBuffer())).getApduRequest())); // HACK
            }
        }

        // Transfert CSM commands
        // System.out.println("\t========= Opening ========== Transfert CSM commands");
        logger.info("Opening: CSM Request", "action", "po_secure_session.open_csm_request",
                "apduList", csmApduRequestList);
        // create a list of SeRequest
        List<SeRequest> csmRequestElements = new ArrayList<SeRequest>();
        csmRequestElements.add(new SeRequest(null, csmApduRequestList, true));
        SeRequestSet csmRequest = new SeRequestSet(csmRequestElements);

        SeResponseSet csmResponse = csmReader.transmit(csmRequest);
        // fclairamb(2018-03-02): TODO: We don't check the result ?
        // List<ApduResponse> csmApduResponseList = csmResponse.getApduResponses();

        currentState = SessionState.SESSION_OPEN;
        return poResponseElement;
    }

    /**
     * Change sendable in session tab to list apdu.
     *
     * @param poCommandsInsideSession the po commands inside session
     * @return the list
     */
    // private List<ApduRequest> changeSendableInSessionTabToListApdu(SendableInSession[]
    // poCommandsInsideSession) {
    private List<ApduRequest> getApduRequestListFromSendableInSessionListTo(
            List<SendableInSession> poCommandsInsideSession) {
        List<ApduRequest> retour = new ArrayList<ApduRequest>();
        if (poCommandsInsideSession != null) {
            for (SendableInSession cmd : poCommandsInsideSession) {
                // retour.add(cmd.getAPDURequest()); TODO => suppress all methods getAPDURequest()
                // from SendableInSession & most of AbstractPoCommandBuilder extensions
                retour.add(((AbstractPoCommandBuilder) cmd).getApduRequest()); // Il fallait faire
                // un "CAST"
            }
        }
        return retour;
    }


    /**
     * Function used to get the secureSession.
     *
     * @param responseOpenSession the response open session
     * @return the secure session by SE response and poRevision
     * @throws UnexpectedReaderException the unexpected reader exception
     */
    private AbstractOpenSessionRespPars getSecureSessionBySEResponseAndRevision(
            SeResponseSet responseOpenSession) throws UnexpectedReaderException {
        SeResponse responseOpenSessionElement = responseOpenSession.getElements().get(0);
        AbstractOpenSessionRespPars openSessionRespPars = AbstractOpenSessionRespPars
                .create(responseOpenSessionElement.getApduResponses().get(0), poRevision);
        if (!openSessionRespPars.isSuccessful()) {
            throw new UnexpectedReaderException(openSessionRespPars.getStatusInformation());
        }

        return openSessionRespPars;
    }


    /**
     * Process proceeding. On poReader, generate a SERequest with the current selected AID, with
     * keepChannelOpen set at true, and apduRequests defined with the poCommands_InsideSession.
     * Returns the corresponding SEResponseElement (for poCommands_InsideSession). On
     * csmSessionReader, automatically operate potentially several Digest Update Multiple.
     *
     * @param poCommandsInsideSession the po commands inside session
     * @return a SE Response
     *
     * @throws IOReaderException IO Reader exception
     */
    public SeResponse processProceeding(List<SendableInSession> poCommandsInsideSession)
            throws IOReaderException {

        // Get PO ApduRequest List from SendableInSession List
        List<ApduRequest> poApduRequestList =
                this.getApduRequestListFromSendableInSessionListTo(poCommandsInsideSession);
        // Init CSM ApduRequest List
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();
        // PO & CSM to be kept "Open"
        boolean keepChannelOpen = true;

        // Transfert PO commands
        // System.out.println("\t========= Continuation ===== Transfert PO commands");
        // create a list of SeRequest
        List<SeRequest> poRequestElements = new ArrayList<SeRequest>();
        poRequestElements
                .add(new SeRequest(poCalypsoInstanceAid, poApduRequestList, keepChannelOpen));
        SeRequestSet poRequest = new SeRequestSet(poRequestElements);

        logger.info("Processing: Sending PO commands", "action",
                "po_secure_session.process_po_request", "apduList", poApduRequestList);
        SeResponseSet poResponse = poReader.transmit(poRequest);
        SeResponse poResponseElement = poResponse.getElements().get(0);
        List<ApduResponse> poApduResponseList = poResponseElement.getApduResponses();
        logger.info("Processing: Receiving PO responses", "action",
                "po_secure_session.process_po_response", "apduList", poApduResponseList);

        // Browse all exchanged PO commands to compute CSM digest
        // TODO => rajouter un contrôle afin de vérifier que poApduResponseList a même taille que
        // poApduRequestList
        for (int i = 0; i < poApduRequestList.size(); i++) {
            // Build "Digest Update" command for each PO APDU Request
            /*
             * System.out.println(
             * "\t========= Continuation ===== Generate CSM cmd request - Digest Update for PO request : "
             * + ByteBufferUtils.toHex((new DigestUpdateCmdBuild(csmRevision, false,
             * poApduRequestList.get(i).getBuffer())).getApduRequest() .getBuffer()));
             */
            csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
                    poApduRequestList.get(i).getBuffer())).getApduRequest());
            // Build "Digest Update" command for each PO APDU Response
            // System.out.println("\t========= Continuation ===== Generate CSM cmd request - Digest
            // Update for PO response : " + DatatypeConverter.printHexBinary((new
            // DigestUpdateCmdBuild(csmRevision, false,
            // poApduResponseList.get(i).getBytes())).getApduRequest().getBytes()));
            /*
             * System.out.println(
             * "\t==WRONG== Continuation ===== Generate CSM cmd request - Digest Update for PO response : "
             * + ByteBufferUtils.toHex((new DigestUpdateCmdBuild(csmRevision, false,
             * poApduResponseList.get(i).getBuffer())).getApduRequest() .getBuffer()));
             */
            // csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
            // poApduResponseList.get(i).getBytes())).getApduRequest()); //TODO => this is the rigth
            // command, to fix ApduResponse.getBytes
            /*
             * byte[] additionOfGetBytesAndGetStatusCode =
             * ArrayUtils.addAll(poApduResponseList.get(i).getBytes(),
             * poApduResponseList.get(i).getStatusCodeOld());
             */
            // System.out.println("\t\tDEBUG ##### csmApduResponseList.size() : " +
            // DatatypeConverter.printHexBinary(additionOfGetBytesAndGetStatusCode));
            /*
             * System.out.println(
             * "\t==HACK=== Continuation ===== Generate CSM cmd request - Digest Update for PO response : "
             * + ByteBufferUtils.toHex((new DigestUpdateCmdBuild(csmRevision, false,
             * poApduResponseList.get(i).getBuffer())).getApduRequest() .getBuffer()));
             */
            csmApduRequestList.add(((new DigestUpdateCmdBuild(csmRevision, false,
                    poApduResponseList.get(i).getBuffer())).getApduRequest())); // HACK

        }

        // Transfert CSM commands
        // System.out.println("\t========= Continuation ===== Transfert CSM commands");
        // create a list of SeRequest
        List<SeRequest> csmRequestElements = new ArrayList<SeRequest>();
        csmRequestElements.add(new SeRequest(null, csmApduRequestList, keepChannelOpen));
        SeRequestSet csmRequest = new SeRequestSet(csmRequestElements);

        logger.info("Processing: Sending CSM requests", "action",
                "po_secure_session.process_csm_request", "apduList", csmApduRequestList);
        SeResponseSet csmResponse = csmReader.transmit(csmRequest);
        SeResponse csmResponseElement = csmResponse.getElements().get(0);
        // List<ApduResponse> csmApduResponseList = csmResponse.getApduResponses();
        logger.info("Processing: Receiving CSM response", "action",
                "po_secure_session.process_csm_response", "apduList",
                csmResponseElement.getApduResponses());
        return poResponseElement;
    }

    /**
     * Process closing. On csmSessionReader, automatically operate potentially several Digest Update
     * Multiple, and the Digest Close. Identifies the terminal signature. On poReader, generate a
     * SERequest with the current selected AID, with keepChannelOpen set at false, and apduRequests
     * defined with poCommands_InsideSession, closeCommand, and ratificationCommand. Identifies the
     * PO signature. On csmSessionReader, automatically operates the Digest Authenticate. Returns
     * the corresponding SEResponseElement and the boolean status of the authentication.
     *
     * @param poCommandsInsideSession the po commands inside session
     * @param poAnticipatedResponseInsideSession The anticipated PO response in the sessions
     * @param ratificationCommand the ratification command
     * @return SEResponseElement close session response
     * @throws IOReaderException the IO reader exception
     */
    // public SeResponse processClosing(List<SendableInSession> poCommandsInsideSession,
    // CloseSessionCmdBuild closeCommand, PoGetChallengeCmdBuild ratificationCommand)
    // TODO - prévoir une variante pour enchainer plusieurs session d'affilée (la commande de
    // ratification étant un nouveau processOpening)
    public SeResponse processClosing(List<SendableInSession> poCommandsInsideSession,
            List<ApduResponse> poAnticipatedResponseInsideSession,
            AbstractPoCommandBuilder ratificationCommand) throws IOReaderException {

        // Get PO ApduRequest List from SendableInSession List - for the first PO exchange
        List<ApduRequest> poApduRequestList =
                this.getApduRequestListFromSendableInSessionListTo(poCommandsInsideSession);
        // Init CSM ApduRequest List - for the first CSM exchange
        List<ApduRequest> csmApduRequestList_1 = new ArrayList<ApduRequest>();
        // Init CSM ApduRequest List - for the second CSM exchange
        List<ApduRequest> csmApduRequestList_2 = new ArrayList<ApduRequest>();

        // The CSM channel should stay 'Open' for the first CSM exchange
        boolean keepChannelOpen = true;
        // Next PO & CSM channels will be 'Close' for the last exchanges

        SeResponseSet poResponse;

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
                            poApduRequestList.get(i).getBuffer())).getApduRequest());
                    // Build "Digest Update" command for each "ANTICIPATED" PO APDU Response
                    // csmApduRequestList_1.add((new DigestUpdateCmdBuild(csmRevision, false,
                    // poApduResponseList_1.get(i).getBytes())).getApduRequest());
                    csmApduRequestList_1.add((new DigestUpdateCmdBuild(csmRevision, false,
                            poAnticipatedResponseInsideSession.get(i).getBuffer()))
                                    .getApduRequest());
                }
            } else {
                // TODO => error processing
            }
        }

        // Build "Digest Close" command
        // csmApduRequestList_1.add((new DigestCloseCmdBuild(csmRevision,
        // poRevision.equals(PoRevision.REV3_2) ? (byte) 0x08 : (byte) 0x04)).getApduRequest());
        DigestCloseCmdBuild digestClose = new DigestCloseCmdBuild(csmRevision,
                poRevision.equals(PoRevision.REV3_2) ? (byte) 0x08 : (byte) 0x04);
        /*
         * System.out
         * .println("\t========= Closing ========== Generate CSM cmd request - Digest Close : " +
         * ByteBufferUtils.toHex(digestClose.getApduRequest().getBuffer()));
         */
        csmApduRequestList_1.add(digestClose.getApduRequest());

        // ****FIRST**** transfert of CSM commands
        // System.out.println("\t========= Closing ========== Transfert CSM commands - #1");
        logger.info("Closing: Sending CSM request", "action", "po_secure_session.close_csm_req",
                "apduList", csmApduRequestList_1);
        // create a list of SeRequest
        List<SeRequest> csmRequestElements = new ArrayList<SeRequest>();
        csmRequestElements.add(new SeRequest(null, csmApduRequestList_1, keepChannelOpen));
        SeRequestSet csmRequest = new SeRequestSet(csmRequestElements);

        SeResponseSet csmResponse_1 = csmReader.transmit(csmRequest);
        SeResponse csmResponseElement_1 = csmResponse_1.getElements().get(0);
        List<ApduResponse> csmApduResponseList_1 = csmResponseElement_1.getApduResponses();
        // System.out.println("\t\tDEBUG ##### csmApduResponseList_1.size() : " +
        // csmApduResponseList_1.size());

        // Get Terminal Signature
        if ((csmApduResponseList_1 != null) && !csmApduResponseList_1.isEmpty()) {
            // T item = csmApduResponseList.get(csmApduResponseList.size()-1);
            DigestCloseRespPars respPars = new DigestCloseRespPars(
                    csmApduResponseList_1.get(csmApduResponseList_1.size() - 1)); // .getApduResponses().get(0);

            // System.out.println("\t========= Closing ========== Parse CSM cmd response - Digest
            // Close : " + ByteBufferUtils.toHex(respPars.getApduResponse().getBuffer()));
            // System.out.println("\t\tDEBUG ##### csmApduResponseList_1.size() : "+
            // csmApduResponseList_1.size());
            sessionTerminalSignature = respPars.getSignature();
        }

        // a ****SINGLE**** PO exchange - the "LAST" one
        // System.out.println("\t========= Closing ========== Transfert PO commands - SINGLE");
        keepChannelOpen = false; // a last PO Request & a last CSM Reaquest

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
        // create a list of SeRequest
        List<SeRequest> poRequestElements = new ArrayList<SeRequest>();
        poRequestElements
                .add(new SeRequest(poCalypsoInstanceAid, poApduRequestList, keepChannelOpen));
        SeRequestSet poRequest = new SeRequestSet(poRequestElements);

        logger.info("Closing: Sending PO request", "action", "po_secure_session.close_po_req",
                "apduList", poRequestElements.get(0).getApduRequests());
        poResponse = poReader.transmit(poRequest);
        SeResponse poResponseElement = poResponse.getElements().get(0);
        List<ApduResponse> poApduResponseList = poResponseElement.getApduResponses();

        // TODO => check that PO response is equal to anticipated PO response (that
        // poApduResponseList equals poAnticipatedResponseInsideSession)

        // parse Card Signature
        // CloseSessionRespPars poCloseSessionPars = new
        // CloseSessionRespPars(poApduResponseList.get(0), poRevision); TODO add support of
        // poRevision parameter to CloseSessionRespPars for REV2.4 PO CLAss byte
        CloseSessionRespPars poCloseSessionPars = new CloseSessionRespPars(
                poApduResponseList.get(poApduResponseList.size() - ((ratificationAsked) ? 2 : 1))); // before
        // last
        // if
        // ratification,
        // otherwise
        // last
        // one
        if (!poCloseSessionPars.isSuccessful()) {
            throw new InvalidMessageException("Didn't get a signature",
                    InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
        }
        sessionCardSignature = poCloseSessionPars.getSignatureLo();

        // Build CSM Digest Authenticate command
        AbstractApduCommandBuilder digestAuth =
                new DigestAuthenticateCmdBuild(this.csmRevision, sessionCardSignature);
        csmApduRequestList_2.add(digestAuth.getApduRequest());

        // ****SECOND**** transfert of CSM commands
        // System.out.println("\t========= Closing ========== Transfert CSM commands - #2");
        // create a list of SeRequest
        List<SeRequest> csmRequestElements_2 = new ArrayList<SeRequest>();
        csmRequestElements_2.add(new SeRequest(null, csmApduRequestList_2, keepChannelOpen));
        SeRequestSet csmRequest_2 = new SeRequestSet(csmRequestElements_2);

        SeResponseSet csmResponse_2 = csmReader.transmit(csmRequest_2);
        SeResponse csmResponseElement_2 = csmResponse_2.getElements().get(0);
        List<ApduResponse> csmApduResponseList_2 = csmResponseElement_2.getApduResponses();

        // Get transaction result
        if ((csmApduResponseList_2 != null) && !csmApduResponseList_2.isEmpty()) {
            DigestAuthenticateRespPars respPars =
                    new DigestAuthenticateRespPars(csmApduResponseList_2.get(0));
            transactionResult = respPars.isSuccessful();
        }

        // TODO => to check:
        // if (!digestCloseRespPars.isSuccessful()) {
        // throw new InconsistentCommandException(digestCloseRespPars.getStatusInformation());
        // }

        currentState = SessionState.SESSION_CLOSED;
        return poResponseElement;
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

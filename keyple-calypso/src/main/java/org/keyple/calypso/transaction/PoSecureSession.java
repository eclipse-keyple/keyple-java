/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.transaction;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.ArrayUtils;
import org.keyple.calypso.commands.csm.CsmRevision;
import org.keyple.calypso.commands.csm.builder.CsmGetChallengeCmdBuild;
import org.keyple.calypso.commands.csm.builder.DigestAuthenticateCmdBuild;
import org.keyple.calypso.commands.csm.builder.DigestCloseCmdBuild;
import org.keyple.calypso.commands.csm.builder.DigestInitCmdBuild;
import org.keyple.calypso.commands.csm.builder.DigestUpdateCmdBuild;
import org.keyple.calypso.commands.csm.builder.SelectDiversifierCmdBuild;
import org.keyple.calypso.commands.csm.parser.CsmGetChallengeRespPars;
import org.keyple.calypso.commands.csm.parser.DigestAuthenticateRespPars;
import org.keyple.calypso.commands.csm.parser.DigestCloseRespPars;
import org.keyple.calypso.commands.po.PoCommandBuilder;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.SendableInSession;
import org.keyple.calypso.commands.po.builder.CloseSessionCmdBuild;
import org.keyple.calypso.commands.po.builder.OpenSessionCmdBuild;
import org.keyple.calypso.commands.po.parser.CloseSessionRespPars;
import org.keyple.calypso.commands.po.parser.GetDataFciRespPars;
import org.keyple.calypso.commands.po.parser.OpenSessionRespPars;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeResponse;
import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InvalidApduReaderException;
import org.keyple.seproxy.exceptions.TimeoutReaderException;
import org.keyple.seproxy.exceptions.UnexpectedReaderException;

/**
 * Portable Object Secure Session.
 *
 * A non-encrypted secure session with a Calypso PO requires the management of two
 * {@link ProxyReader} in order to communicate with both a Calypso PO and a CSM
 *
 * @author Calypso Networks Association
 */
public class PoSecureSession {

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
    private byte[] poCalypsoInstanceAid;
    /** Serial Number of the selected Calypso instance. */
    private byte[] poCalypsoInstanceSerial;

    /** The PO Calypso Revision. */
    public PoRevision poRevision = PoRevision.REV3_1;// PoCommandBuilder.defaultRevision; // TODO =>
                                                     // add a getter

    /** The CSM default revision. */
    private CsmRevision csmRevision = CsmRevision.S1D;

    /** The default key index for a PO session. */
    private byte defaultKeyIndex;

    public byte[] sessionTerminalChallenge;
    private byte[] sessionCardChallenge;

    public byte[] sessionTerminalSignature;
    private byte[] sessionCardSignature;

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

    // /**
    // * Gets the SE response diversifier by AID reader and FCI.
    // *
    // * @param keepChannelOpen
    // * the keep channel open
    // * @param reader
    // * the reader
    // * @param applicationSN
    // * the application SN
    // * @return the SE response diversifier by AID reader and FCI
    // * @throws UnexpectedReaderException
    // * the unexpected reader exception
    // * @throws IOReaderException
    // * the IO reader exception
    // * @throws ChannelStateReaderException
    // * the channel state reader exception
    // * @throws InvalidApduReaderException
    // * the invalid apdu reader exception
    // * @throws TimeoutReaderException
    // * the timeout reader exception
    // * @throws InconsistentCommandException
    // * the inconsistent command exception
    // */
    // private SeResponse selectDiversifier(boolean keepChannelOpen, ProxyReader reader, byte[]
    // applicationSN)
    // throws UnexpectedReaderException, IOReaderException, ChannelStateReaderException,
    // InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {
    // List<ApduRequest> apduRequestList = new ArrayList<ApduRequest>();
    //
    // ApduCommandBuilder selectDiversifier = new SelectDiversifierCmdBuild(this.csmRevision,
    // applicationSN);
    // ApduRequest requestDiversifier = selectDiversifier.getApduRequest();
    // apduRequestList.add(requestDiversifier);
    //
    // return transmitApduResquests(null, reader, keepChannelOpen, apduRequestList);
    //
    // }

    // /**
    // * Gets the SE response challenge by AID reader and FCI.
    // *
    // * @param keepChannelOpen
    // * the keep channel open
    // * @param reader
    // * the reader
    // * @return the SE response challenge by AID reader and FCI
    // * @throws UnexpectedReaderException
    // * the unexpected reader exception
    // * @throws IOReaderException
    // * the IO reader exception
    // * @throws ChannelStateReaderException
    // * the channel state reader exception
    // * @throws InvalidApduReaderException
    // * the invalid apdu reader exception
    // * @throws TimeoutReaderException
    // * the timeout reader exception
    // * @throws InconsistentCommandException
    // * the inconsistent command exception
    // */
    // private SeResponse getChallenge(boolean keepChannelOpen, ProxyReader reader)
    // throws UnexpectedReaderException, IOReaderException, ChannelStateReaderException,
    // InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {
    // List<ApduRequest> apduRequestList = new ArrayList<ApduRequest>();
    //
    // ApduCommandBuilder csmGetChallenge = new CsmGetChallengeCmdBuild(this.csmRevision, (byte)
    // 0x04);
    // ApduRequest request = csmGetChallenge.getApduRequest();
    // apduRequestList.add(request);
    //
    // return transmitApduResquests(null, reader, keepChannelOpen, apduRequestList);
    // }

    /**
     * Process identification. On poReader, generate a SERequest for the specified PO AID, with
     * keepChannelOpen set at true, and apduRequests defined with the optional
     * poCommands_OutsideSession. Returns the corresponding SEResponse. Identifies the serial and
     * the revision of the PO from FCI data. On csmSessionReader, automatically operate the Select
     * Diversifier and the Get Challenge.
     *
     * @param poAid the po AID
     * @param poCommandsOutsideSession the po commands outside session
     * @return the SE response
     * @throws UnexpectedReaderException the unexpected reader exception
     * @throws IOReaderException the IO reader exception
     * @throws ChannelStateReaderException the channel state reader exception
     * @throws InvalidApduReaderException the invalid apdu reader exception
     * @throws TimeoutReaderException the timeout reader exception
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public SeResponse processIdentification(byte[] poAid,
            List<SendableInSession> poCommandsOutsideSession)
            throws UnexpectedReaderException, IOReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {


        // Get PO ApduRequest List from SendableInSession List
        List<ApduRequest> poApduRequestList =
                this.getApduRequestListFromSendableInSessionListTo(poCommandsOutsideSession);
        // Init CSM ApduRequest List
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();
        // PO & CSM channels to be kept "Open"
        boolean keepChannelOpen = true;

        // Transfert PO commands
        System.out.println("\t========= Identification === Transfert PO commands");
        SeRequest poRequest = new SeRequest(poAid, poApduRequestList, keepChannelOpen);
        SeResponse poResponse = poReader.transmit(poRequest);


        // Parse PO FCI - to retrieve Calypso Revision, Serial Number, & DF Name (AID)
        GetDataFciRespPars poFciRespPars = new GetDataFciRespPars(poResponse.getFci());
        poRevision = computePoRevision(poFciRespPars.getApplicationTypeByte());
        poCalypsoInstanceAid = poFciRespPars.getDfName();
        System.out.println("\t========= Identification === Selected DF Name : "
                + DatatypeConverter.printHexBinary(poCalypsoInstanceAid));
        poCalypsoInstanceSerial = poFciRespPars.getApplicationSerialNumber();
        System.out.println("\t========= Identification === Calypso Serial Number : "
                + DatatypeConverter.printHexBinary(poCalypsoInstanceSerial));

        // Define CSM Select Diversifier command
        ApduCommandBuilder selectDiversifier =
                new SelectDiversifierCmdBuild(this.csmRevision, poCalypsoInstanceSerial);
        csmApduRequestList.add(selectDiversifier.getApduRequest());
        System.out.println(
                "\t========= Identification === Generate CSM cmd request - Select Diversifier : "
                        + DatatypeConverter
                                .printHexBinary(selectDiversifier.getApduRequest().getbytes()));

        // Define CSM Get Challenge command
        ApduCommandBuilder csmGetChallenge =
                new CsmGetChallengeCmdBuild(this.csmRevision, (byte) 0x04);
        csmApduRequestList.add(csmGetChallenge.getApduRequest());
        System.out.println(
                "\t========= Identification === Generate CSM cmd request - Get Challenge : "
                        + DatatypeConverter
                                .printHexBinary(csmGetChallenge.getApduRequest().getbytes()));

        // Transfert CSM commands
        System.out.println("\t========= Identification === Transfert CSM commands");
        SeRequest csmRequest = new SeRequest(null, csmApduRequestList, keepChannelOpen);
        SeResponse csmResponse = csmReader.transmit(csmRequest);
        List<ApduResponse> csmApduResponseList = csmResponse.getApduResponses();

        System.out.println(
                "\t\tDEBUG ##### csmApduResponseList.size() : " + csmApduResponseList.size());
        if (csmApduResponseList.size() == 2) {
            // TODO => check that csmApduResponseList.get(1) has the right length (challenge +
            // status)
            CsmGetChallengeRespPars csmChallengePars =
                    new CsmGetChallengeRespPars(csmApduResponseList.get(1));
            System.out.println(
                    "\t========= Identification === Parse CSM cmd response - Select Diversifier : "
                            + DatatypeConverter
                                    .printHexBinary(csmChallengePars.getApduResponse().getbytes()));
            sessionTerminalChallenge = csmChallengePars.getChallenge();
            System.out.println("\t========= Identification === Terminal Challenge : "
                    + DatatypeConverter.printHexBinary(sessionTerminalChallenge));
        } else {
            // TODO traitement erreur
        }

        currentState = SessionState.PO_IDENTIFIED;
        return poResponse;
    }

    /**
     * Process opening. On poReader, generate a SERequest with the current selected AID, with
     * keepChannelOpen set at true, and apduRequests defined with openCommand and the optional
     * poCommands_InsideSession. Returns the corresponding SEResponse (for openCommand and
     * poCommands_InsideSession). Identifies the session PO keyset. On csmSessionReader,
     * automatically operate the Digest Init and potentially several Digest Update Multiple.
     *
     * @param openCommand the open command
     * @param poCommandsInsideSession the po commands inside session
     * @return the SE response
     * @throws UnexpectedReaderException the unexpected reader exception
     * @throws IOReaderException the IO reader exception
     * @throws ChannelStateReaderException the channel state reader exception
     * @throws InvalidApduReaderException the invalid apdu reader exception
     * @throws TimeoutReaderException the timeout reader exception
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public SeResponse processOpening(OpenSessionCmdBuild openCommand,
            List<SendableInSession> poCommandsInsideSession)
            throws UnexpectedReaderException, IOReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        // Get PO ApduRequest List from SendableInSession List
        // List<ApduRequest> poApduRequestList =
        // this.getApduRequestListFromSendableInSessionListTo(poCommandsInsideSession);
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();
        // Init CSM ApduRequest List
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();
        // PO & CSM channels to be kept "Open"
        boolean keepChannelOpen = true;

        // Add Open Session command to PO ApduRequest list
        poApduRequestList.add(openCommand.getApduRequest());
        // Add list of SendableInSession commands to PO ApduRequest
        if ((poCommandsInsideSession != null) && !poCommandsInsideSession.isEmpty()) {
            poApduRequestList.addAll(
                    this.getApduRequestListFromSendableInSessionListTo(poCommandsInsideSession));
        }

        // Transfert PO commands
        System.out.println("\t========= Opening ========== Transfert PO commands");
        SeRequest poRequest =
                new SeRequest(poCalypsoInstanceAid, poApduRequestList, keepChannelOpen);
        SeResponse poResponse = poReader.transmit(poRequest);
        List<ApduResponse> poApduResponseList = poResponse.getApduResponses();

        // Parse OpenSession Response to get Card Challenge
        if (poApduResponseList.isEmpty()) {
            // TODO traitement erreur
        }
        // TODO => check that csmApduResponseList.get(1) has the right length (challenge + status)
        OpenSessionRespPars poOpenSessionPars =
                new OpenSessionRespPars(poApduResponseList.get(0), poRevision); // first item of
                                                                                // poApduResponseList
        System.out.println("\t========= Opening ========== Parse PO cmd response - Open Session : "
                + DatatypeConverter.printHexBinary(poOpenSessionPars.getApduResponse().getbytes()));
        sessionCardChallenge = poOpenSessionPars.getPoChallenge();
        System.out.println("\t========= Opening ========== WRONG Card Challenge : "
                + DatatypeConverter.printHexBinary((byte[]) sessionCardChallenge));


        // HACK - OpenSessionRespPars.getPoChallenge() ne retourne pas la bonne valeur de PO
        // challenge
        // TODO - corriger => OpenSessionRespPars.getPoChallenge()
        sessionCardChallenge = DatatypeConverter.parseHexBinary(
                DatatypeConverter.printHexBinary(poOpenSessionPars.getApduResponse().getbytes())
                        .substring(0, 4 * 2)); // HACK
        System.out.println("\t========= Opening ========== HACKED Card Challenge : "
                + DatatypeConverter.printHexBinary((byte[]) sessionCardChallenge));

        // Build "Digest Init" command from PO Open Session
        byte kif = poOpenSessionPars.getSelectedKif();
        System.out.println("\t========= Opening ========== PO KIF : "
                + DatatypeConverter.printHexBinary(new byte[] {(byte) kif}));
        System.out.println("\t========= Opening ========== PO KVC : " + DatatypeConverter
                .printHexBinary(new byte[] {(byte) poOpenSessionPars.getSelectedKvc()}));
        if (kif == (byte) 0xFF) {
            if (defaultKeyIndex == (byte) 0x01) {
                kif = (byte) 0x21;
            } else if (defaultKeyIndex == (byte) 0x02) {
                kif = (byte) 0x27;
            } else if (defaultKeyIndex == (byte) 0x03) {
                kif = (byte) 0x30;
            }
        }
        ApduCommandBuilder digestInit = new DigestInitCmdBuild(csmRevision, false,
                poRevision.equals(PoRevision.REV3_2), defaultKeyIndex, kif,
                poOpenSessionPars.getSelectedKvc(), poOpenSessionPars.getRecordDataRead());
        System.out
                .println("\t========= Opening ========== Generate CSM cmd request - Digest Init : "
                        + DatatypeConverter.printHexBinary(digestInit.getApduRequest().getbytes()));
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
                System.out.println(
                        "\t========= Opening ========== Generate CSM cmd request - Digest Update for PO request : "
                                + DatatypeConverter
                                        .printHexBinary((new DigestUpdateCmdBuild(csmRevision,
                                                false, poApduRequestList.get(i).getbytes()))
                                                        .getApduRequest().getbytes()));
                csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
                        poApduRequestList.get(i).getbytes())).getApduRequest());
                // Build "Digest Update" command for each PO APDU Response
                System.out.println(
                        "\t========= Opening ==WRONG=== Generate CSM cmd request - Digest Update for PO response : "
                                + DatatypeConverter
                                        .printHexBinary((new DigestUpdateCmdBuild(csmRevision,
                                                false, poApduResponseList.get(i).getbytes()))
                                                        .getApduRequest().getbytes()));
                // csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
                // poApduResponseList.get(i).getbytes())).getApduRequest()); //TODO => this is the
                // rigth command, to fix ApduResponse.getbytes
                byte[] additionOfGetBytesAndGetStatusCode =
                        ArrayUtils.addAll(poApduResponseList.get(i).getbytes(),
                                poApduResponseList.get(i).getStatusCode());
                // System.out.println("\t\tDEBUG ##### csmApduResponseList.size() : " +
                // DatatypeConverter.printHexBinary(additionOfGetBytesAndGetStatusCode));
                System.out.println(
                        "\t========= Opening ==HACK==== Generate CSM cmd request - Digest Update for PO response : "
                                + DatatypeConverter
                                        .printHexBinary((new DigestUpdateCmdBuild(csmRevision,
                                                false, additionOfGetBytesAndGetStatusCode))
                                                        .getApduRequest().getbytes()));
                csmApduRequestList.add(((new DigestUpdateCmdBuild(csmRevision, false,
                        additionOfGetBytesAndGetStatusCode)).getApduRequest())); // HACK
            }
        }

        // Transfert CSM commands
        System.out.println("\t========= Opening ========== Transfert CSM commands");
        SeRequest csmRequest = new SeRequest(null, csmApduRequestList, keepChannelOpen);
        SeResponse csmResponse = csmReader.transmit(csmRequest);
        // List<ApduResponse> csmApduResponseList = csmResponse.getApduResponses();

        currentState = SessionState.SESSION_OPEN;
        return poResponse;
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
                // from SendableInSession & most of PoCommandBuilder extensions
                retour.add(((PoCommandBuilder) cmd).getApduRequest()); // Il fallait faire un "CAST"
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
    private OpenSessionRespPars getSecureSessionBySEResponseAndRevision(
            SeResponse responseOpenSession) throws UnexpectedReaderException {

        OpenSessionRespPars openSessionRespPars =
                new OpenSessionRespPars(responseOpenSession.getApduResponses().get(0), poRevision);
        if (!openSessionRespPars.isSuccessful()) {
            throw new UnexpectedReaderException(openSessionRespPars.getStatusInformation());
        }

        return openSessionRespPars;
    }


    /**
     * Process proceeding. On poReader, generate a SERequest with the current selected AID, with
     * keepChannelOpen set at true, and apduRequests defined with the poCommands_InsideSession.
     * Returns the corresponding SEResponse (for poCommands_InsideSession). On csmSessionReader,
     * automatically operate potentially several Digest Update Multiple.
     *
     * @param poCommandsInsideSession the po commands inside session
     * @return a SE Response
     *
     * @throws IOReaderException
     * @throws UnexpectedReaderException
     * @throws ChannelStateReaderException
     * @throws InvalidApduReaderException
     * @throws TimeoutReaderException
     * @throws InconsistentCommandException
     */
    public SeResponse processProceeding(List<SendableInSession> poCommandsInsideSession)
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        // Get PO ApduRequest List from SendableInSession List
        List<ApduRequest> poApduRequestList =
                this.getApduRequestListFromSendableInSessionListTo(poCommandsInsideSession);
        // Init CSM ApduRequest List
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();
        // PO & CSM to be kept "Open"
        boolean keepChannelOpen = true;

        // Transfert PO commands
        System.out.println("\t========= Continuation ===== Transfert PO commands");
        SeRequest poRequest =
                new SeRequest(poCalypsoInstanceAid, poApduRequestList, keepChannelOpen);
        SeResponse poResponse = poReader.transmit(poRequest);
        List<ApduResponse> poApduResponseList = poResponse.getApduResponses();

        // Browse all exchanged PO commands to compute CSM digest
        // TODO => rajouter un contr�le afin de v�rifier que poApduResponseList a m�me taille que
        // poApduRequestList
        for (int i = 0; i < poApduRequestList.size(); i++) {
            // Build "Digest Update" command for each PO APDU Request
            System.out.println(
                    "\t========= Continuation ===== Generate CSM cmd request - Digest Update for PO request : "
                            + DatatypeConverter
                                    .printHexBinary((new DigestUpdateCmdBuild(csmRevision, false,
                                            poApduRequestList.get(i).getbytes())).getApduRequest()
                                                    .getbytes()));
            csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
                    poApduRequestList.get(i).getbytes())).getApduRequest());
            // Build "Digest Update" command for each PO APDU Response
            // System.out.println("\t========= Continuation ===== Generate CSM cmd request - Digest
            // Update for PO response : " + DatatypeConverter.printHexBinary((new
            // DigestUpdateCmdBuild(csmRevision, false,
            // poApduResponseList.get(i).getbytes())).getApduRequest().getbytes()));
            System.out.println(
                    "\t==WRONG== Continuation ===== Generate CSM cmd request - Digest Update for PO response : "
                            + DatatypeConverter
                                    .printHexBinary((new DigestUpdateCmdBuild(csmRevision, false,
                                            poApduResponseList.get(i).getbytes())).getApduRequest()
                                                    .getbytes()));
            // csmApduRequestList.add((new DigestUpdateCmdBuild(csmRevision, false,
            // poApduResponseList.get(i).getbytes())).getApduRequest()); //TODO => this is the rigth
            // command, to fix ApduResponse.getbytes
            byte[] additionOfGetBytesAndGetStatusCode =
                    ArrayUtils.addAll(poApduResponseList.get(i).getbytes(),
                            poApduResponseList.get(i).getStatusCode());
            // System.out.println("\t\tDEBUG ##### csmApduResponseList.size() : " +
            // DatatypeConverter.printHexBinary(additionOfGetBytesAndGetStatusCode));
            System.out.println(
                    "\t==HACK=== Continuation ===== Generate CSM cmd request - Digest Update for PO response : "
                            + DatatypeConverter
                                    .printHexBinary((new DigestUpdateCmdBuild(csmRevision, false,
                                            additionOfGetBytesAndGetStatusCode)).getApduRequest()
                                                    .getbytes()));
            csmApduRequestList.add(((new DigestUpdateCmdBuild(csmRevision, false,
                    additionOfGetBytesAndGetStatusCode)).getApduRequest())); // HACK

        }

        // Transfert CSM commands
        System.out.println("\t========= Continuation ===== Transfert CSM commands");
        SeRequest csmRequest = new SeRequest(null, csmApduRequestList, keepChannelOpen);
        SeResponse csmResponse = csmReader.transmit(csmRequest);
        // List<ApduResponse> csmApduResponseList = csmResponse.getApduResponses();

        return poResponse;
    }

    /**
     * Process closing. On csmSessionReader, automatically operate potentially several Digest Update
     * Multiple, and the Digest Close. Identifies the terminal signature. On poReader, generate a
     * SERequest with the current selected AID, with keepChannelOpen set at false, and apduRequests
     * defined with poCommands_InsideSession, closeCommand, and ratificationCommand. Identifies the
     * PO signature. On csmSessionReader, automatically operates the Digest Authenticate. Returns
     * the corresponding SEResponse and the boolean status of the authentication.
     *
     * @param poCommandsInsideSession the po commands inside session
     * @param ratificationCommand the ratification command
     * @return SEResponse close session response
     * @throws IOReaderException the IO reader exception
     * @throws UnexpectedReaderException the unexpected reader exception
     * @throws ChannelStateReaderException the channel state reader exception
     * @throws InvalidApduReaderException the invalid apdu reader exception
     * @throws TimeoutReaderException the timeout reader exception
     * @throws InconsistentCommandException the inconsistent command exception
     */
    // public SeResponse processClosing(List<SendableInSession> poCommandsInsideSession,
    // CloseSessionCmdBuild closeCommand, PoGetChallengeCmdBuild ratificationCommand)
    // TODO - prévoir une variante pour enchainer plusieurs session d'affilée (la commande de
    // ratification étant un nouveau processOpening)
    public SeResponse processClosing(List<SendableInSession> poCommandsInsideSession,
            List<ApduResponse> poAnticipatedResponseInsideSession,
            PoCommandBuilder ratificationCommand)
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

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

        SeResponse poResponse;

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
                            poApduRequestList.get(i).getbytes())).getApduRequest());
                    // Build "Digest Update" command for each "ANTICIPATED" PO APDU Response
                    // csmApduRequestList_1.add((new DigestUpdateCmdBuild(csmRevision, false,
                    // poApduResponseList_1.get(i).getbytes())).getApduRequest());
                    csmApduRequestList_1.add((new DigestUpdateCmdBuild(csmRevision, false,
                            poAnticipatedResponseInsideSession.get(i).getbytes()))
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
        System.out
                .println("\t========= Closing ========== Generate CSM cmd request - Digest Close : "
                        + DatatypeConverter
                                .printHexBinary(digestClose.getApduRequest().getbytes()));
        csmApduRequestList_1.add(digestClose.getApduRequest());

        // ****FIRST**** transfert of CSM commands
        System.out.println("\t========= Closing ========== Transfert CSM commands - #1");
        SeRequest csmRequest_1 = new SeRequest(null, csmApduRequestList_1, keepChannelOpen);
        SeResponse csmResponse_1 = csmReader.transmit(csmRequest_1);
        List<ApduResponse> csmApduResponseList_1 = csmResponse_1.getApduResponses();
        System.out.println(
                "\t\tDEBUG ##### csmApduResponseList_1.size() : " + csmApduResponseList_1.size());

        // Get Terminal Signature
        if ((csmApduResponseList_1 != null) && !csmApduResponseList_1.isEmpty()) {
            // T item = csmApduResponseList.get(csmApduResponseList.size()-1);
            DigestCloseRespPars respPars = new DigestCloseRespPars(
                    csmApduResponseList_1.get(csmApduResponseList_1.size() - 1)); // .getApduResponses().get(0);

            System.out.println(
                    "\t========= Closing ========== Parse CSM cmd response - Digest Close : "
                            + DatatypeConverter
                                    .printHexBinary(respPars.getApduResponse().getbytes()));
            System.out.println("\t\tDEBUG ##### csmApduResponseList_1.size() : "
                    + csmApduResponseList_1.size());
            sessionTerminalSignature = respPars.getSignature();
        }

        // a ****SINGLE**** PO exchange - the "LAST" one
        System.out.println("\t========= Closing ========== Transfert PO commands - SINGLE");
        keepChannelOpen = false; // a last PO Request & a last CSM Reaquest

        boolean ratificationAsked = (ratificationCommand != null);

        // Build PO Close Session command
        CloseSessionCmdBuild closeCommand =
                new CloseSessionCmdBuild(poRevision, ratificationAsked, sessionTerminalSignature);

        poApduRequestList.add(closeCommand.getApduRequest());

        // Build PO Ratification command
        if (ratificationAsked)
            poApduRequestList.add(ratificationCommand.getApduRequest());

        // Transfert PO commands
        SeRequest poRequest =
                new SeRequest(poCalypsoInstanceAid, poApduRequestList, keepChannelOpen);
        poResponse = poReader.transmit(poRequest);
        List<ApduResponse> poApduResponseList = poResponse.getApduResponses();

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
        sessionCardSignature = poCloseSessionPars.getSignatureLo();

        // Build CSM Digest Authenticate command
        ApduCommandBuilder digestAuth =
                new DigestAuthenticateCmdBuild(this.csmRevision, sessionCardSignature);
        csmApduRequestList_2.add(digestAuth.getApduRequest());

        // ****SECOND**** transfert of CSM commands
        System.out.println("\t========= Closing ========== Transfert CSM commands - #2");
        SeRequest csmRequest_2 = new SeRequest(null, csmApduRequestList_2, keepChannelOpen);
        SeResponse csmResponse_2 = csmReader.transmit(csmRequest_2);
        List<ApduResponse> csmApduResponseList_2 = csmResponse_2.getApduResponses();

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
        return poResponse;
    }

    public static PoRevision computePoRevision(byte applicationTypeByte) {
        PoRevision rev = PoRevision.REV3_1;
        if (applicationTypeByte <= (byte) 0x1F) {
            rev = PoRevision.REV2_4;
        } else if (Byte.valueOf(applicationTypeByte).compareTo((byte) 0x7f) <= 0
                && Byte.valueOf(applicationTypeByte).compareTo((byte) 0x20) >= 0) {
            if (isBitEqualsOne(applicationTypeByte, 3)) {
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
    public byte[] getSessionTerminalChallenge() {
        return sessionTerminalChallenge;
    }

    /**
     * Checks if is bit equals one.
     *
     * @param thebyte the thebyte
     * @param position the position
     * @return true, if is bit equals one
     */
    private static boolean isBitEqualsOne(byte thebyte, int position) {
        return (1 == ((thebyte >> position) & 1));
    }
}

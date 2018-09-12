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
import java.util.EnumMap;
import java.util.List;
import org.eclipse.keyple.calypso.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.calypso.command.SendableInSession;
import org.eclipse.keyple.calypso.command.csm.CsmRevision;
import org.eclipse.keyple.calypso.command.csm.CsmSendableInSession;
import org.eclipse.keyple.calypso.command.csm.builder.*;
import org.eclipse.keyple.calypso.command.csm.parser.CsmGetChallengeRespPars;
import org.eclipse.keyple.calypso.command.csm.parser.DigestAuthenticateRespPars;
import org.eclipse.keyple.calypso.command.csm.parser.DigestCloseRespPars;
import org.eclipse.keyple.calypso.command.po.PoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoModificationCommand;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;
import org.eclipse.keyple.calypso.command.po.builder.session.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.session.CloseSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.calypso.command.po.parser.session.AbstractOpenSessionRespPars;
import org.eclipse.keyple.calypso.command.po.parser.session.CloseSessionRespPars;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.InvalidMessageException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Portable Object Secure Session.
 *
 * A non-encrypted secure session with a Calypso PO requires the management of two
 * {@link ProxyReader} in order to communicate with both a Calypso PO and a CSM
 *
 * @author Calypso Networks Association
 */
public class PoSecureSession {

    /* public constants */
    /** The key index for personalization operations (issuer key needed) */
    public static final byte KEY_INDEX_PERSONALIZATION = (byte) 0x01;
    /** The key index for reloading operations (loading key needed) */
    public static final byte KEY_INDEX_LOAD = (byte) 0x02;
    /** The key index for debit and validation operations (validation key needed) */
    public static final byte KEY_INDEX_VALIDATION_DEBIT = (byte) 0x03;
    /** The default KIF value for personalization */
    public final static byte DEFAULT_KIF_PERSO = (byte) 0x21;
    /** The default KIF value for loading */
    public final static byte DEFAULT_KIF_LOAD = (byte) 0x27;
    /** The default KIF value for debiting */
    public final static byte DEFAULT_KIF_DEBIT = (byte) 0x30;
    /** The default key record number */
    public final static byte DEFAULT_KEY_RECORD_NUMER = (byte) 0x00;

    /* private constants */
    private final static byte KIF_UNDEFINED = (byte) 0xFF;

    private final static byte CHALLENGE_LENGTH_REV_INF_32 = (byte) 0x04;
    private final static byte CHALLENGE_LENGTH_REV32 = (byte) 0x08;
    private final static byte SIGNATURE_LENGTH_REV_INF_32 = (byte) 0x04;
    private final static byte SIGNATURE_LENGTH_REV32 = (byte) 0x08;

    private static final Logger logger = LoggerFactory.getLogger(PoSecureSession.class);

    /** The reader for PO. */
    private final ProxyReader poReader;
    /** The reader for session CSM. */
    private final ProxyReader csmReader;
    /** The CSM default revision. */
    private final CsmRevision csmRevision = CsmRevision.C1;
    /** The CSM settings map. */
    private final EnumMap<CsmSettings, Byte> csmSetting =
            new EnumMap<CsmSettings, Byte>(CsmSettings.class);
    /** the type of the notified event. */
    private SessionState currentState;
    /** Selected AID of the Calypso PO. */
    private ByteBuffer poCalypsoInstanceAid;
    /** The PO Calypso Revision. */
    private PoRevision poRevision = PoRevision.REV3_1;
    /** The PO Secure Session final status according to mutual authentication result */
    private boolean transactionResult;

    /**
     * Instantiates a new po plain secure session.
     * <ul>
     * <li>Logical channels with PO &amp; CSM could already be established or not.</li>
     * <li>A list of CSM parameters is provided as en EnumMap.</li>
     * </ul>
     *
     * @param poReader the PO reader
     * @param csmReader the SAM reader
     * @param csmSetting a list of CSM related parameters. In the case this parameter is null,
     *        default parameters are applied. The available setting keys are defined in
     *        {@link CsmSettings}
     */
    public PoSecureSession(ProxyReader poReader, ProxyReader csmReader,
            EnumMap<CsmSettings, Byte> csmSetting) {
        this.poReader = poReader;
        this.csmReader = csmReader;

        /* Initialize csmSetting with provided settings */
        if (csmSetting != null) {
            this.csmSetting.putAll(csmSetting);
        }

        /* Just work mode: we make sure that all the necessary parameters exist at least. */
        if (!this.csmSetting.containsKey(CsmSettings.CS_DEFAULT_KIF_PERSO)) {
            this.csmSetting.put(CsmSettings.CS_DEFAULT_KIF_PERSO, DEFAULT_KIF_PERSO);
        }
        if (!this.csmSetting.containsKey(CsmSettings.CS_DEFAULT_KIF_LOAD)) {
            this.csmSetting.put(CsmSettings.CS_DEFAULT_KIF_LOAD, DEFAULT_KIF_LOAD);
        }
        if (!this.csmSetting.containsKey(CsmSettings.CS_DEFAULT_KIF_DEBIT)) {
            this.csmSetting.put(CsmSettings.CS_DEFAULT_KIF_DEBIT, DEFAULT_KIF_DEBIT);
        }
        if (!this.csmSetting.containsKey(CsmSettings.CS_DEFAULT_KEY_RECORD_NUMBER)) {
            this.csmSetting.put(CsmSettings.CS_DEFAULT_KEY_RECORD_NUMBER, DEFAULT_KEY_RECORD_NUMER);
        }

        logger.debug("PoSecureSession => contructor: CSMSETTING = {}", this.csmSetting);

        currentState = SessionState.SESSION_CLOSED;
    }

    /**
     * Open a Secure Session.
     * <ul>
     * <li>The PO must have been previously selected, so a logical channel with the PO application
     * must be already active.</li>
     * <li>The PO serial &amp; revision are identified from FCI data.</li>
     * <li>A first request is sent to the CSM session reader.
     * <ul>
     * <li>In case not logical channel is active with the CSM, a channel is open.</li>
     * <li>Then a Select Diversifier (with the PO serial) &amp; a Get Challenge are automatically
     * operated. The CSM challenge is recovered.</li>
     * </ul>
     * </li>
     * <li>The PO Open Session command is built according to the PO revision, the CSM challenge, the
     * keyIndex, and openingSfiToSelect / openingRecordNumberToRead.</li>
     * <li>Next the PO reader is requested:
     * <ul>
     * <li>for the current selected PO AID, with keepChannelOpen set at true,</li>
     * <li>and some PO Apdu Requests including at least the Open Session command and optionally some
     * PO command to operate inside the session.</li>
     * </ul>
     * </li>
     * <li>The session PO keyset reference is identified from the PO Open Session response, the PO
     * challenge is recovered too.</li>
     * <li>According to the PO responses of Open Session and the PO commands sent inside the
     * session, a "cache" of CSM commands is filled with the corresponding Digest Init &amp; Digest
     * Update commands.</li>
     * <li>Returns the corresponding PO SeResponse (for openCommand and
     * poCommandsInsideSession).</li>
     * </ul>
     *
     * @param poFciData the po response to the application selection (FCI)
     * @param accessLevel access level of the session (personalization, load or debit).
     * @param openingSfiToSelect SFI of the file to select (0 means no file to select)
     * @param openingRecordNumberToRead number of the record to read
     * @param poCommandsInsideSession the po commands inside session
     * @return SeResponse response to all executed commands including the self generated "Open
     *         Secure Session" command
     * @throws IOReaderException the IO reader exception
     */
    public SeResponse processOpening(ApduResponse poFciData, SessionAccessLevel accessLevel,
            byte openingSfiToSelect, byte openingRecordNumberToRead,
            List<PoSendableInSession> poCommandsInsideSession) throws IOReaderException {

        /* CSM ApduRequest List to hold Select Diversifier and Get Challenge commands */
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();

        /* Parse PO FCI - to retrieve Calypso Revision, Serial Number, &amp; DF Name (AID) */
        GetDataFciRespPars poFciRespPars = new GetDataFciRespPars(poFciData);
        poRevision = computePoRevision(poFciRespPars.getApplicationTypeByte());
        poCalypsoInstanceAid = poFciRespPars.getDfName();

        /* Serial Number of the selected Calypso instance. */
        ByteBuffer poCalypsoInstanceSerial = poFciRespPars.getApplicationSerialNumber();

        if (logger.isDebugEnabled()) {
            logger.debug("processOpening => Identification: DFNAME = {}, SERIALNUMBER = {}",
                    ByteBufferUtils.toHex(poCalypsoInstanceAid),
                    ByteBufferUtils.toHex(poCalypsoInstanceSerial));
        }

        /* Build the CSM Select Diversifier command to provide the CSM with the PO S/N */
        AbstractApduCommandBuilder selectDiversifier =
                new SelectDiversifierCmdBuild(this.csmRevision, poCalypsoInstanceSerial);

        csmApduRequestList.add(selectDiversifier.getApduRequest());

        /* Build the CSM Get Challenge command */
        byte challengeLength = poRevision.equals(PoRevision.REV3_2) ? CHALLENGE_LENGTH_REV32
                : CHALLENGE_LENGTH_REV_INF_32;

        AbstractApduCommandBuilder csmGetChallenge =
                new CsmGetChallengeCmdBuild(this.csmRevision, challengeLength);

        csmApduRequestList.add(csmGetChallenge.getApduRequest());

        /* Build a CSM SeRequest */
        SeRequest csmSeRequest = new SeRequest(null, csmApduRequestList, true);

        logger.debug("PoSecureSession => processOpening, identification: CSMSEREQUEST = {}",
                csmSeRequest);

        /*
         * Create a SeRequestSet (list of SeRequest), transmit it to the CSM and get back the
         * SeResponse (list of ApduResponse)
         */
        SeRequestSet csmSeRequestSet = new SeRequestSet(csmSeRequest);
        SeResponse csmSeResponse = csmReader.transmit(csmSeRequestSet).getSingleResponse();

        if (csmSeResponse == null) {
            throw new InvalidMessageException("Null response received",
                    InvalidMessageException.Type.CSM, csmSeRequest.getApduRequests(), null);
        }

        logger.debug("PoSecureSession => processOpening, identification: CSMSERESPONSE = {}",
                csmSeResponse);

        List<ApduResponse> csmApduResponseList = csmSeResponse.getApduResponses();
        ByteBuffer sessionTerminalChallenge;

        if (csmApduResponseList.size() == 2 && csmApduResponseList.get(1).isSuccessful()
                && csmApduResponseList.get(1).getDataOut().limit() == challengeLength) {
            CsmGetChallengeRespPars csmChallengePars =
                    new CsmGetChallengeRespPars(csmApduResponseList.get(1));
            sessionTerminalChallenge = csmChallengePars.getChallenge();
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "PoSecureSession => processOpening, identification: TERMINALCHALLENGE = {}",
                        ByteBufferUtils.toHex(sessionTerminalChallenge));
            }
        } else {
            throw new InvalidMessageException("Invalid message received",
                    InvalidMessageException.Type.CSM, csmApduRequestList, csmApduResponseList);
        }

        /* PO ApduRequest List to hold Open Secure Session and other optional commands */
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();

        /* Build the PO Open Secure Session command */
        AbstractOpenSessionCmdBuild poOpenSession = AbstractOpenSessionCmdBuild.create(
                getRevision(), (byte) (accessLevel.ordinal() + 1), sessionTerminalChallenge,
                openingSfiToSelect, openingRecordNumberToRead);

        /* Add the resulting ApduRequest to the PO ApduRequest list */
        poApduRequestList.add(poOpenSession.getApduRequest());

        /* Add all optional PoSendableInSession commands to the PO ApduRequest list */
        if (poCommandsInsideSession != null) {
            poApduRequestList.addAll(this.getApduRequestsToSendInSession(
                    (List<SendableInSession>) (List<?>) poCommandsInsideSession));
        }

        /* Create a SeRequest from the ApduRequest list, PO AID as Selector, keepChannelOpen true */
        SeRequest poSeRequest = new SeRequest(new SeRequest.AidSelector(poCalypsoInstanceAid),
                poApduRequestList, true);

        logger.debug("PoSecureSession => processOpening, opening:  POSEREQUEST = {}", poSeRequest);

        /* Create a SeRequestSet from a unique SeRequest in this case */
        SeRequestSet poRequestSet = new SeRequestSet(poSeRequest);

        /* Transmit the commands to the PO */
        SeResponse poSeResponse = poReader.transmit(poRequestSet).getSingleResponse();

        logger.debug("PoSecureSession => processOpening, opening:  POSERESPONSE = {}",
                poSeResponse);

        if (poSeResponse == null) {
            throw new InvalidMessageException("Null response received",
                    InvalidMessageException.Type.PO, poSeRequest.getApduRequests(), null);
        }

        /* Retrieve and check the ApduResponses */
        List<ApduResponse> poApduResponseList = poSeResponse.getApduResponses();

        /* Do some basic checks */
        if (poApduRequestList.size() != poApduResponseList.size()) {
            throw new InvalidMessageException("Inconsistent requests and responses",
                    InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
        }

        for (ApduResponse apduR : poApduResponseList) {
            if (!apduR.isSuccessful()) {
                throw new InvalidMessageException("Invalid response",
                        InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
            }
        }

        /* Parse the response to Open Secure Session (the first item of poApduResponseList) */
        AbstractOpenSessionRespPars poOpenSessionPars =
                AbstractOpenSessionRespPars.create(poApduResponseList.get(0), poRevision);
        ByteBuffer sessionCardChallenge = poOpenSessionPars.getPoChallenge();

        /* Build the Digest Init command from PO Open Session */
        byte kif = poOpenSessionPars.getSelectedKif();
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "PoSecureSession => processOpening, opening: CARDCHALLENGE = {}, POKIF = {}, POKVC = {}",
                    ByteBufferUtils.toHex(sessionCardChallenge),
                    String.format("%02X", poOpenSessionPars.getSelectedKif()),
                    String.format("%02X", poOpenSessionPars.getSelectedKvc()));
        }

        if (kif == KIF_UNDEFINED) {
            switch (accessLevel) {
                case SESSION_LVL_PERSO:
                    kif = csmSetting.get(CsmSettings.CS_DEFAULT_KIF_PERSO);
                    break;
                case SESSION_LVL_LOAD:
                    kif = csmSetting.get(CsmSettings.CS_DEFAULT_KIF_LOAD);
                    break;
                case SESSION_LVL_DEBIT:
                    kif = csmSetting.get(CsmSettings.CS_DEFAULT_KIF_DEBIT);
                    break;
            }
        }

        /*
         * Initialize the DigestProcessor. It will store all digest operations (Digest Init, Digest
         * Update) until the session closing. AT this moment, all CSM Apdu will be processed at
         * once.
         */
        DigestProcessor.initialize(poRevision, csmRevision, false, false,
                poRevision.equals(PoRevision.REV3_2),
                csmSetting.get(CsmSettings.CS_DEFAULT_KEY_RECORD_NUMBER), kif,
                poOpenSessionPars.getSelectedKvc(), poOpenSessionPars.getRecordDataRead());

        /*
         * Add all commands data to the digest computation. The first command in the list is the
         * open secure session command. This command is not included in the digest computation, so
         * we skip it and start the loop at index 1.
         */
        if ((poCommandsInsideSession != null) && !poCommandsInsideSession.isEmpty()) {

            for (int i = 1; i < poApduRequestList.size(); i++) { // The loop starts after the Open
                /*
                 * Add requests and responses to the DigestProcessor
                 */
                DigestProcessor.pushPoExchangeData(poApduRequestList.get(i),
                        poApduResponseList.get(i));
            }
        }

        currentState = SessionState.SESSION_OPEN;
        return poSeResponse;
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
     * Process PO commands in a Secure Session.
     * <ul>
     * <li>On the PO reader, generates a SeRequest for the current selected AID, with
     * keepChannelOpen set at true, and ApduRequests with the PO commands.</li>
     * <li>In case the secure session is active, the "cache" of CSM commands is completed with the
     * corresponding Digest Update commands.</li>
     * <li>Returns the corresponding PO SeResponse.</li>
     * </ul>
     *
     * @param poCommands the po commands inside session
     * @return SeResponse all responses to the provided commands
     *
     * @throws IOReaderException IO Reader exception
     */
    public SeResponse processPoCommands(List<PoSendableInSession> poCommands)
            throws IOReaderException {

        // Get PO ApduRequest List from PoSendableInSession List
        List<ApduRequest> poApduRequestList =
                this.getApduRequestsToSendInSession((List<SendableInSession>) (List<?>) poCommands);

        /* Create a SeRequest from the ApduRequest list, PO AID as Selector, keepChannelOpen true */
        SeRequest poSeRequest = new SeRequest(new SeRequest.AidSelector(poCalypsoInstanceAid),
                poApduRequestList, true);

        logger.debug("PoSecureSession => processPoCommands: POREQUEST = {}", poSeRequest);

        /* Create a SeRequestSet from a unique SeRequest in this case */
        SeRequestSet poRequestSet = new SeRequestSet(poSeRequest);

        /* Transmit the commands to the PO */
        SeResponse poSeResponse = poReader.transmit(poRequestSet).getSingleResponse();

        logger.debug("PoSecureSession => processPoCommands:PORESPONSE = {}", poSeResponse);

        if (poSeResponse == null) {
            throw new InvalidMessageException("Null response received",
                    InvalidMessageException.Type.PO, poSeRequest.getApduRequests(), null);
        }

        /* Retrieve and check the ApduResponses */
        List<ApduResponse> poApduResponseList = poSeResponse.getApduResponses();

        /* Do some basic checks */
        if (poApduRequestList.size() != poApduResponseList.size()) {
            throw new InvalidMessageException("Inconsistent requests and responses",
                    InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
        }

        for (ApduResponse apduR : poApduResponseList) {
            if (!apduR.isSuccessful()) {
                throw new InvalidMessageException("Invalid response",
                        InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
            }
        }

        /*
         * Add all commands data to the digest computation if this method is called within a Secure
         * Session.
         */
        if (currentState == SessionState.SESSION_OPEN) {
            for (int i = 0; i < poApduRequestList.size(); i++) { // The loop starts after the Open
                /*
                 * Add requests and responses to the DigestProcessor
                 */
                DigestProcessor.pushPoExchangeData(poApduRequestList.get(i),
                        poApduResponseList.get(i));
            }
        }
        return poSeResponse;
    }

    /**
     * Process CSM commands.
     * <ul>
     * <li>On the CSM reader, transmission of a SeRequest with keepChannelOpen set at true.</li>
     * <li>Returns the corresponding CSM SeResponse.</li>
     * </ul>
     *
     * @param csmCommands a list of commands to sent to the CSM
     * @return SeResponse all csm responses
     * @throws IOReaderException if a reader error occurs
     */
    public SeResponse processCsmCommands(List<CsmSendableInSession> csmCommands)
            throws IOReaderException {

        /* Init CSM ApduRequest List - for the first CSM exchange */
        List<ApduRequest> csmApduRequestList = this
                .getApduRequestsToSendInSession((List<SendableInSession>) (List<?>) csmCommands);

        /* SeRequest from the command list */
        SeRequest csmSeRequest = new SeRequest(null, csmApduRequestList, true);

        logger.debug("PoSecureSession => processCsmCommands: CSMSEREQUEST = {}", csmSeRequest);

        /* create a SeRequestSet (list of SeRequest) */
        SeRequestSet csmRequestSet = new SeRequestSet(csmSeRequest);

        SeResponse csmSeResponse = csmReader.transmit(csmRequestSet).getSingleResponse();

        logger.debug("PoSecureSession => processCsmCommands: CSMSERESPONSE = {}", csmSeResponse);

        return csmSeResponse;
    }

    /**
     * Close the Secure Session.
     * <ul>
     * <li>The CSM cache is completed with the Digest Update commands related to the new PO commands
     * to send and their anticipated responses. A Digest Close command is also added to the CSM
     * cache.</li>
     * <li>On the CSM session reader, a SeRequest is transmitted with CSM commands of the cache. The
     * CSM cache is emptied.</li>
     * <li>The CSM certificate is recovered from the Digest Close response. The terminal signature
     * is identified.</li>
     * <li>Next on the PO reader, a SeRequest is transmitted for the current selected AID, with
     * keepChannelOpen set at the reverse value of closeSeChannel, and apduRequests including the
     * new PO commands to send in the session, a Close Session command (defined with the CSM
     * certificate), and optionally a ratificationCommand.
     * <ul>
     * <li>If a PO ratification command is present, the PO Close Secure Session command is defined
     * to set the PO as non ratified.</li>
     * <li>Otherwise, the PO Close Secure Session command is defined to directly set the PO as
     * ratified.</li>
     * </ul>
     * </li>
     * <li>The PO responses of the poModificationCommands are compared with the
     * poAnticipatedResponses. The PO signature is identified from the PO Close Session
     * response.</li>
     * <li>The PO certificate is recovered from the Close Session response. The card signature is
     * identified.</li>
     * <li>Finally, on the CSM session reader, a Digest Authenticate is automatically operated in
     * order to verify the PO signature.</li>
     * <li>Returns the corresponding PO SeResponse.</li>
     * </ul>
     *
     * @param poModificationCommands a list of commands that can modify the PO memory content
     * @param poAnticipatedResponses The anticipated PO response in the sessions
     * @param ratificationCommand the ratification command
     * @param closeSeChannel if true the SE channel of the po reader is closed after the last
     *        command
     * @return SeResponse close session response
     * @throws IOReaderException the IO reader exception
     */
    public SeResponse processClosing(List<PoModificationCommand> poModificationCommands,
            List<ApduResponse> poAnticipatedResponses, PoCommandBuilder ratificationCommand,
            boolean closeSeChannel) throws IOReaderException {

        if (currentState != SessionState.SESSION_OPEN) {
            throw new IllegalStateException("Bad session state. Current: " + currentState.toString()
                    + ", expected: " + SessionState.SESSION_OPEN.toString());
        }

        /* Get PO ApduRequest List from PoSendableInSession List - for the first PO exchange */
        List<ApduRequest> poApduRequestList = this.getApduRequestsToSendInSession(
                (List<SendableInSession>) (List<?>) poModificationCommands);

        /* Compute "anticipated" Digest Update (for optional poModificationCommands) */
        if ((poModificationCommands != null) && !poApduRequestList.isEmpty()) {
            if (poApduRequestList.size() == poAnticipatedResponses.size()) {
                /*
                 * Add all commands data to the digest computation: commands and anticipated
                 * responses.
                 */
                for (int i = 0; i < poApduRequestList.size(); i++) {
                    /*
                     * Add requests and responses to the DigestProcessor
                     */
                    DigestProcessor.pushPoExchangeData(poApduRequestList.get(i),
                            poAnticipatedResponses.get(i));
                }
            } else {
                throw new InvalidMessageException("Inconsistent requests and anticipated responses",
                        InvalidMessageException.Type.PO, poApduRequestList, poAnticipatedResponses);
            }
        }

        /* All CSM digest operations will now run at once. */
        /* Get the CSM Digest request from the cache manager */
        SeRequest csmSeRequest = DigestProcessor.getCsmDigestRequest();

        logger.debug("PoSecureSession => processClosing: CSMREQUEST = {}", csmSeRequest);

        /* create a SeRequestSet */
        SeRequestSet csmRequestSet = new SeRequestSet(csmSeRequest);

        SeResponse csmSeResponse = csmReader.transmit(csmRequestSet).getSingleResponse();

        logger.debug("PoSecureSession => processClosing: CSMRESPONSE = {}", csmSeResponse);

        List<ApduResponse> csmApduResponseList = csmSeResponse.getApduResponses();

        for (int i = 0; i < csmApduResponseList.size(); i++) {
            if (!csmApduResponseList.get(i).isSuccessful()) {

                logger.debug(
                        "PoSecureSession => processClosing: command failure REQUEST = {}, RESPONSE = {}",
                        csmSeRequest.getApduRequests().get(i), csmApduResponseList.get(i));
                throw new IllegalStateException(
                        "ProcessClosing command failure during digest computation process.");
            }
        }

        /* Get Terminal Signature from the latest response */
        ByteBuffer sessionTerminalSignature = null;
        // TODO Add length check according to Calypso REV (4 / 8)
        if (!csmApduResponseList.isEmpty()) {
            DigestCloseRespPars respPars = new DigestCloseRespPars(
                    csmApduResponseList.get(csmApduResponseList.size() - 1));

            sessionTerminalSignature = respPars.getSignature();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("PoSecureSession => processClosing: SIGNATURE = {}",
                    ByteBufferUtils.toHex(sessionTerminalSignature));
        }


        /* the ratification will be asked only if no ratification command is provided */
        boolean ratificationAsked = (ratificationCommand == null);

        /* Build the PO Close Session command. The last one for this session */
        CloseSessionCmdBuild closeCommand =
                new CloseSessionCmdBuild(poRevision, ratificationAsked, sessionTerminalSignature);

        poApduRequestList.add(closeCommand.getApduRequest());

        /* Add the PO Ratification command is present */
        if (ratificationCommand != null) {
            poApduRequestList.add(ratificationCommand.getApduRequest());
        }

        /*
         * Create a SeRequestSet (list of SeRequests), transfer PO commands
         */
        SeRequest poSeRequest = new SeRequest(new SeRequest.AidSelector(poCalypsoInstanceAid),
                poApduRequestList, !closeSeChannel);
        logger.debug("PoSecureSession => processClosing: POSEREQUEST = {}", poSeRequest);

        SeRequestSet poRequestSet = new SeRequestSet(poSeRequest);

        SeResponse poSeResponse = poReader.transmit(poRequestSet).getSingleResponse();

        logger.debug("PoSecureSession => processClosing: POSERESPONSE = {}", poSeResponse);

        List<ApduResponse> poApduResponseList = poSeResponse.getApduResponses();

        // TODO add support of poRevision parameter to CloseSessionRespPars for REV2.4 PO CLAss byte
        // before last if ratification, otherwise last one
        CloseSessionRespPars poCloseSessionPars = new CloseSessionRespPars(
                poApduResponseList.get(poApduResponseList.size() - ((ratificationAsked) ? 1 : 2)));
        if (!poCloseSessionPars.isSuccessful()) {
            throw new InvalidMessageException("Didn't get a signature",
                    InvalidMessageException.Type.PO, poApduRequestList, poApduResponseList);
        }

        /* Check the PO signature part with the CSM */
        /* Build and send CSM Digest Authenticate command */

        AbstractApduCommandBuilder digestAuth =
                new DigestAuthenticateCmdBuild(csmRevision, poCloseSessionPars.getSignatureLo());

        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();
        csmApduRequestList.add(digestAuth.getApduRequest());

        csmSeRequest = new SeRequest(null, csmApduRequestList, true);

        logger.debug("PoSecureSession.DigestProcessor => checkPoSignature: CSMREQUEST = {}",
                csmSeRequest);

        csmRequestSet = new SeRequestSet(csmSeRequest);

        csmSeResponse = csmReader.transmit(csmRequestSet).getSingleResponse();

        logger.debug("PoSecureSession.DigestProcessor => checkPoSignature: CSMRESPONSE = {}",
                csmSeResponse);

        /* Get transaction result parsing the response */
        csmApduResponseList = csmSeResponse.getApduResponses();

        transactionResult = false;
        if ((csmApduResponseList != null) && !csmApduResponseList.isEmpty()) {
            DigestAuthenticateRespPars respPars =
                    new DigestAuthenticateRespPars(csmApduResponseList.get(0));
            transactionResult = respPars.isSuccessful();
            if (transactionResult) {
                logger.debug(
                        "PoSecureSession.DigestProcessor => checkPoSignature: mutual authentication successful.");
            } else {
                logger.debug(
                        "PoSecureSession.DigestProcessor => checkPoSignature: mutual authentication failure.");
            }
        } else {
            logger.debug(
                    "DigestProcessor => checkPoSignature: no response to Digest Authenticate.");
            throw new IllegalStateException("No response to Digest Authenticate.");
        }

        currentState = SessionState.SESSION_CLOSED;
        return poSeResponse;
    }

    /**
     * Determine the PO revision from the application type byte:
     *
     * <ul>
     * <li>if
     * <code>%1-------</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;CLAP&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.1</li>
     * <li>if <code>%00101---</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.2</li>
     * <li>if <code>%00100---</code>&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV3.1</li>
     * <li>otherwise&nbsp;&nbsp;&rarr;&nbsp;&nbsp;REV2.4</li>
     * </ul>
     *
     * @param applicationTypeByte the application type byte from FCI
     * @return the PO revision
     */
    public static PoRevision computePoRevision(byte applicationTypeByte) {
        PoRevision rev;
        if ((applicationTypeByte & (1 << 7)) != 0) {
            /* CLAP */
            rev = PoRevision.REV3_1;
        } else if ((applicationTypeByte >> 3) == (byte) (0x05)) {
            rev = PoRevision.REV3_2;
        } else if ((applicationTypeByte >> 3) == (byte) (0x04)) {
            rev = PoRevision.REV3_1;
        } else {
            rev = PoRevision.REV2_4;
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
     * Get the Secure Session Status.
     * <ul>
     * <li>To check the result of a closed secure session, returns true if the CSM Digest
     * Authenticate is successful.</li>
     * </ul>
     *
     * @return the {@link PoSecureSession}.transactionResult
     */
    public boolean isSuccessful() {

        if (currentState != SessionState.SESSION_CLOSED) {
            throw new IllegalStateException(
                    "Session is not closed, state:" + currentState.toString() + ", expected: "
                            + SessionState.SESSION_OPEN.toString());
        }

        return transactionResult;
    }

    /**
     * List of CSM settings keys that can be provided when the secure session is created.
     */
    public enum CsmSettings {
        /** KIF for personalization used when not provided by the PO */
        CS_DEFAULT_KIF_PERSO,
        /** KIF for load used when not provided by the PO */
        CS_DEFAULT_KIF_LOAD,
        /** KIF for debit used when not provided by the PO */
        CS_DEFAULT_KIF_DEBIT,
        /** Key record number to use when KIF/KVC is unavailable */
        CS_DEFAULT_KEY_RECORD_NUMBER
    }

    /**
     * The PO Transaction Access Level: personalization, loading or debiting.
     */
    public enum SessionAccessLevel {
        /** Session Access Level used for personalization purposes. */
        SESSION_LVL_PERSO,
        /** Session Access Level used for reloading purposes. */
        SESSION_LVL_LOAD,
        /** Session Access Level used for validating and debiting purposes. */
        SESSION_LVL_DEBIT
    }

    /**
     * The PO Transaction State defined with the elements: ‘IOError’, ‘SEInserted’ and ‘SERemoval’.
     */
    public enum SessionState {
        /** Initial state of a PO transaction. The PO must have been previously selected. */
        SESSION_CLOSED,
        /** The secure session is active. */
        SESSION_OPEN
    }

    /**
     * This class embeds all the resources to manage the secure session digest computation.
     *
     * - initialize: Digest Init command
     *
     * - pushPoExchangeData and appendResponse: check consistency and all needed Digest Update
     * commands
     *
     * - getTerminalSignature: Digest Close, returns the terminal part of the signature
     *
     * - checkPoSignature: Digest Authenticate, verify the PO part of the signature
     */
    private static class DigestProcessor {
        /*
         * The digest data cache stores all PO data to be send to CSM during a Secure Session. The
         * 1st buffer is the data buffer to be provided with Digest Init. The following buffers are
         * PO command/response pairs
         */
        private static final List<ByteBuffer> poDigestDataCache = new ArrayList<ByteBuffer>();
        private static CsmRevision csmRevision;
        private static PoRevision poRevision;
        private static boolean encryption;
        private static boolean verification;
        private static boolean revMode;
        private static byte keyRecordNumber;
        private static byte keyKIF;
        private static byte keyKVC;

        /**
         * Initializes the digest computation process
         *
         * @param poRev the PO revision
         * @param csmRev the CSM revision
         * @param sessionEncryption true if the session is encrypted
         * @param verificationMode true if the verification mode is active
         * @param rev3_2Mode true if the REV3.2 mode is active
         * @param workKeyRecordNumber the key record number
         * @param workKeyKif the PO KIF
         * @param workKeyKVC the PO KVC
         * @param digestData a first bunch of data to digest.
         */
        static void initialize(PoRevision poRev, CsmRevision csmRev, boolean sessionEncryption,
                boolean verificationMode, boolean rev3_2Mode, byte workKeyRecordNumber,
                byte workKeyKif, byte workKeyKVC, ByteBuffer digestData) {
            /* Store work context */
            poRevision = poRev;
            csmRevision = csmRev;
            encryption = sessionEncryption;
            verification = verificationMode;
            revMode = rev3_2Mode;
            keyRecordNumber = workKeyRecordNumber;
            keyKIF = workKeyKif;
            keyKVC = workKeyKVC;
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "PoSecureSession.DigestProcessor => initialize: POREVISION = {}, CSMREVISION = {}, SESSIONENCRYPTION = {}",
                        poRev, csmRev, sessionEncryption, verificationMode);
                logger.debug(
                        "PoSecureSession.DigestProcessor => initialize: VERIFICATIONMODE = {}, REV32MODE = {} KEYRECNUMBER = {}",
                        verificationMode, rev3_2Mode, workKeyRecordNumber);
                logger.debug(
                        "PoSecureSession.DigestProcessor => initialize: KIF = {}, KVC {}, DIGESTDATA = {}",
                        String.format("%02X", workKeyKif), String.format("%02X", workKeyKVC),
                        ByteBufferUtils.toHex(digestData));
            }

            /* Clear data cache */
            poDigestDataCache.clear();

            /*
             * Build Digest Init command as first ApduRequest of the digest computation process
             */
            poDigestDataCache.add(digestData);
        }

        /**
         * Appends a full PO exchange (request and response) to the digest data cache.
         *
         * @param request PO request
         * @param response PO response
         */
        static void pushPoExchangeData(ApduRequest request, ApduResponse response) {

            logger.debug("PoSecureSession.DigestProcessor => pushPoExchangeData: REQUEST = {}",
                    request);

            /*
             * Add an ApduRequest to the digest computation: if the request is of case4 type, Le
             * must be excluded from the digest computation. In this cas, we remove here the last
             * byte of the command buffer.
             */
            if (request.isCase4()) {
                poDigestDataCache.add(ByteBufferUtils.subLen(request.getBytes(), 0,
                        request.getBytes().limit() - 1));
            } else {
                poDigestDataCache.add(request.getBytes());
            }

            logger.debug("PoSecureSession.DigestProcessor => pushPoExchangeData: RESPONSE = {}",
                    response);

            /* Add an ApduResponse to the digest computation */
            poDigestDataCache.add(response.getBytes());
        }

        /**
         * Get a unique CSM request for the whole digest computation process.
         * 
         * @return SeRequest all the ApduRequest to send to the CSM in order to get the terminal
         *         signature
         */
        // TODO optimization with the use of Digest Update Multiple whenever possible.
        static SeRequest getCsmDigestRequest() {
            List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();

            if (poDigestDataCache.size() == 0) {
                logger.debug(
                        "PoSecureSession.DigestProcessor => getCsmDigestRequest: no data in cache.");
                throw new IllegalStateException("Digest data cache is empty.");
            }
            if (poDigestDataCache.size() % 2 == 0) {
                /* the number of buffers should be 2*n + 1 */
                logger.debug(
                        "PoSecureSession.DigestProcessor => getCsmDigestRequest: wrong number of buffer in cache NBR = {}.",
                        poDigestDataCache.size());
                throw new IllegalStateException("Digest data cache is inconsistent.");
            }

            /*
             * Build and append Digest Init command as first ApduRequest of the digest computation
             * process
             */
            csmApduRequestList.add(new DigestInitCmdBuild(csmRevision, verification, revMode,
                    keyRecordNumber, keyKIF, keyKVC, poDigestDataCache.get(0)).getApduRequest());

            /*
             * Build and append Digest Update commands
             *
             * The first command is at index 1.
             */
            for (int i = 1; i < poDigestDataCache.size(); i++) {
                csmApduRequestList.add(
                        new DigestUpdateCmdBuild(csmRevision, encryption, poDigestDataCache.get(i))
                                .getApduRequest());
            }

            /*
             * Build and append Digest Close command
             */
            csmApduRequestList.add((new DigestCloseCmdBuild(csmRevision,
                    poRevision.equals(PoRevision.REV3_2) ? SIGNATURE_LENGTH_REV32
                            : SIGNATURE_LENGTH_REV_INF_32).getApduRequest()));


            return new SeRequest(null, csmApduRequestList, true);
        }
    }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.transaction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import org.eclipse.keyple.calypso.command.SendableInSession;
import org.eclipse.keyple.calypso.command.csm.CsmRevision;
import org.eclipse.keyple.calypso.command.csm.CsmSendableInSession;
import org.eclipse.keyple.calypso.command.csm.builder.*;
import org.eclipse.keyple.calypso.command.csm.parser.CsmGetChallengeRespPars;
import org.eclipse.keyple.calypso.command.csm.parser.DigestAuthenticateRespPars;
import org.eclipse.keyple.calypso.command.csm.parser.DigestCloseRespPars;
import org.eclipse.keyple.calypso.command.po.*;
import org.eclipse.keyple.calypso.command.po.builder.*;
import org.eclipse.keyple.calypso.command.po.builder.session.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.session.CloseSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.session.AbstractOpenSessionRespPars;
import org.eclipse.keyple.calypso.command.po.parser.session.CloseSessionRespPars;
import org.eclipse.keyple.calypso.transaction.exception.*;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.util.ByteArrayUtils;
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
public class PoTransaction {

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

    private final static int OFFSET_CLA = 0;
    private final static int OFFSET_INS = 1;
    private final static int OFFSET_P1 = 2;
    private final static int OFFSET_P2 = 3;
    private final static int OFFSET_Lc = 4;
    private final static int OFFSET_DATA = 5;

    /** Ratification command APDU for rev <= 2.4 */
    private final static byte[] ratificationCmdApduLegacy = ByteArrayUtils.fromHex("94B2000000");
    /** Ratification command APDU for rev > 2.4 */
    private final static byte[] ratificationCmdApdu = ByteArrayUtils.fromHex("00B2000000");

    private static final Logger logger = LoggerFactory.getLogger(PoTransaction.class);

    /** The reader for PO. */
    private final ProxyReader poReader;
    /** The reader for session CSM. */
    private ProxyReader csmReader;
    /** The CSM default revision. */
    private final CsmRevision csmRevision = CsmRevision.C1;
    /** The CSM settings map. */
    private final EnumMap<CsmSettings, Byte> csmSetting =
            new EnumMap<CsmSettings, Byte>(CsmSettings.class);
    /** The PO serial number extracted from FCI */
    private final byte[] poCalypsoInstanceSerial;
    /** The current CalypsoPO */
    protected final CalypsoPO calypsoPo;
    /** The PO selector from the selection result */
    private SeRequest.Selector selector;
    /** the type of the notified event. */
    private SessionState currentState;
    /** Selected AID of the Calypso PO. */
    private byte[] poCalypsoInstanceAid;
    /** The PO Calypso Revision. */
    private PoRevision poRevision = PoRevision.REV3_1;
    /** The PO Secure Session final status according to mutual authentication result */
    private boolean transactionResult;
    /** The diversification status */
    private boolean isDiversificationDone;
    /** The PO KIF */
    private byte poKif;
    /** The previous PO Secure Session ratification status */
    private boolean wasRatified;
    /** The data read at opening */
    private byte[] openRecordDataRead;
    /** The list to contain the prepared commands */
    private List<PoSendableInSession> poCommandBuilderList = new ArrayList<PoSendableInSession>();
    /** The SAM settings status */
    private boolean csmSettingsDefined;
    /** List of authorized KVCs */
    private List<Byte> authorizedKvcList;

    /**
     * PoTransaction with PO and SAM readers.
     * <ul>
     * <li>Logical channels with PO &amp; CSM could already be established or not.</li>
     * <li>A list of CSM parameters is provided as en EnumMap.</li>
     * </ul>
     *
     * @param poReader the PO reader
     * @param calypsoPO the CalypsoPO object obtained at the end of the selection step
     * @param csmReader the SAM reader
     * @param csmSetting a list of CSM related parameters. In the case this parameter is null,
     *        default parameters are applied. The available setting keys are defined in
     *        {@link CsmSettings}
     */
    public PoTransaction(ProxyReader poReader, CalypsoPO calypsoPO, ProxyReader csmReader,
            EnumMap<CsmSettings, Byte> csmSetting) {

        this(poReader, calypsoPO);

        setCsmSettings(csmReader, csmSetting);
    }

    /**
     * PoTransaction with PO reader and without SAM reader.
     * <ul>
     * <li>Logical channels with PO could already be established or not.</li>
     * </ul>
     *
     * @param poReader the PO reader
     * @param calypsoPO the CalypsoPO object obtained at the end of the selection step
     */
    public PoTransaction(ProxyReader poReader, CalypsoPO calypsoPO) {
        this.poReader = poReader;

        this.calypsoPo = calypsoPO;

        poRevision = calypsoPO.getRevision();

        poCalypsoInstanceAid = calypsoPO.getDfName();

        /* Configure an Aid or Atr selector depending on whether the DF name is available or not. */
        if (poCalypsoInstanceAid != null) {
            selector = new SeRequest.AidSelector(poCalypsoInstanceAid);
        } else {
            selector = new SeRequest.AtrSelector(ByteArrayUtils.toHex(calypsoPO.getAtr()));
        }

        /* Serial Number of the selected Calypso instance. */
        poCalypsoInstanceSerial = calypsoPO.getApplicationSerialNumber();

        currentState = SessionState.SESSION_CLOSED;
    }

    /**
     * Sets the SAM parameters for Secure Session management
     * 
     * @param csmReader
     * @param csmSetting
     */
    public void setCsmSettings(ProxyReader csmReader, EnumMap<CsmSettings, Byte> csmSetting) {
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

        logger.debug("Contructor => CSMSETTING = {}", this.csmSetting);

        csmSettingsDefined = true;
    }

    /**
     * Provides a list of authorized KVC
     *
     * If this method is not called, the list will remain empty and all KVCs will be accepted.
     *
     * If a list is provided and a PO with a KVC not belonging to this list is presented, a
     * {@link KeypleCalypsoSecureSessionUnauthorizedKvcException} will be raised.
     * 
     * @param authorizedKvcList the list of authorized KVCs
     */
    public void setAuthorizedKvcList(List<Byte> authorizedKvcList) {
        this.authorizedKvcList = authorizedKvcList;
    }

    /**
     * Indicates whether or not the SAM settings have been defined
     * 
     * @return true if the SAM settings have been defined.
     */
    public boolean isCsmSettingsDefined() {
        return csmSettingsDefined;
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
     * <li>Returns the corresponding PO SeResponse (responses to poCommandsInsideSession).</li>
     * </ul>
     *
     * @param accessLevel access level of the session (personalization, load or debit).
     * @param openingSfiToSelect SFI of the file to select (0 means no file to select)
     * @param openingRecordNumberToRead number of the record to read
     * @param poCommandsInsideSession the po commands inside session
     * @return SeResponse response to all executed commands including the self generated "Open
     *         Secure Session" command
     * @throws KeypleReaderException the IO reader exception
     */
    private SeResponse processAtomicOpening(SessionAccessLevel accessLevel, byte openingSfiToSelect,
            byte openingRecordNumberToRead, List<PoSendableInSession> poCommandsInsideSession)
            throws KeypleReaderException {

        /*
         * counts 'select diversifier' and 'get challenge' commands. At least get challenge is
         * present
         */
        int numberOfCsmCmd = 1;

        /* CSM ApduRequest List to hold Select Diversifier and Get Challenge commands */
        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();

        if (logger.isDebugEnabled()) {
            logger.debug("processAtomicOpening => Identification: DFNAME = {}, SERIALNUMBER = {}",
                    ByteArrayUtils.toHex(poCalypsoInstanceAid),
                    ByteArrayUtils.toHex(poCalypsoInstanceSerial));
        }
        /* diversify only if this has not already been done. */
        if (!isDiversificationDone) {
            /* Build the CSM Select Diversifier command to provide the CSM with the PO S/N */
            AbstractApduCommandBuilder selectDiversifier =
                    new SelectDiversifierCmdBuild(this.csmRevision, poCalypsoInstanceSerial);

            csmApduRequestList.add(selectDiversifier.getApduRequest());

            /* increment command number */
            numberOfCsmCmd++;

            /* change the diversification status */
            isDiversificationDone = true;
        }
        /* Build the CSM Get Challenge command */
        byte challengeLength = poRevision.equals(PoRevision.REV3_2) ? CHALLENGE_LENGTH_REV32
                : CHALLENGE_LENGTH_REV_INF_32;

        AbstractApduCommandBuilder csmGetChallenge =
                new CsmGetChallengeCmdBuild(this.csmRevision, challengeLength);

        csmApduRequestList.add(csmGetChallenge.getApduRequest());

        /* Build a CSM SeRequest */
        SeRequest csmSeRequest = new SeRequest(null, csmApduRequestList, true);

        logger.debug("processAtomicOpening => identification: CSMSEREQUEST = {}", csmSeRequest);

        /*
         * Transmit the SeRequest to the CSM and get back the SeResponse (list of ApduResponse)
         */
        SeResponse csmSeResponse = csmReader.transmit(csmSeRequest);

        if (csmSeResponse == null) {
            throw new KeypleCalypsoSecureSessionException("Null response received",
                    KeypleCalypsoSecureSessionException.Type.CSM, csmSeRequest.getApduRequests(),
                    null);
        }

        logger.debug("processAtomicOpening => identification: CSMSERESPONSE = {}", csmSeResponse);

        List<ApduResponse> csmApduResponseList = csmSeResponse.getApduResponses();
        byte[] sessionTerminalChallenge;

        if (csmApduResponseList.size() == numberOfCsmCmd
                && csmApduResponseList.get(numberOfCsmCmd - 1).isSuccessful() && csmApduResponseList
                        .get(numberOfCsmCmd - 1).getDataOut().length == challengeLength) {
            CsmGetChallengeRespPars csmChallengePars =
                    new CsmGetChallengeRespPars(csmApduResponseList.get(numberOfCsmCmd - 1));
            sessionTerminalChallenge = csmChallengePars.getChallenge();
            if (logger.isDebugEnabled()) {
                logger.debug("processAtomicOpening => identification: TERMINALCHALLENGE = {}",
                        ByteArrayUtils.toHex(sessionTerminalChallenge));
            }
        } else {
            throw new KeypleCalypsoSecureSessionException("Invalid message received",
                    KeypleCalypsoSecureSessionException.Type.CSM, csmApduRequestList,
                    csmApduResponseList);
        }

        /* PO ApduRequest List to hold Open Secure Session and other optional commands */
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();

        /* Build the PO Open Secure Session command */
        // TODO decide how to define the extraInfo field. Empty for the moment.
        AbstractOpenSessionCmdBuild poOpenSession = AbstractOpenSessionCmdBuild.create(
                getRevision(), (byte) (accessLevel.ordinal() + 1), sessionTerminalChallenge,
                openingSfiToSelect, openingRecordNumberToRead, "");

        /* Add the resulting ApduRequest to the PO ApduRequest list */
        poApduRequestList.add(poOpenSession.getApduRequest());

        /* Add all optional PoSendableInSession commands to the PO ApduRequest list */
        if (poCommandsInsideSession != null) {
            poApduRequestList.addAll(this.getApduRequestsToSendInSession(
                    (List<SendableInSession>) (List<?>) poCommandsInsideSession));
        }

        /* Create a SeRequest from the ApduRequest list, PO AID as Selector, keepChannelOpen true */
        SeRequest poSeRequest = new SeRequest(selector, poApduRequestList, true);

        logger.debug("processAtomicOpening => opening:  POSEREQUEST = {}", poSeRequest);

        /* Transmit the commands to the PO */
        SeResponse poSeResponse = poReader.transmit(poSeRequest);

        logger.debug("processAtomicOpening => opening:  POSERESPONSE = {}", poSeResponse);

        if (poSeResponse == null) {
            throw new KeypleCalypsoSecureSessionException("Null response received",
                    KeypleCalypsoSecureSessionException.Type.PO, poSeRequest.getApduRequests(),
                    null);
        }

        /* Retrieve and check the ApduResponses */
        List<ApduResponse> poApduResponseList = poSeResponse.getApduResponses();

        /* Do some basic checks */
        if (poApduRequestList.size() != poApduResponseList.size()) {
            throw new KeypleCalypsoSecureSessionException("Inconsistent requests and responses",
                    KeypleCalypsoSecureSessionException.Type.PO, poApduRequestList,
                    poApduResponseList);
        }

        for (ApduResponse apduR : poApduResponseList) {
            if (!apduR.isSuccessful()) {
                throw new KeypleCalypsoSecureSessionException("Invalid response",
                        KeypleCalypsoSecureSessionException.Type.PO, poApduRequestList,
                        poApduResponseList);
            }
        }

        /* Track Read Records for later use to build anticipated responses. */
        AnticipatedResponseBuilder.storeCommandResponse(poCommandsInsideSession, poApduRequestList,
                poApduResponseList, true);

        /* Parse the response to Open Secure Session (the first item of poApduResponseList) */
        AbstractOpenSessionRespPars poOpenSessionPars =
                AbstractOpenSessionRespPars.create(poApduResponseList.get(0), poRevision);
        byte[] sessionCardChallenge = poOpenSessionPars.getPoChallenge();

        /* Build the Digest Init command from PO Open Session */
        poKif = poOpenSessionPars.getSelectedKif();
        /** The PO KVC */
        // TODO handle rev 1 KVC (provided in the response to select DF. CalypsoPO?)
        byte poKvc = poOpenSessionPars.getSelectedKvc();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "processAtomicOpening => opening: CARDCHALLENGE = {}, POKIF = {}, POKVC = {}",
                    ByteArrayUtils.toHex(sessionCardChallenge), String.format("%02X", poKif),
                    String.format("%02X", poKvc));
        }

        if (authorizedKvcList != null && !authorizedKvcList.contains(poKvc)) {
            throw new KeypleCalypsoSecureSessionUnauthorizedKvcException(
                    String.format("PO KVC = %02X", poKvc));
        }

        byte kif;
        if (poKif == KIF_UNDEFINED) {
            switch (accessLevel) {
                case SESSION_LVL_PERSO:
                    kif = csmSetting.get(CsmSettings.CS_DEFAULT_KIF_PERSO);
                    break;
                case SESSION_LVL_LOAD:
                    kif = csmSetting.get(CsmSettings.CS_DEFAULT_KIF_LOAD);
                    break;
                case SESSION_LVL_DEBIT:
                default:
                    kif = csmSetting.get(CsmSettings.CS_DEFAULT_KIF_DEBIT);
                    break;
            }
        } else {
            kif = poKif;
        }

        /* Keep the ratification status and read data */
        wasRatified = poOpenSessionPars.wasRatified();
        openRecordDataRead = poOpenSessionPars.getRecordDataRead();

        /*
         * Initialize the DigestProcessor. It will store all digest operations (Digest Init, Digest
         * Update) until the session closing. AT this moment, all CSM Apdu will be processed at
         * once.
         */
        DigestProcessor.initialize(poRevision, csmRevision, false, false,
                poRevision.equals(PoRevision.REV3_2),
                csmSetting.get(CsmSettings.CS_DEFAULT_KEY_RECORD_NUMBER), kif, poKvc,
                poApduResponseList.get(0).getDataOut());

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

        /* Remove Open Secure Session response and create a new SeResponse */
        poApduResponseList.remove(0);

        return new SeResponse(true, poSeResponse.getAtr(), poSeResponse.getFci(),
                poApduResponseList);
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
     * @throws KeypleReaderException IO Reader exception
     */
    private SeResponse processAtomicPoCommands(List<PoSendableInSession> poCommands)
            throws KeypleReaderException {

        // Get PO ApduRequest List from PoSendableInSession List
        List<ApduRequest> poApduRequestList =
                this.getApduRequestsToSendInSession((List<SendableInSession>) (List<?>) poCommands);

        /* Create a SeRequest from the ApduRequest list, PO AID as Selector, keepChannelOpen true */
        SeRequest poSeRequest = new SeRequest(selector, poApduRequestList, true);

        logger.debug("processAtomicPoCommands => POREQUEST = {}", poSeRequest);

        /* Transmit the commands to the PO */
        SeResponse poSeResponse = poReader.transmit(poSeRequest);

        logger.debug("processAtomicPoCommands => PORESPONSE = {}", poSeResponse);

        if (poSeResponse == null) {
            throw new KeypleCalypsoSecureSessionException("Null response received",
                    KeypleCalypsoSecureSessionException.Type.PO, poSeRequest.getApduRequests(),
                    null);
        }

        /* Retrieve and check the ApduResponses */
        List<ApduResponse> poApduResponseList = poSeResponse.getApduResponses();

        /* Do some basic checks */
        if (poApduRequestList.size() != poApduResponseList.size()) {
            throw new KeypleCalypsoSecureSessionException("Inconsistent requests and responses",
                    KeypleCalypsoSecureSessionException.Type.PO, poApduRequestList,
                    poApduResponseList);
        }

        for (ApduResponse apduR : poApduResponseList) {
            if (!apduR.isSuccessful()) {
                throw new KeypleCalypsoSecureSessionException("Invalid response",
                        KeypleCalypsoSecureSessionException.Type.PO, poApduRequestList,
                        poApduResponseList);
            }
        }

        /* Track Read Records for later use to build anticipated responses. */
        AnticipatedResponseBuilder.storeCommandResponse(poCommands, poApduRequestList,
                poApduResponseList, false);

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
     * @throws KeypleReaderException if a reader error occurs
     */
    public SeResponse processCsmCommands(List<CsmSendableInSession> csmCommands)
            throws KeypleReaderException {

        /* Init CSM ApduRequest List - for the first CSM exchange */
        List<ApduRequest> csmApduRequestList = this
                .getApduRequestsToSendInSession((List<SendableInSession>) (List<?>) csmCommands);

        /* SeRequest from the command list */
        SeRequest csmSeRequest = new SeRequest(null, csmApduRequestList, true);

        logger.debug("processCsmCommands => CSMSEREQUEST = {}", csmSeRequest);

        /* Transmit SeRequest and get SeResponse */
        SeResponse csmSeResponse = csmReader.transmit(csmSeRequest);

        logger.debug("processCsmCommands => CSMSERESPONSE = {}", csmSeResponse);

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
     * <li>The management of ratification is conditioned by the mode of communication.
     * <ul>
     * <li>If the communication mode is CONTACTLESS, a specific ratification command is sent after
     * the Close Session command. No ratification is requested in the Close Session command.</li>
     * <li>If the communication mode is CONTACTS, no ratification command is sent after the Close
     * Session command. Ratification is requested in the Close Session command.</li>
     * </ul>
     * </li>
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
     * The method is marked as deprecated because the advanced variant defined below must be used at
     * the application level.
     * 
     * @param poModificationCommands a list of commands that can modify the PO memory content
     * @param poAnticipatedResponses a list of anticipated PO responses to the modification commands
     * @param communicationMode the communication mode. If the communication mode is
     *        CONTACTLESS_MODE, a ratification command will be generated and sent to the PO after
     *        the Close Session command; the ratification will not be requested in the Close Session
     *        command. On the contrary, if the communication mode is CONTACTS_MODE, no ratification
     *        command will be sent to the PO and ratification will be requested in the Close Session
     *        command
     * @param closeSeChannel if true the SE channel of the PO reader must be closed after the last
     *        command
     * @return SeResponse close session response
     * @throws KeypleReaderException the IO reader exception This method is deprecated.
     *         <ul>
     *         <li>The argument of the ratification command is replaced by an indication of the PO
     *         communication mode.</li>
     *         </ul>
     */
    private SeResponse processAtomicClosing(List<PoModificationCommand> poModificationCommands,
            List<ApduResponse> poAnticipatedResponses, CommunicationMode communicationMode,
            boolean closeSeChannel) throws KeypleReaderException {

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
                throw new KeypleCalypsoSecureSessionException(
                        "Inconsistent requests and anticipated responses",
                        KeypleCalypsoSecureSessionException.Type.PO, poApduRequestList,
                        poAnticipatedResponses);
            }
        }

        /* All CSM digest operations will now run at once. */
        /* Get the CSM Digest request from the cache manager */
        SeRequest csmSeRequest = DigestProcessor.getCsmDigestRequest();

        logger.debug("processAtomicClosing => CSMREQUEST = {}", csmSeRequest);

        /* Transmit SeRequest and get SeResponse */
        SeResponse csmSeResponse = csmReader.transmit(csmSeRequest);

        logger.debug("processAtomicClosing => CSMRESPONSE = {}", csmSeResponse);

        List<ApduResponse> csmApduResponseList = csmSeResponse.getApduResponses();

        for (int i = 0; i < csmApduResponseList.size(); i++) {
            if (!csmApduResponseList.get(i).isSuccessful()) {

                logger.debug("processAtomicClosing => command failure REQUEST = {}, RESPONSE = {}",
                        csmSeRequest.getApduRequests().get(i), csmApduResponseList.get(i));
                throw new IllegalStateException(
                        "ProcessClosing command failure during digest computation process.");
            }
        }

        /* Get Terminal Signature from the latest response */
        byte[] sessionTerminalSignature = null;
        // TODO Add length check according to Calypso REV (4 / 8)
        if (!csmApduResponseList.isEmpty()) {
            DigestCloseRespPars respPars = new DigestCloseRespPars(
                    csmApduResponseList.get(csmApduResponseList.size() - 1));

            sessionTerminalSignature = respPars.getSignature();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("processAtomicClosing => SIGNATURE = {}",
                    ByteArrayUtils.toHex(sessionTerminalSignature));
        }

        PoCustomCommandBuilder ratificationCommand;
        boolean ratificationAsked;

        if (communicationMode == CommunicationMode.CONTACTLESS_MODE) {
            if (poRevision == PoRevision.REV2_4) {
                ratificationCommand = new PoCustomCommandBuilder("Ratification command",
                        new ApduRequest(ratificationCmdApduLegacy, false));
            } else {
                ratificationCommand = new PoCustomCommandBuilder("Ratification command",
                        new ApduRequest(ratificationCmdApdu, false));
            }
            /*
             * Ratification is done by the ratification command above so is not requested in the
             * Close Session command
             */
            ratificationAsked = false;
        } else {
            /* Ratification is requested in the Close Session command in contacts mode */
            ratificationAsked = true;
            ratificationCommand = null;
        }

        /* Build the PO Close Session command. The last one for this session */
        CloseSessionCmdBuild closeCommand =
                new CloseSessionCmdBuild(poRevision, ratificationAsked, sessionTerminalSignature);

        poApduRequestList.add(closeCommand.getApduRequest());

        /* Keep the position of the Close Session command in request list */
        int closeCommandIndex = poApduRequestList.size() - 1;

        /*
         * Add the PO Ratification command if any
         */
        if (ratificationCommand != null) {
            poApduRequestList.add(ratificationCommand.getApduRequest());
        }

        /*
         * Transfer PO commands
         */
        SeRequest poSeRequest = new SeRequest(selector, poApduRequestList, !closeSeChannel);

        logger.debug("processAtomicClosing => POSEREQUEST = {}", poSeRequest);

        SeResponse poSeResponse;
        try {
            poSeResponse = poReader.transmit(poSeRequest);
        } catch (KeypleReaderException ex) {
            poSeResponse = ex.getSeResponse();
            /*
             * The current exception may have been caused by a communication issue with the PO
             * during the ratification command.
             *
             * In this case, we do not stop the process and consider the Secure Session close. We'll
             * check the signature.
             *
             * We should have one response less than requests.
             */
            if (ratificationAsked || poSeResponse == null
                    || poSeResponse.getApduResponses().size() != poApduRequestList.size() - 1) {
                /* Add current PO SeResponse to exception */
                ex.setSeResponse(poSeResponse);
                throw new KeypleReaderException("PO Reader Exception while closing Secure Session",
                        ex);
            }
        }

        logger.debug("processAtomicClosing => POSERESPONSE = {}", poSeResponse);

        List<ApduResponse> poApduResponseList = poSeResponse.getApduResponses();

        // TODO add support of poRevision parameter to CloseSessionRespPars for REV2.4 PO CLAss byte
        // before last if ratification, otherwise last one
        CloseSessionRespPars poCloseSessionPars =
                new CloseSessionRespPars(poApduResponseList.get(closeCommandIndex));
        if (!poCloseSessionPars.isSuccessful()) {
            throw new KeypleCalypsoSecureSessionException("Didn't get a signature",
                    KeypleCalypsoSecureSessionException.Type.PO, poApduRequestList,
                    poApduResponseList);
        }

        /* Check the PO signature part with the CSM */
        /* Build and send CSM Digest Authenticate command */
        AbstractApduCommandBuilder digestAuth =
                new DigestAuthenticateCmdBuild(csmRevision, poCloseSessionPars.getSignatureLo());

        List<ApduRequest> csmApduRequestList = new ArrayList<ApduRequest>();
        csmApduRequestList.add(digestAuth.getApduRequest());

        csmSeRequest = new SeRequest(null, csmApduRequestList, true);

        logger.debug("PoTransaction.DigestProcessor => checkPoSignature: CSMREQUEST = {}",
                csmSeRequest);

        csmSeResponse = csmReader.transmit(csmSeRequest);

        logger.debug("PoTransaction.DigestProcessor => checkPoSignature: CSMRESPONSE = {}",
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
                        "PoTransaction.DigestProcessor => checkPoSignature: mutual authentication successful.");
            } else {
                logger.debug(
                        "PoTransaction.DigestProcessor => checkPoSignature: mutual authentication failure.");
            }
        } else {
            logger.debug(
                    "DigestProcessor => checkPoSignature: no response to Digest Authenticate.");
            throw new IllegalStateException("No response to Digest Authenticate.");
        }

        currentState = SessionState.SESSION_CLOSED;

        /* Remove ratification response if any */
        if (!ratificationAsked) {
            poApduResponseList.remove(poApduResponseList.size() - 1);
        }
        /* Remove Close Secure Session response and create a new SeResponse */
        poApduResponseList.remove(poApduResponseList.size() - 1);

        return new SeResponse(true, poSeResponse.getAtr(), poSeResponse.getFci(),
                poApduResponseList);
    }

    /**
     * Advanced variant of processAtomicClosing in which the list of expected responses is
     * determined from previous reading operations.
     *
     * @param poModificationCommands a list of commands that can modify the PO memory content
     * @param communicationMode the communication mode. If the communication mode is
     *        CONTACTLESS_MODE, a ratification command will be generated and sent to the PO after
     *        the Close Session command; the ratification will not be requested in the Close Session
     *        command. On the contrary, if the communication mode is CONTACTS_MODE, no ratification
     *        command will be sent to the PO and ratification will be requested in the Close Session
     *        command
     * @param closeSeChannel if true the SE channel of the PO reader must be closed after the last
     *        command
     * @return SeResponse close session response
     * @throws KeypleReaderException the IO reader exception This method is deprecated.
     *         <ul>
     *         <li>The argument of the ratification command is replaced by an indication of the PO
     *         communication mode.</li>
     *         </ul>
     */
    private SeResponse processAtomicClosing(List<PoModificationCommand> poModificationCommands,
            CommunicationMode communicationMode, boolean closeSeChannel)
            throws KeypleReaderException {
        List<ApduResponse> poAnticipatedResponses =
                AnticipatedResponseBuilder.getResponses(poModificationCommands);
        return processAtomicClosing(poModificationCommands, poAnticipatedResponses,
                communicationMode, closeSeChannel);
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
     * @return the {@link PoTransaction}.transactionResult
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
     * Get the PO KIF
     * 
     * @return the PO KIF byte
     */
    public byte getPoKif() {
        return poKif;
    }

    /**
     * Get the ratification status obtained at Session Opening
     * 
     * @return true or false
     */
    public boolean wasRatified() {
        return wasRatified;
    }

    /**
     * Get the data read at Session Opening
     * 
     * @return a byte array containing the data
     */
    public byte[] getOpenRecordDataRead() {
        return openRecordDataRead;
    }

    /**
     * Two communication modes are available for the PO.
     * 
     * It will be taken into account to handle the ratification when closing the Secure Session.
     */
    public enum CommunicationMode {
        CONTACTLESS_MODE, CONTACTS_MODE
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
     * The modification mode indicates whether the secure session can be closed and reopened to
     * manage the limitation of the PO buffer memory.
     */
    public enum ModificationMode {
        /**
         * The secure session is atomic. The consistency of the content of the resulting PO memory
         * is guaranteed.
         */
        ATOMIC,
        /**
         * Several secure sessions can be chained (to manage the writing of large amounts of data).
         * The resulting content of the PO's memory can be inconsistent if the PO is removed during
         * the process.
         */
        MULTIPLE
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
        private static final List<byte[]> poDigestDataCache = new ArrayList<byte[]>();
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
                byte workKeyKif, byte workKeyKVC, byte[] digestData) {
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
                        "PoTransaction.DigestProcessor => initialize: POREVISION = {}, CSMREVISION = {}, SESSIONENCRYPTION = {}",
                        poRev, csmRev, sessionEncryption, verificationMode);
                logger.debug(
                        "PoTransaction.DigestProcessor => initialize: VERIFICATIONMODE = {}, REV32MODE = {} KEYRECNUMBER = {}",
                        verificationMode, rev3_2Mode, workKeyRecordNumber);
                logger.debug(
                        "PoTransaction.DigestProcessor => initialize: KIF = {}, KVC {}, DIGESTDATA = {}",
                        String.format("%02X", workKeyKif), String.format("%02X", workKeyKVC),
                        ByteArrayUtils.toHex(digestData));
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

            logger.debug("PoTransaction.DigestProcessor => pushPoExchangeData: REQUEST = {}",
                    request);

            /*
             * Add an ApduRequest to the digest computation: if the request is of case4 type, Le
             * must be excluded from the digest computation. In this cas, we remove here the last
             * byte of the command buffer.
             */
            if (request.isCase4()) {
                poDigestDataCache.add(
                        Arrays.copyOfRange(request.getBytes(), 0, request.getBytes().length - 1));
            } else {
                poDigestDataCache.add(request.getBytes());
            }

            logger.debug("PoTransaction.DigestProcessor => pushPoExchangeData: RESPONSE = {}",
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
                        "PoTransaction.DigestProcessor => getCsmDigestRequest: no data in cache.");
                throw new IllegalStateException("Digest data cache is empty.");
            }
            if (poDigestDataCache.size() % 2 == 0) {
                /* the number of buffers should be 2*n + 1 */
                logger.debug(
                        "PoTransaction.DigestProcessor => getCsmDigestRequest: wrong number of buffer in cache NBR = {}.",
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

    /**
     * The class handles the anticipated response computation.
     */
    private static class AnticipatedResponseBuilder {
        /**
         * A nested class to associate a request with a response
         */
        private static class CommandResponse {
            private final ApduRequest apduRequest;
            private final ApduResponse apduResponse;

            CommandResponse(ApduRequest apduRequest, ApduResponse apduResponse) {
                this.apduRequest = apduRequest;
                this.apduResponse = apduResponse;
            }

            public ApduRequest getApduRequest() {
                return apduRequest;
            }

            public ApduResponse getApduResponse() {
                return apduResponse;
            }
        }

        /**
         * A Map of SFI and Commands/Responses
         */
        private static Map<Byte, CommandResponse> sfiCommandResponseHashMap =
                new HashMap<Byte, CommandResponse>();

        /**
         * Store all Read Record exchanges in a Map whose key is the SFI.
         * 
         * @param poSendableInSessions the list of commands sent to the PO
         * @param apduRequests the sent apduRequests
         * @param apduResponses the received apduResponses
         * @param skipFirstItem a flag to indicate if the first apduRequest/apduResponse pair has to
         *        be ignored or not.
         */
        static void storeCommandResponse(List<PoSendableInSession> poSendableInSessions,
                List<ApduRequest> apduRequests, List<ApduResponse> apduResponses,
                Boolean skipFirstItem) {
            if (poSendableInSessions != null) {
                /*
                 * Store Read Records' requests and responses for later use to build anticipated
                 * responses.
                 */
                Iterator<ApduRequest> apduRequestIterator = apduRequests.iterator();
                Iterator<ApduResponse> apduResponseIterator = apduResponses.iterator();
                if (skipFirstItem) {
                    /* case of processAtomicOpening */
                    apduRequestIterator.next();
                    apduResponseIterator.next();
                }
                /* Iterate over the poCommandsInsideSession list */
                for (PoSendableInSession poSendableInSession : poSendableInSessions) {
                    if (poSendableInSession instanceof ReadRecordsCmdBuild) {
                        ApduRequest apduRequest = apduRequestIterator.next();
                        byte sfi = (byte) ((apduRequest.getBytes()[OFFSET_P2] >> 3) & 0x1F);
                        sfiCommandResponseHashMap.put(sfi,
                                new CommandResponse(apduRequest, apduResponseIterator.next()));
                    } else {
                        apduRequestIterator.next();
                        apduResponseIterator.next();
                    }
                }
            }
        }

        /**
         * Establish the anticipated responses to commands provided in poModificationCommands.
         * <p>
         * Append Record and Update Record commands return 9000
         * <p>
         * Increase and Decrease return NNNNNN9000 where NNNNNNN is the new counter value.
         * <p>
         * NNNNNN is determine with the current value of the counter (extracted from the Read Record
         * responses previously collected) and the value to add or subtract provided in the command.
         * <p>
         * The SFI field is used to determine which data should be used to extract the needed
         * information.
         *
         * @param poModificationCommands the modification command list
         * @return the anticipated responses.
         * @throws KeypleCalypsoSecureSessionException if an response can't be determined.
         */
        public static List<ApduResponse> getResponses(
                List<PoModificationCommand> poModificationCommands)
                throws KeypleCalypsoSecureSessionException {
            List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
            if (poModificationCommands != null) {
                for (PoModificationCommand poModificationCommand : poModificationCommands) {
                    if (poModificationCommand instanceof DecreaseCmdBuild
                            || poModificationCommand instanceof IncreaseCmdBuild) {
                        /* response = NNNNNN9000 */
                        byte[] modCounterApduRequest = ((PoCommandBuilder) poModificationCommand)
                                .getApduRequest().getBytes();
                        /* Retrieve SFI from the current Decrease command */
                        byte sfi = (byte) ((modCounterApduRequest[OFFSET_P2] >> 3) & 0x1F);
                        /*
                         * Look for the counter value in the stored records. Only the first
                         * occurrence of the SFI is taken into account. We assume here that the
                         * record number is always 1.
                         */
                        CommandResponse commandResponse = sfiCommandResponseHashMap.get(sfi);
                        if (commandResponse != null) {
                            byte counterNumber = modCounterApduRequest[OFFSET_P1];
                            /*
                             * The record containing the counters is structured as follow:
                             * AAAAAAABBBBBBCCCCCC...XXXXXX each counter being a 3-byte unsigned
                             * number. Convert the 3-byte block indexed by the counter number to an
                             * int.
                             */
                            int currentCounterValue =
                                    ByteBuffer.wrap(commandResponse.getApduResponse().getBytes())
                                            .order(ByteOrder.BIG_ENDIAN)
                                            .getInt((counterNumber - 1) * 3) >> 8;
                            /* Extract the add or subtract value from the modification request */
                            int addSubtractValue = ByteBuffer.wrap(modCounterApduRequest)
                                    .order(ByteOrder.BIG_ENDIAN).getInt(OFFSET_DATA) >> 8;
                            /* Build the response */
                            byte[] response = new byte[5];
                            int newCounterValue;
                            if (poModificationCommand instanceof DecreaseCmdBuild) {
                                newCounterValue = currentCounterValue - addSubtractValue;
                            } else {
                                newCounterValue = currentCounterValue + addSubtractValue;
                            }
                            response[0] = (byte) ((newCounterValue & 0x00FF0000) >> 16);
                            response[1] = (byte) ((newCounterValue & 0x0000FF00) >> 8);
                            response[2] = (byte) ((newCounterValue & 0x000000FF) >> 0);
                            response[3] = (byte) 0x90;
                            response[4] = (byte) 0x00;
                            apduResponses.add(new ApduResponse(response, null));
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "Anticipated response. COMMAND = {}, SFI = {}, COUNTERVALUE = {}, DECREMENT = {}, NEWVALUE = {} ",
                                        (poModificationCommand instanceof DecreaseCmdBuild)
                                                ? "Decrease"
                                                : "Increase",
                                        sfi, currentCounterValue, addSubtractValue,
                                        newCounterValue);
                            }
                        } else {
                            throw new KeypleCalypsoSecureSessionException(
                                    "Anticipated response. COMMAND = "
                                            + ((poModificationCommand instanceof DecreaseCmdBuild)
                                                    ? "Decrease"
                                                    : "Increase")
                                            + ". Unable to determine anticipated counter value. SFI = "
                                            + sfi,
                                    ((PoCommandBuilder) poModificationCommand).getApduRequest(),
                                    null);
                        }
                    } else {
                        /* Append/Update/Write Record: response = 9000 */
                        apduResponses.add(new ApduResponse(ByteArrayUtils.fromHex("9000"), null));
                    }
                }
            }
            return apduResponses;
        }
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
     * <li>and some PO Apdu Requests including at least the Open Session command and all prepared PO
     * command to operate inside the session.</li>
     * </ul>
     * </li>
     * <li>The session PO keyset reference is identified from the PO Open Session response, the PO
     * challenge is recovered too.</li>
     * <li>According to the PO responses of Open Session and the PO commands sent inside the
     * session, a "cache" of CSM commands is filled with the corresponding Digest Init &amp; Digest
     * Update commands.</li>
     * <li>Returns the corresponding PO SeResponse (for all commands prepared before calling this
     * method).</li>
     * </ul>
     *
     * @param modificationMode the modification mode: ATOMIC or MULTIPLE (see
     *        {@link ModificationMode})
     * @param accessLevel access level of the session (personalization, load or debit).
     * @param openingSfiToSelect SFI of the file to select (0 means no file to select)
     * @param openingRecordNumberToRead number of the record to read
     * @return SeResponse response to all executed commands including the self generated "Open
     *         Secure Session" command
     * @throws KeypleReaderException the IO reader exception
     */
    public SeResponse processOpening(ModificationMode modificationMode,
            SessionAccessLevel accessLevel, byte openingSfiToSelect, byte openingRecordNumberToRead)
            throws KeypleReaderException {
        SeResponse seResponse = processAtomicOpening(accessLevel, openingSfiToSelect,
                openingRecordNumberToRead, poCommandBuilderList);
        poCommandBuilderList.clear();
        return seResponse;
    }

    /**
     * Process all prepared PO commands in a Secure Session.
     * <ul>
     * <li>On the PO reader, generates a SeRequest for the current selected AID, with
     * keepChannelOpen set at true, and ApduRequests with the PO commands.</li>
     * <li>In case the secure session is active, the "cache" of CSM commands is completed with the
     * corresponding Digest Update commands.</li>
     * <li>Returns the corresponding PO SeResponse.</li>
     * </ul>
     *
     * @return SeResponse all responses to the provided commands
     *
     * @throws KeypleReaderException IO Reader exception
     */
    public SeResponse processPoCommands() throws KeypleReaderException {
        SeResponse seResponse = processAtomicPoCommands(poCommandBuilderList);
        poCommandBuilderList.clear();
        return seResponse;
    }

    /**
     * processAtomicClosing in which the list of prepared commands is sent
     *
     * The list of expected responses is determined from previous reading operations.
     *
     * @param communicationMode the communication mode. If the communication mode is
     *        CONTACTLESS_MODE, a ratification command will be generated and sent to the PO after
     *        the Close Session command; the ratification will not be requested in the Close Session
     *        command. On the contrary, if the communication mode is CONTACTS_MODE, no ratification
     *        command will be sent to the PO and ratification will be requested in the Close Session
     *        command
     * @param closeSeChannel if true the SE channel of the PO reader must be closed after the last
     *        command
     * @return SeResponse close session response
     * @throws KeypleReaderException the IO reader exception This method is deprecated.
     *         <ul>
     *         <li>The argument of the ratification command is replaced by an indication of the PO
     *         communication mode.</li>
     *         </ul>
     */
    public SeResponse processClosing(CommunicationMode communicationMode, boolean closeSeChannel)
            throws KeypleReaderException {
        List<PoModificationCommand> poModificationCommandList =
                new ArrayList<PoModificationCommand>();
        for (PoSendableInSession command : poCommandBuilderList) {
            poModificationCommandList.add((PoModificationCommand) command);
        }
        SeResponse seResponse =
                processAtomicClosing(poModificationCommandList, communicationMode, closeSeChannel);
        poCommandBuilderList.clear();
        return null;
    }

    /**
     * Build a ReadRecords command and add it to the list of commands to be sent with the next
     * process command
     *
     * @param sfi the sfi top select
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param readJustOneRecord the read just one record
     * @param expectedLength the expected length of the record(s)
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if record number &lt; 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public void prepareReadRecordsCmd(byte sfi, byte firstRecordNumber, boolean readJustOneRecord,
            byte expectedLength, String extraInfo) {
        poCommandBuilderList.add(new ReadRecordsCmdBuild(calypsoPo.getRevision(), sfi,
                firstRecordNumber, readJustOneRecord, expectedLength, extraInfo));
    }

    /**
     * Build an AppendRecord command and add it to the list of commands to be sent with the next
     * process command
     *
     * @param sfi the sfi to select
     * @param newRecordData the new record data to write
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public void prepareAppendRecordCmd(byte sfi, byte[] newRecordData, String extraInfo) {
        poCommandBuilderList.add(
                new AppendRecordCmdBuild(calypsoPo.getRevision(), sfi, newRecordData, extraInfo));
    }

    /**
     * Build an UpdateRecord command and add it to the list of commands to be sent with the next
     * process command
     *
     * @param sfi the sfi to select
     * @param recordNumber the record number to update
     * @param newRecordData the new record data to write
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if record number is &lt; 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public void prepareUpdateRecordCmd(byte sfi, byte recordNumber, byte[] newRecordData,
            String extraInfo) {
        poCommandBuilderList.add(new UpdateRecordCmdBuild(calypsoPo.getRevision(), sfi,
                recordNumber, newRecordData, extraInfo));
    }

    /**
     * Build a Increase command and add it to the list of commands to be sent with the next process
     * command
     *
     * @param counterNumber &gt;= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param sfi SFI of the file to select or 00h for current EF
     * @param incValue Value to add to the counter (defined as a positive int &lt;= 16777215
     *        [FFFFFFh])
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if the decrement value is out of range
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public void prepareIncreaseCmd(byte sfi, byte counterNumber, int incValue, String extraInfo) {
        poCommandBuilderList.add(new IncreaseCmdBuild(calypsoPo.getRevision(), sfi, counterNumber,
                incValue, extraInfo));
    }

    /**
     * Build a Decrease command and add it to the list of commands to be sent with the next process
     * command
     *
     * @param counterNumber &gt;= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param sfi SFI of the file to select or 00h for current EF
     * @param decValue Value to subtract to the counter (defined as a positive int &lt;= 16777215
     *        [FFFFFFh])
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if the decrement value is out of range
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public void prepareDecreaseCmd(byte sfi, byte counterNumber, int decValue, String extraInfo) {
        poCommandBuilderList.add(new DecreaseCmdBuild(calypsoPo.getRevision(), sfi, counterNumber,
                decValue, extraInfo));
    }
}

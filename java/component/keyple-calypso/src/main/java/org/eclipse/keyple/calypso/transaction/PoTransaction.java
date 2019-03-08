/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;

import java.util.*;
import org.eclipse.keyple.calypso.command.SendableInSession;
import org.eclipse.keyple.calypso.command.po.*;
import org.eclipse.keyple.calypso.command.po.builder.*;
import org.eclipse.keyple.calypso.command.po.builder.session.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.session.CloseSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.*;
import org.eclipse.keyple.calypso.command.po.parser.session.AbstractOpenSessionRespPars;
import org.eclipse.keyple.calypso.command.po.parser.session.CloseSessionRespPars;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.SamSendableInSession;
import org.eclipse.keyple.calypso.command.sam.builder.session.DigestAuthenticateCmdBuild;
import org.eclipse.keyple.calypso.command.sam.builder.session.SelectDiversifierCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.session.DigestAuthenticateRespPars;
import org.eclipse.keyple.calypso.command.sam.parser.session.DigestCloseRespPars;
import org.eclipse.keyple.calypso.command.sam.parser.session.SamGetChallengeRespPars;
import org.eclipse.keyple.calypso.transaction.exception.*;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.*;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Portable Object Secure Session.
 *
 * A non-encrypted secure session with a Calypso PO requires the management of two
 * {@link ProxyReader} in order to communicate with both a Calypso PO and a SAM
 *
 * @author Calypso Networks Association
 */
public final class PoTransaction {

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
    /** The reader for session SAM. */
    private ProxyReader samReader;
    /** The SAM default revision. */
    private final SamRevision samRevision = SamRevision.C1;
    /** The SAM settings map. */
    private final EnumMap<SamSettings, Byte> samSetting =
            new EnumMap<SamSettings, Byte>(SamSettings.class);
    /** The PO serial number extracted from FCI */
    private final byte[] poCalypsoInstanceSerial;
    /** The current CalypsoPo */
    protected final CalypsoPo calypsoPo;
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
    /** The list to contain the parsers associated to the prepared commands */
    private List<AbstractApduResponseParser> poResponseParserList =
            new ArrayList<AbstractApduResponseParser>();
    /** The SAM settings status */
    private boolean samSettingsDefined;
    /** List of authorized KVCs */
    private List<Byte> authorizedKvcList;
    /** The current secure session modification mode: ATOMIC or MULTIPLE */
    private ModificationMode currentModificationMode;
    /** The current secure session access level: PERSO, RELOAD, DEBIT */
    private SessionAccessLevel currentAccessLevel;
    /* modifications counter management */
    private boolean modificationsCounterIsInBytes;
    private int modificationsCounterMax;
    private int modificationsCounter;

    /**
     * PoTransaction with PO and SAM readers.
     * <ul>
     * <li>Logical channels with PO &amp; SAM could already be established or not.</li>
     * <li>A list of SAM parameters is provided as en EnumMap.</li>
     * </ul>
     *
     * @param poReader the PO reader
     * @param calypsoPO the CalypsoPo object obtained at the end of the selection step
     * @param samReader the SAM reader
     * @param samSetting a list of SAM related parameters. In the case this parameter is null,
     *        default parameters are applied. The available setting keys are defined in
     *        {@link SamSettings}
     */
    public PoTransaction(SeReader poReader, CalypsoPo calypsoPO, SeReader samReader,
            EnumMap<SamSettings, Byte> samSetting) {

        this(poReader, calypsoPO);

        setSamSettings(samReader, samSetting);
    }

    /**
     * PoTransaction with PO reader and without SAM reader.
     * <ul>
     * <li>Logical channels with PO could already be established or not.</li>
     * </ul>
     *
     * @param poReader the PO reader
     * @param calypsoPO the CalypsoPo object obtained at the end of the selection step
     */
    public PoTransaction(SeReader poReader, CalypsoPo calypsoPO) {
        this.poReader = (ProxyReader) poReader;

        this.calypsoPo = calypsoPO;

        poRevision = calypsoPO.getRevision();

        poCalypsoInstanceAid = calypsoPO.getDfName();

        modificationsCounterIsInBytes = calypsoPO.isModificationsCounterInBytes();

        modificationsCounterMax = modificationsCounter = calypsoPO.getModificationsCounter();

        /* Serial Number of the selected Calypso instance. */
        poCalypsoInstanceSerial = calypsoPO.getApplicationSerialNumber();

        currentState = SessionState.SESSION_CLOSED;
    }

    /**
     * Sets the SAM parameters for Secure Session management
     * 
     * @param samReader
     * @param samSetting
     */
    public void setSamSettings(SeReader samReader, EnumMap<SamSettings, Byte> samSetting) {
        this.samReader = (ProxyReader) samReader;

        /* Initialize samSetting with provided settings */
        if (samSetting != null) {
            this.samSetting.putAll(samSetting);
        }

        /* Just work mode: we make sure that all the necessary parameters exist at least. */
        if (!this.samSetting.containsKey(SamSettings.SAM_DEFAULT_KIF_PERSO)) {
            this.samSetting.put(SamSettings.SAM_DEFAULT_KIF_PERSO, DEFAULT_KIF_PERSO);
        }
        if (!this.samSetting.containsKey(SamSettings.SAM_DEFAULT_KIF_LOAD)) {
            this.samSetting.put(SamSettings.SAM_DEFAULT_KIF_LOAD, DEFAULT_KIF_LOAD);
        }
        if (!this.samSetting.containsKey(SamSettings.SAM_DEFAULT_KIF_DEBIT)) {
            this.samSetting.put(SamSettings.SAM_DEFAULT_KIF_DEBIT, DEFAULT_KIF_DEBIT);
        }
        if (!this.samSetting.containsKey(SamSettings.SAM_DEFAULT_KEY_RECORD_NUMBER)) {
            this.samSetting.put(SamSettings.SAM_DEFAULT_KEY_RECORD_NUMBER,
                    DEFAULT_KEY_RECORD_NUMER);
        }

        logger.debug("Contructor => SAMSETTING = {}", this.samSetting);

        samSettingsDefined = true;
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
    public boolean isSamSettingsDefined() {
        return samSettingsDefined;
    }

    /**
     * Open a Secure Session.
     * <ul>
     * <li>The PO must have been previously selected, so a logical channel with the PO application
     * must be already active.</li>
     * <li>The PO serial &amp; revision are identified from FCI data.</li>
     * <li>A first request is sent to the SAM session reader.
     * <ul>
     * <li>In case not logical channel is active with the SAM, a channel is open.</li>
     * <li>Then a Select Diversifier (with the PO serial) &amp; a Get Challenge are automatically
     * operated. The SAM challenge is recovered.</li>
     * </ul>
     * </li>
     * <li>The PO Open Session command is built according to the PO revision, the SAM challenge, the
     * keyIndex, and openingSfiToSelect / openingRecordNumberToRead.</li>
     * <li>Next the PO reader is requested:
     * <ul>
     * <li>for the current selected PO AID, with channelState set to KEEP_OPEN,</li>
     * <li>and some PO Apdu Requests including at least the Open Session command and optionally some
     * PO command to operate inside the session.</li>
     * </ul>
     * </li>
     * <li>The session PO keyset reference is identified from the PO Open Session response, the PO
     * challenge is recovered too.</li>
     * <li>According to the PO responses of Open Session and the PO commands sent inside the
     * session, a "cache" of SAM commands is filled with the corresponding Digest Init &amp; Digest
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
        int numberOfSamCmd = 1;

        /* SAM ApduRequest List to hold Select Diversifier and Get Challenge commands */
        List<ApduRequest> samApduRequestList = new ArrayList<ApduRequest>();

        if (logger.isDebugEnabled()) {
            logger.debug("processAtomicOpening => Identification: DFNAME = {}, SERIALNUMBER = {}",
                    ByteArrayUtils.toHex(poCalypsoInstanceAid),
                    ByteArrayUtils.toHex(poCalypsoInstanceSerial));
        }
        /* diversify only if this has not already been done. */
        if (!isDiversificationDone) {
            /* Build the SAM Select Diversifier command to provide the SAM with the PO S/N */
            AbstractApduCommandBuilder selectDiversifier =
                    new SelectDiversifierCmdBuild(this.samRevision, poCalypsoInstanceSerial);

            samApduRequestList.add(selectDiversifier.getApduRequest());

            /* increment command number */
            numberOfSamCmd++;

            /* change the diversification status */
            isDiversificationDone = true;
        }
        /* Build the SAM Get Challenge command */
        byte challengeLength = poRevision.equals(PoRevision.REV3_2) ? CHALLENGE_LENGTH_REV32
                : CHALLENGE_LENGTH_REV_INF_32;

        AbstractApduCommandBuilder samGetChallenge =
                new org.eclipse.keyple.calypso.command.sam.builder.session.SamGetChallengeCmdBuild(
                        this.samRevision, challengeLength);

        samApduRequestList.add(samGetChallenge.getApduRequest());

        /* Build a SAM SeRequest */
        SeRequest samSeRequest = new SeRequest(samApduRequestList, ChannelState.KEEP_OPEN);

        logger.debug("processAtomicOpening => identification: SAMSEREQUEST = {}", samSeRequest);

        /*
         * Transmit the SeRequest to the SAM and get back the SeResponse (list of ApduResponse)
         */
        SeResponse samSeResponse = samReader.transmit(samSeRequest);

        if (samSeResponse == null) {
            throw new KeypleCalypsoSecureSessionException("Null response received",
                    KeypleCalypsoSecureSessionException.Type.SAM, samSeRequest.getApduRequests(),
                    null);
        }

        logger.debug("processAtomicOpening => identification: SAMSERESPONSE = {}", samSeResponse);

        List<ApduResponse> samApduResponseList = samSeResponse.getApduResponses();
        byte[] sessionTerminalChallenge;

        if (samApduResponseList.size() == numberOfSamCmd
                && samApduResponseList.get(numberOfSamCmd - 1).isSuccessful() && samApduResponseList
                        .get(numberOfSamCmd - 1).getDataOut().length == challengeLength) {
            SamGetChallengeRespPars samChallengePars =
                    new SamGetChallengeRespPars(samApduResponseList.get(numberOfSamCmd - 1));
            sessionTerminalChallenge = samChallengePars.getChallenge();
            if (logger.isDebugEnabled()) {
                logger.debug("processAtomicOpening => identification: TERMINALCHALLENGE = {}",
                        ByteArrayUtils.toHex(sessionTerminalChallenge));
            }
        } else {
            throw new KeypleCalypsoSecureSessionException("Invalid message received",
                    KeypleCalypsoSecureSessionException.Type.SAM, samApduRequestList,
                    samApduResponseList);
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

        /* Create a SeRequest from the ApduRequest list, PO AID as Selector, keep channel open */
        SeRequest poSeRequest = new SeRequest(poApduRequestList, ChannelState.KEEP_OPEN);

        logger.debug("processAtomicOpening => opening:  POSEREQUEST = {}", poSeRequest);

        /* Transmit the commands to the PO */
        SeResponse poSeResponse = poReader.transmit(poSeRequest);

        logger.debug("processAtomicOpening => opening:  POSERESPONSE = {}", poSeResponse);

        if (poSeResponse == null) {
            throw new KeypleCalypsoSecureSessionException("Null response received",
                    KeypleCalypsoSecureSessionException.Type.PO, poSeRequest.getApduRequests(),
                    null);
        }

        if (poSeResponse.wasChannelPreviouslyOpen() == false) {
            throw new KeypleCalypsoSecureSessionException("The logical channel was not open",
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
        // TODO handle rev 1 KVC (provided in the response to select DF. CalypsoPo?)
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
                    kif = samSetting.get(SamSettings.SAM_DEFAULT_KIF_PERSO);
                    break;
                case SESSION_LVL_LOAD:
                    kif = samSetting.get(SamSettings.SAM_DEFAULT_KIF_LOAD);
                    break;
                case SESSION_LVL_DEBIT:
                default:
                    kif = samSetting.get(SamSettings.SAM_DEFAULT_KIF_DEBIT);
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
         * Update) until the session closing. AT this moment, all SAM Apdu will be processed at
         * once.
         */
        DigestProcessor.initialize(poRevision, samRevision, false, false,
                poRevision.equals(PoRevision.REV3_2),
                samSetting.get(SamSettings.SAM_DEFAULT_KEY_RECORD_NUMBER), kif, poKvc,
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

        return new SeResponse(true, poSeResponse.getSelectionStatus(), poApduResponseList);
    }

    /**
     * Change SendableInSession List to ApduRequest List .
     *
     * @param poOrSamCommandsInsideSession a po or sam commands list to be sent in session
     * @return the ApduRequest list
     */
    private List<ApduRequest> getApduRequestsToSendInSession(
            List<SendableInSession> poOrSamCommandsInsideSession) {
        List<ApduRequest> apduRequestList = new ArrayList<ApduRequest>();
        if (poOrSamCommandsInsideSession != null) {
            for (SendableInSession cmd : poOrSamCommandsInsideSession) {
                apduRequestList.add(((AbstractApduCommandBuilder) cmd).getApduRequest());
            }
        }
        return apduRequestList;
    }

    /**
     * Process PO commands in a Secure Session.
     * <ul>
     * <li>On the PO reader, generates a SeRequest with channelState set to KEEP_OPEN, and
     * ApduRequests with the PO commands.</li>
     * <li>In case the secure session is active, the "cache" of SAM commands is completed with the
     * corresponding Digest Update commands.</li>
     * <li>If a session is open and channelState is set to CLOSE_AFTER, the current PO session is
     * aborted</li>
     * <li>Returns the corresponding PO SeResponse.</li>
     * </ul>
     *
     * @param poCommands the po commands inside session
     * @param channelState indicated if the SE channel of the PO reader must be closed after the
     *        last command
     * @return SeResponse all responses to the provided commands
     *
     * @throws KeypleReaderException IO Reader exception
     */
    private SeResponse processAtomicPoCommands(List<PoSendableInSession> poCommands,
            ChannelState channelState) throws KeypleReaderException {

        // Get PO ApduRequest List from PoSendableInSession List
        List<ApduRequest> poApduRequestList =
                this.getApduRequestsToSendInSession((List<SendableInSession>) (List<?>) poCommands);

        /*
         * Create a SeRequest from the ApduRequest list, PO AID as Selector, manage the logical
         * channel according to the channelState enum
         */
        SeRequest poSeRequest = new SeRequest(poApduRequestList, channelState);

        logger.debug("processAtomicPoCommands => POREQUEST = {}", poSeRequest);

        /* Transmit the commands to the PO */
        SeResponse poSeResponse = poReader.transmit(poSeRequest);

        logger.debug("processAtomicPoCommands => PORESPONSE = {}", poSeResponse);

        if (poSeResponse == null) {
            throw new KeypleCalypsoSecureSessionException("Null response received",
                    KeypleCalypsoSecureSessionException.Type.PO, poSeRequest.getApduRequests(),
                    null);
        }

        if (poSeResponse.wasChannelPreviouslyOpen() == false) {
            throw new KeypleCalypsoSecureSessionException("The logical channel was not open",
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
     * Process SAM commands.
     * <ul>
     * <li>On the SAM reader, transmission of a SeRequest with channelState set to KEEP_OPEN.</li>
     * <li>Returns the corresponding SAM SeResponse.</li>
     * </ul>
     *
     * @param samCommands a list of commands to sent to the SAM
     * @return SeResponse all sam responses
     * @throws KeypleReaderException if a reader error occurs
     */
    public SeResponse processSamCommands(List<SamSendableInSession> samCommands)
            throws KeypleReaderException {

        /* Init SAM ApduRequest List - for the first SAM exchange */
        List<ApduRequest> samApduRequestList = this
                .getApduRequestsToSendInSession((List<SendableInSession>) (List<?>) samCommands);

        /* SeRequest from the command list */
        SeRequest samSeRequest = new SeRequest(samApduRequestList, ChannelState.KEEP_OPEN);

        logger.debug("processSamCommands => SAMSEREQUEST = {}", samSeRequest);

        /* Transmit SeRequest and get SeResponse */
        SeResponse samSeResponse = samReader.transmit(samSeRequest);

        if (samSeResponse == null) {
            throw new KeypleCalypsoSecureSessionException("Null response received",
                    KeypleCalypsoSecureSessionException.Type.SAM, samSeRequest.getApduRequests(),
                    null);
        }

        if (currentState == SessionState.SESSION_OPEN
                && samSeResponse.wasChannelPreviouslyOpen() == false) {
            throw new KeypleCalypsoSecureSessionException("The logical channel was not open",
                    KeypleCalypsoSecureSessionException.Type.SAM, samSeRequest.getApduRequests(),
                    null);
        }
        // TODO check if the wasChannelPreviouslyOpen should be done in the case where the session
        // is closed

        return samSeResponse;
    }

    /**
     * Close the Secure Session.
     * <ul>
     * <li>The SAM cache is completed with the Digest Update commands related to the new PO commands
     * to be sent and their anticipated responses. A Digest Close command is also added to the SAM
     * command cache.</li>
     * <li>On the SAM session reader side, a SeRequest is transmitted with SAM commands from the
     * command cache. The SAM command cache is emptied.</li>
     * <li>The SAM certificate is retrieved from the Digest Close response. The terminal signature
     * is identified.</li>
     * <li>Then, on the PO reader, a SeRequest is transmitted with the provided channelState, and
     * apduRequests including the new PO commands to send in the session, a Close Session command
     * (defined with the SAM certificate), and optionally a ratificationCommand.
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
     * <li>Finally, on the SAM session reader, a Digest Authenticate is automatically operated in
     * order to verify the PO signature.</li>
     * <li>Returns the corresponding PO SeResponse.</li>
     * </ul>
     *
     * The method is marked as deprecated because the advanced variant defined below must be used at
     * the application level.
     * 
     * @param poModificationCommands a list of commands that can modify the PO memory content
     * @param poAnticipatedResponses a list of anticipated PO responses to the modification commands
     * @param transmissionMode the communication mode. If the communication mode is CONTACTLESS, a
     *        ratification command will be generated and sent to the PO after the Close Session
     *        command; the ratification will not be requested in the Close Session command. On the
     *        contrary, if the communication mode is CONTACTS, no ratification command will be sent
     *        to the PO and ratification will be requested in the Close Session command
     * @param channelState indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @return SeResponse close session response
     * @throws KeypleReaderException the IO reader exception This method is deprecated.
     *         <ul>
     *         <li>The argument of the ratification command is replaced by an indication of the PO
     *         communication mode.</li>
     *         </ul>
     */
    private SeResponse processAtomicClosing(List<PoModificationCommand> poModificationCommands,
            List<ApduResponse> poAnticipatedResponses, TransmissionMode transmissionMode,
            ChannelState channelState) throws KeypleReaderException {

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

        /* All SAM digest operations will now run at once. */
        /* Get the SAM Digest request from the cache manager */
        SeRequest samSeRequest = DigestProcessor.getSamDigestRequest();

        logger.debug("processAtomicClosing => SAMREQUEST = {}", samSeRequest);

        /* Transmit SeRequest and get SeResponse */
        SeResponse samSeResponse = samReader.transmit(samSeRequest);

        logger.debug("processAtomicClosing => SAMRESPONSE = {}", samSeResponse);

        if (samSeResponse == null) {
            throw new KeypleCalypsoSecureSessionException("Null response received",
                    KeypleCalypsoSecureSessionException.Type.SAM, samSeRequest.getApduRequests(),
                    null);
        }

        if (samSeResponse.wasChannelPreviouslyOpen() == false) {
            throw new KeypleCalypsoSecureSessionException("The logical channel was not open",
                    KeypleCalypsoSecureSessionException.Type.PO, samSeRequest.getApduRequests(),
                    null);
        }

        List<ApduResponse> samApduResponseList = samSeResponse.getApduResponses();

        for (int i = 0; i < samApduResponseList.size(); i++) {
            if (!samApduResponseList.get(i).isSuccessful()) {

                logger.debug("processAtomicClosing => command failure REQUEST = {}, RESPONSE = {}",
                        samSeRequest.getApduRequests().get(i), samApduResponseList.get(i));
                throw new IllegalStateException(
                        "ProcessClosing command failure during digest computation process.");
            }
        }

        /* Get Terminal Signature from the latest response */
        byte[] sessionTerminalSignature = null;
        // TODO Add length check according to Calypso REV (4 / 8)
        if (!samApduResponseList.isEmpty()) {
            DigestCloseRespPars respPars = new DigestCloseRespPars(
                    samApduResponseList.get(samApduResponseList.size() - 1));

            sessionTerminalSignature = respPars.getSignature();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("processAtomicClosing => SIGNATURE = {}",
                    ByteArrayUtils.toHex(sessionTerminalSignature));
        }

        PoCustomReadCommandBuilder ratificationCommand;
        boolean ratificationAsked;

        if (transmissionMode == TransmissionMode.CONTACTLESS) {
            if (poRevision == PoRevision.REV2_4) {
                ratificationCommand = new PoCustomReadCommandBuilder("Ratification command",
                        new ApduRequest(ratificationCmdApduLegacy, false));
            } else {
                ratificationCommand = new PoCustomReadCommandBuilder("Ratification command",
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
        CloseSessionCmdBuild closeCommand = new CloseSessionCmdBuild(calypsoPo.getPoClass(),
                ratificationAsked, sessionTerminalSignature);

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
        SeRequest poSeRequest = new SeRequest(poApduRequestList, channelState);

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

        if (poSeResponse == null) {
            throw new KeypleCalypsoSecureSessionException("Null response received",
                    KeypleCalypsoSecureSessionException.Type.PO, poSeRequest.getApduRequests(),
                    null);
        }

        if (poSeResponse.wasChannelPreviouslyOpen() == false) {
            throw new KeypleCalypsoSecureSessionException("The logical channel was not open",
                    KeypleCalypsoSecureSessionException.Type.PO, poSeRequest.getApduRequests(),
                    null);
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

        /* Check the PO signature part with the SAM */
        /* Build and send SAM Digest Authenticate command */
        AbstractApduCommandBuilder digestAuth =
                new DigestAuthenticateCmdBuild(samRevision, poCloseSessionPars.getSignatureLo());

        List<ApduRequest> samApduRequestList = new ArrayList<ApduRequest>();
        samApduRequestList.add(digestAuth.getApduRequest());

        samSeRequest = new SeRequest(samApduRequestList, ChannelState.KEEP_OPEN);

        logger.debug("PoTransaction.DigestProcessor => checkPoSignature: SAMREQUEST = {}",
                samSeRequest);

        samSeResponse = samReader.transmit(samSeRequest);

        logger.debug("PoTransaction.DigestProcessor => checkPoSignature: SAMRESPONSE = {}",
                samSeResponse);

        if (samSeResponse == null) {
            throw new KeypleCalypsoSecureSessionException("Null response received",
                    KeypleCalypsoSecureSessionException.Type.SAM, samSeRequest.getApduRequests(),
                    null);
        }

        if (samSeResponse.wasChannelPreviouslyOpen() == false) {
            throw new KeypleCalypsoSecureSessionException("The logical channel was not open",
                    KeypleCalypsoSecureSessionException.Type.SAM, samSeRequest.getApduRequests(),
                    null);
        }

        /* Get transaction result parsing the response */
        samApduResponseList = samSeResponse.getApduResponses();

        transactionResult = false;
        if ((samApduResponseList != null) && !samApduResponseList.isEmpty()) {
            DigestAuthenticateRespPars respPars =
                    new DigestAuthenticateRespPars(samApduResponseList.get(0));
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

        return new SeResponse(true, poSeResponse.getSelectionStatus(), poApduResponseList);
    }

    /**
     * Advanced variant of processAtomicClosing in which the list of expected responses is
     * determined from previous reading operations.
     *
     * @param poModificationCommands a list of commands that can modify the PO memory content
     * @param transmissionMode the communication mode. If the communication mode is CONTACTLESS, a
     *        ratification command will be generated and sent to the PO after the Close Session
     *        command; the ratification will not be requested in the Close Session command. On the
     *        contrary, if the communication mode is CONTACTS, no ratification command will be sent
     *        to the PO and ratification will be requested in the Close Session command
     * @param channelState indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @return SeResponse close session response
     * @throws KeypleReaderException the IO reader exception This method is deprecated.
     *         <ul>
     *         <li>The argument of the ratification command is replaced by an indication of the PO
     *         communication mode.</li>
     *         </ul>
     */
    private SeResponse processAtomicClosing(List<PoModificationCommand> poModificationCommands,
            TransmissionMode transmissionMode, ChannelState channelState)
            throws KeypleReaderException {
        List<ApduResponse> poAnticipatedResponses =
                AnticipatedResponseBuilder.getResponses(poModificationCommands);
        return processAtomicClosing(poModificationCommands, poAnticipatedResponses,
                transmissionMode, channelState);
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
     * <li>To check the result of a closed secure session, returns true if the SAM Digest
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
     * List of SAM settings keys that can be provided when the secure session is created.
     */
    public enum SamSettings {
        /** KIF for personalization used when not provided by the PO */
        SAM_DEFAULT_KIF_PERSO,
        /** KIF for load used when not provided by the PO */
        SAM_DEFAULT_KIF_LOAD,
        /** KIF for debit used when not provided by the PO */
        SAM_DEFAULT_KIF_DEBIT,
        /** Key record number to use when KIF/KVC is unavailable */
        SAM_DEFAULT_KEY_RECORD_NUMBER
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
         * The digest data cache stores all PO data to be send to SAM during a Secure Session. The
         * 1st buffer is the data buffer to be provided with Digest Init. The following buffers are
         * PO command/response pairs
         */
        private static final List<byte[]> poDigestDataCache = new ArrayList<byte[]>();
        private static SamRevision samRevision;
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
         * @param samRev the SAM revision
         * @param sessionEncryption true if the session is encrypted
         * @param verificationMode true if the verification mode is active
         * @param rev3_2Mode true if the REV3.2 mode is active
         * @param workKeyRecordNumber the key record number
         * @param workKeyKif the PO KIF
         * @param workKeyKVC the PO KVC
         * @param digestData a first bunch of data to digest.
         */
        static void initialize(PoRevision poRev, SamRevision samRev, boolean sessionEncryption,
                boolean verificationMode, boolean rev3_2Mode, byte workKeyRecordNumber,
                byte workKeyKif, byte workKeyKVC, byte[] digestData) {
            /* Store work context */
            poRevision = poRev;
            samRevision = samRev;
            encryption = sessionEncryption;
            verification = verificationMode;
            revMode = rev3_2Mode;
            keyRecordNumber = workKeyRecordNumber;
            keyKIF = workKeyKif;
            keyKVC = workKeyKVC;
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "PoTransaction.DigestProcessor => initialize: POREVISION = {}, SAMREVISION = {}, SESSIONENCRYPTION = {}",
                        poRev, samRev, sessionEncryption, verificationMode);
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
         * Get a unique SAM request for the whole digest computation process.
         * 
         * @return SeRequest all the ApduRequest to send to the SAM in order to get the terminal
         *         signature
         */
        // TODO optimization with the use of Digest Update Multiple whenever possible.
        static SeRequest getSamDigestRequest() {
            List<ApduRequest> samApduRequestList = new ArrayList<ApduRequest>();

            if (poDigestDataCache.size() == 0) {
                logger.debug(
                        "PoTransaction.DigestProcessor => getSamDigestRequest: no data in cache.");
                throw new IllegalStateException("Digest data cache is empty.");
            }
            if (poDigestDataCache.size() % 2 == 0) {
                /* the number of buffers should be 2*n + 1 */
                logger.debug(
                        "PoTransaction.DigestProcessor => getSamDigestRequest: wrong number of buffer in cache NBR = {}.",
                        poDigestDataCache.size());
                throw new IllegalStateException("Digest data cache is inconsistent.");
            }

            /*
             * Build and append Digest Init command as first ApduRequest of the digest computation
             * process
             */
            samApduRequestList.add(
                    new org.eclipse.keyple.calypso.command.sam.builder.session.DigestInitCmdBuild(
                            samRevision, verification, revMode, keyRecordNumber, keyKIF, keyKVC,
                            poDigestDataCache.get(0)).getApduRequest());

            /*
             * Build and append Digest Update commands
             *
             * The first command is at index 1.
             */
            for (int i = 1; i < poDigestDataCache.size(); i++) {
                samApduRequestList.add(
                        new org.eclipse.keyple.calypso.command.sam.builder.session.DigestUpdateCmdBuild(
                                samRevision, encryption, poDigestDataCache.get(i))
                                        .getApduRequest());
            }

            /*
             * Build and append Digest Close command
             */
            samApduRequestList.add(
                    (new org.eclipse.keyple.calypso.command.sam.builder.session.DigestCloseCmdBuild(
                            samRevision,
                            poRevision.equals(PoRevision.REV3_2) ? SIGNATURE_LENGTH_REV32
                                    : SIGNATURE_LENGTH_REV_INF_32).getApduRequest()));


            return new SeRequest(samApduRequestList, ChannelState.KEEP_OPEN);
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
                            int currentCounterValue = ByteArrayUtils.threeBytesToInt(
                                    commandResponse.getApduResponse().getBytes(),
                                    (counterNumber - 1) * 3);
                            /* Extract the add or subtract value from the modification request */
                            int addSubtractValue = ByteArrayUtils
                                    .threeBytesToInt(modCounterApduRequest, OFFSET_DATA);
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
     * <li>A first request is sent to the SAM session reader.
     * <ul>
     * <li>In case not logical channel is active with the SAM, a channel is open.</li>
     * <li>Then a Select Diversifier (with the PO serial) &amp; a Get Challenge are automatically
     * operated. The SAM challenge is recovered.</li>
     * </ul>
     * </li>
     * <li>The PO Open Session command is built according to the PO revision, the SAM challenge, the
     * keyIndex, and openingSfiToSelect / openingRecordNumberToRead.</li>
     * <li>Next the PO reader is requested:
     * <ul>
     * <li>for the currently selected PO, with channelState set to KEEP_OPEN,</li>
     * <li>and some PO Apdu Requests including at least the Open Session command and all prepared PO
     * command to operate inside the session.</li>
     * </ul>
     * </li>
     * <li>The session PO keyset reference is identified from the PO Open Session response, the PO
     * challenge is recovered too.</li>
     * <li>According to the PO responses of Open Session and the PO commands sent inside the
     * session, a "cache" of SAM commands is filled with the corresponding Digest Init &amp; Digest
     * Update commands.</li>
     * <li>All parsers returned by the prepare command methods are updated with the Apdu responses
     * from the PO.</li>
     * </ul>
     *
     * @param modificationMode the modification mode: ATOMIC or MULTIPLE (see
     *        {@link ModificationMode})
     * @param accessLevel access level of the session (personalization, load or debit).
     * @param openingSfiToSelect SFI of the file to select (0 means no file to select)
     * @param openingRecordNumberToRead number of the record to read
     * @return true if all commands are successful
     * @throws KeypleReaderException the IO reader exception
     */
    public boolean processOpening(ModificationMode modificationMode, SessionAccessLevel accessLevel,
            byte openingSfiToSelect, byte openingRecordNumberToRead) throws KeypleReaderException {
        currentModificationMode = modificationMode;
        currentAccessLevel = accessLevel;
        byte localOpeningRecordNumberToRead = openingRecordNumberToRead;
        boolean poProcessSuccess = true;
        /*
         * Iterator to keep the progress in updating the parsers from the list of prepared commands
         */
        Iterator<AbstractApduResponseParser> apduResponseParserIterator =
                poResponseParserList.iterator();
        List<PoSendableInSession> poAtomicCommandBuilderList = new ArrayList<PoSendableInSession>();
        for (PoSendableInSession poCommandBuilderElement : poCommandBuilderList) {
            if (!(poCommandBuilderElement instanceof PoModificationCommand)) {
                /* This command does not affect the PO modifications buffer */
                poAtomicCommandBuilderList.add(poCommandBuilderElement);
            } else {
                /* This command affects the PO modifications buffer */
                if (willOverflowBuffer((PoModificationCommand) poCommandBuilderElement)) {
                    if (currentModificationMode == ModificationMode.ATOMIC) {
                        throw new IllegalStateException(
                                "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                                        + poCommandBuilderElement.toString());
                    }
                    SeResponse seResponseOpening =
                            processAtomicOpening(currentAccessLevel, openingSfiToSelect,
                                    localOpeningRecordNumberToRead, poAtomicCommandBuilderList);

                    /*
                     * inhibit record reading for next round, keep file selection (TODO check this)
                     */
                    localOpeningRecordNumberToRead = (byte) 0x00;

                    if (!updateParsersWithResponses(seResponseOpening,
                            apduResponseParserIterator)) {
                        poProcessSuccess = false;
                    }
                    /*
                     * Closes the session, resets the modifications buffer counters for the next
                     * round (set the contact mode to avoid the transmission of the ratification)
                     */
                    processAtomicClosing(null, TransmissionMode.CONTACTS, ChannelState.KEEP_OPEN);
                    resetModificationsBufferCounter();
                    /*
                     * Clear the list and add the command that did not fit in the PO modifications
                     * buffer. We also update the usage counter without checking the result.
                     */
                    poAtomicCommandBuilderList.clear();
                    poAtomicCommandBuilderList.add(poCommandBuilderElement);
                    /*
                     * just update modifications buffer usage counter, ignore result (always false)
                     */
                    willOverflowBuffer((PoModificationCommand) poCommandBuilderElement);
                } else {
                    /*
                     * The command fits in the PO modifications buffer, just add it to the list
                     */
                    poAtomicCommandBuilderList.add(poCommandBuilderElement);
                }
            }
        }

        SeResponse seResponseOpening = processAtomicOpening(currentAccessLevel, openingSfiToSelect,
                localOpeningRecordNumberToRead, poAtomicCommandBuilderList);
        if (!updateParsersWithResponses(seResponseOpening, apduResponseParserIterator)) {
            poProcessSuccess = false;
        }

        poCommandBuilderList.clear();
        poResponseParserList.clear();
        return poProcessSuccess;
    }

    /**
     * Process all prepared PO commands (outside a Secure Session).
     * <ul>
     * <li>On the PO reader, generates a SeRequest with channelState set to the provided value and
     * ApduRequests containing the PO commands.</li>
     * <li>All parsers returned by the prepare command methods are updated with the Apdu responses
     * from the PO.</li>
     * </ul>
     *
     * @param channelState indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @return true if all commands are successful
     *
     * @throws KeypleReaderException IO Reader exception
     */
    public boolean processPoCommands(ChannelState channelState) throws KeypleReaderException {

        /** This method should be called only if no session was previously open */
        if (currentState == SessionState.SESSION_OPEN) {
            throw new IllegalStateException("A session is open");
        }

        boolean poProcessSuccess = true;
        /*
         * Iterator to keep the progress in updating the parsers from the list of prepared commands
         */
        Iterator<AbstractApduResponseParser> abstractApduResponseParserIterator =
                poResponseParserList.iterator();
        /* PO commands sent outside a Secure Session. No modifications buffer limitation. */
        SeResponse seResponsePoCommands =
                processAtomicPoCommands(poCommandBuilderList, channelState);

        if (!updateParsersWithResponses(seResponsePoCommands, abstractApduResponseParserIterator)) {
            poProcessSuccess = false;
        }

        /* clean up global lists */
        poCommandBuilderList.clear();
        poResponseParserList.clear();
        return poProcessSuccess;
    }

    /**
     * Process all prepared PO commands in a Secure Session.
     * <ul>
     * <li>On the PO reader, generates a SeRequest with channelState set to KEEP_OPEN, and
     * ApduRequests containing the PO commands.</li>
     * <li>In case the secure session is active, the "cache" of SAM commands is completed with the
     * corresponding Digest Update commands.</li>
     * <li>All parsers returned by the prepare command methods are updated with the Apdu responses
     * from the PO.</li>
     * </ul>
     *
     * @return true if all commands are successful
     *
     * @throws KeypleReaderException IO Reader exception
     */
    public boolean processPoCommandsInSession() throws KeypleReaderException {

        /** This method should be called only if a session was previously open */
        if (currentState == SessionState.SESSION_CLOSED) {
            throw new IllegalStateException("No open session");
        }

        boolean poProcessSuccess = true;
        /*
         * Iterator to keep the progress in updating the parsers from the list of prepared commands
         */
        Iterator<AbstractApduResponseParser> abstractApduResponseParserIterator =
                poResponseParserList.iterator();

        /* A session is open, we have to care about the PO modifications buffer */
        List<PoSendableInSession> poAtomicCommandBuilderList = new ArrayList<PoSendableInSession>();

        for (PoSendableInSession poCommandBuilderElement : poCommandBuilderList) {
            if (!(poCommandBuilderElement instanceof PoModificationCommand)) {
                /* This command does not affect the PO modifications buffer */
                poAtomicCommandBuilderList.add(poCommandBuilderElement);
            } else {
                /* This command affects the PO modifications buffer */
                if (willOverflowBuffer(((PoModificationCommand) poCommandBuilderElement))) {
                    if (currentModificationMode == ModificationMode.ATOMIC) {
                        throw new IllegalStateException(
                                "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                                        + poCommandBuilderElement.toString());
                    }
                    /*
                     * The current command would overflow the modifications buffer in the PO. We
                     * send the current commands and update the parsers. The parsers Iterator is
                     * kept all along the process.
                     */
                    SeResponse seResponsePoCommands = processAtomicPoCommands(
                            poAtomicCommandBuilderList, ChannelState.KEEP_OPEN);
                    if (!updateParsersWithResponses(seResponsePoCommands,
                            abstractApduResponseParserIterator)) {
                        poProcessSuccess = false;
                    }
                    /*
                     * Close the session and reset the modifications buffer counters for the next
                     * round (set the contact mode to avoid the transmission of the ratification)
                     */
                    processAtomicClosing(null, TransmissionMode.CONTACTS, ChannelState.KEEP_OPEN);
                    resetModificationsBufferCounter();
                    /* We reopen a new session for the remaining commands to be sent */
                    SeResponse seResponseOpening = processAtomicOpening(currentAccessLevel,
                            (byte) 0x00, (byte) 0x00, null);
                    /*
                     * Clear the list and add the command that did not fit in the PO modifications
                     * buffer. We also update the usage counter without checking the result.
                     */
                    poAtomicCommandBuilderList.clear();
                    poAtomicCommandBuilderList.add(poCommandBuilderElement);
                    /*
                     * just update modifications buffer usage counter, ignore result (always false)
                     */
                    willOverflowBuffer((PoModificationCommand) poCommandBuilderElement);
                } else {
                    /*
                     * The command fits in the PO modifications buffer, just add it to the list
                     */
                    poAtomicCommandBuilderList.add(poCommandBuilderElement);
                }
            }
        }

        if (!poAtomicCommandBuilderList.isEmpty()) {
            SeResponse seResponsePoCommands =
                    processAtomicPoCommands(poAtomicCommandBuilderList, ChannelState.KEEP_OPEN);
            if (!updateParsersWithResponses(seResponsePoCommands,
                    abstractApduResponseParserIterator)) {
                poProcessSuccess = false;
            }
        }

        /* clean up global lists */
        poCommandBuilderList.clear();
        poResponseParserList.clear();
        return poProcessSuccess;
    }

    /**
     * Sends the currently prepared commands list (may be empty) and closes the Secure Session.
     * <ul>
     * <li>The ratification is handled according to the communication mode.</li>
     * <li>The logical channel can be left open or closed.</li>
     * <li>All parsers returned by the prepare command methods are updated with the Apdu responses
     * from the PO.</li>
     * </ul>
     *
     * @param transmissionMode the communication mode. If the communication mode is CONTACTLESS, a
     *        ratification command will be generated and sent to the PO after the Close Session
     *        command; the ratification will not be requested in the Close Session command. On the
     *        contrary, if the communication mode is CONTACTS, no ratification command will be sent
     *        to the PO and ratification will be requested in the Close Session command
     * @param channelState indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @return true if all commands are successful
     * @throws KeypleReaderException the IO reader exception This method is deprecated.
     *         <ul>
     *         <li>The argument of the ratification command is replaced by an indication of the PO
     *         communication mode.</li>
     *         </ul>
     */
    public boolean processClosing(TransmissionMode transmissionMode, ChannelState channelState)
            throws KeypleReaderException {
        boolean poProcessSuccess = true;
        boolean atLeastOneReadCommand = false;
        boolean sessionPreviouslyClosed = false;
        List<PoModificationCommand> poModificationCommandList =
                new ArrayList<PoModificationCommand>();
        Iterator<AbstractApduResponseParser> abstractApduResponseParserIterator =
                poResponseParserList.iterator();
        List<PoModificationCommand> poAtomicCommandBuilderList =
                new ArrayList<PoModificationCommand>();
        SeResponse seResponseClosing;
        for (PoSendableInSession poCommandBuilderElement : poCommandBuilderList) {
            if (!(poCommandBuilderElement instanceof PoModificationCommand)) {
                /*
                 * This command does not affect the PO modifications buffer. We will call
                 * processPoCommands first
                 */
                poAtomicCommandBuilderList.add((PoModificationCommand) poCommandBuilderElement);
                atLeastOneReadCommand = true;
            } else {
                /* This command affects the PO modifications buffer */
                if (willOverflowBuffer((PoModificationCommand) poCommandBuilderElement)) {
                    if (currentModificationMode == ModificationMode.ATOMIC) {
                        throw new IllegalStateException(
                                "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                                        + poCommandBuilderElement.toString());
                    }
                    /*
                     * Reopen a session with the same access level if it was previously closed in
                     * this current processClosing
                     */
                    if (sessionPreviouslyClosed) {
                        processAtomicOpening(currentAccessLevel, (byte) 0x00, (byte) 0x00, null);
                    }

                    /*
                     * If at least one non-modifying was prepared, we use processAtomicPoCommands
                     * instead of processAtomicClosing to send the list
                     */
                    if (atLeastOneReadCommand) {
                        List<PoSendableInSession> poSendableInSessionList =
                                new ArrayList<PoSendableInSession>();
                        for (PoModificationCommand command : poAtomicCommandBuilderList) {
                            poSendableInSessionList.add((PoSendableInSession) command);
                        }
                        seResponseClosing = processAtomicPoCommands(poSendableInSessionList,
                                ChannelState.KEEP_OPEN);
                        atLeastOneReadCommand = false;
                    } else {
                        /* All commands in the list are 'modifying' */
                        seResponseClosing = processAtomicClosing(poAtomicCommandBuilderList,
                                TransmissionMode.CONTACTS, ChannelState.KEEP_OPEN);
                        resetModificationsBufferCounter();
                        sessionPreviouslyClosed = true;
                    }

                    Iterator<AbstractApduResponseParser> apduResponseParserIterator =
                            poResponseParserList.iterator();
                    if (!updateParsersWithResponses(seResponseClosing,
                            apduResponseParserIterator)) {
                        poProcessSuccess = false;
                    }
                    /*
                     * Clear the list and add the command that did not fit in the PO modifications
                     * buffer. We also update the usage counter without checking the result.
                     */
                    poAtomicCommandBuilderList.clear();
                    poAtomicCommandBuilderList.add((PoModificationCommand) poCommandBuilderElement);
                    /*
                     * just update modifications buffer usage counter, ignore result (always false)
                     */
                    willOverflowBuffer((PoModificationCommand) poCommandBuilderElement);
                } else {
                    /*
                     * The command fits in the PO modifications buffer, just add it to the list
                     */
                    poAtomicCommandBuilderList.add((PoModificationCommand) poCommandBuilderElement);
                }
            }
        }
        if (sessionPreviouslyClosed) {
            /*
             * Reopen if needed, to close the session with the requested conditions
             * (CommunicationMode and channelState)
             */
            processAtomicOpening(currentAccessLevel, (byte) 0x00, (byte) 0x00, null);
        }

        /* Finally, close the session as requested */
        seResponseClosing =
                processAtomicClosing(poAtomicCommandBuilderList, transmissionMode, channelState);

        /* Update parsers */
        if (!updateParsersWithResponses(seResponseClosing, abstractApduResponseParserIterator)) {
            poProcessSuccess = false;
        }

        /* clean up global lists */
        poCommandBuilderList.clear();
        poResponseParserList.clear();
        return poProcessSuccess;
    }

    /**
     * Abort a Secure Session.
     * <p>
     * Send the appropriate command to the PO
     * <p>
     * Clean up internal data and status.
     * 
     * @param channelState indicates if the SE channel of the PO reader must be closed after the
     *        abort session command
     * @return true if the abort command received a successful response from the PO
     */
    public boolean processCancel(ChannelState channelState) {
        /* PO ApduRequest List to hold Close Secure Session command */
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();

        /* Build the PO Close Session command (in "abort" mode since no signature is provided). */
        CloseSessionCmdBuild closeCommand = new CloseSessionCmdBuild(calypsoPo.getPoClass());

        poApduRequestList.add(closeCommand.getApduRequest());

        /*
         * Transfer PO commands
         */
        SeRequest poSeRequest = new SeRequest(poApduRequestList, channelState);

        logger.debug("processCancel => POSEREQUEST = {}", poSeRequest);

        SeResponse poSeResponse;
        try {
            poSeResponse = poReader.transmit(poSeRequest);
        } catch (KeypleReaderException ex) {
            poSeResponse = ex.getSeResponse();
        }

        logger.debug("processCancel => POSERESPONSE = {}", poSeResponse);

        /* clean up global lists */
        poCommandBuilderList.clear();
        poResponseParserList.clear();

        /*
         * session is now considered closed regardless the previous state or the result of the abort
         * session command sent to the PO.
         */
        currentState = SessionState.SESSION_CLOSED;

        /* return the successful status of the abort session command */
        return poSeResponse.getApduResponses().get(0).isSuccessful();
    }

    /**
     * Loops on the SeResponse and updates the list of parsers pointed out by the provided iterator
     * 
     * @param seResponse the seResponse from the PO
     * @param parserIterator the parser list iterator
     * @return false if one or more of the commands do not succeed
     */
    private boolean updateParsersWithResponses(SeResponse seResponse,
            Iterator<AbstractApduResponseParser> parserIterator) {
        boolean allSuccessfulCommands = true;
        /* double loop to set apdu responses to corresponding parsers */
        for (ApduResponse apduResponse : seResponse.getApduResponses()) {
            if (!parserIterator.hasNext()) {
                throw new IllegalStateException("Parsers list and responses list mismatch! ");
            }
            parserIterator.next().setApduResponse(apduResponse);
            if (!apduResponse.isSuccessful()) {
                allSuccessfulCommands = false;
            }
        }
        return allSuccessfulCommands;
    }

    /**
     * Checks whether the requirement for the modifications buffer of the command provided in
     * argument is compatible with the current usage level of the buffer.
     * <p>
     * If it is compatible, the requirement is subtracted from the current level and the method
     * returns false. If this is not the case, the method returns true.
     * 
     * @param modificationCommand the modification command
     * @return true or false
     */
    private boolean willOverflowBuffer(PoModificationCommand modificationCommand) {
        boolean willOverflow = false;
        if (modificationsCounterIsInBytes) {
            int bufferRequirement = ((AbstractApduCommandBuilder) modificationCommand)
                    .getApduRequest().getBytes()[OFFSET_Lc] + 6;

            if (modificationsCounter - bufferRequirement > 0) {
                modificationsCounter = modificationsCounter - bufferRequirement;
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Modifications buffer overflow! BYTESMODE, CURRENTCOUNTER = {}, REQUIREMENT = {}",
                            modificationsCounter, bufferRequirement);
                }
                willOverflow = true;
            }
        } else {
            if (modificationsCounter > 0) {
                modificationsCounter--;
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Modifications buffer overflow! COMMANDSMODE, CURRENTCOUNTER = {}, REQUIREMENT = {}",
                            modificationsCounter, 1);
                }
                willOverflow = true;
            }
        }
        return willOverflow;
    }

    /**
     * Initialized the modifications buffer counter to its maximum value for the current PO
     */
    private void resetModificationsBufferCounter() {
        if (logger.isTraceEnabled()) {
            logger.trace("Modifications buffer counter reset: PREVIOUSVALUE = {}, NEWVALUE = {}",
                    modificationsCounter, modificationsCounterMax);
        }
        modificationsCounter = modificationsCounterMax;
    }

    /**
     * Internal method to handle expectedLength checks in public variants
     * 
     * @param sfi the sfi top select
     * @param readDataStructureEnum read mode enum to indicate a SINGLE, MULTIPLE or COUNTER read
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param expectedLength the expected length of the record(s)
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return ReadRecordsRespPars the ReadRecords command response parser
     * @throws java.lang.IllegalArgumentException - if record number &lt; 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    private ReadRecordsRespPars prepareReadRecordsCmdInternal(byte sfi,
            ReadDataStructure readDataStructureEnum, byte firstRecordNumber, int expectedLength,
            String extraInfo) {

        /*
         * the readJustOneRecord flag is set to false only in case of multiple read records, in all
         * other cases it is set to true
         */
        boolean readJustOneRecord =
                !(readDataStructureEnum == readDataStructureEnum.MULTIPLE_RECORD_DATA);

        poCommandBuilderList.add(new ReadRecordsCmdBuild(calypsoPo.getPoClass(), sfi,
                firstRecordNumber, readJustOneRecord, (byte) expectedLength, extraInfo));

        ReadRecordsRespPars poResponseParser =
                new ReadRecordsRespPars(firstRecordNumber, readDataStructureEnum);

        poResponseParserList.add(poResponseParser);

        return poResponseParser;
    }

    /**
     * Builds a ReadRecords command and add it to the list of commands to be sent with the next
     * process command.
     * <p>
     * The expected length is provided and its value is checked between 1 and 250.
     * <p>
     * Returns the associated response parser.
     *
     * @param sfi the sfi top select
     * @param readDataStructureEnum read mode enum to indicate a SINGLE, MULTIPLE or COUNTER read
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param expectedLength the expected length of the record(s)
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return ReadRecordsRespPars the ReadRecords command response parser
     * @throws java.lang.IllegalArgumentException - if record number &lt; 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public ReadRecordsRespPars prepareReadRecordsCmd(byte sfi,
            ReadDataStructure readDataStructureEnum, byte firstRecordNumber, int expectedLength,
            String extraInfo) {
        if (expectedLength < 1 || expectedLength > 250) {
            throw new IllegalArgumentException("Bad length.");
        }
        return prepareReadRecordsCmdInternal(sfi, readDataStructureEnum, firstRecordNumber,
                expectedLength, extraInfo);
    }

    /**
     * Builds a ReadRecords command and add it to the list of commands to be sent with the next
     * process command. No expected length is specified, the record output length is handled
     * automatically.
     * <p>
     * Returns the associated response parser.
     *
     * @param sfi the sfi top select
     * @param readDataStructureEnum read mode enum to indicate a SINGLE, MULTIPLE or COUNTER read
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return ReadRecordsRespPars the ReadRecords command response parser
     * @throws java.lang.IllegalArgumentException - if record number &lt; 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public ReadRecordsRespPars prepareReadRecordsCmd(byte sfi,
            ReadDataStructure readDataStructureEnum, byte firstRecordNumber, String extraInfo) {
        if (poReader.getTransmissionMode() == TransmissionMode.CONTACTS) {
            throw new IllegalArgumentException(
                    "In contacts mode, the expected length must be specified.");
        }
        return prepareReadRecordsCmdInternal(sfi, readDataStructureEnum, firstRecordNumber, 0,
                extraInfo);
    }

    /**
     * Builds an AppendRecord command and add it to the list of commands to be sent with the next
     * process command.
     * <p>
     * Returns the associated response parser.
     *
     * @param sfi the sfi to select
     * @param newRecordData the new record data to write
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return AppendRecordRespPars the AppendRecord command response parser
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public AppendRecordRespPars prepareAppendRecordCmd(byte sfi, byte[] newRecordData,
            String extraInfo) {
        poCommandBuilderList.add(
                new AppendRecordCmdBuild(calypsoPo.getPoClass(), sfi, newRecordData, extraInfo));

        AppendRecordRespPars poResponseParser = new AppendRecordRespPars();

        poResponseParserList.add(poResponseParser);

        return poResponseParser;
    }

    /**
     * Builds an UpdateRecord command and add it to the list of commands to be sent with the next
     * process command
     * <p>
     * Returns the associated response parser.
     *
     * @param sfi the sfi to select
     * @param recordNumber the record number to update
     * @param newRecordData the new record data to write
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return UpdateRecordRespPars the UpdateRecord command response parser
     * @throws java.lang.IllegalArgumentException - if record number is &lt; 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public UpdateRecordRespPars prepareUpdateRecordCmd(byte sfi, byte recordNumber,
            byte[] newRecordData, String extraInfo) {
        poCommandBuilderList.add(new UpdateRecordCmdBuild(calypsoPo.getPoClass(), sfi, recordNumber,
                newRecordData, extraInfo));

        UpdateRecordRespPars poResponseParser = new UpdateRecordRespPars();

        poResponseParserList.add(poResponseParser);

        return poResponseParser;
    }

    /**
     * Builds a Increase command and add it to the list of commands to be sent with the next process
     * command
     * <p>
     * Returns the associated response parser.
     *
     * @param counterNumber &gt;= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param sfi SFI of the file to select or 00h for current EF
     * @param incValue Value to add to the counter (defined as a positive int &lt;= 16777215
     *        [FFFFFFh])
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return IncreaseRespPars the Increase command response parser
     * @throws java.lang.IllegalArgumentException - if the decrement value is out of range
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public IncreaseRespPars prepareIncreaseCmd(byte sfi, byte counterNumber, int incValue,
            String extraInfo) {
        poCommandBuilderList.add(new IncreaseCmdBuild(calypsoPo.getPoClass(), sfi, counterNumber,
                incValue, extraInfo));

        IncreaseRespPars poResponseParser = new IncreaseRespPars();

        poResponseParserList.add(poResponseParser);

        return poResponseParser;
    }

    /**
     * Builds a Decrease command and add it to the list of commands to be sent with the next process
     * command
     * <p>
     * Returns the associated response parser.
     *
     * @param counterNumber &gt;= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param sfi SFI of the file to select or 00h for current EF
     * @param decValue Value to subtract to the counter (defined as a positive int &lt;= 16777215
     *        [FFFFFFh])
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return DecreaseRespPars the Decrease command response parser
     * @throws java.lang.IllegalArgumentException - if the decrement value is out of range
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public DecreaseRespPars prepareDecreaseCmd(byte sfi, byte counterNumber, int decValue,
            String extraInfo) {
        poCommandBuilderList.add(new DecreaseCmdBuild(calypsoPo.getPoClass(), sfi, counterNumber,
                decValue, extraInfo));
        DecreaseRespPars poResponseParser = new DecreaseRespPars();

        poResponseParserList.add(poResponseParser);

        return poResponseParser;
    }
}

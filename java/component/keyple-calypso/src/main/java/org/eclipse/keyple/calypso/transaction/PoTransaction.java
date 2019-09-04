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
import org.eclipse.keyple.calypso.command.CalypsoBuilderParser;
import org.eclipse.keyple.calypso.command.po.*;
import org.eclipse.keyple.calypso.command.po.builder.*;
import org.eclipse.keyple.calypso.command.po.builder.security.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.CloseSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.*;
import org.eclipse.keyple.calypso.command.po.parser.security.AbstractOpenSessionRespPars;
import org.eclipse.keyple.calypso.command.po.parser.security.CloseSessionRespPars;
import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.security.*;
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestAuthenticateRespPars;
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestCloseRespPars;
import org.eclipse.keyple.calypso.command.sam.parser.security.SamGetChallengeRespPars;
import org.eclipse.keyple.calypso.transaction.exception.*;
import org.eclipse.keyple.core.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
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
    private final static byte[] ratificationCmdApduLegacy = ByteArrayUtil.fromHex("94B2000000");
    /** Ratification command APDU for rev > 2.4 */
    private final static byte[] ratificationCmdApdu = ByteArrayUtil.fromHex("00B2000000");

    private static final Logger logger = LoggerFactory.getLogger(PoTransaction.class);

    /** The reader for PO. */
    private final ProxyReader poReader;
    /** The reader for session SAM. */
    private ProxyReader samReader;
    /** The SAM default revision. */
    private final SamRevision samRevision = SamRevision.C1;
    /** The security settings. */
    private SecuritySettings securitySettings;
    /** The PO serial number extracted from FCI */
    private final byte[] poCalypsoInstanceSerial;
    /** The current CalypsoPo */
    private final CalypsoPo calypsoPo;
    /** the type of the notified event. */
    private SessionState sessionState;
    /** Selected AID of the Calypso PO. */
    private byte[] poCalypsoInstanceAid;
    /** The PO Calypso Revision. */
    private PoRevision poRevision;
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
    /** The list to contain the prepared commands and their parsers */
    private final List<PoBuilderParser> poBuilderParserList = new ArrayList<PoBuilderParser>();
    /** The current secure session modification mode: ATOMIC or MULTIPLE */
    private ModificationMode currentModificationMode;
    /** The current secure session access level: PERSO, RELOAD, DEBIT */
    private SessionAccessLevel currentAccessLevel;
    /* modifications counter management */
    private boolean modificationsCounterIsInBytes;
    private int modificationsCounterMax;
    private int modificationsCounter;

    private boolean preparedCommandsProcessed;
    private int preparedCommandIndex;

    /**
     * PoTransaction with PO and SAM readers.
     * <ul>
     * <li>Logical channels with PO &amp; SAM could already be established or not.</li>
     * <li>A list of SAM parameters is provided as en EnumMap.</li>
     * </ul>
     *
     * @param poResource the PO resource (combination of {@link SeReader} and {@link CalypsoPo})
     * @param samResource the SAM resource (combination of {@link SeReader} and {@link CalypsoSam})
     * @param securitySettings a list of security settings ({@link SecuritySettings}) used in the
     *        session (such as key identification)
     */
    public PoTransaction(PoResource poResource, SamResource samResource,
            SecuritySettings securitySettings) {

        this(poResource);

        samReader = (ProxyReader) samResource.getSeReader();

        this.securitySettings = securitySettings;
    }

    /**
     * PoTransaction with PO reader and without SAM reader.
     * <ul>
     * <li>Logical channels with PO could already be established or not.</li>
     * </ul>
     *
     * @param poResource the PO resource (combination of {@link SeReader} and {@link CalypsoPo})
     */
    public PoTransaction(PoResource poResource) {
        this.poReader = (ProxyReader) poResource.getSeReader();

        this.calypsoPo = poResource.getMatchingSe();

        poRevision = calypsoPo.getRevision();

        poCalypsoInstanceAid = calypsoPo.getDfName();

        modificationsCounterIsInBytes = calypsoPo.isModificationsCounterInBytes();

        modificationsCounterMax = modificationsCounter = calypsoPo.getModificationsCounter();

        /* Serial Number of the selected Calypso instance. */
        poCalypsoInstanceSerial = calypsoPo.getApplicationSerialNumber();

        sessionState = SessionState.SESSION_UNINITIALIZED;

        preparedCommandsProcessed = true;
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
     * <li>Returns the corresponding PO SeResponse (responses to poBuilderParsers).</li>
     * </ul>
     *
     * @param accessLevel access level of the session (personalization, load or debit).
     * @param openingSfiToSelect SFI of the file to select (0 means no file to select)
     * @param openingRecordNumberToRead number of the record to read
     * @param poBuilderParsers the po commands inside session
     * @return SeResponse response to all executed commands including the self generated "Open
     *         Secure Session" command
     * @throws KeypleReaderException the IO reader exception
     */
    private SeResponse processAtomicOpening(SessionAccessLevel accessLevel, byte openingSfiToSelect,
            byte openingRecordNumberToRead, List<PoBuilderParser> poBuilderParsers)
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
                    ByteArrayUtil.toHex(poCalypsoInstanceAid),
                    ByteArrayUtil.toHex(poCalypsoInstanceSerial));
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

        AbstractSamCommandBuilder samGetChallenge =
                new SamGetChallengeCmdBuild(this.samRevision, challengeLength);

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
                        ByteArrayUtil.toHex(sessionTerminalChallenge));
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
        AbstractOpenSessionCmdBuild poOpenSession = AbstractOpenSessionCmdBuild.create(poRevision,
                accessLevel.getSessionKey(), sessionTerminalChallenge, openingSfiToSelect,
                openingRecordNumberToRead, "");

        /* Add the resulting ApduRequest to the PO ApduRequest list */
        poApduRequestList.add(poOpenSession.getApduRequest());

        /* Add all optional PoSendableInSession commands to the PO ApduRequest list */
        if (poBuilderParsers != null) {
            poApduRequestList.addAll(this.getApduRequestsToSendInSession(poBuilderParsers));
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

        if (!poSeResponse.wasChannelPreviouslyOpen()) {
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
        AnticipatedResponseBuilder.storeCommandResponse(poBuilderParsers, poApduRequestList,
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
                    ByteArrayUtil.toHex(sessionCardChallenge), String.format("%02X", poKif),
                    String.format("%02X", poKvc));
        }

        if (!securitySettings.isAuthorizedKvc(poKvc)) {
            throw new KeypleCalypsoSecureSessionUnauthorizedKvcException(
                    String.format("PO KVC = %02X", poKvc));
        }

        byte kif;
        if (poKif == KIF_UNDEFINED) {
            switch (accessLevel) {
                case SESSION_LVL_PERSO:
                    kif = securitySettings
                            .getKeyInfo(SecuritySettings.DefaultKeyInfo.SAM_DEFAULT_KIF_PERSO);
                    break;
                case SESSION_LVL_LOAD:
                    kif = securitySettings
                            .getKeyInfo(SecuritySettings.DefaultKeyInfo.SAM_DEFAULT_KIF_LOAD);
                    break;
                case SESSION_LVL_DEBIT:
                default:
                    kif = securitySettings
                            .getKeyInfo(SecuritySettings.DefaultKeyInfo.SAM_DEFAULT_KIF_DEBIT);
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
                securitySettings
                        .getKeyInfo(SecuritySettings.DefaultKeyInfo.SAM_DEFAULT_KEY_RECORD_NUMBER),
                kif, poKvc, poApduResponseList.get(0).getDataOut());

        /*
         * Add all commands data to the digest computation. The first command in the list is the
         * open secure session command. This command is not included in the digest computation, so
         * we skip it and start the loop at index 1.
         */
        if ((poBuilderParsers != null) && !poBuilderParsers.isEmpty()) {

            for (int i = 1; i < poApduRequestList.size(); i++) { // The loop starts after the Open
                /*
                 * Add requests and responses to the DigestProcessor
                 */
                DigestProcessor.pushPoExchangeData(poApduRequestList.get(i),
                        poApduResponseList.get(i));
            }
        }

        sessionState = SessionState.SESSION_OPEN;

        /* Remove Open Secure Session response and create a new SeResponse */
        poApduResponseList.remove(0);

        return new SeResponse(true, true, poSeResponse.getSelectionStatus(), poApduResponseList);
    }

    /**
     * Change SendableInSession List to ApduRequest List .
     *
     * @param poOrSamCommandsInsideSession a po or sam commands list to be sent in session
     * @return the ApduRequest list
     */
    private List<ApduRequest> getApduRequestsToSendInSession(
            List<? extends CalypsoBuilderParser> poOrSamCommandsInsideSession) {
        List<ApduRequest> apduRequestList = new ArrayList<ApduRequest>();
        if (poOrSamCommandsInsideSession != null) {
            for (CalypsoBuilderParser cmd : poOrSamCommandsInsideSession) {
                apduRequestList.add(cmd.getCommandBuilder().getApduRequest());
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
     * @param poBuilderParsers the po commands inside session
     * @param channelState indicated if the SE channel of the PO reader must be closed after the
     *        last command
     * @return SeResponse all responses to the provided commands
     *
     * @throws KeypleReaderException IO Reader exception
     */
    private SeResponse processAtomicPoCommands(List<PoBuilderParser> poBuilderParsers,
            ChannelState channelState) throws KeypleReaderException {

        // Get PO ApduRequest List from PoSendableInSession List
        List<ApduRequest> poApduRequestList = this.getApduRequestsToSendInSession(poBuilderParsers);

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

        if (!poSeResponse.wasChannelPreviouslyOpen()) {
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
        AnticipatedResponseBuilder.storeCommandResponse(poBuilderParsers, poApduRequestList,
                poApduResponseList, false);

        /*
         * Add all commands data to the digest computation if this method is called within a Secure
         * Session.
         */
        if (sessionState == SessionState.SESSION_OPEN) {
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
     * @param samBuilderParsers a list of commands to sent to the SAM
     * @return SeResponse all sam responses
     * @throws KeypleReaderException if a reader error occurs
     */
    // public SeResponse processSamCommands(List<SamBuilderParser> samBuilderParsers)
    // throws KeypleReaderException {
    //
    // /* Init SAM ApduRequest List - for the first SAM exchange */
    // List<ApduRequest> samApduRequestList =
    // this.getApduRequestsToSendInSession(samBuilderParsers);
    //
    // /* SeRequest from the command list */
    // SeRequest samSeRequest = new SeRequest(samApduRequestList, ChannelState.KEEP_OPEN);
    //
    // logger.debug("processSamCommands => SAMSEREQUEST = {}", samSeRequest);
    //
    // /* Transmit SeRequest and get SeResponse */
    // SeResponse samSeResponse = samReader.transmit(samSeRequest);
    //
    // if (samSeResponse == null) {
    // throw new KeypleCalypsoSecureSessionException("Null response received",
    // KeypleCalypsoSecureSessionException.Type.SAM, samSeRequest.getApduRequests(),
    // null);
    // }
    //
    // if (sessionState == SessionState.SESSION_OPEN
    // && !samSeResponse.wasChannelPreviouslyOpen()) {
    // throw new KeypleCalypsoSecureSessionException("The logical channel was not open",
    // KeypleCalypsoSecureSessionException.Type.SAM, samSeRequest.getApduRequests(),
    // null);
    // }
    // // TODO check if the wasChannelPreviouslyOpen should be done in the case where the session
    // // is closed
    //
    // return samSeResponse;
    // }

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
    private SeResponse processAtomicClosing(List<PoBuilderParser> poModificationCommands,
            List<ApduResponse> poAnticipatedResponses, TransmissionMode transmissionMode,
            ChannelState channelState) throws KeypleReaderException {

        if (sessionState != SessionState.SESSION_OPEN) {
            throw new IllegalStateException("Bad session state. Current: " + sessionState.toString()
                    + ", expected: " + SessionState.SESSION_OPEN.toString());
        }

        /* Get PO ApduRequest List from PoSendableInSession List - for the first PO exchange */
        List<ApduRequest> poApduRequestList =
                this.getApduRequestsToSendInSession(poModificationCommands);

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

        if (!samSeResponse.wasChannelPreviouslyOpen()) {
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
                    ByteArrayUtil.toHex(sessionTerminalSignature));
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

        if (!poSeResponse.wasChannelPreviouslyOpen()) {
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

        if (!samSeResponse.wasChannelPreviouslyOpen()) {
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

        sessionState = SessionState.SESSION_CLOSED;

        /* Remove ratification response if any */
        if (!ratificationAsked) {
            poApduResponseList.remove(poApduResponseList.size() - 1);
        }
        /* Remove Close Secure Session response and create a new SeResponse */
        poApduResponseList.remove(poApduResponseList.size() - 1);

        return new SeResponse(true, true, poSeResponse.getSelectionStatus(), poApduResponseList);
    }

    /**
     * Advanced variant of processAtomicClosing in which the list of expected responses is
     * determined from previous reading operations.
     *
     * @param poBuilderParsers a list of commands that can modify the PO memory content
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
    private SeResponse processAtomicClosing(List<PoBuilderParser> poBuilderParsers,
            TransmissionMode transmissionMode, ChannelState channelState)
            throws KeypleReaderException {
        List<ApduResponse> poAnticipatedResponses =
                AnticipatedResponseBuilder.getResponses(poBuilderParsers);
        return processAtomicClosing(poBuilderParsers, poAnticipatedResponses, transmissionMode,
                channelState);
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

        if (sessionState != SessionState.SESSION_CLOSED) {
            throw new IllegalStateException(
                    "Session is not closed, state:" + sessionState.toString() + ", expected: "
                            + SessionState.SESSION_OPEN.toString());
        }

        return transactionResult;
    }

    /**
     * Get the ratification status obtained at Session Opening
     * 
     * @return true or false
     * @throws IllegalStateException if no session has been initiated
     */
    public boolean wasRatified() {
        if (sessionState == SessionState.SESSION_UNINITIALIZED) {
            throw new IllegalStateException("No active session.");
        }
        return wasRatified;
    }

    /**
     * Get the data read at Session Opening
     * 
     * @return a byte array containing the data
     * @throws IllegalStateException if no session has been initiated
     */
    public byte[] getOpenRecordDataRead() {
        if (sessionState == SessionState.SESSION_UNINITIALIZED) {
            throw new IllegalStateException("No active session.");
        }
        return openRecordDataRead;
    }

    /**
     * The PO Transaction Access Level: personalization, loading or debiting.
     */
    public enum SessionAccessLevel {
        /** Session Access Level used for personalization purposes. */
        SESSION_LVL_PERSO("perso", (byte) 0x01),
        /** Session Access Level used for reloading purposes. */
        SESSION_LVL_LOAD("load", (byte) 0x02),
        /** Session Access Level used for validating and debiting purposes. */
        SESSION_LVL_DEBIT("debit", (byte) 0x03);

        private final String name;
        private final byte sessionKey;

        SessionAccessLevel(String name, byte sessionKey) {
            this.name = name;
            this.sessionKey = sessionKey;
        }

        public String getName() {
            return name;
        }

        public byte getSessionKey() {
            return sessionKey;
        }
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
    private enum SessionState {
        /** Initial state of a PO transaction. The PO must have been previously selected. */
        SESSION_UNINITIALIZED,
        /** The secure session is active. */
        SESSION_OPEN,
        /** The secure session is closed. */
        SESSION_CLOSED
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
                        ByteArrayUtil.toHex(digestData));
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
            samApduRequestList.add(new DigestInitCmdBuild(samRevision, verification, revMode,
                    keyRecordNumber, keyKIF, keyKVC, poDigestDataCache.get(0)).getApduRequest());

            /*
             * Build and append Digest Update commands
             *
             * The first command is at index 1.
             */
            for (int i = 1; i < poDigestDataCache.size(); i++) {
                samApduRequestList.add(
                        new DigestUpdateCmdBuild(samRevision, encryption, poDigestDataCache.get(i))
                                .getApduRequest());
            }

            /*
             * Build and append Digest Close command
             */
            samApduRequestList.add((new DigestCloseCmdBuild(samRevision,
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
         * @param poBuilderParsers the list of commands sent to the PO
         * @param apduRequests the sent apduRequests
         * @param apduResponses the received apduResponses
         * @param skipFirstItem a flag to indicate if the first apduRequest/apduResponse pair has to
         *        be ignored or not.
         */
        static void storeCommandResponse(List<PoBuilderParser> poBuilderParsers,
                List<ApduRequest> apduRequests, List<ApduResponse> apduResponses,
                Boolean skipFirstItem) {
            if (poBuilderParsers != null) {
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
                for (PoBuilderParser poCommand : poBuilderParsers) {
                    if (((CalypsoBuilderParser) poCommand)
                            .getCommandBuilder() instanceof ReadRecordsCmdBuild) {
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
         * @param poBuilderParsers the modification command list
         * @return the anticipated responses.
         * @throws KeypleCalypsoSecureSessionException if an response can't be determined.
         */
        private static List<ApduResponse> getResponses(List<PoBuilderParser> poBuilderParsers)
                throws KeypleCalypsoSecureSessionException {
            List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
            if (poBuilderParsers != null) {
                for (PoBuilderParser poBuilderParser : poBuilderParsers) {
                    if (poBuilderParser.getCommandBuilder() instanceof DecreaseCmdBuild
                            || poBuilderParser.getCommandBuilder() instanceof IncreaseCmdBuild) {
                        /* response = NNNNNN9000 */
                        byte[] modCounterApduRequest =
                                (poBuilderParser.getCommandBuilder()).getApduRequest().getBytes();
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
                            int currentCounterValue = ByteArrayUtil.threeBytesToInt(
                                    commandResponse.getApduResponse().getBytes(),
                                    (counterNumber - 1) * 3);
                            /* Extract the add or subtract value from the modification request */
                            int addSubtractValue = ByteArrayUtil
                                    .threeBytesToInt(modCounterApduRequest, OFFSET_DATA);
                            /* Build the response */
                            byte[] response = new byte[5];
                            int newCounterValue;
                            if (poBuilderParser.getCommandBuilder() instanceof DecreaseCmdBuild) {
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
                                        (poBuilderParser
                                                .getCommandBuilder() instanceof DecreaseCmdBuild)
                                                        ? "Decrease"
                                                        : "Increase",
                                        sfi, currentCounterValue, addSubtractValue,
                                        newCounterValue);
                            }
                        } else {
                            throw new KeypleCalypsoSecureSessionException(
                                    "Anticipated response. COMMAND = " + ((poBuilderParser
                                            .getCommandBuilder() instanceof DecreaseCmdBuild)
                                                    ? "Decrease"
                                                    : "Increase")
                                            + ". Unable to determine anticipated counter value. SFI = "
                                            + sfi,
                                    ((PoBuilderParser) poBuilderParser).getCommandBuilder()
                                            .getApduRequest(),
                                    null);
                        }
                    } else {
                        /* Append/Update/Write Record: response = 9000 */
                        apduResponses.add(new ApduResponse(ByteArrayUtil.fromHex("9000"), null));
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
     * <li>All parsers keept by the prepare command methods are updated with the Apdu responses from
     * the PO and made available with the getCommandParser method.</li>
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
         * clear the prepared command list if processed flag is still set (no new command prepared)
         */
        if (preparedCommandsProcessed) {
            poBuilderParserList.clear();
            preparedCommandsProcessed = false;
        }

        /* create a sublist of PoBuilderParser to be sent atomically */
        List<PoBuilderParser> poAtomicCommandList = new ArrayList<PoBuilderParser>();
        for (PoBuilderParser poCommandElement : poBuilderParserList) {
            if (!(poCommandElement.getCommandBuilder() instanceof PoModificationCommand)) {
                /* This command does not affect the PO modifications buffer */
                poAtomicCommandList.add(poCommandElement);
            } else {
                /* This command affects the PO modifications buffer */
                if (willOverflowBuffer(
                        (PoModificationCommand) poCommandElement.getCommandBuilder())) {
                    if (currentModificationMode == ModificationMode.ATOMIC) {
                        throw new IllegalStateException(
                                "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                                        + poCommandElement.getCommandBuilder().toString());
                    }
                    SeResponse seResponseOpening =
                            processAtomicOpening(currentAccessLevel, openingSfiToSelect,
                                    localOpeningRecordNumberToRead, poAtomicCommandList);

                    /*
                     * inhibit record reading for next round, keep file selection (TODO check this)
                     */
                    localOpeningRecordNumberToRead = (byte) 0x00;

                    if (!createResponseParsers(seResponseOpening, poBuilderParserList)) {
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
                    poAtomicCommandList.clear();
                    poAtomicCommandList.add(poCommandElement);
                    /*
                     * just update modifications buffer usage counter, ignore result (always false)
                     */
                    willOverflowBuffer(
                            (PoModificationCommand) poCommandElement.getCommandBuilder());
                } else {
                    /*
                     * The command fits in the PO modifications buffer, just add it to the list
                     */
                    poAtomicCommandList.add(poCommandElement);
                }
            }
        }

        SeResponse seResponseOpening = processAtomicOpening(currentAccessLevel, openingSfiToSelect,
                localOpeningRecordNumberToRead, poAtomicCommandList);

        if (!createResponseParsers(seResponseOpening, poAtomicCommandList)) {
            poProcessSuccess = false;
        }

        /* sets the flag indicating that the commands have been executed */
        preparedCommandsProcessed = true;

        return poProcessSuccess;
    }

    /**
     * Process all prepared PO commands (outside a Secure Session).
     * <ul>
     * <li>On the PO reader, generates a SeRequest with channelState set to the provided value and
     * ApduRequests containing the PO commands.</li>
     * <li>All parsers keept by the prepare command methods are updated with the Apdu responses from
     * the PO and made available with the getCommandParser method.</li>
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
        if (sessionState == SessionState.SESSION_OPEN) {
            throw new IllegalStateException("A session is open");
        }

        boolean poProcessSuccess = true;

        /* PO commands sent outside a Secure Session. No modifications buffer limitation. */
        SeResponse seResponsePoCommands =
                processAtomicPoCommands(poBuilderParserList, channelState);

        if (!createResponseParsers(seResponsePoCommands, poBuilderParserList)) {
            poProcessSuccess = false;
        }

        /* sets the flag indicating that the commands have been executed */
        preparedCommandsProcessed = true;

        return poProcessSuccess;
    }

    /**
     * Process all prepared PO commands in a Secure Session.
     * <ul>
     * <li>On the PO reader, generates a SeRequest with channelState set to KEEP_OPEN, and
     * ApduRequests containing the PO commands.</li>
     * <li>In case the secure session is active, the "cache" of SAM commands is completed with the
     * corresponding Digest Update commands.</li>
     * <li>All parsers keept by the prepare command methods are updated with the Apdu responses from
     * the PO and made available with the getCommandParser method.</li>
     * </ul>
     *
     * @return true if all commands are successful
     *
     * @throws KeypleReaderException IO Reader exception
     */
    public boolean processPoCommandsInSession() throws KeypleReaderException {

        /** This method should be called only if a session was previously open */
        if (sessionState != SessionState.SESSION_OPEN) {
            throw new IllegalStateException("No open session");
        }

        /*
         * clear the prepared command list if processed flag is still set (no new command prepared)
         */
        if (preparedCommandsProcessed) {
            poBuilderParserList.clear();
            preparedCommandsProcessed = false;
        }

        boolean poProcessSuccess = true;

        /* A session is open, we have to care about the PO modifications buffer */
        List<PoBuilderParser> poAtomicBuilderParserList = new ArrayList<PoBuilderParser>();

        for (PoBuilderParser poBuilderParser : this.poBuilderParserList) {
            if (!(poBuilderParser.getCommandBuilder() instanceof PoModificationCommand)) {
                /* This command does not affect the PO modifications buffer */
                poAtomicBuilderParserList.add(poBuilderParser);
            } else {
                /* This command affects the PO modifications buffer */
                if (willOverflowBuffer(
                        ((PoModificationCommand) poBuilderParser.getCommandBuilder()))) {
                    if (currentModificationMode == ModificationMode.ATOMIC) {
                        throw new IllegalStateException(
                                "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                                        + poBuilderParser.getCommandBuilder().toString());
                    }
                    /*
                     * The current command would overflow the modifications buffer in the PO. We
                     * send the current commands and update the parsers. The parsers Iterator is
                     * kept all along the process.
                     */
                    SeResponse seResponsePoCommands = processAtomicPoCommands(
                            poAtomicBuilderParserList, ChannelState.KEEP_OPEN);
                    if (!createResponseParsers(seResponsePoCommands, poAtomicBuilderParserList)) {
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
                    poAtomicBuilderParserList.clear();
                    poAtomicBuilderParserList.add(poBuilderParser);
                    /*
                     * just update modifications buffer usage counter, ignore result (always false)
                     */
                    willOverflowBuffer((PoModificationCommand) poBuilderParser.getCommandBuilder());
                } else {
                    /*
                     * The command fits in the PO modifications buffer, just add it to the list
                     */
                    poAtomicBuilderParserList.add(poBuilderParser);
                }
            }
        }

        if (!poAtomicBuilderParserList.isEmpty()) {
            SeResponse seResponsePoCommands =
                    processAtomicPoCommands(poAtomicBuilderParserList, ChannelState.KEEP_OPEN);
            if (!createResponseParsers(seResponsePoCommands, poAtomicBuilderParserList)) {
                poProcessSuccess = false;
            }
        }

        /* sets the flag indicating that the commands have been executed */
        preparedCommandsProcessed = true;

        return poProcessSuccess;
    }

    /**
     * Sends the currently prepared commands list (may be empty) and closes the Secure Session.
     * <ul>
     * <li>The ratification is handled according to the communication mode.</li>
     * <li>The logical channel can be left open or closed.</li>
     * <li>All parsers keept by the prepare command methods are updated with the Apdu responses from
     * the PO and made available with the getCommandParser method.</li>
     * </ul>
     *
     * <p>
     * The communication mode is retrieved from CalypsoPO to manage the ratification process. If the
     * communication mode is CONTACTLESS, a ratification command will be generated and sent to the
     * PO after the Close Session command; the ratification will not be requested in the Close
     * Session command. On the contrary, if the communication mode is CONTACTS, no ratification
     * command will be sent to the PO and ratification will be requested in the Close Session
     * command
     * 
     * @param channelState indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @return true if all commands are successful
     * @throws KeypleReaderException the IO reader exception This method is deprecated.
     *         <ul>
     *         <li>The argument of the ratification command is replaced by an indication of the PO
     *         communication mode.</li>
     *         </ul>
     */
    public boolean processClosing(ChannelState channelState) throws KeypleReaderException {
        boolean poProcessSuccess = true;
        boolean atLeastOneReadCommand = false;
        boolean sessionPreviouslyClosed = false;

        /*
         * clear the prepared command list if processed flag is still set (no new command prepared)
         */
        if (preparedCommandsProcessed) {
            poBuilderParserList.clear();
            preparedCommandsProcessed = false;
        }

        List<PoModificationCommand> poModificationCommandList =
                new ArrayList<PoModificationCommand>();
        List<PoBuilderParser> poAtomicBuilderParserList = new ArrayList<PoBuilderParser>();
        SeResponse seResponseClosing;
        for (PoBuilderParser poBuilderParser : poBuilderParserList) {
            if (!(poBuilderParser instanceof PoModificationCommand)) {
                /*
                 * This command does not affect the PO modifications buffer. We will call
                 * processPoCommands first
                 */
                poAtomicBuilderParserList.add(poBuilderParser);
                atLeastOneReadCommand = true;
            } else {
                /* This command affects the PO modifications buffer */
                if (willOverflowBuffer(
                        (PoModificationCommand) poBuilderParser.getCommandBuilder())) {
                    if (currentModificationMode == ModificationMode.ATOMIC) {
                        throw new IllegalStateException(
                                "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                                        + poBuilderParser.getCommandBuilder().toString());
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
                        List<PoBuilderParser> poBuilderParsers = new ArrayList<PoBuilderParser>();
                        poBuilderParsers.addAll(poAtomicBuilderParserList);
                        seResponseClosing =
                                processAtomicPoCommands(poBuilderParsers, ChannelState.KEEP_OPEN);
                        atLeastOneReadCommand = false;
                    } else {
                        /* All commands in the list are 'modifying' */
                        seResponseClosing = processAtomicClosing(poAtomicBuilderParserList,
                                TransmissionMode.CONTACTS, ChannelState.KEEP_OPEN);
                        resetModificationsBufferCounter();
                        sessionPreviouslyClosed = true;
                    }

                    if (!createResponseParsers(seResponseClosing, poAtomicBuilderParserList)) {
                        poProcessSuccess = false;
                    }
                    /*
                     * Clear the list and add the command that did not fit in the PO modifications
                     * buffer. We also update the usage counter without checking the result.
                     */
                    poAtomicBuilderParserList.clear();
                    poAtomicBuilderParserList.add(poBuilderParser);
                    /*
                     * just update modifications buffer usage counter, ignore result (always false)
                     */
                    willOverflowBuffer((PoModificationCommand) poBuilderParser.getCommandBuilder());
                } else {
                    /*
                     * The command fits in the PO modifications buffer, just add it to the list
                     */
                    poAtomicBuilderParserList.add(poBuilderParser);
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
        seResponseClosing = processAtomicClosing(poAtomicBuilderParserList,
                calypsoPo.getTransmissionMode(), channelState);

        /* Update parsers */
        if (!createResponseParsers(seResponseClosing, poAtomicBuilderParserList)) {
            poProcessSuccess = false;
        }

        /* sets the flag indicating that the commands have been executed */
        preparedCommandsProcessed = true;

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

        /* sets the flag indicating that the commands have been executed */
        preparedCommandsProcessed = true;

        /*
         * session is now considered closed regardless the previous state or the result of the abort
         * session command sent to the PO.
         */
        sessionState = SessionState.SESSION_CLOSED;

        /* return the successful status of the abort session command */
        return poSeResponse.getApduResponses().get(0).isSuccessful();
    }

    /**
     * Loops on the SeResponse and create the appropriate builders
     * 
     * @param seResponse the seResponse from the PO
     * @param poBuilderParsers the list of {@link PoBuilderParser} (sublist of the global list)
     * @return false if one or more of the commands do not succeed
     */
    private boolean createResponseParsers(SeResponse seResponse,
            List<PoBuilderParser> poBuilderParsers) {
        boolean allSuccessfulCommands = true;
        Iterator<PoBuilderParser> commandIterator = poBuilderParsers.iterator();
        /* double loop to set apdu responses to corresponding parsers */
        for (ApduResponse apduResponse : seResponse.getApduResponses()) {
            if (!commandIterator.hasNext()) {
                throw new IllegalStateException("Commands list and responses list mismatch! ");
            }
            PoBuilderParser poBuilderParser = commandIterator.next();
            poBuilderParser.setResponseParser((AbstractPoResponseParser) (poBuilderParser
                    .getCommandBuilder().createResponseParser(apduResponse)));
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
     * Manage the builders and parsers lists.
     * <p>
     * Handle the clearing of the lists.
     */
    private int createAndStoreCommandBuilder(AbstractPoCommandBuilder commandBuilder) {
        /* reset the list when preparing the first command after last processing */
        if (preparedCommandsProcessed) {
            poBuilderParserList.clear();
            preparedCommandsProcessed = false;
            preparedCommandIndex = 0;
        }
        poBuilderParserList.add(new PoBuilderParser(commandBuilder));

        /* return and post-increment index */
        preparedCommandIndex++;
        return (preparedCommandIndex - 1);
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     * <p>
     *
     * @param path path from the CURRENT_DF (CURRENT_DF identifier excluded)
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return the command index (input order, starting at 0)
     */
    public int prepareSelectFileCmd(byte[] path, String extraInfo) {

        if (logger.isTraceEnabled()) {
            logger.trace("Select File: PATH = {}", ByteArrayUtil.toHex(path));
        }

        /*
         * create and keep the PoBuilderParser, return the command index
         */

        return createAndStoreCommandBuilder(new SelectFileCmdBuild(calypsoPo.getPoClass(), path));
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     * <p>
     *
     * @param selectControl provides the navigation case: FIRST, NEXT or CURRENT
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return the command index (input order, starting at 0)
     */
    public int prepareSelectFileCmd(SelectFileCmdBuild.SelectControl selectControl,
            String extraInfo) {
        if (logger.isTraceEnabled()) {
            logger.trace("Navigate: CONTROL = {}", selectControl);
        }

        /*
         * create and keep the PoBuilderParser, return the command index
         */

        return createAndStoreCommandBuilder(
                new SelectFileCmdBuild(calypsoPo.getPoClass(), selectControl));
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
     * @return the command index (input order, starting at 0)
     * @throws IllegalArgumentException - if record number &lt; 1
     * @throws IllegalArgumentException - if the request is inconsistent
     */
    private int prepareReadRecordsCmdInternal(byte sfi, ReadDataStructure readDataStructureEnum,
            byte firstRecordNumber, int expectedLength, String extraInfo) {

        /*
         * the readJustOneRecord flag is set to false only in case of multiple read records, in all
         * other cases it is set to true
         */
        boolean readJustOneRecord =
                !(readDataStructureEnum == ReadDataStructure.MULTIPLE_RECORD_DATA);

        /*
         * create and keep the PoBuilderParser, return the command index
         */

        return createAndStoreCommandBuilder(
                new ReadRecordsCmdBuild(calypsoPo.getPoClass(), sfi, readDataStructureEnum,
                        firstRecordNumber, readJustOneRecord, (byte) expectedLength, extraInfo));
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
     * @return the command index (input order, starting at 0)
     * @throws IllegalArgumentException - if record number &lt; 1
     * @throws IllegalArgumentException - if the request is inconsistent
     */
    public int prepareReadRecordsCmd(byte sfi, ReadDataStructure readDataStructureEnum,
            byte firstRecordNumber, int expectedLength, String extraInfo) {
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
     * @return the command index (input order, starting at 0)
     * @throws IllegalArgumentException - if record number &lt; 1
     * @throws IllegalArgumentException - if the request is inconsistent
     */
    public int prepareReadRecordsCmd(byte sfi, ReadDataStructure readDataStructureEnum,
            byte firstRecordNumber, String extraInfo) {
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
     * @return the command index (input order, starting at 0)
     * @throws IllegalArgumentException - if the command is inconsistent
     */
    public int prepareAppendRecordCmd(byte sfi, byte[] newRecordData, String extraInfo) {
        /*
         * create and keep the PoBuilderParser, return the command index
         */

        return createAndStoreCommandBuilder(
                new AppendRecordCmdBuild(calypsoPo.getPoClass(), sfi, newRecordData, extraInfo));
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
     * @return the command index (input order, starting at 0)
     * @throws IllegalArgumentException - if record number is &lt; 1
     * @throws IllegalArgumentException - if the request is inconsistent
     */
    public int prepareUpdateRecordCmd(byte sfi, byte recordNumber, byte[] newRecordData,
            String extraInfo) {
        /*
         * create and keep the PoBuilderParser, return the command index
         */

        return createAndStoreCommandBuilder(new UpdateRecordCmdBuild(calypsoPo.getPoClass(), sfi,
                recordNumber, newRecordData, extraInfo));
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
     * @return the command index (input order, starting at 0)
     * @throws IllegalArgumentException - if the decrement value is out of range
     * @throws IllegalArgumentException - if the command is inconsistent
     */
    public int prepareIncreaseCmd(byte sfi, byte counterNumber, int incValue, String extraInfo) {

        /*
         * create and keep the PoBuilderParser, return the command index
         */

        return createAndStoreCommandBuilder(new IncreaseCmdBuild(calypsoPo.getPoClass(), sfi,
                counterNumber, incValue, extraInfo));
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
     * @return the command index (input order, starting at 0)
     * @throws IllegalArgumentException - if the decrement value is out of range
     * @throws IllegalArgumentException - if the command is inconsistent
     */
    public int prepareDecreaseCmd(byte sfi, byte counterNumber, int decValue, String extraInfo) {

        /*
         * create and keep the PoBuilderParser, return the command index
         */

        return createAndStoreCommandBuilder(new DecreaseCmdBuild(calypsoPo.getPoClass(), sfi,
                counterNumber, decValue, extraInfo));
    }

    /**
     * Get the response parser matching the prepared command for which the index is provided
     * 
     * @param commandIndex the index of the parser to be retrieved
     * @return the corresponding command parser
     */
    public AbstractApduResponseParser getResponseParser(int commandIndex) {
        if (commandIndex >= poBuilderParserList.size()) {
            throw new IllegalArgumentException(
                    String.format("Bad command index: index = %d, number of commands = %d",
                            commandIndex, poBuilderParserList.size()));
        }
        return poBuilderParserList.get(commandIndex).getResponseParser();
    }
}

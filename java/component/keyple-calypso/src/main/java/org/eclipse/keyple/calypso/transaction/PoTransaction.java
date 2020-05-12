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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.keyple.calypso.SelectFileControl;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.PoCustomCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.AppendRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.DecreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.IncreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.UpdateRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.WriteRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.CloseSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.command.po.parser.security.AbstractOpenSessionRespPars;
import org.eclipse.keyple.calypso.command.po.parser.security.CloseSessionRespPars;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoDesynchronizedExchangesException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoIOException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionIllegalStateException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoUnauthorizedKvcException;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.Assert;
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

    // private constants
    private static final int OFFSET_CLA = 0;
    private static final int OFFSET_INS = 1;
    private static final int OFFSET_P1 = 2;
    private static final int OFFSET_P2 = 3;
    private static final int OFFSET_LC = 4;
    private static final int OFFSET_DATA = 5;

    /**
     * commands that modify the content of the PO in session have a cost on the session buffer equal
     * to the length of the outgoing data plus 6 bytes
     */
    private static final int SESSION_BUFFER_CMD_ADDITIONAL_COST = 6;

    /** Ratification command APDU for rev <= 2.4 */
    private static final byte[] RATIFICATION_CMD_APDU_LEGACY = ByteArrayUtil.fromHex("94B2000000");
    /** Ratification command APDU for rev > 2.4 */
    private static final byte[] RATIFICATION_CMD_APDU = ByteArrayUtil.fromHex("00B2000000");

    private static final Logger logger = LoggerFactory.getLogger(PoTransaction.class);

    /** The reader for PO. */
    private final ProxyReader poReader;
    /** The PO security settings used to manage the secure session */
    private PoSecuritySettings poSecuritySettings;
    /** The SAM commands processor */
    private SamCommandProcessor samCommandProcessor;
    /** The current CalypsoPo */
    private final CalypsoPo calypsoPo;
    /** the type of the notified event. */
    private SessionState sessionState;
    /** The current secure session access level: PERSO, RELOAD, DEBIT */
    private SessionSetting.AccessLevel currentAccessLevel;
    /** modifications counter management */
    private int modificationsCounter;

    private final PoCommandManager poCommandManager;

    /**
     * PoTransaction with PO and SAM readers.
     * <ul>
     * <li>Logical channels with PO &amp; SAM could already be established or not.</li>
     * <li>A list of SAM parameters is provided as en EnumMap.</li>
     * </ul>
     *
     * @param poResource the PO resource (combination of {@link SeReader} and {@link CalypsoPo})
     * @param samResource the SAM resource (combination of {@link SeReader} and {@link CalypsoSam})
     * @param poSecuritySettings a list of security settings ({@link PoSecuritySettings}) used in
     *        the session (such as key identification)
     */
    public PoTransaction(PoResource poResource, SamResource samResource,
            PoSecuritySettings poSecuritySettings) {

        this(poResource);

        this.poSecuritySettings = poSecuritySettings;

        samCommandProcessor = new SamCommandProcessor(samResource, poResource, poSecuritySettings);
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

        modificationsCounter = calypsoPo.getModificationsCounter();

        sessionState = SessionState.SESSION_UNINITIALIZED;

        poCommandManager = new PoCommandManager();
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
     * <li>for the current selected PO AID, with channelControl set to KEEP_OPEN,</li>
     * <li>and some PO Apdu Requests including at least the Open Session command and optionally some
     * PO command to operate inside the session.</li>
     * </ul>
     * </li>
     * <li>The session PO keyset reference is identified from the PO Open Session response, the PO
     * challenge is recovered too.</li>
     * <li>According to the PO responses of Open Session and the PO commands sent inside the
     * session, a "cache" of SAM commands is filled with the corresponding Digest Init &amp; Digest
     * Update commands.</li>
     * <li>Returns the corresponding PO SeResponse (responses to poCommands).</li>
     * </ul>
     *
     * @param accessLevel access level of the session (personalization, load or debit).
     * @param poCommands the po commands inside session
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     * @throws CalypsoSamCommandException if a response from the SAM was unexpected
     */
    private void processAtomicOpening(PoTransaction.SessionSetting.AccessLevel accessLevel,
            List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands)
            throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {

        // gets the terminal challenge
        byte[] sessionTerminalChallenge = samCommandProcessor.getSessionTerminalChallenge();

        // PO ApduRequest List to hold Open Secure Session and other optional commands
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();

        // The sfi and record number to be read when the open secure session command is executed.
        // The default value is 0 (no record to read) but we will optimize the exchanges if a read
        // record command has been prepared.
        int sfi = 0;
        int recordNumber = 0;

        // Let's check if we have a read record command at the top of the command list.
        //
        // If so, then the command is withdrawn in favour of its equivalent executed at the same
        // time as the open secure session command.
        if (poCommands != null && !poCommands.isEmpty()) {
            AbstractPoCommandBuilder<? extends AbstractPoResponseParser> poCommand =
                    poCommands.get(0);
            if (poCommand.getCommandRef() == CalypsoPoCommand.READ_RECORDS
                    && ((ReadRecordsCmdBuild) poCommand)
                            .getReadMode() == ReadRecordsCmdBuild.ReadMode.ONE_RECORD) {
                sfi = ((ReadRecordsCmdBuild) poCommand).getSfi();
                recordNumber = ((ReadRecordsCmdBuild) poCommand).getFirstRecordNumber();
                poCommands.remove(0);
            }
        }

        // Build the PO Open Secure Session command
        AbstractOpenSessionCmdBuild<AbstractOpenSessionRespPars> openSessionCmdBuild =
                AbstractOpenSessionCmdBuild.create(calypsoPo.getRevision(),
                        accessLevel.getSessionKey(), sessionTerminalChallenge, sfi, recordNumber);

        // Add the resulting ApduRequest to the PO ApduRequest list
        poApduRequestList.add(openSessionCmdBuild.getApduRequest());

        // Add all optional commands to the PO ApduRequest list
        if (poCommands != null) {
            poApduRequestList.addAll(getApduRequests(poCommands));
        }

        // Create a SeRequest from the ApduRequest list, PO AID as Selector, keep channel open
        SeRequest poSeRequest = new SeRequest(poApduRequestList);

        // Transmit the commands to the PO
        SeResponse poSeResponse;
        try {
            poSeResponse = poReader.transmit(poSeRequest);
        } catch (KeypleReaderIOException e) {
            throw new CalypsoPoIOException("PO IO Exception while transmitting commands.", e);
        }

        // Retrieve and check the ApduResponses
        List<ApduResponse> poApduResponseList = poSeResponse.getApduResponses();

        // Do some basic checks
        if (poApduRequestList.size() != poApduResponseList.size()) {
            throw new CalypsoDesynchronizedExchangesException(
                    "The number of commands/responses does not match: cmd="
                            + poApduRequestList.size() + ", resp=" + poApduResponseList.size());
        }

        // Track Read Records for later use to build anticipated responses.
        AnticipatedResponseBuilder.storeCommandResponse(poCommands, poApduRequestList,
                poApduResponseList, true);

        // Parse the response to Open Secure Session (the first item of poApduResponseList)
        AbstractOpenSessionRespPars poOpenSessionPars = (AbstractOpenSessionRespPars) CalypsoPoUtils
                .updateCalypsoPo(calypsoPo, openSessionCmdBuild, poApduResponseList.get(0));
        byte[] sessionCardChallenge = poOpenSessionPars.getPoChallenge();

        // Build the Digest Init command from PO Open Session
        // The PO KIF
        byte poKif = poOpenSessionPars.getSelectedKif();

        // The PO KVC, may be null for PO Rev 1.0
        Byte poKvc = poOpenSessionPars.getSelectedKvc();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "processAtomicOpening => opening: CARDCHALLENGE = {}, POKIF = {}, POKVC = {}",
                    ByteArrayUtil.toHex(sessionCardChallenge), String.format("%02X", poKif),
                    String.format("%02X", poKvc));
        }

        if (!poSecuritySettings.isSessionKvcAuthorized(poKvc)) {
            throw new CalypsoUnauthorizedKvcException(String.format("PO KVC = %02X", poKvc));
        }

        // Initialize the digest processor. It will store all digest operations (Digest Init, Digest
        // Update) until the session closing. At this moment, all SAM Apdu will be processed at
        // once.
        samCommandProcessor.initializeDigester(accessLevel, false, false, poKif, poKvc,
                poApduResponseList.get(0).getDataOut());

        // Add all commands data to the digest computation. The first command in the list is the
        // open secure session command. This command is not included in the digest computation, so
        // we skip it and start the loop at index 1.
        if ((poCommands != null) && !poCommands.isEmpty()) {

            for (int i = 1; i < poApduRequestList.size(); i++) { // The loop starts after the Open
                // Add requests and responses to the digest processor
                samCommandProcessor.pushPoExchangeData(poApduRequestList.get(i),
                        poApduResponseList.get(i));
            }
        }

        sessionState = SessionState.SESSION_OPEN;

        // Remove Open Secure Session response and create a new SeResponse
        poApduResponseList.remove(0);

        CalypsoPoUtils.updateCalypsoPo(calypsoPo, poCommands, poApduResponseList);
    }

    /**
     * Create an ApduRequest List from a AbstractPoCommandBuilder List.
     *
     * @param poCommands a list of PO commands
     * @return the ApduRequest list
     */
    private List<ApduRequest> getApduRequests(
            List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands) {
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        if (poCommands != null) {
            for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder : poCommands) {
                apduRequests.add(commandBuilder.getApduRequest());
            }
        }
        return apduRequests;
    }

    /**
     * Process PO commands in a Secure Session.
     * <ul>
     * <li>On the PO reader, generates a SeRequest with channelControl set to KEEP_OPEN, and
     * ApduRequests with the PO commands.</li>
     * <li>In case the secure session is active, the "cache" of SAM commands is completed with the
     * corresponding Digest Update commands.</li>
     * <li>If a session is open and channelControl is set to CLOSE_AFTER, the current PO session is
     * aborted</li>
     * <li>Returns the corresponding PO SeResponse.</li>
     * </ul>
     *
     * @param poCommands the po commands inside session
     * @param channelControl indicated if the SE channel of the PO reader must be closed after the
     *        last command
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     */
    private void processAtomicPoCommands(
            List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands,
            ChannelControl channelControl)
            throws CalypsoPoTransactionException, CalypsoPoCommandException {

        // Get the PO ApduRequest List
        List<ApduRequest> poApduRequestList = getApduRequests(poCommands);

        // Create a SeRequest from the ApduRequest list, PO AID as Selector, manage the logical
        // channel according to the channelControl enum
        SeRequest poSeRequest = new SeRequest(poApduRequestList);

        // Transmit the commands to the PO
        SeResponse poSeResponse;
        try {
            poSeResponse = poReader.transmit(poSeRequest, channelControl);
        } catch (KeypleReaderIOException e) {
            throw new CalypsoPoIOException("PO IO Exception while transmitting commands.", e);
        }

        // Retrieve and check the ApduResponses
        List<ApduResponse> poApduResponseList = poSeResponse.getApduResponses();

        // Do some basic checks
        if (poApduRequestList.size() != poApduResponseList.size()) {
            throw new CalypsoDesynchronizedExchangesException(
                    "The number of commands/responses does not match: cmd="
                            + poApduRequestList.size() + ", resp=" + poApduResponseList.size());
        }

        // Track Read Records for later use to build anticipated responses.
        AnticipatedResponseBuilder.storeCommandResponse(poCommands, poApduRequestList,
                poApduResponseList, false);

        // Add all commands data to the digest computation if this method is called within a Secure
        // Session.
        if (sessionState == SessionState.SESSION_OPEN) {
            for (int i = 0; i < poApduRequestList.size(); i++) { // The loop starts after the Open
                // Add requests and responses to the digest processor
                samCommandProcessor.pushPoExchangeData(poApduRequestList.get(i),
                        poApduResponseList.get(i));
            }
        }

        CalypsoPoUtils.updateCalypsoPo(calypsoPo, poCommands, poSeResponse.getApduResponses());
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
     * <li>Then, on the PO reader, a SeRequest is transmitted with the provided channelControl, and
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
     * @param channelControl indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     * @throws CalypsoSamCommandException if a response from the SAM was unexpected
     */
    private void processAtomicClosing(
            List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poModificationCommands,
            List<ApduResponse> poAnticipatedResponses, TransmissionMode transmissionMode,
            ChannelControl channelControl) throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {

        if (sessionState != SessionState.SESSION_OPEN) {
            throw new CalypsoPoTransactionIllegalStateException(
                    "Bad session state. Current: " + sessionState.toString() + ", expected: "
                            + SessionState.SESSION_OPEN.toString());
        }

        // Get the PO ApduRequest List - for the first PO exchange
        List<ApduRequest> poApduRequestList = getApduRequests(poModificationCommands);

        // Compute "anticipated" Digest Update (for optional poModificationCommands)
        if ((poModificationCommands != null) && !poApduRequestList.isEmpty()) {
            if (poApduRequestList.size() == poAnticipatedResponses.size()) {
                // Add all commands data to the digest computation: commands and anticipated
                // responses.
                for (int i = 0; i < poApduRequestList.size(); i++) {
                    // Add requests and responses to the digest processor
                    samCommandProcessor.pushPoExchangeData(poApduRequestList.get(i),
                            poAnticipatedResponses.get(i));
                }
            } else {
                throw new CalypsoDesynchronizedExchangesException(
                        "The number of commands/responses does not match: cmd="
                                + poApduRequestList.size() + ", resp="
                                + poAnticipatedResponses.size());
            }
        }

        // All SAM digest operations will now run at once.
        // Get Terminal Signature from the latest response
        byte[] sessionTerminalSignature = samCommandProcessor.getTerminalSignature();

        PoCustomCommandBuilder ratificationCommand;
        boolean ratificationAsked;

        if (transmissionMode == TransmissionMode.CONTACTLESS) {
            if (calypsoPo.getRevision() == PoRevision.REV2_4) {
                ratificationCommand = new PoCustomCommandBuilder("Ratification command",
                        new ApduRequest(RATIFICATION_CMD_APDU_LEGACY, false));
            } else {
                ratificationCommand = new PoCustomCommandBuilder("Ratification command",
                        new ApduRequest(RATIFICATION_CMD_APDU, false));
            }
            // Ratification is done by the ratification command above so is not requested in the
            // Close Session command
            ratificationAsked = false;
        } else {
            // Ratification is requested in the Close Session command in contacts mode
            ratificationAsked = true;
            ratificationCommand = null;
        }

        // Build the PO Close Session command. The last one for this session
        CloseSessionCmdBuild closeSessionCmdBuild = new CloseSessionCmdBuild(calypsoPo.getPoClass(),
                ratificationAsked, sessionTerminalSignature);

        poApduRequestList.add(closeSessionCmdBuild.getApduRequest());

        // Keep the position of the Close Session command in request list
        int closeCommandIndex = poApduRequestList.size() - 1;

        // Add the PO Ratification command if any
        if (ratificationCommand != null) {
            poApduRequestList.add(ratificationCommand.getApduRequest());
        }

        // Transfer PO commands
        SeRequest poSeRequest = new SeRequest(poApduRequestList);

        SeResponse poSeResponse;
        try {
            poSeResponse = poReader.transmit(poSeRequest, channelControl);
        } catch (KeypleReaderIOException ex) {
            poSeResponse = ex.getSeResponse();
            // The current exception may have been caused by a communication issue with the PO
            // during the ratification command.
            //
            // In this case, we do not stop the process and consider the Secure Session close. We'll
            // check the signature.
            //
            // We should have one response less than requests.
            if (ratificationAsked || poSeResponse == null
                    || poSeResponse.getApduResponses().size() != poApduRequestList.size() - 1) {
                throw new CalypsoPoIOException("PO IO Exception while transmitting commands.", ex);
            }
        }

        List<ApduResponse> poApduResponseList = poSeResponse.getApduResponses();

        // TODO add support of poRevision parameter to CloseSessionRespPars for REV2.4 PO CLAss byte
        // before last if ratification, otherwise last one
        CloseSessionRespPars poCloseSessionPars = closeSessionCmdBuild
                .createResponseParser(poApduResponseList.get(closeCommandIndex));

        poCloseSessionPars.checkStatus();

        samCommandProcessor.authenticatePoSignature(poCloseSessionPars.getSignatureLo());

        sessionState = SessionState.SESSION_CLOSED;

        // Remove ratification response if any
        if (!ratificationAsked) {
            poApduResponseList.remove(poApduResponseList.size() - 1);
        }
        // Remove Close Secure Session response and create a new SeResponse
        poApduResponseList.remove(poApduResponseList.size() - 1);

        CalypsoPoUtils.updateCalypsoPo(calypsoPo, poModificationCommands, poApduResponseList);
    }

    /**
     * Advanced variant of processAtomicClosing in which the list of expected responses is
     * determined from previous reading operations.
     *
     * @param poCommands a list of commands that can modify the PO memory content
     * @param transmissionMode the communication mode. If the communication mode is CONTACTLESS, a
     *        ratification command will be generated and sent to the PO after the Close Session
     *        command; the ratification will not be requested in the Close Session command. On the
     *        contrary, if the communication mode is CONTACTS, no ratification command will be sent
     *        to the PO and ratification will be requested in the Close Session command
     * @param channelControl indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     * @throws CalypsoSamCommandException if a response from the SAM was unexpected
     */
    private void processAtomicClosing(
            List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands,
            TransmissionMode transmissionMode, ChannelControl channelControl)
            throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {
        List<ApduResponse> poAnticipatedResponses =
                AnticipatedResponseBuilder.getResponses(poCommands);
        processAtomicClosing(poCommands, poAnticipatedResponses, transmissionMode, channelControl);
    }

    public static class SessionSetting {
        /**
         * The modification mode indicates whether the secure session can be closed and reopened to
         * manage the limitation of the PO buffer memory.
         */
        public enum ModificationMode {
            /**
             * The secure session is atomic. The consistency of the content of the resulting PO
             * memory is guaranteed.
             */
            ATOMIC,
            /**
             * Several secure sessions can be chained (to manage the writing of large amounts of
             * data). The resulting content of the PO's memory can be inconsistent if the PO is
             * removed during the process.
             */
            MULTIPLE
        }

        /**
         * The PO Transaction Access Level: personalization, loading or debiting.
         */
        public enum AccessLevel {

            /** Session Access Level used for personalization purposes. */
            SESSION_LVL_PERSO("perso", (byte) 0x01),
            /** Session Access Level used for reloading purposes. */
            SESSION_LVL_LOAD("load", (byte) 0x02),
            /** Session Access Level used for validating and debiting purposes. */
            SESSION_LVL_DEBIT("debit", (byte) 0x03);

            private final String name;
            private final byte sessionKey;

            AccessLevel(String name, byte sessionKey) {
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
         * The ratification mode defines the behavior of processClosing regarding the ratification
         * process.
         */
        public enum RatificationMode {
            CLOSE_RATIFIED, CLOSE_NOT_RATIFIED
        }
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
         * @param poCommands the list of commands sent to the PO
         * @param apduRequests the sent apduRequests
         * @param apduResponses the received apduResponses
         * @param skipFirstItem a flag to indicate if the first apduRequest/apduResponse pair has to
         *        be ignored or not.
         */
        private static void storeCommandResponse(
                List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands,
                List<ApduRequest> apduRequests, List<ApduResponse> apduResponses,
                boolean skipFirstItem) {
            if (poCommands != null) {
                // Store Read Records' requests and responses for later use to build anticipated
                // responses.
                Iterator<ApduRequest> apduRequestIterator = apduRequests.iterator();
                Iterator<ApduResponse> apduResponseIterator = apduResponses.iterator();
                if (skipFirstItem) {
                    // case of processAtomicOpening
                    apduRequestIterator.next();
                    apduResponseIterator.next();
                }
                // Iterate over the poCommandsInsideSession list
                for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder : poCommands) {
                    if (commandBuilder instanceof ReadRecordsCmdBuild) {
                        ApduRequest apduRequest = apduRequestIterator.next();
                        // TODO improve this ugly code
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
         * @param poCommands the modification command list
         * @return the anticipated responses.
         * @throws CalypsoPoTransactionIllegalStateException if an response can't be determined.
         */
        private static List<ApduResponse> getResponses(
                List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands)
                throws CalypsoPoTransactionIllegalStateException {
            List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
            if (poCommands != null) {
                for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder : poCommands) {
                    if (commandBuilder instanceof DecreaseCmdBuild
                            || commandBuilder instanceof IncreaseCmdBuild) {
                        // response = NNNNNN9000
                        byte[] modCounterApduRequest = commandBuilder.getApduRequest().getBytes();
                        // Retrieve SFI from the current Decrease command
                        byte sfi = (byte) ((modCounterApduRequest[OFFSET_P2] >> 3) & 0x1F);
                        // Look for the counter value in the stored records. Only the first
                        // occurrence of the SFI is taken into account. We assume here that the
                        // record number is always 1.
                        CommandResponse commandResponse = sfiCommandResponseHashMap.get(sfi);
                        if (commandResponse != null) {
                            byte counterNumber = modCounterApduRequest[OFFSET_P1];
                            // The record containing the counters is structured as follow:
                            // AAAAAAABBBBBBCCCCCC...XXXXXX each counter being a 3-byte unsigned
                            // number. Convert the 3-byte block indexed by the counter number to an
                            // int.
                            int currentCounterValue = ByteArrayUtil.threeBytesToInt(
                                    commandResponse.getApduResponse().getBytes(),
                                    (counterNumber - 1) * 3);
                            // Extract the add or subtract value from the modification request
                            int addSubtractValue = ByteArrayUtil
                                    .threeBytesToInt(modCounterApduRequest, OFFSET_DATA);
                            // Build the response
                            byte[] response = new byte[5];
                            int newCounterValue;
                            if (commandBuilder instanceof DecreaseCmdBuild) {
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
                                        (commandBuilder instanceof DecreaseCmdBuild) ? "Decrease"
                                                : "Increase",
                                        sfi, currentCounterValue, addSubtractValue,
                                        newCounterValue);
                            }
                        } else {
                            throw new CalypsoPoTransactionIllegalStateException(
                                    "Anticipated response. COMMAND = "
                                            + ((commandBuilder instanceof DecreaseCmdBuild)
                                                    ? "Decrease"
                                                    : "Increase")
                                            + ". Unable to determine anticipated counter value. SFI = "
                                            + sfi);
                        }
                    } else {
                        // Append/Update/Write Record: response = 9000
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
     * <li>for the currently selected PO, with channelControl set to KEEP_OPEN,</li>
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
     * @param accessLevel access level of the session (personalization, load or debit).
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     * @throws CalypsoSamCommandException if a response from the SAM was unexpected
     */
    public void processOpening(PoTransaction.SessionSetting.AccessLevel accessLevel)
            throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {
        currentAccessLevel = accessLevel;

        // create a sublist of AbstractPoCommandBuilder to be sent atomically
        List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poAtomicCommandList =
                new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();
        for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder : poCommandManager
                .getPoCommandBuilderList()) {
            if (!commandBuilder.isSessionBufferUsed()) {
                // This command does not affect the PO modifications buffer
                poAtomicCommandList.add(commandBuilder);
            } else {
                // This command affects the PO modifications buffer
                int neededSessionBufferSpace = commandBuilder.getApduRequest().getBytes().length
                        + SESSION_BUFFER_CMD_ADDITIONAL_COST;
                if (isSessionBufferOverflowed(neededSessionBufferSpace)) {
                    if (poSecuritySettings
                            .getSessionModificationMode() == SessionSetting.ModificationMode.ATOMIC) {
                        throw new CalypsoPoTransactionIllegalStateException(
                                "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                                        + commandBuilder.toString());
                    }

                    // Closes the session, resets the modifications buffer counters for the next
                    // round (set the contact mode to avoid the transmission of the ratification)
                    processAtomicClosing(null, TransmissionMode.CONTACTS, ChannelControl.KEEP_OPEN);
                    resetModificationsBufferCounter();
                    // Clear the list and add the command that did not fit in the PO modifications
                    // buffer. We also update the usage counter without checking the result.
                    poAtomicCommandList.clear();
                    poAtomicCommandList.add(commandBuilder);
                    // just update modifications buffer usage counter, ignore result (always false)
                    isSessionBufferOverflowed(neededSessionBufferSpace);
                } else {
                    // The command fits in the PO modifications buffer, just add it to the list
                    poAtomicCommandList.add(commandBuilder);
                }
            }
        }

        processAtomicOpening(currentAccessLevel, poAtomicCommandList);

        // sets the flag indicating that the commands have been executed
        poCommandManager.notifyCommandsProcessed();
    }

    /**
     * Process all prepared PO commands (outside a Secure Session).
     * <ul>
     * <li>On the PO reader, generates a SeRequest with channelControl set to the provided value and
     * ApduRequests containing the PO commands.</li>
     * <li>All parsers keept by the prepare command methods are updated with the Apdu responses from
     * the PO and made available with the getCommandParser method.</li>
     * </ul>
     *
     * @param channelControl indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     */
    public void processPoCommands(ChannelControl channelControl)
            throws CalypsoPoTransactionException, CalypsoPoCommandException {

        /** This method should be called only if no session was previously open */
        if (sessionState == SessionState.SESSION_OPEN) {
            throw new CalypsoPoTransactionIllegalStateException("A session is open");
        }

        // PO commands sent outside a Secure Session. No modifications buffer limitation.
        processAtomicPoCommands(poCommandManager.getPoCommandBuilderList(), channelControl);

        // sets the flag indicating that the commands have been executed
        poCommandManager.notifyCommandsProcessed();
    }

    /**
     * Process all prepared PO commands in a Secure Session.
     * <ul>
     * <li>On the PO reader, generates a SeRequest with channelControl set to KEEP_OPEN, and
     * ApduRequests containing the PO commands.</li>
     * <li>In case the secure session is active, the "cache" of SAM commands is completed with the
     * corresponding Digest Update commands.</li>
     * <li>All parsers keept by the prepare command methods are updated with the Apdu responses from
     * the PO and made available with the getCommandParser method.</li>
     * </ul>
     *
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     * @throws CalypsoSamCommandException if a response from the SAM was unexpected
     */
    public void processPoCommandsInSession() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {

        /** This method should be called only if a session was previously open */
        if (sessionState != SessionState.SESSION_OPEN) {
            throw new CalypsoPoTransactionIllegalStateException("No open session");
        }

        // A session is open, we have to care about the PO modifications buffer
        List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poAtomicBuilderList =
                new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();

        for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder : poCommandManager
                .getPoCommandBuilderList()) {
            if (!commandBuilder.isSessionBufferUsed()) {
                // This command does not affect the PO modifications buffer
                poAtomicBuilderList.add(commandBuilder);
            } else {
                // This command affects the PO modifications buffer
                int neededSessionBufferSpace = commandBuilder.getApduRequest().getBytes().length
                        + SESSION_BUFFER_CMD_ADDITIONAL_COST;
                if (isSessionBufferOverflowed(neededSessionBufferSpace)) {
                    if (poSecuritySettings
                            .getSessionModificationMode() == SessionSetting.ModificationMode.ATOMIC) {
                        throw new CalypsoPoTransactionIllegalStateException(
                                "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                                        + commandBuilder.toString());
                    }
                    // The current command would overflow the modifications buffer in the PO. We
                    // send the current commands and update the parsers. The parsers Iterator is
                    // kept all along the process.
                    processAtomicPoCommands(poAtomicBuilderList, ChannelControl.KEEP_OPEN);

                    // Close the session and reset the modifications buffer counters for the next
                    // round (set the contact mode to avoid the transmission of the ratification)
                    processAtomicClosing(null, TransmissionMode.CONTACTS, ChannelControl.KEEP_OPEN);
                    resetModificationsBufferCounter();
                    // We reopen a new session for the remaining commands to be sent
                    processAtomicOpening(currentAccessLevel, null);
                    // Clear the list and add the command that did not fit in the PO modifications
                    // buffer. We also update the usage counter without checking the result.
                    poAtomicBuilderList.clear();
                    poAtomicBuilderList.add(commandBuilder);
                    // just update modifications buffer usage counter, ignore result (always false)
                    isSessionBufferOverflowed(neededSessionBufferSpace);
                } else {
                    // The command fits in the PO modifications buffer, just add it to the list
                    poAtomicBuilderList.add(commandBuilder);
                }
            }
        }

        if (!poAtomicBuilderList.isEmpty()) {
            processAtomicPoCommands(poAtomicBuilderList, ChannelControl.KEEP_OPEN);
        }

        // sets the flag indicating that the commands have been executed
        poCommandManager.notifyCommandsProcessed();
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
     * @param channelControl indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     * @throws CalypsoSamCommandException if a response from the SAM was unexpected
     */
    public void processClosing(ChannelControl channelControl) throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {
        boolean atLeastOneReadCommand = false;
        boolean sessionPreviouslyClosed = false;

        List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poAtomicBuilderList =
                new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();
        for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder : poCommandManager
                .getPoCommandBuilderList()) {
            if (!commandBuilder.isSessionBufferUsed()) {
                // This command does not affect the PO modifications buffer. We will call
                // processPoCommands first
                poAtomicBuilderList.add(commandBuilder);
                atLeastOneReadCommand = true;
            } else {
                // This command affects the PO modifications buffer
                int neededSessionBufferSpace = commandBuilder.getApduRequest().getBytes().length
                        + SESSION_BUFFER_CMD_ADDITIONAL_COST;
                if (isSessionBufferOverflowed(neededSessionBufferSpace)) {
                    if (poSecuritySettings
                            .getSessionModificationMode() == SessionSetting.ModificationMode.ATOMIC) {
                        throw new CalypsoPoTransactionIllegalStateException(
                                "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                                        + commandBuilder.toString());
                    }
                    // Reopen a session with the same access level if it was previously closed in
                    // this current processClosing
                    if (sessionPreviouslyClosed) {
                        processAtomicOpening(currentAccessLevel, null);
                    }

                    // If at least one non-modifying was prepared, we use processAtomicPoCommands
                    // instead of processAtomicClosing to send the list
                    if (atLeastOneReadCommand) {
                        List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands =
                                new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();
                        poCommands.addAll(poAtomicBuilderList);
                        processAtomicPoCommands(poCommands, ChannelControl.KEEP_OPEN);
                        atLeastOneReadCommand = false;
                    } else {
                        // All commands in the list are 'modifying'
                        processAtomicClosing(poAtomicBuilderList, TransmissionMode.CONTACTS,
                                ChannelControl.KEEP_OPEN);
                        resetModificationsBufferCounter();
                        sessionPreviouslyClosed = true;
                    }

                    // Clear the list and add the command that did not fit in the PO modifications
                    // buffer. We also update the usage counter without checking the result.
                    poAtomicBuilderList.clear();
                    poAtomicBuilderList.add(commandBuilder);
                    // just update modifications buffer usage counter, ignore result (always false)
                    isSessionBufferOverflowed(neededSessionBufferSpace);
                } else {
                    // The command fits in the PO modifications buffer, just add it to the list
                    poAtomicBuilderList.add(commandBuilder);
                }
            }
        }
        if (sessionPreviouslyClosed) {
            // Reopen if needed, to close the session with the requested conditions
            // (CommunicationMode and channelControl)
            processAtomicOpening(currentAccessLevel, null);
        }

        // Finally, close the session as requested
        processAtomicClosing(poAtomicBuilderList, calypsoPo.getTransmissionMode(), channelControl);

        // sets the flag indicating that the commands have been executed
        poCommandManager.notifyCommandsProcessed();
    }

    /**
     * Abort a Secure Session.
     * <p>
     * Send the appropriate command to the PO
     * <p>
     * Clean up internal data and status.
     * 
     * @param channelControl indicates if the SE channel of the PO reader must be closed after the
     *        abort session command
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     */
    public void processCancel(ChannelControl channelControl)
            throws CalypsoPoTransactionException, CalypsoPoCommandException {
        // PO ApduRequest List to hold Close Secure Session command
        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();

        // Build the PO Close Session command (in "abort" mode since no signature is provided).
        CloseSessionCmdBuild closeSessionCmdBuild =
                new CloseSessionCmdBuild(calypsoPo.getPoClass());

        poApduRequestList.add(closeSessionCmdBuild.getApduRequest());

        // Transfer PO commands
        SeRequest poSeRequest = new SeRequest(poApduRequestList);

        SeResponse poSeResponse;
        try {
            poSeResponse = poReader.transmit(poSeRequest, channelControl);
            closeSessionCmdBuild.createResponseParser(poSeResponse.getApduResponses().get(0))
                    .checkStatus();
        } catch (KeypleReaderIOException ex) {
            throw new CalypsoPoIOException("PO IO Exception while transmitting commands.", ex);
        }

        // sets the flag indicating that the commands have been executed
        poCommandManager.notifyCommandsProcessed();

        // session is now considered closed regardless the previous state or the result of the abort
        // session command sent to the PO.
        sessionState = SessionState.SESSION_CLOSED;
    }

    /**
     * Checks whether the requirement for the modifications buffer of the command provided in
     * argument is compatible with the current usage level of the buffer.
     * <p>
     * If it is compatible, the requirement is subtracted from the current level and the method
     * returns false. If this is not the case, the method returns true and the current level is left
     * unchanged.
     * 
     * @param sessionBufferSizeConsumed session buffer requirement
     * @return true or false
     */
    private boolean isSessionBufferOverflowed(int sessionBufferSizeConsumed) {
        boolean isSessionBufferFull = false;
        if (calypsoPo.isModificationsCounterInBytes()) {
            if (modificationsCounter - sessionBufferSizeConsumed > 0) {
                modificationsCounter = modificationsCounter - sessionBufferSizeConsumed;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Modifications buffer overflow! BYTESMODE, CURRENTCOUNTER = {}, REQUIREMENT = {}",
                            modificationsCounter, sessionBufferSizeConsumed);
                }
                isSessionBufferFull = true;
            }
        } else {
            if (modificationsCounter > 0) {
                modificationsCounter--;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Modifications buffer overflow! COMMANDSMODE, CURRENTCOUNTER = {}, REQUIREMENT = {}",
                            modificationsCounter, 1);
                }
                isSessionBufferFull = true;
            }
        }
        return isSessionBufferFull;
    }

    /**
     * Initialized the modifications buffer counter to its maximum value for the current PO
     */
    private void resetModificationsBufferCounter() {
        if (logger.isTraceEnabled()) {
            logger.trace("Modifications buffer counter reset: PREVIOUSVALUE = {}, NEWVALUE = {}",
                    modificationsCounter, calypsoPo.getModificationsCounter());
        }
        modificationsCounter = calypsoPo.getModificationsCounter();
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     * <p>
     *
     * @param path path from the CURRENT_DF (CURRENT_DF identifier excluded)
     */
    public final void prepareSelectFile(byte[] path) {
        // create the builder and add it to the list of commands
        poCommandManager.addRegularCommand(new SelectFileCmdBuild(calypsoPo.getPoClass(), path));
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     * <p>
     *
     * @param selectControl provides the navigation case: FIRST, NEXT or CURRENT
     */
    public final void prepareSelectFile(SelectFileControl selectControl) {
        // create the builder and add it to the list of commands
        poCommandManager
                .addRegularCommand(new SelectFileCmdBuild(calypsoPo.getPoClass(), selectControl));
    }


    /**
     * Read a single record from the indicated EF
     *
     * @param sfi the SFI of the EF to read
     * @param recordNumber the record number to read
     * @throws IllegalArgumentException if one of the provided argument is out of range
     */
    public final void prepareReadRecordFile(byte sfi, int recordNumber) {
        // create the builder and add it to the list of commands
        poCommandManager.addRegularCommand(
                CalypsoPoUtils.prepareReadRecordFile(calypsoPo.getPoClass(), sfi, recordNumber));
    }

    /**
     * Read one or more records from the indicated EF
     *
     * @param sfi the SFI of the EF
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param numberOfRecords the number of records expected
     * @param recordSize the record length
     * @throws IllegalArgumentException if one of the provided argument is out of range
     */
    public final void prepareReadRecordFile(byte sfi, int firstRecordNumber, int numberOfRecords,
            int recordSize) {

        Assert.getInstance() //
                .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi") //
                .isInRange(firstRecordNumber, CalypsoPoUtils.NB_REC_MIN, CalypsoPoUtils.NB_REC_MAX,
                        "firstRecordNumber") //
                .isInRange(numberOfRecords, CalypsoPoUtils.NB_REC_MIN,
                        CalypsoPoUtils.NB_REC_MAX - firstRecordNumber, "numberOfRecords");

        if (numberOfRecords == 1) {
            // create the builder and add it to the list of commands
            poCommandManager.addRegularCommand(new ReadRecordsCmdBuild(calypsoPo.getPoClass(), sfi,
                    firstRecordNumber, ReadRecordsCmdBuild.ReadMode.ONE_RECORD, recordSize));
        } else {
            // Manages the reading of multiple records taking into account the transmission capacity
            // of the PO and the response format (2 extra bytes)
            // Multiple APDUs can be generated depending on record size and transmission capacity.
            int recordsPerApdu = calypsoPo.getPayloadCapacity() / (recordSize + 2);
            int maxSizeDataPerApdu = recordsPerApdu * (recordSize + 2);
            int remainingRecords = numberOfRecords;
            int startRecordNumber = firstRecordNumber;
            while (remainingRecords > 0) {
                int expectedLength;
                if (remainingRecords > recordsPerApdu) {
                    expectedLength = maxSizeDataPerApdu;
                    remainingRecords = remainingRecords - recordsPerApdu;
                    startRecordNumber = startRecordNumber + recordsPerApdu;
                } else {
                    expectedLength = remainingRecords * (recordSize + 2);
                    remainingRecords = 0;
                }
                // create the builder and add it to the list of commands
                poCommandManager.addRegularCommand(
                        new ReadRecordsCmdBuild(calypsoPo.getPoClass(), sfi, startRecordNumber,
                                ReadRecordsCmdBuild.ReadMode.MULTIPLE_RECORD, expectedLength));
            }
        }
    }

    /**
     * Read a record of the indicated EF, which should be a count file.
     * <p>
     * The record will be read up to the counter location indicated in parameter.<br>
     * Thus all previous counters will also be read.
     *
     * @param sfi the SFI of the EF
     * @param countersNumber the number of the last counter to be read
     * @throws IllegalArgumentException if one of the provided argument is out of range
     */
    public final void prepareReadCounterFile(byte sfi, int countersNumber) {
        prepareReadRecordFile(sfi, 1, 1, countersNumber * 3);
    }

    /**
     * Builds an AppendRecord command and add it to the list of commands to be sent with the next
     * process command.
     * <p>
     * Returns the associated response parser.
     *
     * @param sfi the sfi to select
     * @param newRecordData the new record data to write
     * @throws IllegalArgumentException - if the command is inconsistent
     */
    public final void prepareAppendRecord(byte sfi, byte[] newRecordData) {
        Assert.getInstance() //
                .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi");

        // create the builder and add it to the list of commands
        poCommandManager.addRegularCommand(
                new AppendRecordCmdBuild(calypsoPo.getPoClass(), sfi, newRecordData));
    }

    /**
     * Builds an UpdateRecord command and add it to the list of commands to be sent with the next
     * process command
     * <p>
     * Returns the associated response parser index.
     *
     * @param sfi the sfi to select
     * @param recordNumber the record number to update
     * @param newRecordData the new record data. If length &lt; RecSize, bytes beyond length are
     *        left unchanged.
     * @throws IllegalArgumentException - if record number is &lt; 1
     * @throws IllegalArgumentException - if the request is inconsistent
     */
    public final void prepareUpdateRecord(byte sfi, byte recordNumber, byte[] newRecordData) {
        Assert.getInstance() //
                .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi") //
                .isInRange((int) recordNumber, CalypsoPoUtils.NB_REC_MIN, CalypsoPoUtils.NB_REC_MAX,
                        "recordNumber");

        // create the builder and add it to the list of commands
        poCommandManager.addRegularCommand(
                new UpdateRecordCmdBuild(calypsoPo.getPoClass(), sfi, recordNumber, newRecordData));
    }


    /**
     * Builds an WriteRecord command and add it to the list of commands to be sent with the next
     * process command
     * <p>
     * Returns the associated response parser index.
     *
     * @param sfi the sfi to select
     * @param recordNumber the record number to write
     * @param overwriteRecordData the data to overwrite in the record. If length &lt; RecSize, bytes
     *        beyond length are left unchanged.
     * @throws IllegalArgumentException - if record number is &lt; 1
     * @throws IllegalArgumentException - if the request is inconsistent
     */
    public final void prepareWriteRecord(byte sfi, byte recordNumber, byte[] overwriteRecordData) {
        Assert.getInstance() //
                .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi") //
                .isInRange((int) recordNumber, CalypsoPoUtils.NB_REC_MIN, CalypsoPoUtils.NB_REC_MAX,
                        "recordNumber");

        // create the builder and add it to the list of commands
        poCommandManager.addRegularCommand(new WriteRecordCmdBuild(calypsoPo.getPoClass(), sfi,
                recordNumber, overwriteRecordData));
    }

    /**
     * Builds a Increase command and add it to the list of commands to be sent with the next process
     * command
     * <p>
     * Returns the associated response parser index.
     *
     * @param counterNumber &gt;= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param sfi SFI of the file to select or 00h for current EF
     * @param incValue Value to add to the counter (defined as a positive int &lt;= 16777215
     *        [FFFFFFh])
     * @throws IllegalArgumentException - if the decrement value is out of range
     * @throws IllegalArgumentException - if the command is inconsistent
     */
    public final void prepareIncrease(byte sfi, byte counterNumber, int incValue) {
        Assert.getInstance() //
                .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi") //
                .isInRange((int) counterNumber, CalypsoPoUtils.NB_CNT_MIN,
                        CalypsoPoUtils.NB_CNT_MAX, "counterNumber") //
                .isInRange(incValue, CalypsoPoUtils.CNT_VALUE_MIN, CalypsoPoUtils.CNT_VALUE_MAX,
                        "incValue");

        // create the builder and add it to the list of commands
        poCommandManager.addRegularCommand(
                new IncreaseCmdBuild(calypsoPo.getPoClass(), sfi, counterNumber, incValue));
    }

    /**
     * Builds a Decrease command and add it to the list of commands to be sent with the next process
     * command
     * <p>
     * Returns the associated response parser index.
     *
     * @param counterNumber &gt;= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param sfi SFI of the file to select or 00h for current EF
     * @param decValue Value to subtract to the counter (defined as a positive int &lt;= 16777215
     *        [FFFFFFh])
     * @throws IllegalArgumentException - if the decrement value is out of range
     * @throws IllegalArgumentException - if the command is inconsistent
     */
    public final void prepareDecrease(byte sfi, byte counterNumber, int decValue) {
        Assert.getInstance() //
                .isInRange((int) sfi, CalypsoPoUtils.SFI_MIN, CalypsoPoUtils.SFI_MAX, "sfi") //
                .isInRange((int) counterNumber, CalypsoPoUtils.NB_CNT_MIN,
                        CalypsoPoUtils.NB_CNT_MAX, "counterNumber") //
                .isInRange(decValue, CalypsoPoUtils.CNT_VALUE_MIN, CalypsoPoUtils.CNT_VALUE_MAX,
                        "decValue");

        // create the builder and add it to the list of commands
        poCommandManager.addRegularCommand(
                new DecreaseCmdBuild(calypsoPo.getPoClass(), sfi, counterNumber, decValue));
    }
}

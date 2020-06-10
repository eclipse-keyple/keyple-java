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

import static org.eclipse.keyple.calypso.command.po.CalypsoPoCommand.DECREASE;
import static org.eclipse.keyple.calypso.command.po.CalypsoPoCommand.INCREASE;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.keyple.calypso.SelectFileControl;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.builder.AppendRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.DecreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.IncreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.UpdateRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.WriteRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.AbstractOpenSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.CloseSessionCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.security.RatificationCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.command.po.parser.security.AbstractOpenSessionRespPars;
import org.eclipse.keyple.calypso.command.po.parser.security.CloseSessionRespPars;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoAtomicTransactionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoAuthenticationNotVerifiedException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoDesynchronizedExchangesException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoCloseSecureSessionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoIOException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionIllegalStateException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoSamIOException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoSessionAuthenticationException;
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
public class PoTransaction {

    /**
     * commands that modify the content of the PO in session have a cost on the session buffer equal
     * to the length of the outgoing data plus 6 bytes
     */
    private static final int SESSION_BUFFER_CMD_ADDITIONAL_COST = 6;
    private static final int APDU_HEADER_LENGTH = 5;

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
     * @param poSecuritySettings a list of security settings ({@link PoSecuritySettings}) used in
     *        the session (such as key identification)
     */
    public PoTransaction(PoResource poResource, PoSecuritySettings poSecuritySettings) {

        this(poResource);

        this.poSecuritySettings = poSecuritySettings;

        samCommandProcessor = new SamCommandProcessor(poResource, poSecuritySettings);
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

        /** This method should be called only if no session was previously open */
        checkSessionIsNotOpen();

        if (poSecuritySettings == null) {
            throw new CalypsoPoTransactionIllegalStateException("No SAM resource is available");
        }

        // gets the terminal challenge
        byte[] sessionTerminalChallenge = samCommandProcessor.getSessionTerminalChallenge();

        // PO ApduRequest List to hold Open Secure Session and other optional commands
        List<ApduRequest> poApduRequests = new ArrayList<ApduRequest>();

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
        poApduRequests.add(openSessionCmdBuild.getApduRequest());

        // Add all optional commands to the PO ApduRequest list
        if (poCommands != null) {
            poApduRequests.addAll(getApduRequests(poCommands));
        }

        // Create a SeRequest from the ApduRequest list, PO AID as Selector, keep channel open
        SeRequest poSeRequest = new SeRequest(poApduRequests);

        // Transmit the commands to the PO
        SeResponse poSeResponse = safePoTransmit(poSeRequest, ChannelControl.KEEP_OPEN);


        // Retrieve and check the ApduResponses
        List<ApduResponse> poApduResponses = poSeResponse.getApduResponses();

        // Do some basic checks
        checkCommandsResponsesSynchronization(poApduRequests.size(), poApduResponses.size());

        // Parse the response to Open Secure Session (the first item of poApduResponses)
        // The updateCalypsoPo method fills the CalypsoPo object with the command data and return
        // the parser used for an internal usage here.
        AbstractOpenSessionRespPars poOpenSessionPars = (AbstractOpenSessionRespPars) CalypsoPoUtils
                .updateCalypsoPo(calypsoPo, openSessionCmdBuild, poApduResponses.get(0));
        // Build the Digest Init command from PO Open Session
        // the session challenge is needed for the SAM digest computation
        byte[] sessionCardChallenge = poOpenSessionPars.getPoChallenge();

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
                poApduResponses.get(0).getDataOut());

        // Add all commands data to the digest computation. The first command in the list is the
        // open secure session command. This command is not included in the digest computation, so
        // we skip it and start the loop at index 1.
        if ((poCommands != null) && !poCommands.isEmpty()) {
            // Add requests and responses to the digest processor
            samCommandProcessor.pushPoExchangeDataList(poApduRequests, poApduResponses, 1);
        }

        // Remove Open Secure Session response and create a new SeResponse
        poApduResponses.remove(0);

        // update CalypsoPo with the received data
        CalypsoPoUtils.updateCalypsoPo(calypsoPo, poCommands, poApduResponses);

        sessionState = SessionState.SESSION_OPEN;
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
        List<ApduRequest> poApduRequests = getApduRequests(poCommands);

        // Create a SeRequest from the ApduRequest list, PO AID as Selector, manage the logical
        // channel according to the channelControl enum
        SeRequest poSeRequest = new SeRequest(poApduRequests);

        // Transmit the commands to the PO
        SeResponse poSeResponse = safePoTransmit(poSeRequest, channelControl);

        // Retrieve and check the ApduResponses
        List<ApduResponse> poApduResponses = poSeResponse.getApduResponses();

        // Do some basic checks
        checkCommandsResponsesSynchronization(poApduRequests.size(), poApduResponses.size());

        // Add all commands data to the digest computation if this method is called within a Secure
        // Session.
        if (sessionState == SessionState.SESSION_OPEN) {
            samCommandProcessor.pushPoExchangeDataList(poApduRequests, poApduResponses, 0);
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
     * @param ratificationMode the ratification mode tells if the session is closed ratified or not
     * @param channelControl indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     * @throws CalypsoSamCommandException if a response from the SAM was unexpected
     */
    private void processAtomicClosing(
            List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poModificationCommands,
            List<ApduResponse> poAnticipatedResponses,
            SessionSetting.RatificationMode ratificationMode, ChannelControl channelControl)
            throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {

        checkSessionIsOpen();

        // Get the PO ApduRequest List - for the first PO exchange
        List<ApduRequest> poApduRequests = getApduRequests(poModificationCommands);

        // Compute "anticipated" Digest Update (for optional poModificationCommands)
        if ((poModificationCommands != null) && !poApduRequests.isEmpty()) {
            checkCommandsResponsesSynchronization(poApduRequests.size(),
                    poAnticipatedResponses.size());
            // Add all commands data to the digest computation: commands and anticipated
            // responses.
            samCommandProcessor.pushPoExchangeDataList(poApduRequests, poAnticipatedResponses, 0);
        }

        // All SAM digest operations will now run at once.
        // Get Terminal Signature from the latest response
        byte[] sessionTerminalSignature = samCommandProcessor.getTerminalSignature();
        boolean ratificationCommandResponseReceived;

        // Build the PO Close Session command. The last one for this session
        CloseSessionCmdBuild closeSessionCmdBuild = new CloseSessionCmdBuild(calypsoPo.getPoClass(),
                SessionSetting.RatificationMode.CLOSE_RATIFIED.equals(ratificationMode),
                sessionTerminalSignature);

        poApduRequests.add(closeSessionCmdBuild.getApduRequest());

        // Keep the position of the Close Session command in request list
        int closeCommandIndex = poApduRequests.size() - 1;

        // Add the PO Ratification command if any
        boolean ratificationCommandAdded;
        if (SessionSetting.RatificationMode.CLOSE_RATIFIED.equals(ratificationMode)
                && TransmissionMode.CONTACTLESS.equals(calypsoPo.getTransmissionMode())) {
            poApduRequests.add(RatificationCmdBuild.getApduRequest(calypsoPo.getPoClass()));
            ratificationCommandAdded = true;
        } else {
            ratificationCommandAdded = false;
        }

        // Transfer PO commands
        SeRequest poSeRequest = new SeRequest(poApduRequests);

        SeResponse poSeResponse;
        try {
            poSeResponse = poReader.transmitSeRequest(poSeRequest, channelControl);
            // if the ratification command was added and no error occured then the response has been
            // received
            ratificationCommandResponseReceived = ratificationCommandAdded;
        } catch (KeypleReaderIOException ex) {
            poSeResponse = ex.getSeResponse();
            // The current exception may have been caused by a communication issue with the PO
            // during the ratification command.
            //
            // In this case, we do not stop the process and consider the Secure Session close. We'll
            // check the signature.
            //
            // We should have one response less than requests.
            if (!ratificationCommandAdded || poSeResponse == null
                    || poSeResponse.getApduResponses().size() != poApduRequests.size() - 1) {
                throw new CalypsoPoIOException("PO IO Exception while transmitting commands.", ex);
            }
            // we received all responses except the response to the ratification command
            ratificationCommandResponseReceived = false;
        }

        List<ApduResponse> poApduResponses = poSeResponse.getApduResponses();

        // before last if ratification, otherwise last one
        CloseSessionRespPars poCloseSessionPars =
                closeSessionCmdBuild.createResponseParser(poApduResponses.get(closeCommandIndex));
        try {
            poCloseSessionPars.checkStatus();
        } catch (CalypsoPoCommandException ex) {
            throw new CalypsoPoCloseSecureSessionException(
                    "Close Secure Session failed on PO side.", ex);
        }

        try {
            samCommandProcessor.authenticatePoSignature(poCloseSessionPars.getSignatureLo());
        } catch (CalypsoSamIOException ex) {
            throw new CalypsoAuthenticationNotVerifiedException(ex.getMessage());
        } catch (CalypsoSamCommandException ex) {
            throw new CalypsoSessionAuthenticationException("PO authentication failed on SAM side.",
                    ex);
        }

        sessionState = SessionState.SESSION_CLOSED;

        if (ratificationCommandResponseReceived) { // NOSONAR: boolean change in catch
                                                   // is not taken into account by
                                                   // Sonar
            // Remove the ratification response
            poApduResponses.remove(poApduResponses.size() - 1);
        }

        // Remove Close Secure Session response and create a new SeResponse
        poApduResponses.remove(poApduResponses.size() - 1);

        CalypsoPoUtils.updateCalypsoPo(calypsoPo, poModificationCommands, poApduResponses);
    }

    /**
     * Advanced variant of processAtomicClosing in which the list of expected responses is
     * determined from previous reading operations.
     *
     * @param poCommands a list of commands that can modify the PO memory content
     * @param ratificationMode the ratification mode tells if the session is closed ratified or not
     * @param channelControl indicates if the SE channel of the PO reader must be closed after the
     *        last command
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a response from the PO was unexpected
     * @throws CalypsoSamCommandException if a response from the SAM was unexpected
     */
    private void processAtomicClosing(
            List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands,
            SessionSetting.RatificationMode ratificationMode, ChannelControl channelControl)
            throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {
        List<ApduResponse> poAnticipatedResponses = getAnticipatedResponses(poCommands);
        processAtomicClosing(poCommands, poAnticipatedResponses, ratificationMode, channelControl);
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
     * Gets the value of the designated counter
     * 
     * @param sfi the SFI of the EF containing the counter
     * @param counter the number of the counter
     * @return the value of the counter
     * @throws CalypsoPoTransactionIllegalStateException if the counter is not available in the
     *         current {@link CalypsoPo}
     */
    private int getCounterValue(int sfi, int counter)
            throws CalypsoPoTransactionIllegalStateException {
        try {
            ElementaryFile ef = calypsoPo.getFileBySfi((byte) sfi);
            return ef.getData().getContentAsCounterValue(counter);
        } catch (NoSuchElementException e) {
            throw new CalypsoPoTransactionIllegalStateException(
                    "Anticipated response. Unable to determine anticipated value of counter "
                            + counter + " in EF sfi " + sfi);
        }
    }

    /**
     * Create an anticipated response to an Increase/Decrease command
     * 
     * @param newCounterValue the anticipated counter value
     * @return an {@link ApduResponse} containing the expected bytes
     */
    private ApduResponse createIncreaseDecreaseResponse(int newCounterValue) {
        // response = NNNNNN9000
        byte[] response = new byte[5];
        response[0] = (byte) ((newCounterValue & 0x00FF0000) >> 16);
        response[1] = (byte) ((newCounterValue & 0x0000FF00) >> 8);
        response[2] = (byte) (newCounterValue & 0x000000FF);
        response[3] = (byte) 0x90;
        response[4] = (byte) 0x00;
        return new ApduResponse(response, null);
    }

    /**
     * Get the anticipated response to the command sent in processClosing.<br>
     * These commands are supposed to be "modifying commands" i.e.
     * Increase/Decrease/UpdateRecord/WriteRecord ou AppendRecord.
     * 
     * @param poCommands the list of PO commands sent
     * @return the list of the anticipated responses.
     * @throws CalypsoPoTransactionIllegalStateException if the anticipation process failed
     */
    private List<ApduResponse> getAnticipatedResponses(
            List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands)
            throws CalypsoPoTransactionIllegalStateException {
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        if (poCommands != null) {
            for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder : poCommands) {
                if (commandBuilder.getCommandRef() == DECREASE) {
                    int sfi = ((DecreaseCmdBuild) commandBuilder).getSfi();
                    int counter = ((DecreaseCmdBuild) commandBuilder).getCounterNumber();
                    int newCounterValue = getCounterValue(sfi, counter)
                            - ((DecreaseCmdBuild) commandBuilder).getDecValue();
                    apduResponses.add(createIncreaseDecreaseResponse(newCounterValue));
                } else if (commandBuilder.getCommandRef() == INCREASE) {
                    int sfi = ((IncreaseCmdBuild) commandBuilder).getSfi();
                    int counter = ((IncreaseCmdBuild) commandBuilder).getCounterNumber();
                    int newCounterValue = getCounterValue(sfi, counter)
                            + ((IncreaseCmdBuild) commandBuilder).getIncValue();
                    apduResponses.add(createIncreaseDecreaseResponse(newCounterValue));
                } else { // Append/Update/Write Record: response = 9000
                    apduResponses
                            .add(new ApduResponse(new byte[] {(byte) 0x90, (byte) 0x00}, null));
                }
            }
        }
        return apduResponses;
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
    public final void processOpening(PoTransaction.SessionSetting.AccessLevel accessLevel)
            throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {
        currentAccessLevel = accessLevel;

        // create a sublist of AbstractPoCommandBuilder to be sent atomically
        List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poAtomicCommands =
                new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();

        AtomicInteger neededSessionBufferSpace = new AtomicInteger();
        AtomicBoolean overflow = new AtomicBoolean();

        for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder : poCommandManager
                .getPoCommandBuilders()) {
            // check if the command is a modifying one and get it status (overflow yes/no,
            // neededSessionBufferSpace)
            // if the command overflows the session buffer in atomic modification mode, an exception
            // is raised.
            if (checkModifyingCommand(commandBuilder, overflow, neededSessionBufferSpace)) {
                if (overflow.get()) {
                    // Open the session with the current commands
                    processAtomicOpening(currentAccessLevel, poAtomicCommands);
                    // Closes the session, resets the modifications buffer counters for the next
                    // round (set the contact mode to avoid the transmission of the ratification)
                    processAtomicClosing(null, SessionSetting.RatificationMode.CLOSE_RATIFIED,
                            ChannelControl.KEEP_OPEN);
                    resetModificationsBufferCounter();
                    // Clear the list and add the command that did not fit in the PO modifications
                    // buffer. We also update the usage counter without checking the result.
                    poAtomicCommands.clear();
                    poAtomicCommands.add(commandBuilder);
                    // just update modifications buffer usage counter, ignore result (always false)
                    isSessionBufferOverflowed(neededSessionBufferSpace.get());
                } else {
                    // The command fits in the PO modifications buffer, just add it to the list
                    poAtomicCommands.add(commandBuilder);
                }
            } else {
                // This command does not affect the PO modifications buffer
                poAtomicCommands.add(commandBuilder);
            }
        }

        processAtomicOpening(currentAccessLevel, poAtomicCommands);

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
    public final void processPoCommands(ChannelControl channelControl)
            throws CalypsoPoTransactionException, CalypsoPoCommandException {

        /** This method should be called only if no session was previously open */
        checkSessionIsNotOpen();

        // PO commands sent outside a Secure Session. No modifications buffer limitation.
        processAtomicPoCommands(poCommandManager.getPoCommandBuilders(), channelControl);

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
    public final void processPoCommandsInSession() throws CalypsoPoTransactionException,
            CalypsoPoCommandException, CalypsoSamCommandException {

        /** This method should be called only if a session was previously open */
        checkSessionIsOpen();

        // A session is open, we have to care about the PO modifications buffer
        List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poAtomicBuilders =
                new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();

        AtomicInteger neededSessionBufferSpace = new AtomicInteger();
        AtomicBoolean overflow = new AtomicBoolean();

        for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder : poCommandManager
                .getPoCommandBuilders()) {
            // check if the command is a modifying one and get it status (overflow yes/no,
            // neededSessionBufferSpace)
            // if the command overflows the session buffer in atomic modification mode, an exception
            // is raised.
            if (checkModifyingCommand(commandBuilder, overflow, neededSessionBufferSpace)) {
                if (overflow.get()) {
                    // The current command would overflow the modifications buffer in the PO. We
                    // send the current commands and update the parsers. The parsers Iterator is
                    // kept all along the process.
                    processAtomicPoCommands(poAtomicBuilders, ChannelControl.KEEP_OPEN);
                    // Close the session and reset the modifications buffer counters for the next
                    // round (set the contact mode to avoid the transmission of the ratification)
                    processAtomicClosing(null, SessionSetting.RatificationMode.CLOSE_RATIFIED,
                            ChannelControl.KEEP_OPEN);
                    resetModificationsBufferCounter();
                    // We reopen a new session for the remaining commands to be sent
                    processAtomicOpening(currentAccessLevel, null);
                    // Clear the list and add the command that did not fit in the PO modifications
                    // buffer. We also update the usage counter without checking the result.
                    poAtomicBuilders.clear();
                    poAtomicBuilders.add(commandBuilder);
                    // just update modifications buffer usage counter, ignore result (always false)
                    isSessionBufferOverflowed(neededSessionBufferSpace.get());
                } else {
                    // The command fits in the PO modifications buffer, just add it to the list
                    poAtomicBuilders.add(commandBuilder);
                }
            } else {
                // This command does not affect the PO modifications buffer
                poAtomicBuilders.add(commandBuilder);
            }
        }

        if (!poAtomicBuilders.isEmpty()) {
            processAtomicPoCommands(poAtomicBuilders, ChannelControl.KEEP_OPEN);
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
    public final void processClosing(ChannelControl channelControl)
            throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {
        checkSessionIsOpen();

        boolean atLeastOneReadCommand = false;
        boolean sessionPreviouslyClosed = false;

        AtomicInteger neededSessionBufferSpace = new AtomicInteger();
        AtomicBoolean overflow = new AtomicBoolean();

        List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poAtomicCommands =
                new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();
        for (AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder : poCommandManager
                .getPoCommandBuilders()) {
            // check if the command is a modifying one and get it status (overflow yes/no,
            // neededSessionBufferSpace)
            // if the command overflows the session buffer in atomic modification mode, an exception
            // is raised.
            if (checkModifyingCommand(commandBuilder, overflow, neededSessionBufferSpace)) {
                if (overflow.get()) {
                    // Reopen a session with the same access level if it was previously closed in
                    // this current processClosing
                    if (sessionPreviouslyClosed) {
                        processAtomicOpening(currentAccessLevel, null);
                    }

                    // If at least one non-modifying was prepared, we use processAtomicPoCommands
                    // instead of processAtomicClosing to send the list
                    if (atLeastOneReadCommand) {
                        processAtomicPoCommands(poAtomicCommands, ChannelControl.KEEP_OPEN);
                        // Clear the list of commands sent
                        poAtomicCommands.clear();
                        processAtomicClosing(poAtomicCommands,
                                SessionSetting.RatificationMode.CLOSE_RATIFIED,
                                ChannelControl.KEEP_OPEN);
                        resetModificationsBufferCounter();
                        sessionPreviouslyClosed = true;
                        atLeastOneReadCommand = false;
                    } else {
                        // All commands in the list are 'modifying the PO'
                        processAtomicClosing(poAtomicCommands,
                                SessionSetting.RatificationMode.CLOSE_RATIFIED,
                                ChannelControl.KEEP_OPEN);
                        // Clear the list of commands sent
                        poAtomicCommands.clear();
                        resetModificationsBufferCounter();
                        sessionPreviouslyClosed = true;
                    }

                    // Add the command that did not fit in the PO modifications
                    // buffer. We also update the usage counter without checking the result.
                    poAtomicCommands.add(commandBuilder);
                    // just update modifications buffer usage counter, ignore result (always false)
                    isSessionBufferOverflowed(neededSessionBufferSpace.get());
                } else {
                    // The command fits in the PO modifications buffer, just add it to the list
                    poAtomicCommands.add(commandBuilder);
                }
            } else {
                // This command does not affect the PO modifications buffer
                poAtomicCommands.add(commandBuilder);
                atLeastOneReadCommand = true;
            }
        }
        if (sessionPreviouslyClosed) {
            // Reopen if needed, to close the session with the requested conditions
            // (CommunicationMode and channelControl)
            processAtomicOpening(currentAccessLevel, null);
        }

        // Finally, close the session as requested
        processAtomicClosing(poAtomicCommands, poSecuritySettings.getRatificationMode(),
                channelControl);

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
    public final void processCancel(ChannelControl channelControl)
            throws CalypsoPoTransactionException, CalypsoPoCommandException {
        // PO ApduRequest List to hold Close Secure Session command
        List<ApduRequest> poApduRequests = new ArrayList<ApduRequest>();

        // Build the PO Close Session command (in "abort" mode since no signature is provided).
        CloseSessionCmdBuild closeSessionCmdBuild =
                new CloseSessionCmdBuild(calypsoPo.getPoClass());

        poApduRequests.add(closeSessionCmdBuild.getApduRequest());

        // Transfer PO commands
        SeRequest poSeRequest = new SeRequest(poApduRequests);

        SeResponse poSeResponse = safePoTransmit(poSeRequest, channelControl);

        closeSessionCmdBuild.createResponseParser(poSeResponse.getApduResponses().get(0))
                .checkStatus();

        // sets the flag indicating that the commands have been executed
        poCommandManager.notifyCommandsProcessed();

        // session is now considered closed regardless the previous state or the result of the abort
        // session command sent to the PO.
        sessionState = SessionState.SESSION_CLOSED;
    }

    private SeResponse safePoTransmit(SeRequest poSeRequest, ChannelControl channelControl)
            throws CalypsoPoIOException {
        try {
            return poReader.transmitSeRequest(poSeRequest, channelControl);
        } catch (KeypleReaderIOException e) {
            throw new CalypsoPoIOException("PO IO Exception while transmitting commands.", e);
        }
    }

    /**
     * Checks if a Secure Session is open, raises an exception if not
     * 
     * @throws CalypsoPoTransactionIllegalStateException if no session is open
     */
    private void checkSessionIsOpen() throws CalypsoPoTransactionIllegalStateException {
        if (sessionState != SessionState.SESSION_OPEN) {
            throw new CalypsoPoTransactionIllegalStateException("Bad session state. Current: "
                    + sessionState + ", expected: " + SessionState.SESSION_OPEN);
        }
    }

    /**
     * Checks if a Secure Session is not open, raises an exception if not
     *
     * @throws CalypsoPoTransactionIllegalStateException if a session is open
     */
    private void checkSessionIsNotOpen() throws CalypsoPoTransactionIllegalStateException {
        if (sessionState == SessionState.SESSION_OPEN) {
            throw new CalypsoPoTransactionIllegalStateException(
                    "Bad session state. Current: " + sessionState + ", expected: not open");
        }
    }


    /**
     * Checks if the number of responses matches the number of commands.<br>
     * Throw a {@link CalypsoDesynchronizedExchangesException} if not.
     * 
     * @param commandsNumber the number of commands
     * @param responsesNumber the number of responses
     * @throws CalypsoDesynchronizedExchangesException if the test failed
     */
    private void checkCommandsResponsesSynchronization(int commandsNumber, int responsesNumber)
            throws CalypsoDesynchronizedExchangesException {
        if (commandsNumber != responsesNumber) {
            throw new CalypsoDesynchronizedExchangesException(
                    "The number of commands/responses does not match: cmd=" + commandsNumber
                            + ", resp=" + responsesNumber);
        }
    }

    /**
     * Checks the provided command from the session buffer overflow management perspective<br>
     * A exception is raised if the session buffer is overflowed in ATOMIC modification mode.<br>
     * Returns false if the command does not affect the session buffer.<br>
     * Sets the overflow flag and the neededSessionBufferSpace value according to the
     * characteristics of the command in other cases.
     * 
     * @param builder the command builder
     * @param overflow flag set to true if the command overflowed the buffer
     * @param neededSessionBufferSpace updated with the size of the buffer consumed by the command
     * @return true if the command modifies the content of the PO, false if not
     * @throws CalypsoPoTransactionIllegalStateException if the command overflows the buffer in
     *         ATOMIC modification mode
     */
    private boolean checkModifyingCommand(
            AbstractPoCommandBuilder<? extends AbstractPoResponseParser> builder,
            AtomicBoolean overflow, AtomicInteger neededSessionBufferSpace)
            throws CalypsoAtomicTransactionException {
        if (builder.isSessionBufferUsed()) {
            // This command affects the PO modifications buffer
            neededSessionBufferSpace.set(builder.getApduRequest().getBytes().length
                    + SESSION_BUFFER_CMD_ADDITIONAL_COST - APDU_HEADER_LENGTH);
            if (isSessionBufferOverflowed(neededSessionBufferSpace.get())) {
                // raise an exception if in atomic mode
                if (poSecuritySettings
                        .getSessionModificationMode() == SessionSetting.ModificationMode.ATOMIC) {
                    throw new CalypsoAtomicTransactionException(
                            "ATOMIC mode error! This command would overflow the PO modifications buffer: "
                                    + builder.getName());
                }
                overflow.set(true);
            } else {
                overflow.set(false);
            }
            return true;
        } else
            return false;
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
            if (modificationsCounter - sessionBufferSizeConsumed >= 0) {
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
     *
     * @param lid the LID of the EF to select
     */
    public final void prepareSelectFile(byte[] lid) {
        // create the builder and add it to the list of commands
        poCommandManager
                .addRegularCommand(CalypsoPoUtils.prepareSelectFile(calypsoPo.getPoClass(), lid));
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     *
     * @param selectControl provides the navigation case: FIRST, NEXT or CURRENT
     */
    public final void prepareSelectFile(SelectFileControl selectControl) {
        // create the builder and add it to the list of commands
        poCommandManager.addRegularCommand(
                CalypsoPoUtils.prepareSelectFile(calypsoPo.getPoClass(), selectControl));
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

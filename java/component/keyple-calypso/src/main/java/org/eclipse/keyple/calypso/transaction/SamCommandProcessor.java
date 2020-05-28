/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
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
import java.util.Arrays;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.AbstractSamResponseParser;
import org.eclipse.keyple.calypso.command.sam.builder.security.*;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestAuthenticateRespPars;
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestCloseRespPars;
import org.eclipse.keyple.calypso.command.sam.parser.security.SamGetChallengeRespPars;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoDesynchronizedExchangesException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoSamIOException;
import org.eclipse.keyple.core.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SamCommandProcessor class is dedicated to the management of commands sent to the SAM.
 * <p>
 * In particular, it manages the cryptographic computations related to the secure session (digest
 * computation).
 * <p>
 * It also will integrate the SAM commands used for Stored Value and PIN/key management. In session,
 * these commands need to be carefully synchronized with the digest calculation.
 */
class SamCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SamCommandProcessor.class);

    private static final byte KIF_UNDEFINED = (byte) 0xFF;

    private static final byte CHALLENGE_LENGTH_REV_INF_32 = (byte) 0x04;
    private static final byte CHALLENGE_LENGTH_REV32 = (byte) 0x08;
    private static final byte SIGNATURE_LENGTH_REV_INF_32 = (byte) 0x04;
    private static final byte SIGNATURE_LENGTH_REV32 = (byte) 0x08;

    /** The SAM resource */
    private final SamResource samResource;
    /** The Proxy reader to communicate with the SAM */
    private final ProxyReader samReader;
    /** The PO resource */
    private final PoResource poResource;
    /** The security settings. */
    private final PoSecuritySettings poSecuritySettings;
    /*
     * The digest data cache stores all PO data to be send to SAM during a Secure Session. The 1st
     * buffer is the data buffer to be provided with Digest Init. The following buffers are PO
     * command/response pairs
     */
    private static final List<byte[]> poDigestDataCache = new ArrayList<byte[]>();
    private boolean sessionEncryption;
    private boolean verificationMode;
    private byte workKeyRecordNumber;
    private byte workKeyKif;
    private byte workKeyKVC;
    private boolean isDiversificationDone;
    private boolean isDigestInitDone;

    /**
     * Constructor
     * 
     * @param poResource the PO resource containing the PO reader and the Calypso PO information
     * @param poSecuritySettings the security settings from the application layer
     */
    SamCommandProcessor(PoResource poResource, PoSecuritySettings poSecuritySettings) {
        this.poResource = poResource;
        this.poSecuritySettings = poSecuritySettings;
        this.samResource = poSecuritySettings.getSamResource();
        samReader = (ProxyReader) this.samResource.getSeReader();
    }

    /**
     * Gets the terminal challenge
     * <p>
     * Performs key diversification if necessary by sending the SAM Select Diversifier command prior
     * to the Get Challenge command. The diversification flag is set to avoid further unnecessary
     * diversification operations.
     * <p>
     * If the key diversification is already done, the Select Diversifier command is omitted.
     * <p>
     * The length of the challenge varies from one PO revision to another. This information can be
     * found in the PoResource class field.
     * 
     * @return the terminal challenge as an array of bytes
     * @throws CalypsoSamIOException if the communication with the SAM has failed.
     * @throws CalypsoDesynchronizedExchangesException if the APDU SAM exchanges are out of sync
     * @throws CalypsoSamCommandException if the SAM has responded with an error status
     */
    byte[] getSessionTerminalChallenge() throws CalypsoSamIOException,
            CalypsoDesynchronizedExchangesException, CalypsoSamCommandException {
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

        // diversify only if this has not already been done.
        if (!isDiversificationDone) {
            // build the SAM Select Diversifier command to provide the SAM with the PO S/N
            AbstractApduCommandBuilder selectDiversifier =
                    new SelectDiversifierCmdBuild(samResource.getMatchingSe().getSamRevision(),
                            poResource.getMatchingSe().getApplicationSerialNumber());

            apduRequests.add(selectDiversifier.getApduRequest());

            // note that the diversification has been made
            isDiversificationDone = true;
        }

        // build the SAM Get Challenge command
        byte challengeLength = poResource.getMatchingSe().isConfidentialSessionModeSupported()
                ? CHALLENGE_LENGTH_REV32
                : CHALLENGE_LENGTH_REV_INF_32;

        AbstractSamCommandBuilder<? extends AbstractSamResponseParser> getChallengeCmdBuild =
                new SamGetChallengeCmdBuild(samResource.getMatchingSe().getSamRevision(),
                        challengeLength);

        apduRequests.add(getChallengeCmdBuild.getApduRequest());

        // Transmit the SeRequest to the SAM and get back the SeResponse (list of ApduResponse)
        SeResponse samSeResponse;
        try {
            samSeResponse = samReader.transmitSeRequest(new SeRequest(apduRequests),
                    ChannelControl.KEEP_OPEN);
        } catch (KeypleReaderIOException e) {
            throw new CalypsoSamIOException("SAM IO Exception while getting terminal challenge.",
                    e);
        }

        List<ApduResponse> samApduResponses = samSeResponse.getApduResponses();
        byte[] sessionTerminalChallenge;

        int numberOfSamCmd = apduRequests.size();
        if (samApduResponses.size() == numberOfSamCmd) {
            SamGetChallengeRespPars getChallengeRespPars =
                    (SamGetChallengeRespPars) getChallengeCmdBuild
                            .createResponseParser(samApduResponses.get(numberOfSamCmd - 1));

            getChallengeRespPars.checkStatus();

            sessionTerminalChallenge = getChallengeRespPars.getChallenge();
            if (logger.isDebugEnabled()) {
                logger.debug("identification: TERMINALCHALLENGE = {}",
                        ByteArrayUtil.toHex(sessionTerminalChallenge));
            }
        } else {
            throw new CalypsoDesynchronizedExchangesException(
                    "The number of commands/responses does not match: cmd=" + numberOfSamCmd
                            + ", resp=" + samApduResponses.size());
        }
        return sessionTerminalChallenge;
    }

    /**
     * Determine the work KIF from the value returned by the PO and the session access level.
     * <p>
     * If the value provided by the PO undetermined (FFh), the actual value of the work KIF is found
     * in the PoSecuritySettings according to the session access level.
     * <p>
     * If the value provided by the PO is not undetermined, the work KIF is set to this value.
     *
     * @param poKif the KIF value from the PO
     * @param accessLevel the session access level
     * @return the work KIF value byte
     */
    private byte determineWorkKif(byte poKif,
            PoTransaction.SessionSetting.AccessLevel accessLevel) {
        byte kif;
        if (poKif == KIF_UNDEFINED) {
            kif = poSecuritySettings.getSessionDefaultKif(accessLevel);
        } else {
            kif = poKif;
        }
        return kif;
    }

    /**
     * Initializes the digest computation process
     * <p>
     * Resets the digest data cache, then fills a first packet with the provided data (from open
     * secure session).
     * <p>
     * Keeps the session parameters, sets the KIF if not defined
     * <p>
     * Note: there is no communication with the SAM here.
     *
     * @param sessionEncryption true if the session is encrypted
     * @param verificationMode true if the verification mode is active
     * @param poKif the PO KIF
     * @param poKVC the PO KVC
     * @param digestData a first packet of data to digest.
     */
    void initializeDigester(PoTransaction.SessionSetting.AccessLevel accessLevel,
            boolean sessionEncryption, boolean verificationMode, byte poKif, byte poKVC,
            byte[] digestData) {

        this.sessionEncryption = sessionEncryption;
        this.verificationMode = verificationMode;
        this.workKeyRecordNumber = poSecuritySettings.getSessionDefaultKeyRecordNumber(accessLevel);
        this.workKeyKif = determineWorkKif(poKif, accessLevel);
        // TODO handle Rev 1.0 case where KVC is not available
        this.workKeyKVC = poKVC;

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "initialize: POREVISION = {}, SAMREVISION = {}, SESSIONENCRYPTION = {}, VERIFICATIONMODE = {}",
                    poResource.getMatchingSe().getRevision(),
                    samResource.getMatchingSe().getSamRevision(), sessionEncryption,
                    verificationMode);
            logger.debug("initialize: VERIFICATIONMODE = {}, REV32MODE = {} KEYRECNUMBER = {}",
                    verificationMode,
                    poResource.getMatchingSe().isConfidentialSessionModeSupported(),
                    workKeyRecordNumber);
            logger.debug("initialize: KIF = {}, KVC {}, DIGESTDATA = {}",
                    String.format("%02X", poKif), String.format("%02X", poKVC),
                    ByteArrayUtil.toHex(digestData));
        }

        // Clear data cache
        poDigestDataCache.clear();

        // Build Digest Init command as first ApduRequest of the digest computation process
        poDigestDataCache.add(digestData);

        isDigestInitDone = false;
    }

    /**
     * Appends a full PO exchange (request and response) to the digest data cache.
     *
     * @param request PO request
     * @param response PO response
     */
    private void pushPoExchangeData(ApduRequest request, ApduResponse response) {

        logger.trace("pushPoExchangeData: REQUEST = {}", request);

        // Add an ApduRequest to the digest computation: if the request is of case4 type, Le must be
        // excluded from the digest computation. In this cas, we remove here the last byte of the
        // command buffer.
        if (request.isCase4()) {
            poDigestDataCache
                    .add(Arrays.copyOfRange(request.getBytes(), 0, request.getBytes().length - 1));
        } else {
            poDigestDataCache.add(request.getBytes());
        }

        logger.trace("pushPoExchangeData: RESPONSE = {}", response);

        // Add an ApduResponse to the digest computation
        poDigestDataCache.add(response.getBytes());
    }

    /**
     * Appends a list full PO exchange (request and response) to the digest data cache.<br>
     * The startIndex argument makes it possible not to include the beginning of the list when
     * necessary.
     * 
     * @param requests PO request list
     * @param responses PO response list
     * @param startIndex starting point in the list
     */
    void pushPoExchangeDataList(List<ApduRequest> requests, List<ApduResponse> responses,
            int startIndex) {
        for (int i = startIndex; i < requests.size(); i++) {
            // Add requests and responses to the digest processor
            pushPoExchangeData(requests.get(i), responses.get(i));
        }
    }

    /**
     * Gets a single SAM request for all prepared SAM commands.
     * <p>
     * Builds all pending SAM commands related to the digest calculation process of a secure session
     * <ul>
     * <li>Starts with a Digest Init command if not already done,
     * <li>Adds as many Digest Update commands as there are packages in the cache,
     * <li>Appends a Digest Close command if the addDigestClose flag is set to true.
     * </ul>
     * 
     * @param addDigestClose indicates whether to add the Digest Close command
     * @return a list of commands to send to the SAM
     */
    private List<AbstractSamCommandBuilder<? extends AbstractSamResponseParser>> getPendingSamCommands(
            boolean addDigestClose) {
        // TODO optimization with the use of Digest Update Multiple whenever possible.
        List<AbstractSamCommandBuilder<? extends AbstractSamResponseParser>> samCommands =
                new ArrayList<AbstractSamCommandBuilder<? extends AbstractSamResponseParser>>();

        // sanity checks
        if (poDigestDataCache.isEmpty()) {
            logger.debug("getSamDigestRequest: no data in cache.");
            throw new IllegalStateException("Digest data cache is empty.");
        }

        if (!isDigestInitDone && poDigestDataCache.size() % 2 == 0) {
            // the number of buffers should be 2*n + 1
            logger.debug("getSamDigestRequest: wrong number of buffer in cache NBR = {}.",
                    poDigestDataCache.size());
            throw new IllegalStateException("Digest data cache is inconsistent.");
        }

        if (!isDigestInitDone) {
            // Build and append Digest Init command as first ApduRequest of the digest computation
            // process. The Digest Init command comes from the Open Secure Session response from the
            // PO. Once added to the ApduRequest list, the data is remove from the cache to keep
            // only couples of PO request/response
            samCommands.add(new DigestInitCmdBuild(samResource.getMatchingSe().getSamRevision(),
                    verificationMode,
                    poResource.getMatchingSe().isConfidentialSessionModeSupported(),
                    workKeyRecordNumber, workKeyKif, workKeyKVC, poDigestDataCache.get(0)));
            poDigestDataCache.remove(0);
            // note that the digest init has been made
            isDigestInitDone = true;
        }

        // Build and append Digest Update commands
        for (int i = 0; i < poDigestDataCache.size(); i++) {
            samCommands.add(new DigestUpdateCmdBuild(samResource.getMatchingSe().getSamRevision(),
                    sessionEncryption, poDigestDataCache.get(i)));
        }

        // clears cached commands once they have been processed
        poDigestDataCache.clear();

        if (addDigestClose) {
            // Build and append Digest Close command
            samCommands.add((new DigestCloseCmdBuild(samResource.getMatchingSe().getSamRevision(),
                    poResource.getMatchingSe().getRevision().equals(PoRevision.REV3_2)
                            ? SIGNATURE_LENGTH_REV32
                            : SIGNATURE_LENGTH_REV_INF_32)));
        }

        return samCommands;
    }

    /**
     * Gets the terminal signature from the SAM
     * <p>
     * All remaining data in the digest cache is sent to the SAM and the Digest Close command is
     * executed.
     * 
     * @return the terminal signature
     * @throws CalypsoSamIOException if the communication with the SAM has failed.
     * @throws CalypsoDesynchronizedExchangesException if the APDU SAM exchanges are out of sync
     * @throws CalypsoSamCommandException if the SAM has responded with an error status
     */
    byte[] getTerminalSignature() throws CalypsoSamIOException,
            CalypsoDesynchronizedExchangesException, CalypsoSamCommandException {

        // All remaining SAM digest operations will now run at once.
        // Get the SAM Digest request including Digest Close from the cache manager
        List<AbstractSamCommandBuilder<? extends AbstractSamResponseParser>> samCommands =
                getPendingSamCommands(true);

        SeRequest samSeRequest = new SeRequest(getApduRequests(samCommands));

        // Transmit SeRequest and get SeResponse
        SeResponse samSeResponse;

        try {
            samSeResponse = samReader.transmitSeRequest(samSeRequest, ChannelControl.KEEP_OPEN);
        } catch (KeypleReaderIOException e) {
            throw new CalypsoSamIOException("SAM IO Exception while transmitting digest data.", e);
        }

        List<ApduResponse> samApduResponses = samSeResponse.getApduResponses();

        if (samApduResponses.size() != samCommands.size()) {
            throw new CalypsoDesynchronizedExchangesException(
                    "The number of commands/responses does not match: cmd=" + samCommands.size()
                            + ", resp=" + samApduResponses.size());
        }

        // check all responses status
        for (int i = 0; i < samApduResponses.size(); i++) {
            samCommands.get(i).createResponseParser(samApduResponses.get(i)).checkStatus();
        }

        // Get Terminal Signature from the latest response
        DigestCloseRespPars digestCloseRespPars =
                (DigestCloseRespPars) samCommands.get(samCommands.size() - 1)
                        .createResponseParser(samApduResponses.get(samCommands.size() - 1));

        byte[] sessionTerminalSignature = digestCloseRespPars.getSignature();

        if (logger.isDebugEnabled()) {
            logger.debug("SIGNATURE = {}", ByteArrayUtil.toHex(sessionTerminalSignature));
        }

        return sessionTerminalSignature;
    }

    /**
     * Authenticates the signature part from the PO
     * <p>
     * Executes the Digest Authenticate command with the PO part of the signature.
     * 
     * @param poSignatureLo the PO part of the signature
     * @throws CalypsoSamIOException if the communication with the SAM has failed.
     * @throws CalypsoDesynchronizedExchangesException if the APDU SAM exchanges are out of sync
     * @throws CalypsoSamCommandException if the SAM has responded with an error status
     */
    void authenticatePoSignature(byte[] poSignatureLo) throws CalypsoSamIOException,
            CalypsoSamCommandException, CalypsoDesynchronizedExchangesException {
        // Check the PO signature part with the SAM
        // Build and send SAM Digest Authenticate command
        DigestAuthenticateCmdBuild digestAuthenticateCmdBuild = new DigestAuthenticateCmdBuild(
                samResource.getMatchingSe().getSamRevision(), poSignatureLo);

        List<ApduRequest> samApduRequests = new ArrayList<ApduRequest>();
        samApduRequests.add(digestAuthenticateCmdBuild.getApduRequest());

        SeRequest samSeRequest = new SeRequest(samApduRequests);

        SeResponse samSeResponse;
        try {
            samSeResponse = samReader.transmitSeRequest(samSeRequest, ChannelControl.KEEP_OPEN);
        } catch (KeypleReaderIOException e) {
            throw new CalypsoSamIOException(
                    "SAM IO Exception while transmitting digest authentication data.", e);
        }

        // Get transaction result parsing the response
        List<ApduResponse> samApduResponses = samSeResponse.getApduResponses();

        if (samApduResponses == null || samApduResponses.isEmpty()) {
            throw new CalypsoDesynchronizedExchangesException(
                    "No response to Digest Authenticate command.");
        }

        DigestAuthenticateRespPars digestAuthenticateRespPars =
                digestAuthenticateCmdBuild.createResponseParser(samApduResponses.get(0));

        digestAuthenticateRespPars.checkStatus();
    }

    /**
     * Create an ApduRequest List from a AbstractSamCommandBuilder List.
     *
     * @param samCommands a list of SAM commands
     * @return the ApduRequest list
     */
    private List<ApduRequest> getApduRequests(
            List<AbstractSamCommandBuilder<? extends AbstractSamResponseParser>> samCommands) {
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        if (samCommands != null) {
            for (AbstractSamCommandBuilder<? extends AbstractSamResponseParser> commandBuilder : samCommands) {
                apduRequests.add(commandBuilder.getApduRequest());
            }
        }
        return apduRequests;
    }
}

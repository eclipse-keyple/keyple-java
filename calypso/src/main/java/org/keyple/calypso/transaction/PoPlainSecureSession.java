package org.keyple.calypso.transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keyple.commands.calypso.ApduCommandBuilder;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.csm.CsmRevision;
import org.keyple.commands.calypso.csm.builder.CsmGetChallengeCmdBuild;
import org.keyple.commands.calypso.csm.builder.DigestAuthenticateCmdBuild;
import org.keyple.commands.calypso.csm.builder.DigestCloseCmdBuild;
import org.keyple.commands.calypso.csm.builder.DigestInitCmdBuild;
import org.keyple.commands.calypso.csm.builder.DigestUpdateCmdBuild;
import org.keyple.commands.calypso.csm.builder.SelectDiversifierCmdBuild;
import org.keyple.commands.calypso.csm.parser.DigestCloseRespPars;
import org.keyple.commands.calypso.csm.parser.DigestInitRespPars;
import org.keyple.commands.calypso.csm.parser.DigestUpdateRespPars;
import org.keyple.commands.calypso.po.PoCommandBuilder;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.po.SendableInSession;
import org.keyple.commands.calypso.po.builder.CloseSessionCmdBuild;
import org.keyple.commands.calypso.po.builder.OpenSessionCmdBuild;
import org.keyple.commands.calypso.po.builder.PoGetChallengeCmdBuild;
import org.keyple.commands.calypso.po.parser.CloseSessionRespPars;
import org.keyple.commands.calypso.po.parser.GetDataFciRespPars;
import org.keyple.commands.calypso.po.parser.OpenSessionRespPars;
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
 * A non-encrypted secure session with a Calypso
 * PO requires the management of two ProxyReader in order to communicate with
 * both a Calypso PO and a CSM
 *
 * @author Ixxi
 */
public class PoPlainSecureSession {

    /** The po reader. */
    private ProxyReader poReader;

    /** The csm session reader. */
    private ProxyReader csmSessionReader;

    /** The poRevision. */
    private PoRevision poRevision = PoRevision.REV3_1;

    /** default key index. */
    private byte defaultKeyIndex;

    /** The csm revision. */
    private CsmRevision csmRevision = CsmRevision.S1D;

    /**
     * Instantiates a new po plain secure session.
     *
     * @param poReader
     *            the PO reader
     * @param csmReader
     *            the SAM reader
     * @param defaultKeyIndex
     *            default KIF index
     */
    public PoPlainSecureSession(ProxyReader poReader, ProxyReader csmReader, byte defaultKeyIndex) {
        this.poReader = poReader;
        this.csmSessionReader = csmReader;

        this.defaultKeyIndex = defaultKeyIndex;

    }

    /**
     * Gets the SE response diversifier by AID reader and FCI.
     *
     * @param keepChannelOpen
     *            the keep channel open
     * @param reader
     *            the reader
     * @param applicationSN
     *            the application SN
     * @return the SE response diversifier by AID reader and FCI
     * @throws UnexpectedReaderException
     *             the unexpected reader exception
     * @throws IOReaderException
     *             the IO reader exception
     * @throws ChannelStateReaderException
     *             the channel state reader exception
     * @throws InvalidApduReaderException
     *             the invalid apdu reader exception
     * @throws TimeoutReaderException
     *             the timeout reader exception
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    private SeResponse selectDiversifier(boolean keepChannelOpen, ProxyReader reader, byte[] applicationSN)
            throws UnexpectedReaderException, IOReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {
        List<ApduRequest> apduRequestList = new ArrayList<ApduRequest>();

        ApduCommandBuilder selectDiversifier = new SelectDiversifierCmdBuild(this.csmRevision, applicationSN);
        ApduRequest requestDiversifier = selectDiversifier.getApduRequest();
        apduRequestList.add(requestDiversifier);

        return transmitApduResquests(null, reader, keepChannelOpen, apduRequestList);
    }

    /**
     * Gets the SE response challenge by AID reader and FCI.
     *
     * @param keepChannelOpen
     *            the keep channel open
     * @param reader
     *            the reader
     * @return the SE response challenge by AID reader and FCI
     * @throws UnexpectedReaderException
     *             the unexpected reader exception
     * @throws IOReaderException
     *             the IO reader exception
     * @throws ChannelStateReaderException
     *             the channel state reader exception
     * @throws InvalidApduReaderException
     *             the invalid apdu reader exception
     * @throws TimeoutReaderException
     *             the timeout reader exception
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    private SeResponse getChallenge(boolean keepChannelOpen, ProxyReader reader)
            throws UnexpectedReaderException, IOReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {
        List<ApduRequest> apduRequestList = new ArrayList<ApduRequest>();

        ApduCommandBuilder csmGetChallenge = new CsmGetChallengeCmdBuild(this.csmRevision, (byte) 0x04);
        ApduRequest request = csmGetChallenge.getApduRequest();
        apduRequestList.add(request);

        return transmitApduResquests(null, reader, keepChannelOpen, apduRequestList);
    }

    /**
     * Process identification. On poReader, generate a SERequest for the
     * specified PO AID, with keepChannelOpen set at true, and apduRequests
     * defined with the optional poCommands_OutsideSession. Returns the
     * corresponding SEResponse. Identifies the serial and the revision of the
     * PO from FCI data. On csmSessionReader, automatically operate the Select
     * Diversifier and the Get Challenge.
     *
     * @param poAid
     *            the po AID
     * @param poCommandsOutsideSession
     *            the po commands outside session
     * @return the SE response
     * @throws UnexpectedReaderException
     *             the unexpected reader exception
     * @throws IOReaderException
     *             the IO reader exception
     * @throws ChannelStateReaderException
     *             the channel state reader exception
     * @throws InvalidApduReaderException
     *             the invalid apdu reader exception
     * @throws TimeoutReaderException
     *             the timeout reader exception
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public SeResponse processIdentification(byte[] poAid, SendableInSession[] poCommandsOutsideSession)
            throws UnexpectedReaderException, IOReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        boolean keepChannelOpen = true;

        // We need to change object format
        List<ApduRequest> apduRequestsOutsideSession = this
                .changeSendableInSessionTabToListApdu(poCommandsOutsideSession);

        SeResponse seResponseOutsideSession = transmitApduResquests(poAid, this.poReader, true,
                apduRequestsOutsideSession);

        // retrieve fci from this call with AID
        GetDataFciRespPars poFciRespPars = new GetDataFciRespPars(seResponseOutsideSession.getFci());

        // compute poRevision version
        this.poRevision = computePoRevision(poFciRespPars.getApplicationTypeByte());

        byte[] applicationSN = poFciRespPars.getApplicationSerialNumber();

        SeResponse seResponseDiversifier = selectDiversifier(keepChannelOpen, csmSessionReader, applicationSN);

        SeResponse seResponsegetChallenge = getChallenge(keepChannelOpen, csmSessionReader);

        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.addAll(seResponseOutsideSession.getApduResponses());
        apduResponses.add(seResponseDiversifier.getApduResponses().get(0));
        apduResponses.add(seResponsegetChallenge.getApduResponses().get(0));

        return new SeResponse(true, poFciRespPars.getApduResponse(), apduResponses);

    }

    /**
     * Process opening. On poReader, generate a SERequest with the current
     * selected AID, with keepChannelOpen set at true, and apduRequests defined
     * with openCommand and the optional poCommands_InsideSession. Returns the
     * corresponding SEResponse (for openCommand and poCommands_InsideSession).
     * Identifies the session PO keyset. On csmSessionReader, automatically
     * operate the Digest Init and potentially several Digest Update Multiple.
     *
     * @param openCommand
     *            the open command
     * @param poCommandsInsideSession
     *            the po commands inside session
     * @return the SE response
     * @throws UnexpectedReaderException
     *             the unexpected reader exception
     * @throws IOReaderException
     *             the IO reader exception
     * @throws ChannelStateReaderException
     *             the channel state reader exception
     * @throws InvalidApduReaderException
     *             the invalid apdu reader exception
     * @throws TimeoutReaderException
     *             the timeout reader exception
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public SeResponse processOpening(OpenSessionCmdBuild openCommand, SendableInSession[] poCommandsInsideSession)
            throws UnexpectedReaderException, IOReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        boolean keepChannelOpen = true;

        ArrayList<ApduResponse> processOpeningAPDUResponseList = new ArrayList<ApduResponse>();

        SeResponse responseOpenSession = transmitApduResquests(null, poReader, keepChannelOpen,
                Arrays.asList(openCommand.getApduRequest()));

        // identifies the session PO keySet
        OpenSessionRespPars session = this.getSecureSessionBySEResponseAndRevision(responseOpenSession);

        byte kif = session.getSelectedKif();
        if (kif == (byte) 0xFF) {
            if (defaultKeyIndex == (byte) 0x01) {
                kif = (byte) 0x21;
            } else if (defaultKeyIndex == (byte) 0x02) {
                kif = (byte) 0x27;
            } else if (defaultKeyIndex == (byte) 0x03) {
                kif = (byte) 0x30;
            }
        }

        // generate digestInit
        ApduCommandBuilder digestInit = new DigestInitCmdBuild(this.csmRevision, false,
                this.poRevision.equals(PoRevision.REV3_2), defaultKeyIndex, kif, session.getSelectedKvc(),
                session.getRecordDataRead());

        SeResponse responseDigestInit = transmitApduResquests(null, csmSessionReader, keepChannelOpen,
                Arrays.asList(digestInit.getApduRequest()));

        DigestInitRespPars digestInitRespPars = new DigestInitRespPars(responseDigestInit.getApduResponses().get(0));
        if (!digestInitRespPars.isSuccessful()) {
            throw new UnexpectedReaderException(digestInitRespPars.getStatusInformation());
        }

        processOpeningAPDUResponseList.addAll(responseOpenSession.getApduResponses());
        processOpeningAPDUResponseList.addAll(responseDigestInit.getApduResponses());

        // execution des commandes dans la session
        processOpeningAPDUResponseList.addAll(this.processProceeding(poCommandsInsideSession).getApduResponses());

        return new SeResponse(true, null, processOpeningAPDUResponseList);
    }

    /**
     * Change sendable in session tab to list apdu.
     *
     * @param poCommandsInsideSession
     *            the po commands inside session
     * @return the list
     */
    private List<ApduRequest> changeSendableInSessionTabToListApdu(SendableInSession[] poCommandsInsideSession) {
        List<ApduRequest> retour = new ArrayList<ApduRequest>();
        if (poCommandsInsideSession != null) {
            for (SendableInSession cmd : poCommandsInsideSession) {
                retour.add(cmd.getAPDURequest());
            }
        }
        return retour;
    }

    /**
     * generic function to get the SEResponse from APDU Requests
     *
     * @param aid
     *            the aid
     * @param reader
     *            the reader
     * @param keepChannelOpen
     *            the keep channel open
     * @param apduResquestList
     *            the apdu resquest list
     * @return the SEResponse
     * @throws ChannelStateReaderException
     *             the channel state reader exception
     * @throws InvalidApduReaderException
     *             the invalid apdu reader exception
     * @throws TimeoutReaderException
     *             the timeout reader exception
     * @throws UnexpectedReaderException
     *             the unexpected reader exception
     * @throws IOReaderException
     *             the IO reader exception
     */
    private SeResponse transmitApduResquests(byte[] aid, ProxyReader reader, boolean keepChannelOpen,
            List<ApduRequest> apduResquestList) throws ChannelStateReaderException, InvalidApduReaderException,
            TimeoutReaderException, UnexpectedReaderException, IOReaderException {
        SeRequest seApplicationRequest = new SeRequest(aid, apduResquestList, keepChannelOpen);

        return reader.transmit(seApplicationRequest);
    }

    /**
     * Function used to get the secureSession.
     *
     * @param responseOpenSession
     *            the response open session
     * @return the secure session by SE response and poRevision
     * @throws UnexpectedReaderException
     *             the unexpected reader exception
     */
    private OpenSessionRespPars getSecureSessionBySEResponseAndRevision(SeResponse responseOpenSession)
            throws UnexpectedReaderException {

        OpenSessionRespPars openSessionRespPars = new OpenSessionRespPars(responseOpenSession.getApduResponses().get(0),
                poRevision);
        if (!openSessionRespPars.isSuccessful()) {
            throw new UnexpectedReaderException(openSessionRespPars.getStatusInformation());
        }

        return openSessionRespPars;
    }

    /**
     * Process proceeding. On poReader, generate a SERequest with the current
     * selected AID, with keepChannelOpen set at true, and apduRequests defined
     * with the poCommands_InsideSession. Returns the corresponding SEResponse
     * (for poCommands_InsideSession). On csmSessionReader, automatically
     * operate potentially several Digest Update Multiple.
     *
     * @param poCommandsInsideSession
     *            the po commands inside session
     * @return the SE response
     * @throws IOReaderException
     *             the IO reader exception
     * @throws UnexpectedReaderException
     *             the unexpected reader exception
     * @throws ChannelStateReaderException
     *             the channel state reader exception
     * @throws InvalidApduReaderException
     *             the invalid apdu reader exception
     * @throws TimeoutReaderException
     *             the timeout reader exception
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public SeResponse processProceeding(SendableInSession[] poCommandsInsideSession)
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        boolean keepChannelOpen = true;

        // prepare return response list
        ArrayList<ApduResponse> processProceedingAPDUResponseList = new ArrayList<ApduResponse>();

        if (poCommandsInsideSession != null) {
            for (SendableInSession sendable : poCommandsInsideSession) {

                // Execute po command
                SeRequest readOrUpdateSeRequest = new SeRequest(null, Arrays.asList(sendable.getAPDURequest()),
                        keepChannelOpen);
                SeResponse readOrUpdateSeResponse = poReader.transmit(readOrUpdateSeRequest);

                processProceedingAPDUResponseList.addAll(readOrUpdateSeResponse.getApduResponses());

                // digest update SAM with APDU command
                byte[] digestDataRequest = readOrUpdateSeRequest.getApduRequests().get(0).getbytes();

                ApduCommandBuilder digestUpdateRequestCommand = new DigestUpdateCmdBuild(this.csmRevision, false,
                        digestDataRequest);
                SeRequest digestUpdateRequest = new SeRequest(null,
                        Arrays.asList(digestUpdateRequestCommand.getApduRequest()), keepChannelOpen);
                SeResponse responseDigestUpdate = csmSessionReader.transmit(digestUpdateRequest);
                DigestUpdateRespPars digestUpdateRespPars = new DigestUpdateRespPars(
                        responseDigestUpdate.getApduResponses().get(0));
                if (!digestUpdateRespPars.isSuccessful()) {
                    throw new InconsistentCommandException(digestUpdateRespPars.getStatusInformation());
                }

                // digest update SAM with PO response
                byte[] resp = readOrUpdateSeResponse.getApduResponses().get(0).getbytes();
                byte[] statusCode = readOrUpdateSeResponse.getApduResponses().get(0).getStatusCode();
                byte[] digestDataResponse = new byte[resp.length + statusCode.length];

                // append status code to data
                System.arraycopy(resp, 0, digestDataResponse, 0, resp.length);
                System.arraycopy(statusCode, 0, digestDataResponse, resp.length, statusCode.length);

                ApduCommandBuilder digestUpdateResponseCommand = new DigestUpdateCmdBuild(this.csmRevision, false,
                        digestDataResponse);
                SeRequest digestSecondUpdateRequest = new SeRequest(null,
                        Arrays.asList(digestUpdateResponseCommand.getApduRequest()), keepChannelOpen);
                SeResponse responseDigestSecondUpdate = csmSessionReader.transmit(digestSecondUpdateRequest);
                digestUpdateRespPars = new DigestUpdateRespPars(responseDigestSecondUpdate.getApduResponses().get(0));
                if (!digestUpdateRespPars.isSuccessful()) {
                    throw new InconsistentCommandException(digestUpdateRespPars.getStatusInformation());
                }
            }
        }

        return new SeResponse(true, null, processProceedingAPDUResponseList);
    }

    /**
     * Process closing. On csmSessionReader, automatically operate potentially
     * several Digest Update Multiple, and the Digest Close. Identifies the
     * terminal signature. On poReader, generate a SERequest with the current
     * selected AID, with keepChannelOpen set at false, and apduRequests defined
     * with poCommands_InsideSession, closeCommand, and ratificationCommand.
     * Identifies the PO signature. On csmSessionReader, automatically operates
     * the Digest Authenticate. Returns the corresponding SEResponse and the
     * boolean status of the authentication.
     *
     * @param poCommandsInsideSession
     *            the po commands inside session
     * @param closeCommand
     *            the close command
     * @param ratificationCommand
     *            the ratification command
     * @return SEResponse close session response
     * @throws IOReaderException
     *             the IO reader exception
     * @throws UnexpectedReaderException
     *             the unexpected reader exception
     * @throws ChannelStateReaderException
     *             the channel state reader exception
     * @throws InvalidApduReaderException
     *             the invalid apdu reader exception
     * @throws TimeoutReaderException
     *             the timeout reader exception
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public SeResponse processClosing(SendableInSession[] poCommandsInsideSession, CloseSessionCmdBuild closeCommand,
            PoGetChallengeCmdBuild ratificationCommand)
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        boolean keepChannelOpen = true;
        ArrayList<ApduResponse> processClosingAPDUResponseList = new ArrayList<ApduResponse>();

        // exécution des requetes sur le csmReader + digestupdatemultiple
        SeResponse seResponseSendableInsession = this.processProceeding(poCommandsInsideSession);
        processClosingAPDUResponseList.addAll(seResponseSendableInsession.getApduResponses());

        // digest close
        ApduCommandBuilder digestClose = new DigestCloseCmdBuild(this.csmRevision,
                this.poRevision.equals(PoRevision.REV3_2) ? (byte) 0x08 : (byte) 0x04);
        SeRequest seRequestDigestClose = new SeRequest(null, Arrays.asList(digestClose.getApduRequest()),
                keepChannelOpen);
        SeResponse responseDigestClose = this.csmSessionReader.transmit(seRequestDigestClose);
        DigestCloseRespPars respPars = new DigestCloseRespPars(responseDigestClose.getApduResponses().get(0));
        // identification de la signature
        byte[] terminalSignature = respPars.getSignature();
        // création de la commande de fermeture du po et récupération de la
        // requete apdu.
        CloseSessionCmdBuild closeCommandInterne = new CloseSessionCmdBuild(this.poRevision, false, terminalSignature);

        SeRequest seApplicationRequest = new SeRequest(null,
                Arrays.asList(closeCommandInterne.getApduRequest(), ratificationCommand.getApduRequest()),
                keepChannelOpen);
        SeResponse responseClose = poReader.transmit(seApplicationRequest);

        ApduResponse responseCloseSession = responseClose.getApduResponses().get(0);

        processClosingAPDUResponseList.add(responseCloseSession);

        CloseSessionRespPars closeSessionRespPars = new CloseSessionRespPars(responseCloseSession);
        if (!closeSessionRespPars.isSuccessful()) {
            throw new InvalidApduReaderException("Close Error : " + closeSessionRespPars.getStatusInformation());
        }
        byte[] poHalfSessionSignature = closeSessionRespPars.getSignatureLo();
        ApduCommandBuilder digestAuth = new DigestAuthenticateCmdBuild(this.csmRevision, poHalfSessionSignature);

        SeRequest seRequestDigestAuth = new SeRequest(null, Arrays.asList(digestAuth.getApduRequest()),
                keepChannelOpen);
        SeResponse responseDigestAuth = csmSessionReader.transmit(seRequestDigestAuth);
        DigestCloseRespPars digestCloseRespPars = new DigestCloseRespPars(responseDigestAuth.getApduResponses().get(0));
        if (!digestCloseRespPars.isSuccessful()) {
            throw new InconsistentCommandException(digestCloseRespPars.getStatusInformation());
        }

        return new SeResponse(false, null, processClosingAPDUResponseList);
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
     * Gets the poRevision.
     *
     * @return the PO poRevision
     */
    public PoRevision getRevision() {
        return poRevision;
    }

    /**
     * Checks if is bit equals one.
     *
     * @param thebyte
     *            the thebyte
     * @param position
     *            the position
     * @return true, if is bit equals one
     */
    private static boolean isBitEqualsOne(byte thebyte, int position) {
        return (1 == ((thebyte >> position) & 1));
    }
}

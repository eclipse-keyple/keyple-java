package cna.sdk.calypso.transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.enumTagUtils;
import cna.sdk.calypso.commandset.csm.CsmRevision;
import cna.sdk.calypso.commandset.csm.builder.CsmGetChallengeCmdBuild;
import cna.sdk.calypso.commandset.csm.builder.DigestAuthenticateCmdBuild;
import cna.sdk.calypso.commandset.csm.builder.DigestCloseCmdBuild;
import cna.sdk.calypso.commandset.csm.builder.DigestInitCmdBuild;
import cna.sdk.calypso.commandset.csm.builder.DigestUpdateCmdBuild;
import cna.sdk.calypso.commandset.csm.builder.SelectDiversifierCmdBuild;
import cna.sdk.calypso.commandset.csm.parser.DigestCloseRespPars;
import cna.sdk.calypso.commandset.dto.AID;
import cna.sdk.calypso.commandset.dto.FCI;
import cna.sdk.calypso.commandset.dto.POHalfSessionSignature;
import cna.sdk.calypso.commandset.dto.SecureSession;
import cna.sdk.calypso.commandset.dto.StartupInformation;
import cna.sdk.calypso.commandset.po.PoCommandBuilder;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.calypso.commandset.po.SendableInSession;
import cna.sdk.calypso.commandset.po.builder.CloseSessionCmdBuild;
import cna.sdk.calypso.commandset.po.builder.GetAIDCmdBuild;
import cna.sdk.calypso.commandset.po.builder.GetDataFciCmdBuild;
import cna.sdk.calypso.commandset.po.builder.OpenSessionCmdBuild;
import cna.sdk.calypso.commandset.po.builder.PoGetChallengeCmdBuild;
import cna.sdk.calypso.commandset.po.parser.PoFciRespPars;
import cna.sdk.seproxy.APDURequest;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.ProxyReader;
import cna.sdk.seproxy.ReaderException;
import cna.sdk.seproxy.SERequest;
import cna.sdk.seproxy.SEResponse;

/**
 * The Class PoPlainSecureSession. A non-encrypted secure session with a Calypso
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

    /** The revision. */
    private PoRevision revision = PoCommandBuilder.defaultRevision;

    private AID selectedAID;

    /**
     * Instantiates a new PoPlainSecureSession.
     *
     * @param poReader
     *            the po reader
     * @param csmReader
     *            the csm reader
     * @param csmSettings
     *            the csm settings
     */
    public PoPlainSecureSession(ProxyReader poReader, ProxyReader csmReader, byte[] csmSettings) {
        this.poReader = poReader;
        this.csmSessionReader = csmReader;
    }

    /**
     * Process identification. On poReader, generate a SERequest for the
     * specified PO AID, with keepChannelOpen set at true, and apduRequests
     * defined with the optional poCommands_OutsideSession. Returns the
     * corresponding SEResponse. Identifies the serial and the revision of the
     * PO from FCI data. On csmSessionReader, automatically operate the Select
     * Diversifier and the Get Challenge.
     *
     * @param poAID
     *            the po AID
     * @param poCommandsOutsideSession
     *            the po commands outside session
     * @return the SE response
     * @throws ReaderException
     *             the reader exception
     */
    public SEResponse processIdentification(byte[] poAID, SendableInSession[] poCommandsOutsideSession)
            throws ReaderException {
        boolean keepChannelOpen = true;

        selectedAID = new AID(poAID);

        // if no forced AID, try to find the current one
        if (selectedAID.getValue() == null) {
            ApduCommandBuilder getAidRequest = new GetAIDCmdBuild(PoCommandBuilder.defaultRevision);
            List<APDURequest> listRequests = new ArrayList<>();
            listRequests.add(getAidRequest.getApduRequest());
            SERequest seRequest = new SERequest(null, keepChannelOpen, listRequests);

            SEResponse response = poReader.transmit(seRequest);

            PoFciRespPars responseParser = new PoFciRespPars(response.getApduResponses().get(0),
                    enumTagUtils.AID_OF_CURRENT_DF);
            selectedAID = responseParser.getAid();
        }

        List<APDUResponse> apduResponses = new ArrayList<>();

        // Execute outsite sessions commands
        if (poCommandsOutsideSession != null) {
            for (SendableInSession commandsOutsideSession : poCommandsOutsideSession) {
                apduResponses.addAll(poReader
                        .transmit(new SERequest(selectedAID.getValue(), true,
                                Arrays.asList(new APDURequest[] { commandsOutsideSession.getAPDURequest() })))
                        .getApduResponses());
            }
        }

        // get FCI to retrieve the PO revision
        List<APDURequest> apduFCIRequest = new ArrayList<>();
        apduFCIRequest.add(new GetDataFciCmdBuild(PoCommandBuilder.defaultRevision).getApduRequest());
        SERequest seRequestFCI = new SERequest(selectedAID.getValue(), true, apduFCIRequest);
        SEResponse seResponseFCI = poReader.transmit(seRequestFCI);
        PoFciRespPars getDataFCIRespPars = new PoFciRespPars(seResponseFCI.getApduResponses().get(0),
                enumTagUtils.FCI_TEMPLATE);
        FCI fci = getDataFCIRespPars.getFci();
        StartupInformation startupInformation = fci.getStartupInformation();

        if (startupInformation.getApplicationType() < (byte) 0x1F
                && startupInformation.getApplicationType() > (byte) 0x06) {
            revision = PoRevision.REV2_4;
        } else if (startupInformation.getApplicationType() < (byte) 0x7f
                && startupInformation.getApplicationType() > (byte) 0x20) {
            revision = PoRevision.REV3_1;
        }

        // prepare the session opening
        List<APDURequest> apduRequestList = new ArrayList<>();
        // SELECT DIVERSIFIER
        ApduCommandBuilder selectDiversifier = new SelectDiversifierCmdBuild(null, fci.getApplicationSN());
        APDURequest requestDiversifier = selectDiversifier.getApduRequest();
        apduRequestList.add(requestDiversifier);
        SERequest seRequestDiversifier = new SERequest(selectedAID.getValue(), keepChannelOpen, apduRequestList);
        SEResponse seResponseDiversifier = csmSessionReader.transmit(seRequestDiversifier);
        apduRequestList.clear();
        // GET CHALLENGE - only rev 2 of CSM supported
        ApduCommandBuilder csmGetChallenge = new CsmGetChallengeCmdBuild(CsmRevision.C1);
        APDURequest request = csmGetChallenge.getApduRequest();
        apduRequestList.add(request);
        SERequest seRequestGetChallenge = new SERequest(selectedAID.getValue(), keepChannelOpen, apduRequestList);
        SEResponse seResponsegetChallenge = csmSessionReader.transmit(seRequestGetChallenge);

        apduResponses.add(seResponseFCI.getApduResponses().get(0));
        apduResponses.add(seResponseDiversifier.getApduResponses().get(0));
        apduResponses.add(seResponsegetChallenge.getApduResponses().get(0));

        return new SEResponse(true, seResponseFCI.getApduResponses().get(0), apduResponses);

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
     * @throws ReaderException
     *             the reader exception
     */
    public SEResponse processOpening(OpenSessionCmdBuild openCommand, SendableInSession[] poCommandsInsideSession)
            throws ReaderException {

        boolean keepChannelOpen = true;
        List<APDURequest> apduRequestList = new ArrayList<>();
        APDURequest requestOpenSession = openCommand.getApduRequest();
        apduRequestList.add(requestOpenSession);

        SERequest seApplicationRequest = new SERequest(selectedAID.getValue(), keepChannelOpen, apduRequestList);
        SEResponse responseOpenSession = poReader.transmit(seApplicationRequest);

        byte[] responsebyte = responseOpenSession.getApduResponses().get(0).getbytes();

        SecureSession secureSession;

        if (PoRevision.REV2_4 == revision) {
            secureSession = ResponseUtils.toSecureSessionRev2(responsebyte);
        } else if (PoRevision.REV3_1 == revision || PoRevision.REV3_2 == revision) {
            secureSession = ResponseUtils.toSecureSession(responsebyte);
        } else
            secureSession = ResponseUtils.toSecureSessionRev2(responsebyte);

        apduRequestList.clear();

        ApduCommandBuilder digestInit = new DigestInitCmdBuild(CsmRevision.C1, secureSession.getKIF(),
                secureSession.getKVC(), secureSession.getSecureSessionData());
        APDURequest requestDigestInit = digestInit.getApduRequest();
        apduRequestList.add(requestDigestInit);

        SERequest seRequestDigestInit = new SERequest(selectedAID.getValue(), keepChannelOpen, apduRequestList);
        SEResponse responseDigestInit = csmSessionReader.transmit(seRequestDigestInit);

        apduRequestList.clear();
        ArrayList<APDUResponse> processOpeningAPDUResponseList = new ArrayList<>();
        processOpeningAPDUResponseList.addAll(responseOpenSession.getApduResponses());
        processOpeningAPDUResponseList.addAll(responseDigestInit.getApduResponses());

        processOpeningAPDUResponseList.addAll(this.processProceeding(poCommandsInsideSession).getApduResponses());

        return new SEResponse(true, null, processOpeningAPDUResponseList);
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
     * @throws ReaderException
     *             the reader exception
     */
    public SEResponse processProceeding(SendableInSession[] poCommandsInsideSession) throws ReaderException {

        boolean keepChannelOpen = true;
        List<APDURequest> apduRequestList = new ArrayList<>();
        List<APDURequest> apduRequestListDigestRequest = new ArrayList<>();
        List<APDURequest> apduRequestListDigestResponse = new ArrayList<>();
        ArrayList<APDUResponse> processOpeningAPDUResponseList = new ArrayList<>();
        byte[] endbytes = { (byte) 0x90, (byte) 0x00 };

        if (poCommandsInsideSession != null) {
            for (SendableInSession sendable : poCommandsInsideSession) {
                APDURequest request = sendable.getAPDURequest();
                apduRequestList.add(request);
                SERequest readOrUpdateSeRequest = new SERequest(selectedAID.getValue(), keepChannelOpen,
                        apduRequestList);
                SEResponse readOrUpdateSeResponse = poReader.transmit(readOrUpdateSeRequest);
                processOpeningAPDUResponseList.addAll(readOrUpdateSeResponse.getApduResponses());

                // Request Digest Update
                byte[] digestDataRequest = readOrUpdateSeRequest.getApduRequests().get(0).getbytes();

                ApduCommandBuilder digestUpdateRequestCommand = new DigestUpdateCmdBuild(CsmRevision.C1, true,
                        digestDataRequest);
                apduRequestListDigestRequest.add(digestUpdateRequestCommand.getApduRequest());
                SERequest digestUpdateRequest = new SERequest(selectedAID.getValue(), keepChannelOpen,
                        apduRequestListDigestRequest);
                SEResponse responseDigestUpdate = csmSessionReader.transmit(digestUpdateRequest);
                processOpeningAPDUResponseList.addAll(responseDigestUpdate.getApduResponses());
                apduRequestListDigestRequest.clear();

                // Response Digest Update
                byte[] digestDataResponse = ArrayUtils
                        .addAll(readOrUpdateSeResponse.getApduResponses().get(0).getbytes(), endbytes);
                ApduCommandBuilder digestUpdateResponseCommand = new DigestUpdateCmdBuild(CsmRevision.C1, true,
                        digestDataResponse);
                apduRequestListDigestResponse.add(digestUpdateResponseCommand.getApduRequest());
                SERequest digestSecondUpdateRequest = new SERequest(selectedAID.getValue(), keepChannelOpen,
                        apduRequestListDigestResponse);
                SEResponse responseDigestSecondUpdate = csmSessionReader.transmit(digestSecondUpdateRequest);
                processOpeningAPDUResponseList.addAll(responseDigestSecondUpdate.getApduResponses());
                apduRequestListDigestResponse.clear();
                apduRequestList.clear();
            }
        }

        return new SEResponse(true, null, processOpeningAPDUResponseList);
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
     * @param closeCommand
     *            the close command
     * @param ratificationCommand
     *            the ratification command
     * @param poCommandsInsideSession
     *            the command(s) inside the secure session
     * @return the map
     * @throws ReaderException
     *             the reader exception
     */
    public SEResponse processClosing(SendableInSession[] poCommandsInsideSession, CloseSessionCmdBuild closeCommand,
            PoGetChallengeCmdBuild ratificationCommand) throws ReaderException {

        boolean keepChannelOpen = true;
        List<APDURequest> apduRequestList = new ArrayList<>();
        ArrayList<APDUResponse> processOpeningAPDUResponseList = new ArrayList<>();

        processOpeningAPDUResponseList.addAll(this.processProceeding(poCommandsInsideSession).getApduResponses());

        // DIGEST CLOSE
        ApduCommandBuilder digestClose = new DigestCloseCmdBuild(CsmRevision.C1);
        APDURequest requestDigestCLose = digestClose.getApduRequest();
        apduRequestList.add(requestDigestCLose);
        SERequest seRequestDigestClose = new SERequest(selectedAID.getValue(), keepChannelOpen, apduRequestList);
        SEResponse responseDigestClose = csmSessionReader.transmit(seRequestDigestClose);
        DigestCloseRespPars respPars = new DigestCloseRespPars(responseDigestClose.getApduResponses().get(0));
        apduRequestList.clear();

        // CLOSE SESSION
        CloseSessionCmdBuild requestCloseSession = new CloseSessionCmdBuild(revision, ratificationCommand != null,
                respPars.getSamHalfSessionSignature().getValue());
        apduRequestList.add(requestCloseSession.getApduRequest());
        SERequest seApplicationRequest = new SERequest(selectedAID.getValue(), keepChannelOpen, apduRequestList);
        SEResponse responseCloseSession = poReader.transmit(seApplicationRequest);
        apduRequestList.clear();

        // PO SIGNATURE
        POHalfSessionSignature poHalfSessionSignature = new POHalfSessionSignature(
                responseCloseSession.getApduResponses().get(0).getbytes());

        // DIGEST AUTHENTICATE
        ApduCommandBuilder digestAuth = new DigestAuthenticateCmdBuild(CsmRevision.C1,
                poHalfSessionSignature.getValue());
        APDURequest requestDigestAuth = digestAuth.getApduRequest();
        apduRequestList.add(requestDigestAuth);
        SERequest seRequestDigestAuth = new SERequest(selectedAID.getValue(), keepChannelOpen, apduRequestList);
        SEResponse responseDigestAuth = csmSessionReader.transmit(seRequestDigestAuth);
        apduRequestList.clear();
        return responseDigestAuth;
    }

    public PoRevision getRevision() {
        return revision;
    }

    public AID getSelectedAID() {
        return selectedAID;
    }
}

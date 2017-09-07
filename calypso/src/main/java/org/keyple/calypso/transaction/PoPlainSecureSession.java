package org.keyple.calypso.transaction;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.ResponseUtils;
import org.keyple.calypso.commandset.enumTagUtils;
import org.keyple.calypso.commandset.csm.CsmRevision;
import org.keyple.calypso.commandset.csm.builder.CsmGetChallengeCmdBuild;
import org.keyple.calypso.commandset.csm.builder.DigestAuthenticateCmdBuild;
import org.keyple.calypso.commandset.csm.builder.DigestCloseCmdBuild;
import org.keyple.calypso.commandset.csm.builder.DigestInitCmdBuild;
import org.keyple.calypso.commandset.csm.builder.DigestUpdateCmdBuild;
import org.keyple.calypso.commandset.csm.builder.DigestUpdateMultipleCmdBuild;
import org.keyple.calypso.commandset.csm.builder.SelectDiversifierCmdBuild;
import org.keyple.calypso.commandset.csm.parser.DigestCloseRespPars;
import org.keyple.calypso.commandset.dto.AID;
import org.keyple.calypso.commandset.dto.FCI;
import org.keyple.calypso.commandset.dto.POHalfSessionSignature;
import org.keyple.calypso.commandset.dto.SecureSession;
import org.keyple.calypso.commandset.dto.StartupInformation;
import org.keyple.calypso.commandset.po.PoCommandBuilder;
import org.keyple.calypso.commandset.po.PoRevision;
import org.keyple.calypso.commandset.po.SendableInSession;
import org.keyple.calypso.commandset.po.builder.CloseSessionCmdBuild;
import org.keyple.calypso.commandset.po.builder.GetAIDCmdBuild;
import org.keyple.calypso.commandset.po.builder.GetDataFciCmdBuild;
import org.keyple.calypso.commandset.po.builder.OpenSessionCmdBuild;
import org.keyple.calypso.commandset.po.builder.PoGetChallengeCmdBuild;
import org.keyple.calypso.commandset.po.builder.SelectAidCmdBuild;
import org.keyple.calypso.commandset.po.parser.PoFciRespPars;
import org.keyple.seproxy.APDURequest;
import org.keyple.seproxy.APDUResponse;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.SERequest;
import org.keyple.seproxy.SEResponse;
import org.keyple.seproxy.exceptions.ReaderException;

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

	public void setSelectedAID(AID selectedAID) {
		this.selectedAID = selectedAID;
	}

	private FCI fci;

	private CsmRevision csmRevision = CsmRevision.C1;

	private SecureSession secureSession;

	private PoFciRespPars poFciRespPars;

	/**
	 * Instantiates a new PoPlainSecureSession.
	 *
	 * @param poReader
	 *            the po reader
	 * @param csmReader
	 *            the csm reader
	 * @throws ReaderException
	 */
	public PoPlainSecureSession(ProxyReader poReader, ProxyReader csmReader) {
		this.poReader = poReader;
		this.csmSessionReader = csmReader;
	}
	
	public PoPlainSecureSession(ProxyReader poReader, ProxyReader csmReader, AID poAID, CsmRevision revision) throws ReaderException{
		this.poReader = poReader;
		this.csmSessionReader = csmReader;
		this.selectedAID = poAID;
		this.csmRevision = revision;
		
		boolean keepChannelOpen = true;

		// récupération de l'AID
		this.selectedAID = selectAIDByPoRevisionClass(selectedAID.getValue(), keepChannelOpen, poReader, this.revision);

//		generateAndSetFCIPoInformations(keepChannelOpen);
		StartupInformation startupInformation = this.fci.getStartupInformation();

		// initialisation de la révision
		if (startupInformation.getApplicationType() < (byte) 0x1F
				&& startupInformation.getApplicationType() > (byte) 0x06) {
			this.revision = PoRevision.REV2_4;
		} else if (startupInformation.getApplicationType() < (byte) 0x7f
				&& startupInformation.getApplicationType() > (byte) 0x20) {
			this.revision = PoRevision.REV3_1;
		}else{
			this.revision = PoRevision.REV3_2;
		}
	}
	
	public void generateAndSetFCIPoInformations(boolean keepChannelOpen) throws ReaderException{
		// Récupération du FCI
		SEResponse seResponseFCI = selectFCIByPoRevisionClass(this.selectedAID, keepChannelOpen, this.poReader,
				this.revision);
		poFciRespPars = new PoFciRespPars(seResponseFCI.getApduResponses().get(0), enumTagUtils.FCI_TEMPLATE);

		this.fci = poFciRespPars.getFci();
	}
	/**
	 * Function used to initialize or not the AID of the PO
	 * 
	 * @param poAID
	 * @param keepChannelOpen
	 * @param reader
	 * @param revision
	 * @return
	 * @throws ReaderException
	 */
	private AID selectAIDByPoRevisionClass(byte[] poAID, boolean keepChannelOpen, ProxyReader reader,
			PoRevision revision) throws ReaderException {
		AID aidToReturn = new AID(poAID);
//
		List<APDURequest> listRequests = new ArrayList<>();
		if (aidToReturn.getValue() == null) {
			this.fci = null;
			ApduCommandBuilder getAidRequest = new GetAIDCmdBuild(revision);
			listRequests.add(getAidRequest.getApduRequest());
			SEResponse response = getSEResponseByAIDAndReader(aidToReturn, reader, keepChannelOpen, listRequests);

			this.poFciRespPars = new PoFciRespPars(response.getApduResponses().get(0),
					enumTagUtils.AID_OF_CURRENT_DF_D);
			aidToReturn = this.poFciRespPars.getAid();
			listRequests.clear();
		}

		if(this.fci == null){
			ApduCommandBuilder selectAidRequest = new SelectAidCmdBuild(aidToReturn);
			listRequests.add(selectAidRequest.getApduRequest());
			SEResponse response = getSEResponseByAIDAndReader(aidToReturn, reader, keepChannelOpen, listRequests);
	
			this.poFciRespPars = new PoFciRespPars(response.getApduResponses().get(0),
					enumTagUtils.FCI_TEMPLATE);
			
			this.fci = this.poFciRespPars.getFci();
		}
		return aidToReturn;
	}

	/**
	 * Function used to get the FCI of a po
	 * 
	 * @param selectedAID
	 * @param keepChannelOpen
	 * @param reader
	 * @param revision
	 * @return
	 * @throws ReaderException
	 */
	private SEResponse selectFCIByPoRevisionClass(AID selectedAID, boolean keepChannelOpen, ProxyReader reader,
			PoRevision revision) throws ReaderException {

		// get FCI to retrieve the PO revision
		List<APDURequest> apduFCIRequest = new ArrayList<>();
		apduFCIRequest.add(new GetDataFciCmdBuild(revision).getApduRequest());
		SEResponse seResponseFCI = getSEResponseByAIDAndReader(selectedAID, reader, keepChannelOpen, apduFCIRequest);

		return seResponseFCI;
	}

	/**
	 * 
	 * @param selectAID
	 * @param keepChannelOpen
	 * @param reader
	 * @param revision
	 * @param fci
	 * @return
	 * @throws ReaderException
	 */
	private SEResponse getSEResponseDiversifierByAIDReaderAndFCI(AID selectAID, boolean keepChannelOpen,
			ProxyReader reader, CsmRevision revision, FCI fci) throws ReaderException {
		List<APDURequest> apduRequestList = new ArrayList<>();

		// SELECT DIVERSIFIER
		ApduCommandBuilder selectDiversifier = new SelectDiversifierCmdBuild(revision, fci.getApplicationSN());
		APDURequest requestDiversifier = selectDiversifier.getApduRequest();
		apduRequestList.add(requestDiversifier);

		return getSEResponseByAIDAndReader(selectedAID, reader, keepChannelOpen, apduRequestList);
	}

	/**
	 * 
	 * @param selectAID
	 * @param keepChannelOpen
	 * @param reader
	 * @param revision
	 * @return
	 * @throws ReaderException
	 */
	private SEResponse getSEResponseChallengeByAIDReaderAndFCI(AID selectAID, boolean keepChannelOpen,
			ProxyReader reader, CsmRevision revision) throws ReaderException {
		List<APDURequest> apduRequestList = new ArrayList<>();

		ApduCommandBuilder csmGetChallenge = new CsmGetChallengeCmdBuild(revision);
		APDURequest request = csmGetChallenge.getApduRequest();
		apduRequestList.add(request);

		return getSEResponseByAIDAndReader(selectedAID, reader, keepChannelOpen, apduRequestList);
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

		// récupération de l'AID
		this.selectedAID = selectAIDByPoRevisionClass(poAID, keepChannelOpen, poReader, this.revision);

		// Récupération du diversifier
		SEResponse seResponseDiversifier = getSEResponseDiversifierByAIDReaderAndFCI(this.selectedAID, keepChannelOpen,
				csmSessionReader, this.csmRevision, this.fci);

		// getChallenge
		SEResponse seResponsegetChallenge = getSEResponseChallengeByAIDReaderAndFCI(this.selectedAID, keepChannelOpen,
				csmSessionReader, this.csmRevision);


		// //initialisation de la révision
		List<APDUResponse> apduResponses = new ArrayList<>();
		// Execute outside sessions commands jamais exécuté dans le code.
		if (poCommandsOutsideSession != null) {
			for (SendableInSession commandsOutsideSession : poCommandsOutsideSession) {
				apduResponses.addAll(poReader
						.transmit(new SERequest(selectedAID.getValue(), true,
								Arrays.asList(new APDURequest[] { commandsOutsideSession.getAPDURequest() })))
						.getApduResponses());
			}
		}
		apduResponses.add(this.poFciRespPars.getApduResponse());
		apduResponses.add(seResponseDiversifier.getApduResponses().get(0));
		apduResponses.add(seResponsegetChallenge.getApduResponses().get(0));

		return new SEResponse(true, this.poFciRespPars.getApduResponse(), apduResponses);

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

		// Récupération de la sécureSession
		SEResponse responseOpenSession = getSEResponseByAIDAndReader(this.selectedAID, poReader, keepChannelOpen,
				apduRequestList);

		this.secureSession = this.getSecureSessionBySEResponseAndRevision(responseOpenSession, this.revision);

		apduRequestList.clear();

		// DigestInit
		ApduCommandBuilder digestInit = new DigestInitCmdBuild(this.csmRevision, this.secureSession.getKIF(),
				this.secureSession.getKVC(), this.secureSession.getSecureSessionData()); //CsmRevision.C1

		APDURequest requestDigestInit = digestInit.getApduRequest();
		apduRequestList.add(requestDigestInit);

		SEResponse responseDigestInit = getSEResponseByAIDAndReader(selectedAID, csmSessionReader, keepChannelOpen,
				apduRequestList);

		apduRequestList.clear();
		ArrayList<APDUResponse> processOpeningAPDUResponseList = new ArrayList<>();
		processOpeningAPDUResponseList.addAll(responseOpenSession.getApduResponses());
		processOpeningAPDUResponseList.addAll(responseDigestInit.getApduResponses());

		processOpeningAPDUResponseList.addAll(this.processProceeding(poCommandsInsideSession).getApduResponses());

		return new SEResponse(true, null, processOpeningAPDUResponseList);
	}

	/**
	 * generic function to get the SEResponse
	 * 
	 * @param selectedAID
	 * @param reader
	 * @param keepChannelOpen
	 * @param apduResquestList
	 * @return the SEResponse
	 * @throws ReaderException
	 */
	private SEResponse getSEResponseByAIDAndReader(AID selectedAID, ProxyReader reader, boolean keepChannelOpen,
			List<APDURequest> apduResquestList) throws ReaderException {
		SERequest seApplicationRequest = new SERequest(selectedAID.getValue(), keepChannelOpen, apduResquestList);
		SEResponse responseOpenSession = reader.transmit(seApplicationRequest);

		return responseOpenSession;
	}

	/**
	 * Function used to get the secureSession
	 * 
	 * @param selectAID
	 * @param responseOpenSession
	 * @throws ReaderException
	 */
	private SecureSession getSecureSessionBySEResponseAndRevision(SEResponse responseOpenSession, PoRevision revision) {

		byte[] responsebyte = responseOpenSession.getApduResponses().get(0).getbytes();

		SecureSession secureSession;

		if (PoRevision.REV3_1.equals(revision) || PoRevision.REV3_2.equals(revision)) {
			secureSession = ResponseUtils.toSecureSession(responsebyte);
		} else
			secureSession = ResponseUtils.toSecureSessionRev2(responsebyte);

		return secureSession;
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

				ApduCommandBuilder digestUpdateRequestCommand = new DigestUpdateMultipleCmdBuild(this.csmRevision,
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
				ApduCommandBuilder digestUpdateResponseCommand = new DigestUpdateMultipleCmdBuild(this.csmRevision,
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

		// CLOSE SESSION
		apduRequestList.add(closeCommand.getApduRequest());
		SERequest seApplicationRequest = new SERequest(selectedAID.getValue(), keepChannelOpen, apduRequestList);
		SEResponse responseCloseSession = poReader.transmit(seApplicationRequest);

		apduRequestList.clear();

		// PO SIGNATURE
		POHalfSessionSignature poHalfSessionSignature = new POHalfSessionSignature(
				responseCloseSession.getApduResponses().get(0).getbytes());

		// DIGEST AUTHENTICATE
		ApduCommandBuilder digestAuth = new DigestAuthenticateCmdBuild(this.csmRevision,
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

	public CsmRevision getCsmRevision() {
		return this.csmRevision;
	}

	public AID getSelectedAID() {
		return selectedAID;
	}

	public void setCsmRevision(CsmRevision csmRevision) {
		this.csmRevision = csmRevision;
	}
}

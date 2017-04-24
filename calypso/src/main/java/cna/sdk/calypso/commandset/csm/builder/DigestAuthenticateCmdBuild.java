package cna.sdk.calypso.commandset.csm.builder;

import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.csm.CsmCommandBuilder;
import cna.sdk.calypso.commandset.csm.CsmRevision;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;

/** This class provides the dedicated constructor to build the Digest Authenticate APDU command.
 * @author Ixxi
 *
 */
public class DigestAuthenticateCmdBuild extends CsmCommandBuilder {

	/** The command reference. */
	private CalypsoCommands reference = CalypsoCommands.CSM_DIGEST_AUTHENTICATE;

	/**
	 * Instantiates a new DigestAuthenticateCmdBuild .
	 *
	 * @param revision of the CSM(SAM)
	 * @param signaturePO from the response of the PO CloseSessionCmdBuild
	 */
	public DigestAuthenticateCmdBuild(CsmRevision revision, byte [] signaturePO){
	    super(revision);
	    byte cla = csmRevision.getCla();
		byte p1 = 0x00;
		byte p2 = (byte) 0x00;

		CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, signaturePO);
		request = RequestUtils.constructAPDURequest(calypsoRequest);
	}

}

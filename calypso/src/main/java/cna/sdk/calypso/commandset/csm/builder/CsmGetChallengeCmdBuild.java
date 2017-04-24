package cna.sdk.calypso.commandset.csm.builder;

import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.csm.CsmCommandBuilder;
import cna.sdk.calypso.commandset.csm.CsmRevision;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;

/**
 * This class provides the dedicated constructor to build the CSM Get Challenge
 * APDU command.
 *
 * @author Ixxi
 *
 */
public class CsmGetChallengeCmdBuild extends CsmCommandBuilder {

    /** The command reference. */
    private CalypsoCommands reference = CalypsoCommands.CSM_GET_CHALLENGE;

    /**
	 *  Instantiates a new CsmGetChallengeCmdBuild.
	 *
	 * @param revision of the CSM (SAM)
	 */
	public CsmGetChallengeCmdBuild(CsmRevision revision) {
	    super(revision);
		byte cla = csmRevision.getCla();
		byte p1 = 0x00;
		byte p2 = 0x00;
		byte expectedResponseLength = 0x04;

		CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, null, expectedResponseLength);
		request = RequestUtils.constructAPDURequest(calypsoRequest);

	}

}

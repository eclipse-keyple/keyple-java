package org.keyple.calypso.commandset.csm.builder;

import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.csm.CsmCommandBuilder;
import org.keyple.calypso.commandset.csm.CsmRevision;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Logger log = LoggerFactory.getLogger(this.getClass());
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

		log.debug("Creating " + this.getClass());
		CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, null, expectedResponseLength);
		request = RequestUtils.constructAPDURequest(calypsoRequest);

	}

}

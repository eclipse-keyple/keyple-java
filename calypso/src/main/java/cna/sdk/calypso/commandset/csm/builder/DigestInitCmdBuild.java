package cna.sdk.calypso.commandset.csm.builder;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.csm.CsmCommandBuilder;
import cna.sdk.calypso.commandset.csm.CsmRevision;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;
import cna.sdk.calypso.commandset.dto.KIF;
import cna.sdk.calypso.commandset.dto.KVC;

/** This class provides the dedicated constructor to build the CSM Digest Init APDU command.
 * @author Ixxi
 *
 */
public class DigestInitCmdBuild extends CsmCommandBuilder {

	/** The command reference. */
	private CalypsoCommands reference = CalypsoCommands.CSM_DIGEST_INIT;

	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(DigestInitCmdBuild.class);

	/**
	 * Instantiates a new DigestInitCmdBuild.
	 *
	 * @param revision of the CSM(SAM)
	 * @param workKeyKif from the OpenSessionCmdBuild response
	 * @param workKeyKVC from the OpenSessionCmdBuild response
	 * @param digestData all data out from the OpenSessionCmdBuild response
	 */
	public DigestInitCmdBuild(CsmRevision revision, KIF workKeyKif, KVC workKeyKVC, byte[] digestData) {
	    super(revision);
	    byte cla = csmRevision.getCla();
		byte p1 = 0x00;
		byte p2 = (byte) 0xFF;

		byte[] sessionKeyIdentifier = new byte[]{0x30, workKeyKVC.getValue()};

		byte[] dataIn =  ArrayUtils.addAll(sessionKeyIdentifier, digestData);

		CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, dataIn);
		request = RequestUtils.constructAPDURequest(calypsoRequest);

	}
}

package org.keyple.calypso.commandset.csm.builder;

import java.nio.charset.StandardCharsets;

import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.csm.CsmCommandBuilder;
import org.keyple.calypso.commandset.csm.CsmRevision;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class provides the dedicated constructor to build the Digest Authenticate APDU command.
 * @author Ixxi
 *
 */
public class DigestAuthenticateCmdBuild extends CsmCommandBuilder {

	/** The command reference. */
	private CalypsoCommands reference = CalypsoCommands.CSM_DIGEST_AUTHENTICATE;

	 private Logger log = LoggerFactory.getLogger(this.getClass());
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
		String g = new String(signaturePO, StandardCharsets.UTF_8);
		
		log.debug("Creating " + this.getClass() + Byte.valueOf(revision.getCla()) + " -- "+ g);
		CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, signaturePO);
		request = RequestUtils.constructAPDURequest(calypsoRequest);
	}

}

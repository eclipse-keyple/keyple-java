package org.keyple.calypso.commandset.csm.builder;

import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.csm.CsmCommandBuilder;
import org.keyple.calypso.commandset.csm.CsmRevision;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the dedicated constructor to build the CSM Digest Update
 * APDU command. This command have to be sent twice for each command executed
 * during a session. First time for the command sent and second time for the
 * answer received
 *
 * @author Ixxi
 *
 */
public class DigestUpdateCmdBuild extends CsmCommandBuilder {

    /** The command reference. */
    private CalypsoCommands reference = CalypsoCommands.CSM_DIGEST_UPDATE;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new DigestUpdateCmdBuild.
     *
     * @param revision
     *            of the CSM(SAM)
     * @param encryptedSession
     *            the encrypted session
     * @param digestData
     *            all bytes from command sent by the PO or response from the
     *            command
     */
    public DigestUpdateCmdBuild(CsmRevision revision, boolean encryptedSession, byte[] digestData) {
        super(revision);
        byte cla = csmRevision.getCla();
        byte p1 = 0x00;
        byte p2 = (byte) 0x00;
        log.debug("Creating " + this.getClass());
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, digestData);
        request = RequestUtils.constructAPDURequest(calypsoRequest);
    }

}

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
 * Multiple APDU command.
 *
 * @author Ixxi
 *
 */
public class DigestUpdateMultipleCmdBuild extends CsmCommandBuilder {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /** The command reference. */
    private CalypsoCommands reference = CalypsoCommands.CSM_DIGEST_UPDATE_MULTIPLE;
    /**
     * Instantiates a new DigestUpdateMultipleCmdBuild.
     *
     * @param revision
     *            the revision
     * @param digestData
     *            the digest data
     */
    public DigestUpdateMultipleCmdBuild(CsmRevision revision, byte[] digestData) {
        super(revision);
        log.debug("Creating " + this.getClass());
        
        byte cla = csmRevision.getCla();
        byte p1 = 0x00;
        byte p2 = (byte) 0x00;
        log.debug("Creating " + this.getClass());
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, digestData);
        request = RequestUtils.constructAPDURequest(calypsoRequest);
    }

}

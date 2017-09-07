package org.keyple.calypso.commandset.csm.builder;

import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.enumCmdWriteRecords;
import org.keyple.calypso.commandset.csm.CsmCommandBuilder;
import org.keyple.calypso.commandset.csm.CsmRevision;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the dedicated constructor to build the CSM Digest Close
 * APDU command.
 *
 * @author Ixxi
 *
 */
public class DigestCloseCmdBuild extends CsmCommandBuilder {

    /** The command reference. */
    private CalypsoCommands reference = CalypsoCommands.CSM_DIGEST_CLOSE;

    private Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * Instantiates a new DigestCloseCmdBuild .
     *
     * @param revision
     *            of the CSM(SAM)
     */
    public DigestCloseCmdBuild(CsmRevision revision) {
        super(revision);
        byte cla = csmRevision.getCla();
        byte p1 = 0x00;
        byte p2 = (byte) 0x00;
        byte expectedResponseLength;

        // default case C1
        expectedResponseLength = 0x04;
        if (revision == CsmRevision.S1D) {//94
            expectedResponseLength = 0x08;
        }
        log.debug("Creating " + this.getClass());
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, null, expectedResponseLength);
        request = RequestUtils.constructAPDURequest(calypsoRequest);
    }

}

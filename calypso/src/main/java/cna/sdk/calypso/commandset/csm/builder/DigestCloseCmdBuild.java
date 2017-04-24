package cna.sdk.calypso.commandset.csm.builder;

import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.csm.CsmCommandBuilder;
import cna.sdk.calypso.commandset.csm.CsmRevision;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;

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
        if (revision == CsmRevision.S1D) {
            expectedResponseLength = 0x08;
        }
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, null, expectedResponseLength);
        request = RequestUtils.constructAPDURequest(calypsoRequest);
    }

}

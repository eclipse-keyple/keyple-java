package cna.sdk.calypso.commandset.csm.builder;

import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.csm.CsmCommandBuilder;
import cna.sdk.calypso.commandset.csm.CsmRevision;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;

/**
 * This class provides the dedicated constructor to build the CSM Select
 * Diversifier APDU command.
 *
 * @author Ixxi
 *
 */
public class SelectDiversifierCmdBuild extends CsmCommandBuilder {

    /** The command reference. */
    private CalypsoCommands reference = CalypsoCommands.CSM_SELECT_DIVERSIFIER;

    /**
     * Instantiates a new SelectDiversifierCmdBuild.
     *
     * @param revision
     *            the CSM(SAM) revision
     * @param diversifier
     *            the application serial number
     */
    public SelectDiversifierCmdBuild(CsmRevision revision, byte[] diversifier) {
        super(revision);
        byte cla = csmRevision.getCla();
        byte p1 = 0x00;
        byte p2 = 0x00;
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, diversifier);
        request = RequestUtils.constructAPDURequest(calypsoRequest);

    }

}

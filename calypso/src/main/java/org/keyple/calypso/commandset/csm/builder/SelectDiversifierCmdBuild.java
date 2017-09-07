package org.keyple.calypso.commandset.csm.builder;

import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.csm.CsmCommandBuilder;
import org.keyple.calypso.commandset.csm.CsmRevision;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Logger log = LoggerFactory.getLogger(this.getClass());

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
        byte cla = revision.getCla();//csmRevision.getCla();
        byte p1 = 0x00;
        byte p2 = 0x00;
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, diversifier);

        log.debug("Creating " + this.getClass());
        request = RequestUtils.constructAPDURequest(calypsoRequest);

    }

}

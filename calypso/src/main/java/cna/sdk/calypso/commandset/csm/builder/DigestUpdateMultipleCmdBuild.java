package cna.sdk.calypso.commandset.csm.builder;

import cna.sdk.calypso.commandset.csm.CsmCommandBuilder;
import cna.sdk.calypso.commandset.csm.CsmRevision;

/**
 * This class provides the dedicated constructor to build the CSM Digest Update
 * Multiple APDU command.
 *
 * @author Ixxi
 *
 */
public class DigestUpdateMultipleCmdBuild extends CsmCommandBuilder {

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
    }

}

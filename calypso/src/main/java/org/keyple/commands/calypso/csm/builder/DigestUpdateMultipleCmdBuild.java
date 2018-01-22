package org.keyple.commands.calypso.csm.builder;

import org.keyple.commands.calypso.CalypsoCommands;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.csm.CsmCommandBuilder;
import org.keyple.commands.calypso.csm.CsmRevision;
import org.keyple.commands.calypso.dto.CalypsoRequest;
import org.keyple.commands.calypso.utils.RequestUtils;
import org.keyple.seproxy.ApduRequest;

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the CSM Digest Update
 * Multiple APDU command.
 *
 * @author Ixxi
 *
 */
public class DigestUpdateMultipleCmdBuild extends CsmCommandBuilder {

    /** The command. */
    private static CalypsoCommands command = CalypsoCommands.CSM_DIGEST_UPDATE_MULTIPLE;

    /**
     * Instantiates a new DigestUpdateMultipleCmdBuild.
     *
     * @param revision
     *            the revision
     * @param digestData
     *            the digest data
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public DigestUpdateMultipleCmdBuild(CsmRevision revision, byte[] digestData) throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = (byte) 0x80;
        byte p2 = (byte) 0x00;

        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, p2, digestData);
        request = RequestUtils.constructAPDURequest(calypsoRequest);
    }

    /**
     * Instantiates a new digest update multiple cmd build.
     *
     * @param request
     *            the request
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public DigestUpdateMultipleCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }
}

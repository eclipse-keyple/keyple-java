package cna.sdk.calypso.commandset.po.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.InconsistentCommandException;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;
import cna.sdk.calypso.commandset.po.PoCommandBuilder;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.seproxy.APDURequest;

/**
 * This class provides the dedicated constructor to build the Close Secure
 * Session APDU command.
 *
 * @author Ixxi
 */
public class CloseSessionCmdBuild extends PoCommandBuilder {

    /** The Constant logger. */
    static final Logger logger = LoggerFactory.getLogger(CloseSessionCmdBuild.class);

    private static CalypsoCommands defaultCommandReference = CalypsoCommands.PO_CLOSE_SESSION;

    CloseSessionCmdBuild(APDURequest request) throws InconsistentCommandException {
        super(defaultCommandReference, request);
    }

    /**
     * Instantiates a new CloseSessionCmdBuild depending of the revision of the
     * PO.
     *
     * @param revision
     *            of the PO
     * @param ratificationAsked
     *            the ratification asked
     * @param terminalSessionSignature
     *            the sam half session signature
     */
    public CloseSessionCmdBuild(PoRevision revision, boolean ratificationAsked, byte[] terminalSessionSignature) {
        super(revision, defaultCommandReference);
        byte cla = poRevision.getCla();

        byte p1 = ratificationAsked ? (byte) 0x80 : (byte) 0x00;
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, defaultCommandReference, p1, (byte) 0x00,
                terminalSessionSignature);
        request = RequestUtils.constructAPDURequest(calypsoRequest, 0);
    }
}

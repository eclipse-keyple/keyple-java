package org.keyple.calypso.commandset.po.builder;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.InconsistentCommandException;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.enumCmdSelectApplication;
import org.keyple.calypso.commandset.enumCmdWriteRecords;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.keyple.calypso.commandset.po.PoCommandBuilder;
import org.keyple.calypso.commandset.po.PoRevision;
import org.keyple.seproxy.APDURequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the dedicated constructor to build the Close Secure
 * Session APDU command.
 *
 * @author Ixxi
 */
public class CloseSessionCmdBuild extends PoCommandBuilder {

    /** The Constant logger. */
    private Logger log = LoggerFactory.getLogger(this.getClass());

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
        log.debug("Creating " + this.getClass());
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, defaultCommandReference, p1, (byte) 0x00,
                terminalSessionSignature);
        request = RequestUtils.constructAPDURequest(calypsoRequest, 0);
    }
}

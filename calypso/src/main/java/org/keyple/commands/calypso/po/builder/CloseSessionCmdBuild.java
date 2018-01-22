package org.keyple.commands.calypso.po.builder;

import org.keyple.commands.calypso.CalypsoCommands;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.dto.CalypsoRequest;
import org.keyple.commands.calypso.po.PoCommandBuilder;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.utils.RequestUtils;
import org.keyple.seproxy.ApduRequest;

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the Close Secure
 * Session APDU command.
 *
 * @author Ixxi
 */
public class CloseSessionCmdBuild extends PoCommandBuilder {

    /** The command. */
    private static CalypsoCommands command = CalypsoCommands.PO_CLOSE_SESSION;

    /**
     * Instantiates a new CloseSessionCmdBuild depending of the revision of the
     * PO.
     *
     * @param revision            of the PO
     * @param ratificationAsked            the ratification asked
     * @param terminalSessionSignature            the sam half session signature
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public CloseSessionCmdBuild(PoRevision revision, boolean ratificationAsked, byte[] terminalSessionSignature)
            throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        // The optional parameter terminalSessionSignature could contain 4 or 8
        // bytes.
        if (terminalSessionSignature != null) {
            if (terminalSessionSignature.length != 4 && terminalSessionSignature.length != 8) {
                throw new InconsistentCommandException();
            }
        }

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;

        byte p1 = ratificationAsked ? (byte) 0x80 : (byte) 0x00;
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, (byte) 0x00, terminalSessionSignature);
        request = RequestUtils.constructAPDURequest(calypsoRequest, 0);
    }

    /**
     * Instantiates a new close session cmd build.
     *
     * @param request the request
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public CloseSessionCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }
}

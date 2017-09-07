package org.keyple.calypso.commandset.po.builder;

import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.InconsistentCommandException;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.enumTagUtils;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.keyple.calypso.commandset.po.PoCommandBuilder;
import org.keyple.calypso.commandset.po.PoRevision;
import org.keyple.calypso.commandset.po.SendableInSession;
import org.keyple.seproxy.APDURequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements SendableInSession, it provides the dedicated
 * constructor to build the Get data APDU commands.
 *
 *
 * @author Ixxi
 */
public class GetListEfCmdBuild extends PoCommandBuilder implements SendableInSession {

    /** The Constant logger. */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static CalypsoCommands defaultCommandReference = CalypsoCommands.PO_GET_DATA_FCI;

    GetListEfCmdBuild() {
        commandReference = defaultCommandReference;
    }

    GetListEfCmdBuild(APDURequest request) throws InconsistentCommandException {
        super(defaultCommandReference, request);
    }

    /**
     * Instantiates a new GetDataFciCmdBuild.
     *
     * @param revision
     *            the PO revision
     */
    public GetListEfCmdBuild(PoRevision revision) {
        super(revision, defaultCommandReference);
        byte cla = poRevision.getCla();
        log.debug("Creating " + this.getClass());
        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, commandReference, enumTagUtils.LIST_OF_EF.getTagbyte1(),
                enumTagUtils.LIST_OF_EF.getTagbyte2(), null, (byte) 0x00);
        request = RequestUtils.constructAPDURequest(calypsoRequest);
    }

    /*
     * (non-Javadoc)
     *
     * @see cna.sdk.calypso.commandset.po.SendableInSession#getAPDURequest()
     */
    @Override
    public APDURequest getAPDURequest() {
        return request;
    }
}

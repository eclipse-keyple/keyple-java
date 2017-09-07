package org.keyple.calypso.commandset.po.builder;

import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.InconsistentCommandException;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.keyple.calypso.commandset.po.PoCommandBuilder;
import org.keyple.calypso.commandset.po.PoRevision;
import org.keyple.calypso.commandset.po.SendableInSession;
import org.keyple.seproxy.APDURequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PoGetChallengeCmdBuild. This class provides the dedicated
 * constructor to build the PO Get Challenge.
 *
 * @author Ixxi
 *
 */
public class PoGetChallengeCmdBuild extends PoCommandBuilder implements SendableInSession {

    private static CalypsoCommands defaultCommandReference = CalypsoCommands.PO_GET_CHALLENGE;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    PoGetChallengeCmdBuild(APDURequest request) throws InconsistentCommandException {
        super(defaultCommandReference, request);
    }

    /**
     * Instantiates a new PoGetChallengeCmdBuild.
     *
     * @param revision
     *            the revision of the PO
     */
    public PoGetChallengeCmdBuild(PoRevision revision) {
        super(revision, defaultCommandReference);

        byte cla = revision.getCla();

        CalypsoRequest request;
        APDURequest apduRequest;

        byte p1 = (byte) 0x01;
        byte p2 = (byte) 0x10;
        byte[] dataIn = null;
        byte optionnalLe = (byte) 0x08;
        log.debug("Creating " + this.getClass());
        request = new CalypsoRequest(cla, commandReference, p1, p2, dataIn, optionnalLe);
        apduRequest = RequestUtils.constructAPDURequest(request);

        this.request = apduRequest;

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

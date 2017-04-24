package cna.sdk.calypso.commandset.po.builder;

import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.InconsistentCommandException;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;
import cna.sdk.calypso.commandset.po.PoCommandBuilder;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.calypso.commandset.po.SendableInSession;
import cna.sdk.seproxy.APDURequest;

/**
 * The Class PoGetChallengeCmdBuild. This class provides the dedicated
 * constructor to build the PO Get Challenge.
 *
 * @author Ixxi
 *
 */
public class PoGetChallengeCmdBuild extends PoCommandBuilder implements SendableInSession {

    private static CalypsoCommands defaultCommandReference = CalypsoCommands.PO_GET_CHALLENGE;

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

        byte cla = poRevision.getCla();

        CalypsoRequest request;
        APDURequest apduRequest;

        byte p1 = (byte) 0x01;
        byte p2 = (byte) 0x10;
        byte[] dataIn = null;
        byte optionnalLe = (byte) 0x08;
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

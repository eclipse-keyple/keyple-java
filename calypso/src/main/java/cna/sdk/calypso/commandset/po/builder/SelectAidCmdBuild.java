package cna.sdk.calypso.commandset.po.builder;

import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.InconsistentCommandException;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.dto.AID;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;
import cna.sdk.calypso.commandset.po.PoCommandBuilder;
import cna.sdk.seproxy.APDURequest;

/**
 * The Class SelectAidCmdBuild. This class provides the dedicated constructor to
 * build a select aid APDU command
 *
 * @author Ixxi
 */
public class SelectAidCmdBuild extends PoCommandBuilder {

    private static CalypsoCommands defaultCommandReference = CalypsoCommands.PO_SELECT_APPLICATION;

       SelectAidCmdBuild(APDURequest request) throws InconsistentCommandException {
        super(defaultCommandReference, request);
    }

    /**
     * Instantiates a new SelectAidCmdBuild.
     *
     * @param aid
     *            the aid of the application to select
     */
    public SelectAidCmdBuild(AID aid) {
        super(defaultCommandReference);
        byte cla = poRevision.getCla();
        byte p1 = (byte) 0x04;
        byte p2 = (byte) 0x00;
        byte[] dataIn = aid.getValue();
        CalypsoRequest request;
        APDURequest apduRequest;

        request = new CalypsoRequest(cla, commandReference, p1, p2, dataIn);
        apduRequest = RequestUtils.constructAPDURequest(request);

        this.request = apduRequest;

    }
}

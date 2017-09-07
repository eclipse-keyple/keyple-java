package org.keyple.calypso.commandset.po.builder;

import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.InconsistentCommandException;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.enumTagUtils;
import org.keyple.calypso.commandset.dto.AID;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.keyple.calypso.commandset.po.PoCommandBuilder;
import org.keyple.seproxy.APDURequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SelectAidCmdBuild. This class provides the dedicated constructor to
 * build a select aid APDU command
 *
 * @author Ixxi
 */
public class SelectAidCmdBuild extends PoCommandBuilder {

    private static CalypsoCommands defaultCommandReference = CalypsoCommands.PO_SELECT_APPLICATION;

    private Logger log = LoggerFactory.getLogger(this.getClass());

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
        byte p1 = enumTagUtils.AID_OF_CURRENT_DF_D.getTagbyte1();
        byte p2 = enumTagUtils.AID_OF_CURRENT_DF_D.getTagbyte2();
        byte[] dataIn = aid.getValue();
        CalypsoRequest request;
        APDURequest apduRequest;
        log.debug("Creating " + this.getClass());

        request = new CalypsoRequest(cla, commandReference, p1, p2, dataIn);
        apduRequest = RequestUtils.constructAPDURequest(request);

        this.request = apduRequest;

    }
}

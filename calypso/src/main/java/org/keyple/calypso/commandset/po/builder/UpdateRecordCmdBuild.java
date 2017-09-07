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
 * The Class UpdateRecordCmdBuild. This class provides the dedicated constructor
 * to build the Update Record APDU command.
 *
 * @author Ixxi
 *
 */
public class UpdateRecordCmdBuild extends PoCommandBuilder implements SendableInSession {

    /** The Constant logger. */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static CalypsoCommands defaultCommandReference = CalypsoCommands.PO_UPDATE_RECORD;

    UpdateRecordCmdBuild(APDURequest request) throws InconsistentCommandException {
        super(defaultCommandReference, request);
    }

    /**
     * Instantiates a new UpdateRecordCmdBuild.
     *
     * @param revision
     *            the revision of the PO
     * @param recordNumber
     *            the record number to update
     * @param sfi
     *            the sfi to select
     * @param newRecordData
     *            the new record data to write
     */
    public UpdateRecordCmdBuild(PoRevision revision, byte recordNumber, byte sfi, byte[] newRecordData) {
        super(revision, defaultCommandReference);
        byte cla = poRevision.getCla();
        byte p1 = recordNumber;
        byte p2 = (sfi == 0) ? (byte) 0x04 : (byte) ((byte) (sfi * 8) + 4);
        byte[] dataIn = newRecordData;
        log.debug("Creating " + this.getClass());
        CalypsoRequest request = new CalypsoRequest(cla, commandReference, p1, p2, dataIn);
        APDURequest apduRequest = RequestUtils.constructAPDURequest(request);

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

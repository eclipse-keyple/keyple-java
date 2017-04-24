package cna.sdk.calypso.commandset.po.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.InconsistentCommandException;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;
import cna.sdk.calypso.commandset.po.PoCommandBuilder;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.calypso.commandset.po.SendableInSession;
import cna.sdk.seproxy.APDURequest;

/**
 * The Class UpdateRecordCmdBuild. This class provides the dedicated constructor
 * to build the Update Record APDU command.
 *
 * @author Ixxi
 *
 */
public class UpdateRecordCmdBuild extends PoCommandBuilder implements SendableInSession {

    /** The Constant logger. */
    static final Logger logger = LoggerFactory.getLogger(UpdateRecordCmdBuild.class);

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

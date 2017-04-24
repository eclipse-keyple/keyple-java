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
 * The Class ReadRecordsCmdBuild. This class provides the dedicated constructor
 * to build the Read Records APDU command.
 *
 * @author Ixxi
 *
 */
public class ReadRecordsCmdBuild extends PoCommandBuilder implements SendableInSession {

    private static CalypsoCommands defaultCommandReference = CalypsoCommands.PO_READ_RECORDS;

    ReadRecordsCmdBuild(APDURequest request) throws InconsistentCommandException {
        super(defaultCommandReference, request);
    }

    /**
     * Instantiates a new read records cmd build.
     *
     * @param revision
     *            the revision of the PO
     * @param firstRecordNumber
     *            the record number to read (or first record to read in case of
     *            several records)
     * @param sfi
     *            the sfi top select
     * @param expectedLength
     *            the expected lenght of the record(s)
     */
    public ReadRecordsCmdBuild(PoRevision revision, byte firstRecordNumber, byte sfi, byte expectedLength) {
        super(revision, defaultCommandReference);

        CalypsoRequest calypsoRequest;
        APDURequest apduRequest;
        CalypsoCommands reference = CalypsoCommands.PO_READ_RECORDS;
        byte cla = poRevision.getCla();
        byte p1 = firstRecordNumber;
        byte p2 = (sfi == 0) ? (byte) 0x05 : (byte) ((byte) (sfi * 8) + 5);
        byte[] dataIn = null;

        calypsoRequest = new CalypsoRequest(cla, reference, p1, p2, dataIn, expectedLength);
        apduRequest = RequestUtils.constructAPDURequest(calypsoRequest);
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

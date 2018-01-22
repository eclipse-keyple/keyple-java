package org.keyple.commands.calypso.po.builder;

import org.keyple.commands.calypso.CalypsoCommands;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.dto.CalypsoRequest;
import org.keyple.commands.calypso.po.PoCommandBuilder;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.po.SendableInSession;
import org.keyple.commands.calypso.utils.RequestUtils;
import org.keyple.seproxy.ApduRequest;

/**
 * The Class ReadRecordsCmdBuild. This class provides the dedicated constructor
 * to build the Read Records APDU command.
 *
 * @author Ixxi
 *
 */
public class ReadRecordsCmdBuild extends PoCommandBuilder implements SendableInSession {


    /** The command. */
    private static CalypsoCommands command = CalypsoCommands.PO_READ_RECORDS;

    /**
     * Instantiates a new read records cmd build.
     *
     * @param revision
     *            the revision of the PO
     * @param firstRecordNumber
     *            the record number to read (or first record to read in case of
     *            several records)
     * @param readJustOneRecord
     *            the read just one record
     * @param sfi
     *            the sfi top select
     * @param expectedLength
     *            the expected lenght of the record(s)
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public ReadRecordsCmdBuild(PoRevision revision, byte firstRecordNumber, boolean readJustOneRecord, byte sfi,
            byte expectedLength) throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }

        if (firstRecordNumber < 1) {
            throw new InconsistentCommandException();
        }

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;
        byte p1 = firstRecordNumber;
        byte[] dataIn = null;
        byte p2 = (sfi == (byte) 0x00) ? (byte) 0x05 : (byte) ((byte) (sfi * 8) + 5);
        if (readJustOneRecord) {
            p2 = (byte) (p2 - (byte) 0x01);
        }

        CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, p2, dataIn, expectedLength);
        ApduRequest apduRequest = RequestUtils.constructAPDURequest(calypsoRequest);
        this.request = apduRequest;
    }

    /*
     * (non-Javadoc)
     *
     * @see cna.sdk.calypso.commandset.po.SendableInSession#getAPDURequest()
     */
    @Override
    public ApduRequest getAPDURequest() {
        return request;
    }

    /**
     * Instantiates a new read records cmd build.
     *
     * @param request
     *            the request
     * @throws InconsistentCommandException
     *             the inconsistent command exception
     */
    public ReadRecordsCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}

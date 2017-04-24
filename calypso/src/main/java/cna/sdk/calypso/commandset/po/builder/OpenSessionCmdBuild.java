package cna.sdk.calypso.commandset.po.builder;

import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.InconsistentCommandException;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;
import cna.sdk.calypso.commandset.po.PoCommandBuilder;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.seproxy.APDURequest;

/**
 * The Class OpenSessionCmdBuild. This class provides the dedicated constructor
 * to build the Open Secure Session APDU command.
 *
 * @author Ixxi
 *
 */
public class OpenSessionCmdBuild extends PoCommandBuilder {

    private static CalypsoCommands defaultCommandReference = CalypsoCommands.PO_OPEN_SESSION;

    OpenSessionCmdBuild(APDURequest request) throws InconsistentCommandException {
        super(defaultCommandReference, request);
    }

    /**
     * Instantiates a new OpenSessionCmdBuild.
     *
     * @param revision
     *            the revision of the PO
     * @param keyIndex
     *            the key index
     * @param samChallenge
     *            the sam challenge returned by the CSM Get Challenge APDU
     *            command
     * @param recordNumberToRead
     *            the record number to read
     * @param sfiToSelect
     *            the sfi to select
     */
    public OpenSessionCmdBuild(PoRevision revision, byte keyIndex, byte[] samChallenge, byte sfiToSelect,
            byte recordNumberToRead) {
        super(revision, defaultCommandReference);
        byte p1;
        byte p2;

        switch (poRevision) {

        case REV2_4:
            p1 = (byte) (0x80 + (recordNumberToRead * 8) + keyIndex);
            p2 = (byte) (sfiToSelect * 8);
            break;

        case REV3_2:
            p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
            p2 = (byte) ((sfiToSelect * 8) + 2);
            break;

        case REV3_1:
        default:
            p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
            p2 = (byte) ((sfiToSelect * 8) + 1);
            break;
        }

        CalypsoRequest request = new CalypsoRequest(poRevision.getCla(), commandReference, p1, p2, samChallenge);
        this.request = RequestUtils.constructAPDURequest(request);

    }
}

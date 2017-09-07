package org.keyple.calypso.commandset.po.builder;

import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.InconsistentCommandException;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.keyple.calypso.commandset.po.PoCommandBuilder;
import org.keyple.calypso.commandset.po.PoRevision;
import org.keyple.seproxy.APDURequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class OpenSessionCmdBuild. This class provides the dedicated constructor
 * to build the Open Secure Session APDU command.
 *
 * @author Ixxi
 *
 */
public class OpenSessionCmdBuild extends PoCommandBuilder {

    private static CalypsoCommands defaultCommandReference = CalypsoCommands.PO_OPEN_SESSION;

    private Logger log = LoggerFactory.getLogger(this.getClass());

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
        log.debug("Creating " + this.getClass());
        byte p1;
        byte p2;
        byte Lc;

        switch (poRevision) {
//
        case REV2_4:
            p1 = (byte) (0x80 + (recordNumberToRead * 8) + keyIndex);
            p2 = (byte) (sfiToSelect * 8);
            Lc = (byte) 0x04;
            break;
//
        case REV3_2:
            p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
            p2 = (byte) ((sfiToSelect * 8) + 2);
            Lc = (byte) 0x09;
            break;
//
        case REV3_1:
        default:
            p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
            p2 = (byte) ((sfiToSelect * 8) + 1);
    		Lc = (byte) 0x04;
            break;
        }

        CalypsoRequest request = new CalypsoRequest(revision.getCla() , commandReference, p1, p2, samChallenge);
//        request.setLc(Lc);
        this.request = RequestUtils.constructAPDURequest(request);

    }
}

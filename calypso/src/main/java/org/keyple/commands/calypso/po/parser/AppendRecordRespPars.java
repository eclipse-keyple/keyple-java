package org.keyple.commands.calypso.po.parser;

import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * The Class UpdateRecordRespPars. This class provides status code properties of
 * an Update Record response. the Update Record APDU command
 *
 * @author Ixxi .
 */
public class AppendRecordRespPars extends ApduResponseParser {

    /**
     * Instantiates a new AppendRecordRespPars.
     *
     * @param response
     *            the response from the Update Records APDU command
     */
    public AppendRecordRespPars(ApduResponse response) {

        super(response);
        initStatusTable();
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x6B, (byte) 0x00 },
                new StatusProperties(false, "P1 or P2 vaue not supported."));
        statusTable.put(new byte[] { (byte) 0x67, (byte) 0x00 },
                new StatusProperties(false, "Lc value not supported."));
        statusTable.put(new byte[] { (byte) 0x64, (byte) 0x00 },
                new StatusProperties(false, "Too many modifications in session."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x81 },
                new StatusProperties(false, "The current EF is not a Cyclic EF."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x82 },
                new StatusProperties(false, "Security conditions not fulfilled (no session, wrong key)."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x85 },
                new StatusProperties(false, "Access forbidden (Never access mode, DF is invalidated, etc..)."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x86 },
                new StatusProperties(false, "Command not allowed (no current EF)."));
        statusTable.put(new byte[] { (byte) 0x6A, (byte) 0x82 }, new StatusProperties(false, "File not found."));
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Correct execution."));
    }

}

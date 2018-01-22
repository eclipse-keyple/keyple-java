package org.keyple.commands.calypso.csm.parser;

import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * This class provides the dedicated constructor to parse the Digest
 * Authenticate response.
 *
 * @author Ixxi
 *
 */
public class DigestAuthenticateRespPars extends ApduResponseParser {

    /**
     * Instantiates a new DigestAuthenticateRespPars.
     *
     * @param response
     *            from the CSM DigestAuthenticateCmdBuild
     */
    public DigestAuthenticateRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Successful execution."));
    }

}

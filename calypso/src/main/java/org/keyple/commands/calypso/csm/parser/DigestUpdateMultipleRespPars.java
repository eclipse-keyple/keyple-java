package org.keyple.commands.calypso.csm.parser;

import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * This class provides status code properties and the getters to access to the
 * structured fields of a Digest Update Multiple response.
 *
 * @author Ixxi
 *
 */
public class DigestUpdateMultipleRespPars extends ApduResponseParser {

    /**
     * Instantiates a new DigestUpdateMultipleRespPars.
     *
     * @param response
     *            the response
     */
    public DigestUpdateMultipleRespPars(ApduResponse response) {
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

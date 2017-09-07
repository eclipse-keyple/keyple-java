package org.keyple.calypso.commandset.csm.parser;

import org.keyple.calypso.commandset.ApduResponseParser;
import org.keyple.seproxy.APDUResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public DigestAuthenticateRespPars(APDUResponse response) {
        super(response);
        initStatusTable();
        logger = LoggerFactory.getLogger(this.getClass());
        logger.debug(this.getStatusInformation());
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Successful execution."));
    }

}

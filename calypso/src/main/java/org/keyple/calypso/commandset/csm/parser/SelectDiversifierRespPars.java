package org.keyple.calypso.commandset.csm.parser;

import org.keyple.calypso.commandset.ApduResponseParser;
import org.keyple.seproxy.APDUResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides status code properties and the getters to access to the
 * structured fields of a Select Diversifier response.
 *
 * @author Ixxi
 *
 */
public class SelectDiversifierRespPars extends ApduResponseParser {


    /**
     * Instantiates a new SelectDiversifierRespPars.
     *
     * @param response
     *            the response
     */
    public SelectDiversifierRespPars(APDUResponse response) {
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

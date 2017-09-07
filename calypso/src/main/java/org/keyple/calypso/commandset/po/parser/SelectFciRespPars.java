package org.keyple.calypso.commandset.po.parser;

import org.keyple.calypso.commandset.ApduResponseParser;
import org.keyple.calypso.commandset.ResponseUtils;
import org.keyple.calypso.commandset.dto.FCI;
import org.keyple.seproxy.APDUResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SelectFciRespPars.
 *
 * @author Ixxi
 */
public class SelectFciRespPars extends ApduResponseParser {

    /** The fci. */
    private FCI fci;


    /**
     * Instantiates a new SelectFciRespPars.
     *
     * @param response
     *            the response from select AID APDU command
     */

    public SelectFciRespPars(APDUResponse response) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            fci = ResponseUtils.toFCI(response.getbytes());
        }
        logger = LoggerFactory.getLogger(this.getClass());
        logger.debug(this.getStatusInformation());
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Successful execution."));
    }

    /**
     * Gets the fci.
     *
     * @return the fci
     */
    public FCI getFci() {
        return fci;
    }

}

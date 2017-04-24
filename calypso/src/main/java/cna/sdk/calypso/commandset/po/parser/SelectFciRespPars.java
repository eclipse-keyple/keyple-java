package cna.sdk.calypso.commandset.po.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.dto.FCI;
import cna.sdk.seproxy.APDUResponse;

/**
 * The Class SelectFciRespPars.
 *
 * @author Ixxi
 */
public class SelectFciRespPars extends ApduResponseParser {

    /** The fci. */
    private FCI fci;

    Logger logger = LoggerFactory.getLogger(this.getClass());

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

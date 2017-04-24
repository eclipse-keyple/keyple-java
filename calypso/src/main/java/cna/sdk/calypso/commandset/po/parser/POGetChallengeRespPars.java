package cna.sdk.calypso.commandset.po.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.seproxy.APDUResponse;

/**
 * The Class POGetChallengeRespPars. This class provides status code properties
 * and the getters to access to the structured fields of a Get Challenge
 * response.
 *
 * @author Ixxi
 *
 */
public class POGetChallengeRespPars extends ApduResponseParser {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new POGetChallengeRespPars.
     *
     * @param response
     *            the response from PO Get Challenge APDU Command
     */
    public POGetChallengeRespPars(APDUResponse response) {
        super(response);
        initStatusTable();
        logger.debug(this.getStatusInformation());
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Successful execution."));
    }
}

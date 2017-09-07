package org.keyple.calypso.commandset.csm.parser;

import org.keyple.calypso.commandset.ApduResponseParser;
import org.keyple.calypso.commandset.ResponseUtils;
import org.keyple.calypso.commandset.dto.SamHalfSessionSignature;
import org.keyple.seproxy.APDUResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides status code properties and the getters to access to the
 * structured fields of a Digest Close response.
 *
 * @author Ixxi
 *
 */
public class DigestCloseRespPars extends ApduResponseParser {

    /** Challenge from the response of DigestCloseCmdBuild. */
    private SamHalfSessionSignature samHalfSessionSignature;

    /**
     * Instantiates a new DigestCloseRespPars.
     *
     * @param response
     *            from the DigestCloseCmdBuild
     */
    public DigestCloseRespPars(APDUResponse response) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            samHalfSessionSignature = ResponseUtils.toSamHalfSessionSignature(response.getbytes());
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
     * Gets the sam half session signature.
     *
     * @return the sam half session signature
     */
    public SamHalfSessionSignature getSamHalfSessionSignature() {
        return samHalfSessionSignature;
    }

}

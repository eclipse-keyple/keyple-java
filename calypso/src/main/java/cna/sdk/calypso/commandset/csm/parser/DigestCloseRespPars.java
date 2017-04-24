package cna.sdk.calypso.commandset.csm.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.dto.SamHalfSessionSignature;
import cna.sdk.seproxy.APDUResponse;

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
    Logger logger = LoggerFactory.getLogger(this.getClass());

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

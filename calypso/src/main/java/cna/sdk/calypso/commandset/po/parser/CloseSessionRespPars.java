package cna.sdk.calypso.commandset.po.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.dto.POHalfSessionSignature;
import cna.sdk.seproxy.APDUResponse;

/**
 * This class provides status code properties and the getters to access to the
 * structured fields of a Close Secure Session response.
 *
 * @author Ixxi
 *
 */
public class CloseSessionRespPars extends ApduResponseParser {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The po half session signature. */
    private POHalfSessionSignature poHalfSessionSignature;

    /**
     * Instantiates a new CloseSessionRespPars from the response.
     *
     * @param response
     *            from CloseSessionCmdBuild
     */
    public CloseSessionRespPars(APDUResponse response) {
        super(response);
        initStatusTable();
        poHalfSessionSignature = ResponseUtils.toPoHalfSessionSignature(response.getbytes());
        logger.debug(this.getStatusInformation());
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x67, (byte) 0x00 }, new StatusProperties(false,
                "Lc value not supported (e.g. Lc=4 with a Revision 3.2 mode for Open Secure Session)."));
        statusTable.put(new byte[] { (byte) 0x6B, (byte) 0x00 },
                new StatusProperties(false, "P1 or P2 value not supported."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x88 }, new StatusProperties(false, "incorrect signature."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x85 }, new StatusProperties(false, "No session was opened."));
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Successful execution."));
    }

    /**
     * Gets the po half session signature.
     *
     * @return a POHalfSessionSignature
     */
    public POHalfSessionSignature getPoHalfSessionSignature() {
        return poHalfSessionSignature;
    }

}

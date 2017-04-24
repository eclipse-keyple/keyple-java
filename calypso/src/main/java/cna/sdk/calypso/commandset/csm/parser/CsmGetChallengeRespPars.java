package cna.sdk.calypso.commandset.csm.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.dto.SamChallenge;
import cna.sdk.seproxy.APDUResponse;

/**
 * This class provides status code properties and the getters to access to the
 * structured fields of a Get Challenge response.
 *
 * @author Ixxi
 *
 */
public class CsmGetChallengeRespPars extends ApduResponseParser {

    /** The CSM(SAM) challenge. */
    private SamChallenge challenge;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new CsmGetChallengeRespPars .
     *
     * @param response
     *            of the CsmGetChallengeCmdBuild
     */
    public CsmGetChallengeRespPars(APDUResponse response) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            challenge = ResponseUtils.toSamChallenge(response.getbytes());
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
     * Gets the challenge.
     *
     * @return the challenge
     */
    public SamChallenge getChallenge() {
        return challenge;
    }

}

package org.keyple.calypso.commandset.csm.parser;

import org.keyple.calypso.commandset.ApduResponseParser;
import org.keyple.calypso.commandset.ResponseUtils;
import org.keyple.calypso.commandset.dto.SamChallenge;
import org.keyple.seproxy.APDUResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Gets the challenge.
     *
     * @return the challenge
     */
    public SamChallenge getChallenge() {
        return challenge;
    }

}

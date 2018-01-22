package org.keyple.commands.calypso.csm.parser;

import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * This class provides status code properties and the getters to access to the
 * structured fields of a Get Challenge response.
 *
 * @author Ixxi
 *
 */
public class CsmGetChallengeRespPars extends ApduResponseParser {

    /** The CSM(SAM) challenge. */
    private byte[] challenge;

    /**
     * Instantiates a new CsmGetChallengeRespPars .
     *
     * @param response
     *            of the CsmGetChallengeCmdBuild
     */
    public CsmGetChallengeRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            challenge = response.getbytes();
        }
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
    public byte[] getChallenge() {
        if (challenge != null) {
            return challenge.clone();
        } else {
            return new byte[0];
        }
    }

}

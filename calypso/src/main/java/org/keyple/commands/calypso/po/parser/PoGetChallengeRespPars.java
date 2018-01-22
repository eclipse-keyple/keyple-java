package org.keyple.commands.calypso.po.parser;

import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * The Class PoGetChallengeRespPars. This class provides status code properties
 * and the getters to access to the structured fields of a Get Challenge
 * response.
 *
 * @author Ixxi
 *
 */
public class PoGetChallengeRespPars extends ApduResponseParser {

    /**
     * Instantiates a new PoGetChallengeRespPars.
     *
     * @param response
     *            the response from PO Get Challenge APDU Command
     */
    public PoGetChallengeRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Successful execution."));
    }

    /**
     * Gets the po challenge.
     *
     * @return the po challenge
     */
    public byte[] getPoChallenge() {
        if (isSuccessful()) {
            return getApduResponse().getbytes();
        }
        return null;
    }
}

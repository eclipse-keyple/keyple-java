package org.keyple.commands.calypso.csm.parser;

import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * This class provides status code properties and the getters to access to the
 * structured fields of a Digest Update response.
 *
 * @author Ixxi
 *
 */
public class DigestUpdateRespPars extends ApduResponseParser {

    private byte[] processedData;

    /**
     * Instantiates a new DigestUpdateRespPars.
     *
     * @param response
     *            the response
     */
    public DigestUpdateRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            this.processedData = response.getbytes();
        }
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Successful execution."));
    }

    public byte[] getProcessedData() {
        if (processedData != null) {
            return processedData.clone();
        } else {
            return new byte[0];
        }
    }

}

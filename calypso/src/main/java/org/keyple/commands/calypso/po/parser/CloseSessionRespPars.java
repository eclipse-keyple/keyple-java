package org.keyple.commands.calypso.po.parser;

import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.commands.calypso.dto.POHalfSessionSignature;
import org.keyple.commands.calypso.utils.ResponseUtils;
import org.keyple.seproxy.ApduResponse;

/**
 * This class provides status code properties and the getters to access to the
 * structured fields of a Close Secure Session response.
 *
 * @author Ixxi
 *
 */
public class CloseSessionRespPars extends ApduResponseParser {

    /** The po half session signature. */
    private POHalfSessionSignature poHalfSessionSignature;

    /**
     * Instantiates a new CloseSessionRespPars from the response.
     *
     * @param response
     *            from CloseSessionCmdBuild
     */
    public CloseSessionRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
        poHalfSessionSignature = ResponseUtils.toPoHalfSessionSignature(response.getbytes());
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

    public boolean hasPostponedData() {
        return poHalfSessionSignature.getPostponedData().length != 0;
    }

    public byte[] getPostponedData() {
        return poHalfSessionSignature.getPostponedData();
    }

    public byte[] getSignatureLo() {
        return poHalfSessionSignature.getValue();
    }

}

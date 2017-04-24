package cna.sdk.calypso.commandset.po.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.dto.SecureSession;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.seproxy.APDUResponse;

/**
 * The Class OpenSessionRespPars. This class provides status code properties and
 * the getters to access to the structured fields of an Open Secure Session
 * response.
 *
 * @author Ixxi
 *
 */
public class OpenSessionRespPars extends ApduResponseParser {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The secure session. */
    private SecureSession secureSession;

    /**
     * Instantiates a new OpenSessionRespPars.
     *
     * @param response
     *            the response from Open secure session APDU command
     * @param revision
     *            the revision of the PO
     */
    public OpenSessionRespPars(APDUResponse response, PoRevision revision) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            switch (revision) {
            case REV3_2:
            case REV3_1:
                secureSession = ResponseUtils.toSecureSession(response.getbytes());
                break;
            case REV2_4:
                if (response.isSuccessful()) {
                    secureSession = ResponseUtils.toSecureSessionRev2(response.getbytes());
                }
                break;
            default:
                break;
            }
        }
        logger.debug(this.getStatusInformation());
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x67, (byte) 0x00 },
                new StatusProperties(false, "Lc value not supported."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x00 },
                new StatusProperties(false, "Transaction Counter is 0"));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x81 },
                new StatusProperties(false, "Command forbidden (read requested and current EF is a Binary file)."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x82 }, new StatusProperties(false,
                "Security conditions not fulfilled (PIN code not presented, encryption required). "));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x85 },
                new StatusProperties(false, "Access forbidden (Never access mode, Session already opened)."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x86 },
                new StatusProperties(false, "Command not allowed (read requested and no current EF)."));
        statusTable.put(new byte[] { (byte) 0x6A, (byte) 0x81 }, new StatusProperties(false, "Wrong key index."));
        statusTable.put(new byte[] { (byte) 0x6A, (byte) 0x82 }, new StatusProperties(false, "File not found."));
        statusTable.put(new byte[] { (byte) 0x6A, (byte) 0x83 },
                new StatusProperties(false, "Record not found (record index is above NumRec)."));
        statusTable.put(new byte[] { (byte) 0x6B, (byte) 0x00 },
                new StatusProperties(false, "P1 or P2 value not supported (e.g. REV.3.2 mode not supported)."));
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Successful execution."));
    }

    /**
     * Gets the secure session.
     *
     * @return the secure session
     */
    public SecureSession getSecureSession() {
        return secureSession;
    }

}


package cna.sdk.calypso.commandset.po.parser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.enumCmdReadRecords;
import cna.sdk.calypso.commandset.dto.Record;
import cna.sdk.seproxy.APDUResponse;

/**
 * The Class ReadRecordsRespPars. This class provides status code properties and
 * the getters to access to the structured fields of a Read Records response.
 *
 * @author Ixxi
 *
 */
public class ReadRecordsRespPars extends ApduResponseParser {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The records. */
    private List<Record> records;

    /**
     * Instantiates a new ReadRecordsRespPars.
     *
     * @param response
     *            the response from read records APDU command
     * @param readCommand
     *            the read command type
     */
    public ReadRecordsRespPars(APDUResponse response, enumCmdReadRecords readCommand) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            records = ResponseUtils.toRecords(response.getbytes(), readCommand);
        }
        logger.debug(this.getStatusInformation());
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x81 },
                new ApduResponseParser.StatusProperties(false, "Command forbidden on binary files"));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x82 }, new StatusProperties(false,
                "Security conditions not fulfilled (PIN code not presented, encryption required)."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x85 }, new StatusProperties(false,
                "Access forbidden (Never access mode, stored value log file and a stored value operation was done during the current session)."));
        statusTable.put(new byte[] { (byte) 0x69, (byte) 0x86 },
                new StatusProperties(false, "Command not allowed (no current EF)"));
        statusTable.put(new byte[] { (byte) 0x6A, (byte) 0x82 }, new StatusProperties(false, "File not found"));
        statusTable.put(new byte[] { (byte) 0x6A, (byte) 0x83 },
                new StatusProperties(false, "Record not found (record index is 0, or above NumRec"));
        statusTable.put(new byte[] { (byte) 0x6B, (byte) 0x00 }, new StatusProperties(false, "P2 value not supported"));
        statusTable.put(new byte[] { (byte) 0x6C, (byte) 0xFF }, new StatusProperties(false, "Le value incorrect"));
        statusTable.put(new byte[] { (byte) 0x90, (byte) 0x00 }, new StatusProperties(true, "Successful execution."));
    }

    /**
     * Gets the records.
     *
     * @return the records
     */
    public List<Record> getRecords() {
        return records;
    }

}

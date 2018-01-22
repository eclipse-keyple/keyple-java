
package org.keyple.commands.calypso.po.parser;

import java.util.List;

import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.commands.calypso.dto.Record;
import org.keyple.commands.calypso.utils.ResponseUtils;
import org.keyple.seproxy.ApduResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class ReadRecordsRespPars. This class provides status code properties and
 * the getters to access to the structured fields of a Read Records response.
 *
 * @author Ixxi
 *
 */
public class ReadRecordsRespPars extends ApduResponseParser {

    /** The records. */
    private List<Record> records;

    /**
     * Instantiates a new ReadRecordsRespPars.
     *
     * @param response            the response from read records APDU command
     */
    public ReadRecordsRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            records = ResponseUtils.toRecords(response.getbytes(), false);
        }
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
     * Gets the records number.
     *
     * @return the records number
     */
    public int getRecordsNumber() {
        return records.size();
    }

    /**
     * Gets the records data.
     *
     * @return the records data
     */
    public byte[][] getRecordsData() {
        if (records != null) {
            byte[][] ret = new byte[records.size()][];
            for (int i = 0; i < records.size(); i++) {
                ret[i] = records.get(i).getData();
            }
            return ret;
        }
        return null;
    }

}

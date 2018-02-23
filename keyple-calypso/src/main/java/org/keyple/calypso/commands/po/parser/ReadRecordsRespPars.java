/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.keyple.calypso.commands.utils.ResponseUtils;
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * Read Records (00B2) response parser. See specs: Calypso / page 89 / 9.4.7 Read Records
 */
public class ReadRecordsRespPars extends ApduResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(ApduResponseParser.STATUS_TABLE);
        m.put(0x6981, new StatusProperties(false, "Command forbidden on binary files"));
        m.put(0x6982, new StatusProperties(false,
                "Security conditions not fulfilled (PIN code not presented, encryption required)."));
        m.put(0x6985, new StatusProperties(false,
                "Access forbidden (Never access mode, stored value log file and a stored value operation was done during the current session)."));
        m.put(0x6986, new StatusProperties(false, "Command not allowed (no current EF)"));
        m.put(0x6A82, new StatusProperties(false, "File not found"));
        m.put(0x6A83, new StatusProperties(false,
                "Record not found (record index is 0, or above NumRec"));
        m.put(0x6B00, new StatusProperties(false, "P2 value not supported"));
        m.put(0x6CFF, new StatusProperties(false, "Le value incorrect"));
        m.put(0x9000, new StatusProperties(true, "Successful execution."));
        STATUS_TABLE = m;
    }

    Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /** The records. */
    private List<Record> records;

    /**
     * Instantiates a new ReadRecordsRespPars.
     *
     * @param response the response from read records APDU command
     */
    public ReadRecordsRespPars(ApduResponse response) {
        super(response);
        if (isSuccessful()) {
            records = parseRecords(response.getBytesBeforeStatus(), false);
        }
    }

    /**
     * Method to get the Records from the response.
     *
     * @param apduResponse the apdu response
     * @param oneRecordOnly the one record only
     * @return a Maps of Records
     */
    private static List<Record> parseRecords(byte[] apduResponse, boolean oneRecordOnly) {
        List<Record> records = new ArrayList<Record>();
        if (oneRecordOnly) {
            records.add(new Record(apduResponse, 0));
        } else {
            int i = 0;
            while (i < apduResponse.length) {
                if (i + 2 + apduResponse[i + 1] > apduResponse.length - 1) {
                    records.add(new Record(
                            ResponseUtils.subArray(apduResponse, i + 2, apduResponse.length - 1),
                            apduResponse[i]));
                } else {
                    records.add(new Record(ResponseUtils.subArray(apduResponse, i + 2,
                            i + 2 + apduResponse[i + 1]), apduResponse[i]));
                }
                // add data length to iterator
                i += apduResponse[i + 1];
                // add byte of data length to iterator
                i++;
                // add byte of num record to iterator
                i++;
            }
        }
        return records;
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

    /**
     * The Class Record. The data in the files are organized in records of equal size.
     */
    public static class Record {

        /** The data. */
        private byte[] data;

        /** The record number. */
        private int recordNumber;

        /**
         * Instantiates a new Record.
         *
         * @param data the data
         * @param recordNumber the record number
         */
        public Record(byte[] data, int recordNumber) {
            super();
            this.data = (data == null ? null : data.clone());
            this.recordNumber = recordNumber;
        }

        /**
         * Gets the data.
         *
         * @return the data
         */
        public byte[] getData() {
            return data.clone();
        }

        /**
         * Gets the record number.
         *
         * @return the record number
         */
        public int getRecordNumber() {
            return recordNumber;
        }

    }
}

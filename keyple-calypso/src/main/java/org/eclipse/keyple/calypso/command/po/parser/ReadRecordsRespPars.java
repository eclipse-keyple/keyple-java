/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.keyple.calypso.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * Read Records (00B2) response parser. See specs: Calypso / page 89 / 9.4.7 Read Records
 */
public class ReadRecordsRespPars extends AbstractApduResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
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

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
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
            records = parseRecords(response.getDataOut());
        }
    }

    /**
     * Method to get the Records from the response.
     *
     * @param apduResponse the apdu response
     * @return a Maps of Records
     */
    private static List<Record> parseRecords(ByteBuffer apduResponse) {
        List<Record> records = new ArrayList<Record>();
        int i = 0;
        while (i < apduResponse.limit()) {
            if (i + 2 + apduResponse.get(i + 1) > apduResponse.limit() - 1) {
                records.add(new Record(apduResponse.get(i),
                        ByteBufferUtils.subIndex(apduResponse, i + 2, apduResponse.limit() - 1)));
            } else {
                records.add(new Record(apduResponse.get(i), ByteBufferUtils.subIndex(apduResponse,
                        i + 2, i + 2 + apduResponse.get(i + 1))));
            }
            // add data length to iterator
            i += apduResponse.get(i + 1);
            // add byte of data length to iterator
            i++;
            // add byte of num record to iterator
            i++;
        }
        return records;
    }

    // fclairamb: I might be missing something. The doc says:
    // <Record Number:1 byte> < Length:1 byte> <Record data: Length bytes>
    private static List<Record> parseRecordsV2(ByteBuffer apdu) {
        List<Record> records = new ArrayList<Record>();

        while (apdu.hasRemaining()) {
            byte recordNb = apdu.get();
            byte len = apdu.get();
            records.add(new Record(recordNb, ByteBufferUtils.subLen(apdu, 0, len)));
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


    public List<Record> getRecords() {
        return records;
    }

    /**
     * The Class Record. The data in the files are organized in records of equal size.
     */
    public static class Record {

        /** The data. */
        private final ByteBuffer data;

        /** The record number. */
        private final int recordNumber;

        /**
         * Instantiates a new Record.
         *
         * @param recordNumber the record number
         * @param data the data
         */
        Record(int recordNumber, ByteBuffer data) {
            super();
            this.data = data;
            this.recordNumber = recordNumber;
        }

        /**
         * Gets the data.
         *
         * @return the data
         */
        public ByteBuffer getData() {
            return data;
        }

        /**
         * Gets the record number.
         *
         * @return the record number
         */
        public int getRecordNumber() {
            return recordNumber;
        }

        @Override
        public String toString() {
            return String.format("Record{nb=%d,data=%s}", recordNumber,
                    ByteBufferUtils.toHex(data));
        }
    }
}

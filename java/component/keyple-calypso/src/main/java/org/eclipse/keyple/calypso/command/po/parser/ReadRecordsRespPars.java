/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.parser;

import java.util.*;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * Read Records (00B2) response parser. See specs: Calypso / page 89 / 9.4.7 Read Records The
 * {@link ReadRecordsRespPars} class holds the data resulting from a Read Records command. It
 * provides methods to retrieve these data according to the file structure profile specified in the
 * command preparation step: SINGLE or MULTIPLE RECORD or COUNTER.
 */
public final class ReadRecordsRespPars extends AbstractApduResponseParser {

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

    /** Type of data to parse: record data or counter, single or multiple */
    private ReadDataStructure readDataStructure;
    /** Number of the first record read */
    private byte recordNumber;

    /**
     * Instantiates a new ReadRecordsRespPars.
     *
     * @param recordNumber the record number
     * @param readDataStructure the type of content in the response to parse
     */
    public ReadRecordsRespPars(byte recordNumber, ReadDataStructure readDataStructure) {
        this.recordNumber = recordNumber;
        this.readDataStructure = readDataStructure;
    }

    /**
     * Indicates whether the parser is associated with a counter file.
     * 
     * @return true or false
     */
    public boolean isCounterFile() {
        return readDataStructure == ReadDataStructure.SINGLE_COUNTER
                || readDataStructure == ReadDataStructure.MULTIPLE_COUNTER;
    }

    /**
     * Parses the Apdu response as a data record (single or multiple), retrieves the records and
     * place it in an map.
     * <p>
     * The map index follows the PO specification, i.e. starts at 1 for the first record.
     * <p>
     * An empty map is returned if no data is available.
     * 
     * @return a map of records
     * @exception IllegalStateException if the parser has not been initialized
     */
    public SortedMap<Integer, byte[]> getRecords() {
        if (!isInitialized()) {
            throw new IllegalStateException("Parser not initialized.");
        }
        SortedMap<Integer, byte[]> records = new TreeMap<Integer, byte[]>();
        if (!response.isSuccessful()) {
            /* return an empty map */
            // TODO should we raise an exception?
            return records;
        }
        if (readDataStructure == ReadDataStructure.SINGLE_RECORD_DATA) {
            records.put((int) recordNumber, response.getDataOut());
        } else if (readDataStructure == ReadDataStructure.MULTIPLE_RECORD_DATA) {
            byte[] apdu = response.getDataOut();
            int apduLen = apdu.length;
            int index = 0;
            while (apduLen > 0) {
                byte recordNb = apdu[index++];
                byte len = apdu[index++];
                records.put((int) recordNb, Arrays.copyOfRange(apdu, index, index + len));
                index = index + len;
                apduLen = apduLen - 2 - len;
            }
        } else {
            throw new IllegalStateException("The file is a counter file.");
        }
        return records;
    }

    /**
     * Parses the Apdu response as a counter record (single or multiple), retrieves the counters
     * values and place it in an map indexed with the counter number.
     * <p>
     * The map index follows the PO specification, i.e. starts at 1 for the first counter.
     * <p>
     * An empty map is returned if no data is available.
     *
     * @return a map of counters
     * @exception IllegalStateException if the parser has not been initialized
     */
    public SortedMap<Integer, Integer> getCounters() {
        if (!isInitialized()) {
            throw new IllegalStateException("Parser not initialized.");
        }
        SortedMap<Integer, Integer> counters = new TreeMap<Integer, Integer>();
        if (!response.isSuccessful()) {
            /* return an empty map */
            // TODO should we raise an exception?
            return counters;
        }
        if (readDataStructure == ReadDataStructure.SINGLE_COUNTER
                || readDataStructure == ReadDataStructure.MULTIPLE_COUNTER) {
            byte[] apdu = response.getDataOut();
            int numberOfCounters = apdu.length / 3;
            int index = 0;
            int key = 1; /* the first counter is indexed 1 */
            for (int i = 0; i < numberOfCounters; i++) {
                /*
                 * convert the 3-byte unsigned value of the counter into an integer (up to 2^24 -1)
                 */
                int counterValue = ((apdu[index + 0] & 0xFF) * 65536)
                        + ((apdu[index + 1] & 0xFF) * 256) + (apdu[index + 2] & 0xFF);
                counters.put(key++, counterValue);
                index = index + 3;
            }

        } else {
            throw new IllegalStateException("The file is a data file.");
        }
        return counters;
    }

    @Override
    public String toString() {
        String string;
        if (isInitialized()) {
            switch (readDataStructure) {
                case SINGLE_RECORD_DATA: {
                    SortedMap<Integer, byte[]> recordMap = getRecords();
                    string = String.format("Single record data: {RECORD = %d, DATA = %s}",
                            recordMap.firstKey(),
                            ByteArrayUtils.toHex(recordMap.get(recordMap.firstKey())));
                }
                    break;
                case MULTIPLE_RECORD_DATA: {
                    SortedMap<Integer, byte[]> recordMap = getRecords();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Multiple record data: ");
                    Set records = recordMap.keySet();
                    for (Iterator it = records.iterator(); it.hasNext();) {
                        Integer record = (Integer) it.next();
                        sb.append(String.format("{RECORD = %d, DATA = %s}", record,
                                ByteArrayUtils.toHex(recordMap.get(record))));
                        if (it.hasNext()) {
                            sb.append(", ");
                        }
                    }
                    string = sb.toString();
                }
                    break;
                case SINGLE_COUNTER: {
                    SortedMap<Integer, Integer> counterMap = getCounters();
                    string = String.format("Single counter: {COUNTER = %d, VALUE = %d}",
                            counterMap.firstKey(), counterMap.get(counterMap.firstKey()));
                }
                    break;
                case MULTIPLE_COUNTER: {
                    SortedMap<Integer, Integer> counterMap = getCounters();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Multiple counter: ");
                    Set counters = counterMap.keySet();
                    for (Iterator it = counters.iterator(); it.hasNext();) {
                        Integer counter = (Integer) it.next();
                        sb.append(String.format("{COUNTER = %d, VALUE = %d}", counter,
                                counterMap.get(counter)));
                        if (it.hasNext()) {
                            sb.append(", ");
                        }
                    }
                    string = sb.toString();
                }
                    break;
                default:
                    throw new IllegalStateException("Unexpected data structure");
            }
        } else {
            string = "Not initialized.";
        }
        return string;
    }
}

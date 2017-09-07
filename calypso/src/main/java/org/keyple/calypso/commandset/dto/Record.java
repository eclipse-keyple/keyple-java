package org.keyple.calypso.commandset.dto;

import org.keyple.calypso.utils.LogUtils;

/**
 * The Class Record.
 * The data in the files are organized in records of equal size.
 */
public class Record {

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
        this.data = data;
        this.recordNumber = recordNumber;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public byte[] getData() {
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
        return "(" + recordNumber + ") " + LogUtils.hexaToString(data);
    }

}

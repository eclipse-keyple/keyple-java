/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

/**
 * The Class Record. The data in the files are organized in records of equal size.
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

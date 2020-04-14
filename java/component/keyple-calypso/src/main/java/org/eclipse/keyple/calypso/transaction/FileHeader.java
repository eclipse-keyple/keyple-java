/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;

import java.util.Arrays;
import org.eclipse.keyple.core.util.Assert;

/**
 * The class {@code FileHeader} contains all header information of a Calypso EF.
 */
public class FileHeader {

    private final short lid;
    private final int recordsNumber;
    private final int recordSize;
    private final FileType type;
    private final byte[] accessConditions;
    private final byte[] keyIndexes;
    private final Short sharedReference;

    /** Private constructor */
    private FileHeader(FileHeaderBuilder builder) {

        Assert.getInstance()//
                .notNull(builder.lid, "lid")//
                .notNull(builder.recordsNumber, "recordsNumber")//
                .notNull(builder.recordSize, "recordSize")//
                .notNull(builder.type, "type")//
                .notEmpty(builder.accessConditions, "accessConditions")//
                .notEmpty(builder.keyIndexes, "keyIndexes");

        this.lid = builder.lid;
        this.recordsNumber = builder.recordsNumber;
        this.recordSize = builder.recordSize;
        this.type = builder.type;
        this.accessConditions = builder.accessConditions;
        this.keyIndexes = builder.keyIndexes;
        this.sharedReference = builder.sharedReference;
    }

    /** The EF type enum */
    public enum FileType {
        LINEAR, BINARY, CYCLIC, COUNTERS, SIMULATED_COUNTERS;
    }

    /**
     * Builder pattern
     */
    static final class FileHeaderBuilder {

        private Short lid;
        private Integer recordsNumber;
        private Integer recordSize;
        private FileType type;
        private Short sharedReference;
        private byte[] accessConditions;
        private byte[] keyIndexes;

        /** Private constructor */
        private FileHeaderBuilder() {}

        FileHeaderBuilder lid(Short lid) {
            this.lid = lid;
            return this;
        }

        FileHeaderBuilder recordsNumber(Integer recordsNumber) {
            this.recordsNumber = recordsNumber;
            return this;
        }

        FileHeaderBuilder recordSize(Integer recordSize) {
            this.recordSize = recordSize;
            return this;
        }

        FileHeaderBuilder type(FileType type) {
            this.type = type;
            return this;
        }

        FileHeaderBuilder sharedReference(Short sharedReference) {
            this.sharedReference = sharedReference;
            return this;
        }

        FileHeaderBuilder accessConditions(byte[] accessConditions) {
            this.accessConditions = accessConditions;
            return this;
        }

        FileHeaderBuilder keyIndexes(byte[] keyIndexes) {
            this.keyIndexes = keyIndexes;
            return this;
        }

        FileHeader build() {
            return new FileHeader(this);
        }
    }

    /**
     * Gets the associated LID.
     *
     * @return a not null value
     */
    public short getLid() {
        return lid;
    }

    /**
     * Gets the number of records :
     * <ul>
     * <li>For a Counter file, the number of records is corresponding to the number of counters.<br>
     * Extra bytes (rest of the division of the file size by 3) aren't accessible.</li>
     * <li>For a Binary file, the number of records is always 1.</li>
     * </ul>
     *
     * @return a not null value
     */
    public int getRecordsNumber() {
        return recordsNumber;
    }

    /**
     * Gets the size of a record :
     * <ul>
     * <li>For a Counter file, the record size is always 3 bytes.<br>
     * Extra bytes (rest of the division of the file size by 3) aren't accessible.</li>
     * <li>For a Binary file, the size of the record is corresponding to the file size.</li>
     * </ul>
     *
     * @return a not null value
     */
    public int getRecordSize() {
        return recordSize;
    }

    /**
     * Gets the file type.
     *
     * @return a not null value
     */
    public FileType getType() {
        return type;
    }

    /**
     * Gets the access conditions.
     *
     * @return a not null value
     */
    public byte[] getAccessConditions() {
        return accessConditions;
    }

    /**
     * Gets the keys indexes.
     *
     * @return a not null value
     */
    public byte[] getKeyIndexes() {
        return keyIndexes;
    }

    /**
     * @return true if the EF is a shared file
     */
    public boolean isShared() {
        return sharedReference != null;
    }

    /**
     * Gets the shared reference of a shared file.
     *
     * @return a not null value if file is shared, or null
     */
    public Short getSharedReference() {
        return sharedReference;
    }

    /**
     * @return a new builder instance
     */
    static FileHeaderBuilder builder() {
        return new FileHeaderBuilder();
    }

    /**
     * Comparison is based on field "lid".
     *
     * @param o the object to compare
     * @return the comparison evaluation
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FileHeader that = (FileHeader) o;

        return lid == that.lid;
    }

    /**
     * Comparison is based on field "lid".
     *
     * @return the comparison evaluation
     */
    @Override
    public int hashCode() {
        return lid;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileHeader{");
        sb.append("lid=").append(lid);
        sb.append(", recordsNumber=").append(recordsNumber);
        sb.append(", recordSize=").append(recordSize);
        sb.append(", type=").append(type);
        sb.append(", accessConditions=").append(Arrays.toString(accessConditions));
        sb.append(", keyIndexes=").append(Arrays.toString(keyIndexes));
        sb.append(", sharedReference=").append(sharedReference);
        sb.append('}');
        return sb.toString();
    }
}

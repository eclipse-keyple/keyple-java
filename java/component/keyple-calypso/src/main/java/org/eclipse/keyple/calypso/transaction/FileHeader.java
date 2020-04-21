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

import java.io.Serializable;
import java.util.Arrays;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * The class {@code FileHeader} contains all metadata of a Calypso EF.
 * 
 * @since 0.9
 */
public class FileHeader implements Serializable, Cloneable {

    private final short lid;
    private final int recordsNumber;
    private final int recordSize;
    private final FileType type;
    private final byte[] accessConditions;
    private final byte[] keyIndexes;
    private final Short sharedReference;

    /** Private constructor */
    private FileHeader(FileHeaderBuilder builder) {
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
     * (package-private)<br>
     * Builder pattern
     */
    static final class FileHeaderBuilder {

        private short lid;
        private int recordsNumber;
        private int recordSize;
        private FileType type;
        private byte[] accessConditions;
        private byte[] keyIndexes;
        private Short sharedReference;

        /** Private constructor */
        private FileHeaderBuilder() {}

        /**
         * (package-private)<br>
         * Sets the LID.
         *
         * @param lid the LID
         * @return the builder instance
         */
        FileHeaderBuilder lid(short lid) {
            this.lid = lid;
            return this;
        }

        /**
         * (package-private)<br>
         * Sets the number of records.
         *
         * @param recordsNumber the number of records (should be {@code >=} 1)
         * @return the builder instance
         */
        FileHeaderBuilder recordsNumber(int recordsNumber) {
            this.recordsNumber = recordsNumber;
            return this;
        }

        /**
         * (package-private)<br>
         * Sets the size of a record.
         *
         * @param recordSize the size of a record (should be {@code >=} 1)
         * @return the builder instance
         */
        FileHeaderBuilder recordSize(int recordSize) {
            this.recordSize = recordSize;
            return this;
        }

        /**
         * (package-private)<br>
         * Sets the file type.
         *
         * @param type the file type (should be not null)
         * @return the builder instance
         */
        FileHeaderBuilder type(FileType type) {
            this.type = type;
            return this;
        }

        /**
         * (package-private)<br>
         * Sets a reference to the provided access conditions byte array.
         *
         * @param accessConditions the access conditions (should be not null and 4 bytes length)
         * @return the builder instance
         */
        FileHeaderBuilder accessConditions(byte[] accessConditions) {
            this.accessConditions = accessConditions;
            return this;
        }

        /**
         * (package-private)<br>
         * Sets a reference to the provided key indexes byte array.
         *
         * @param keyIndexes the key indexes (should be not null and 4 bytes length)
         * @return the builder instance
         */
        FileHeaderBuilder keyIndexes(byte[] keyIndexes) {
            this.keyIndexes = keyIndexes;
            return this;
        }

        /**
         * (package-private)<br>
         * Sets the shared reference.
         *
         * @param sharedReference the shared reference
         * @return the builder instance
         */
        FileHeaderBuilder sharedReference(short sharedReference) {
            this.sharedReference = sharedReference;
            return this;
        }

        /**
         * (package-private)<br>
         * Build a new {@code FileHeader}.
         *
         * @return a new instance
         */
        FileHeader build() {
            return new FileHeader(this);
        }
    }

    /**
     * Gets the associated LID.
     *
     * @return the LID
     * @since 0.9
     */
    public short getLid() {
        return lid;
    }

    /**
     * Gets the number of records :
     * <ul>
     * <li>For a Counter file, the number of records is always 1.<br>
     * Extra bytes (rest of the division of the file size by 3) aren't accessible.</li>
     * <li>For a Binary file, the number of records is always 1.</li>
     * </ul>
     *
     * @return the number of records
     * @since 0.9
     */
    public int getRecordsNumber() {
        return recordsNumber;
    }

    /**
     * Gets the size of a record :
     * <ul>
     * <li>For a Counter file, the record size is the original size of the record #1.<br>
     * Extra bytes (rest of the division of the file size by 3) aren't accessible.</li>
     * <li>For a Binary file, the size of the record is corresponding to the file size.</li>
     * </ul>
     *
     * @return the size of a record
     * @since 0.9
     */
    public int getRecordSize() {
        return recordSize;
    }

    /**
     * Gets the file type.
     *
     * @return a not null file type
     * @since 0.9
     */
    public FileType getType() {
        return type;
    }

    /**
     * Gets a reference to the access conditions.
     *
     * @return a not empty byte array reference
     * @since 0.9
     */
    public byte[] getAccessConditions() {
        return accessConditions;
    }

    /**
     * Gets a reference to the keys indexes.
     *
     * @return a not empty byte array reference
     * @since 0.9
     */
    public byte[] getKeyIndexes() {
        return keyIndexes;
    }

    /**
     * Returns true if EF is a shared file.
     *
     * @return true if the EF is a shared file
     * @since 0.9
     */
    public boolean isShared() {
        return sharedReference != null;
    }

    /**
     * Gets the shared reference of a shared file.
     *
     * @return null if file is not shared
     * @since 0.9
     */
    public Short getSharedReference() {
        return sharedReference;
    }

    /**
     * (package-private)<br>
     * Gets a new builder.
     *
     * @return a new builder instance
     */
    static FileHeaderBuilder builder() {
        return new FileHeaderBuilder();
    }

    /**
     * Gets a clone of the current instance.
     *
     * @return a not null object
     * @since 0.9
     */
    @Override
    public FileHeader clone() {
        return FileHeader.builder()//
                .lid(lid)//
                .recordsNumber(recordsNumber)//
                .recordSize(recordSize)//
                .type(type)//
                .accessConditions(Arrays.copyOf(accessConditions, accessConditions.length))//
                .keyIndexes(Arrays.copyOf(keyIndexes, keyIndexes.length))//
                .sharedReference(sharedReference)//
                .build();
    }

    /**
     * Comparison is based on field "lid".
     *
     * @param o the object to compare
     * @return the comparison evaluation
     * @since 0.9
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
     * @return the hashcode
     * @since 0.9
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
        sb.append(", accessConditions=").append("0x").append(ByteArrayUtil.toHex(accessConditions));
        sb.append(", keyIndexes=").append("0x").append(ByteArrayUtil.toHex(keyIndexes));
        sb.append(", sharedReference=").append(sharedReference);
        sb.append('}');
        return sb.toString();
    }
}

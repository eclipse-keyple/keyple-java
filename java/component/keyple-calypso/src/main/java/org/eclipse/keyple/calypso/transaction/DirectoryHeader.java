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
import java.util.HashMap;
import java.util.NoSuchElementException;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * The class {@code DirectoryHeader} contains all metadata of a Calypso DF.
 * 
 * @since 0.9
 */
public class DirectoryHeader {

    private final short lid;
    private final byte[] accessConditions;
    private final byte[] keyIndexes;
    private final HashMap<SessionAccessLevel, Byte> kif;
    private final HashMap<SessionAccessLevel, Byte> kvc;

    /** Private constructor */
    private DirectoryHeader(DirectoryHeaderBuilder builder) {
        this.lid = builder.lid;
        this.accessConditions = builder.accessConditions;
        this.keyIndexes = builder.keyIndexes;
        this.kif = builder.kif;
        this.kvc = builder.kvc;
    }

    /**
     * (package-private)<br>
     * Builder pattern
     */
    static final class DirectoryHeaderBuilder {

        private short lid;
        private byte[] accessConditions;
        private byte[] keyIndexes;
        private final HashMap<SessionAccessLevel, Byte> kif =
                new HashMap<SessionAccessLevel, Byte>();
        private final HashMap<SessionAccessLevel, Byte> kvc =
                new HashMap<SessionAccessLevel, Byte>();

        /** Private constructor */
        private DirectoryHeaderBuilder() {}

        /**
         * (package-private)<br>
         * Sets the LID.
         *
         * @param lid the LID
         * @return the builder instance
         */
        DirectoryHeaderBuilder lid(short lid) {
            this.lid = lid;
            return this;
        }

        /**
         * (package-private)<br>
         * Sets a copy of the provided access conditions byte array.
         *
         * @param accessConditions the access conditions (should be not null and 4 bytes length)
         * @return the builder instance
         */
        DirectoryHeaderBuilder accessConditions(byte[] accessConditions) {
            this.accessConditions = Arrays.copyOf(accessConditions, accessConditions.length);
            return this;
        }

        /**
         * (package-private)<br>
         * Sets a copy of the provided key indexes byte array.
         *
         * @param keyIndexes the key indexes (should be not null and 4 bytes length)
         * @return the builder instance
         */
        DirectoryHeaderBuilder keyIndexes(byte[] keyIndexes) {
            this.keyIndexes = Arrays.copyOf(keyIndexes, keyIndexes.length);
            return this;
        }

        /**
         * (package-private)<br>
         * Add a KIF.
         *
         * @param level the KIF session access level (should be not null)
         * @param kif the KIF value
         * @return the builder instance
         */
        DirectoryHeaderBuilder kif(SessionAccessLevel level, byte kif) {
            this.kif.put(level, kif);
            return this;
        }

        /**
         * (package-private)<br>
         * Add a KVC.
         *
         * @param level the KVC session access level (should be not null)
         * @param kvc the KVC value
         * @return the builder instance
         */
        DirectoryHeaderBuilder kvc(SessionAccessLevel level, byte kvc) {
            this.kvc.put(level, kvc);
            return this;
        }

        /**
         * (package-private)<br>
         * Build a new {@code DirectoryHeader}.
         *
         * @return a new instance
         */
        DirectoryHeader build() {
            return new DirectoryHeader(this);
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
     * Gets a copy of access conditions.
     *
     * @return a not empty byte array copy
     * @since 0.9
     */
    public byte[] getAccessConditions() {
        return Arrays.copyOf(accessConditions, accessConditions.length);
    }

    /**
     * Gets a copy of keys indexes.
     *
     * @return a not empty byte array copy
     * @since 0.9
     */
    public byte[] getKeyIndexes() {
        return Arrays.copyOf(keyIndexes, keyIndexes.length);
    }

    /**
     * Gets the KIF associated to the provided session access level.
     *
     * @param level the session access level (should be not null)
     * @return a not null value
     * @throws IllegalArgumentException if level is null.
     * @throws NoSuchElementException if KIF is not found.
     * @since 0.9
     */
    public byte getKif(SessionAccessLevel level) {

        Assert.getInstance().notNull(level, "level");

        Byte result = kif.get(level);
        if (result == null) {
            throw new NoSuchElementException(
                    "KIF not found for session access level [" + level + "].");
        }
        return result;
    }

    /**
     * Gets the KVC associated to the provided session access level.
     *
     * @param level the session access level (should be not null)
     * @return a not null value
     * @throws IllegalArgumentException if level is null.
     * @throws NoSuchElementException if KVC is not found.
     * @since 0.9
     */
    public byte getKvc(SessionAccessLevel level) {

        Assert.getInstance().notNull(level, "level");

        Byte result = kvc.get(level);
        if (result == null) {
            throw new NoSuchElementException(
                    "KVC not found for session access level [" + level + "].");
        }
        return result;
    }

    /**
     * (package-private)<br>
     * Gets a new builder.
     *
     * @return a new builder instance
     */
    static DirectoryHeaderBuilder builder() {
        return new DirectoryHeaderBuilder();
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

        DirectoryHeader that = (DirectoryHeader) o;

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
        final StringBuilder sb = new StringBuilder("DirectoryHeader{");
        sb.append("lid=").append(lid);
        sb.append(", accessConditions=").append("0x").append(ByteArrayUtil.toHex(accessConditions));
        sb.append(", keyIndexes=").append("0x").append(ByteArrayUtil.toHex(keyIndexes));
        sb.append(", kif=").append(kif);
        sb.append(", kvc=").append(kvc);
        sb.append('}');
        return sb.toString();
    }
}

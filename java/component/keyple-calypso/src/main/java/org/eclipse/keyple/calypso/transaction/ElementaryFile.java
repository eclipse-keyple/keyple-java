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

import org.eclipse.keyple.core.util.Assert;

/**
 * The class {@code ElementaryFile} contains the description of a Calypso EF.
 * 
 * @since 0.9
 */
public class ElementaryFile {

    private final byte sfi;
    private FileHeader header;
    private FileData data;

    /**
     * (package-private)<br>
     * Constructor
     *
     * @param sfi the associated SFI
     * @since 0.9
     */
    ElementaryFile(byte sfi) {
        this.sfi = sfi;
    }

    /**
     * Gets the associated SFI.
     *
     * @return the SFI
     * @since 0.9
     */
    public byte getSfi() {
        return sfi;
    }

    /**
     * Gets the file header.
     *
     * @return a header reference or null if header is not yet set.
     * @since 0.9
     */
    public FileHeader getHeader() {
        return header;
    }

    /**
     * Sets the file header.
     *
     * @param header the file header (should be not null)
     * @return the current instance.
     * @throws IllegalArgumentException if header is null.
     * @since 0.9
     */
    public ElementaryFile setHeader(FileHeader header) {
        Assert.getInstance().notNull(header, "header");
        this.header = header;
        return this;
    }

    /**
     * Gets the file data.
     *
     * @return a data reference of null if data is not yet set.
     * @since 0.9
     */
    public FileData getData() {
        return data;
    }

    /**
     * Sets the file data.
     *
     * @param data the file data (should be not null)
     * @return the current instance.
     * @throws IllegalArgumentException if data is null.
     * @since 0.9
     */
    public ElementaryFile setData(FileData data) {
        Assert.getInstance().notNull(data, "data");
        this.data = data;
        return this;
    }

    /**
     * Comparison is based on field "sfi".
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

        ElementaryFile that = (ElementaryFile) o;

        return sfi == that.sfi;
    }

    /**
     * Comparison is based on field "sfi".
     *
     * @return the hashcode
     * @since 0.9
     */
    @Override
    public int hashCode() {
        return sfi;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ElementaryFile{");
        sb.append("sfi=").append(sfi);
        sb.append(", header=").append(header);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}

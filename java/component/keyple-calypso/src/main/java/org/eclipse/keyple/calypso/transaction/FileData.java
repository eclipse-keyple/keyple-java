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
import java.util.SortedMap;
import org.eclipse.keyple.core.util.Assert;


/**
 * The class {@code FileData} contains data content of a Calypso EF.
 */
public class FileData {

    private SortedMap<Integer, byte[]> records;

    /**
     * Gets the content of record #1.
     *
     * @return an empty byte array if record #1 is not set.
     */
    public byte[] getContent() {
        return getContent(1);
    }

    /**
     * Gets the content of a specific record.
     *
     * @param numRecord the record number (should be greater or equal to 1)
     * @return a reference of the original array or an empty byte array if record #{@code numRecord} is not set.
     */
    public byte[] getContent(int numRecord) {
        Assert.getInstance().greaterOrEqual(numRecord, 1, "numRecord");
        byte[] content = records.get(numRecord);
        return content != null ? content : new byte[0];
    }

    /**
     * Gets the sub-content of a specific record from {@code dataOffset} to {@code dataLength}.
     *
     * @param numRecord the record number (should be greater or equal to 1)
     * @param dataOffset the offset index (should be greater or equal to 0)
     * @param dataLength the data length (should be greater or equal to 1)
     * @return a not null byte array copy eventually padded with zeros to obtain the required length.
     */
    public byte[] getContent(int numRecord, int dataOffset, int dataLength) {

        Assert.getInstance()//
                .greaterOrEqual(numRecord, 1, "numRecord")//
                .greaterOrEqual(dataOffset, 0, "dataOffset")//
                .greaterOrEqual(dataLength, 1, "dataLength");

        byte[] content = records.get(numRecord);
        if (content == null) {
            content = new byte[0];
        }
        return Arrays.copyOfRange(content, dataOffset, dataOffset + dataLength);
    }
}

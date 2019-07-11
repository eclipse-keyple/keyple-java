/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.integration.poData;

import java.util.Arrays;

public class RecordData {

    private String index;

    private byte[] value;

    public RecordData(int recordIndex, byte[] recordData) {

        index = String.format("%02X", recordIndex);

        value = Arrays.copyOf(recordData, recordData.length);;
    }

    public String getIndex() {
        return index;
    }

    public byte[] getValue() {
        return value;
    }
}

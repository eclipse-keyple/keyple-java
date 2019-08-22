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
package org.eclipse.keyple.integration.samData;

import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.integration.IntegrationUtils;
import org.slf4j.Logger;

public class SamCounterData {

    private String index;

    private int indexDec;

    private String counterValue;

    private long counterValueDec;

    private String ceilingValue;

    private long ceilingValueDec;

    public SamCounterData(int counterIndex, byte[] counterData, byte[] ceilingData) {

        index = String.format("%02X", counterIndex);

        indexDec = counterIndex;

        counterValue = ByteArrayUtil.toHex(counterData);

        counterValueDec = IntegrationUtils.bytesToLong(counterData, 3);

        ceilingValue = ByteArrayUtil.toHex(ceilingData);

        ceilingValueDec = IntegrationUtils.bytesToLong(ceilingData, 3);
    }

    public void print(Logger logger) {

        logger.info("{}",
                String.format("| %03d ($%2s) | %8d ($%6s) | %8d ($%6s) |", this.getIndexDec(),
                        this.getIndex(), this.getCounterValueDec(), this.getCounterValue(),
                        this.getCeilingValueDec(), this.getCeilingValue()));
    }

    public String getIndex() {
        return index;
    }

    public int getIndexDec() {
        return indexDec;
    }

    public String getCounterValue() {
        return counterValue;
    }

    public long getCounterValueDec() {
        return counterValueDec;
    }

    public String getCeilingValue() {
        return ceilingValue;
    }

    public long getCeilingValueDec() {
        return ceilingValueDec;
    }
}

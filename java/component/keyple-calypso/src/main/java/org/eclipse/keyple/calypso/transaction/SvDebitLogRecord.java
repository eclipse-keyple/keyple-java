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

import org.eclipse.keyple.core.util.ByteArrayUtil;

import java.util.Arrays;

public class SvDebitLogRecord {
    final int offset;
    final byte[] poResponse;

    public SvDebitLogRecord(byte[] poResponse, int offset) {
        this.poResponse = poResponse;
        this.offset = offset;
    }

    public int getAmount() {
        return ByteArrayUtil.twoBytesSignedToInt(poResponse, offset);
    }

    public int getBalance() {
        return ByteArrayUtil.threeBytesSignedToInt(poResponse, offset + 14);
    }

    public int getDebitTime() {
        return ByteArrayUtil.twoBytesToInt(getDebitTimeBytes(), 0);
    }

    public byte[] getDebitTimeBytes() {
        final byte[] time = new byte[2];
        time[0] = poResponse[offset + 4];
        time[1] = poResponse[offset + 5];
        return time;
    }

    public int getDebitDate() {
        return ByteArrayUtil.twoBytesToInt(getDebitDateBytes(), 0);
    }

    public byte[] getDebitDateBytes() {
        final byte[] date = new byte[2];
        date[0] = poResponse[offset + 2];
        date[1] = poResponse[offset + 3];
        return date;
    }

    public long getSamId() {
        return ByteArrayUtil.fourBytesToInt(getSamIdBytes(), 0);
    }

    public byte[] getSamIdBytes() {
        byte[] samId = new byte[4];
        System.arraycopy(poResponse, offset + 7, samId, 0, 4);
        return samId;
    }

    public int getSvTNum() {
        return ByteArrayUtil.twoBytesToInt(getSvTNumBytes(), 0);
    }

    public byte[] getSvTNumBytes() {
        final byte[] tnNum = new byte[2];
        tnNum[0] = poResponse[offset + 17];
        tnNum[1] = poResponse[offset + 18];
        return tnNum;
    }

    public int getSamTNum() {
        return ByteArrayUtil.twoBytesToInt(getSamTNumBytes(), 0);
    }

    public byte[] getSamTNumBytes() {
        byte[] samTNum = new byte[3];
        System.arraycopy(poResponse, offset + 11, samTNum, 0, 3);
        return samTNum;
    }

    @Override
    public String toString() {
        return "SvDebitLogRecord{" +
                "amount=" + getAmount() +
                ", balance=" + getBalance() +
                ", debitDate=" + getDebitDate() +
                ", debitTime=" + getDebitDate() +
                ", samId=" + ByteArrayUtil.toHex(getSamIdBytes()) +
                ", svTransactionNumber=" + getSvTNum() +
                ", svSamTransactionNumber=" + getSamTNum() +
                '}';
    }
}

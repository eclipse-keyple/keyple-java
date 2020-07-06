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
import org.eclipse.keyple.core.util.ByteArrayUtil;

public class SvLoadLogRecord {
    final int offset;
    final byte[] poResponse;

    public SvLoadLogRecord(byte[] poResponse, int offset) {
        this.poResponse = poResponse;
        this.offset = offset;
    }

    public int getAmount() {
        return ByteArrayUtil.threeBytesSignedToInt(poResponse, offset + 8);
    }

    public int getBalance() {
        return ByteArrayUtil.threeBytesSignedToInt(poResponse, offset + 5);
    }

    public int getLoadTime() {
        return ByteArrayUtil.twoBytesToInt(getLoadTimeBytes(), 0);
    }

    public byte[] getLoadTimeBytes() {
        final byte[] time = new byte[2];
        time[0] = poResponse[offset + 11];
        time[1] = poResponse[offset + 12];
        return time;
    }

    public int getLoadDate() {
        return ByteArrayUtil.twoBytesToInt(getLoadDateBytes(), 0);
    }

    public byte[] getLoadDateBytes() {
        final byte[] date = new byte[2];
        date[0] = poResponse[offset + 0];
        date[1] = poResponse[offset + 1];
        return date;
    }

    public String getFreeByte() {
        return Arrays.toString(getFreeByteBytes());
    }

    public byte[] getFreeByteBytes() {
        final byte[] free = new byte[2];
        free[0] = poResponse[offset + 2];
        free[1] = poResponse[offset + 4];
        return free;
    }

    public byte getKVC() {
        return poResponse[offset + 3];
    }

    public long getSamId() {
        return ByteArrayUtil.fourBytesToInt(getSamIdBytes(), 0);
    }

    public byte[] getSamIdBytes() {
        byte[] samId = new byte[4];
        System.arraycopy(poResponse, offset + 13, samId, 0, 4);
        return samId;
    }

    public int getSvTNum() {
        return ByteArrayUtil.twoBytesToInt(getSvTNumBytes(), 0);
    }

    public byte[] getSvTNumBytes() {
        final byte[] tnNum = new byte[2];
        tnNum[0] = poResponse[offset + 20];
        tnNum[1] = poResponse[offset + 21];
        return tnNum;
    }

    public int getSamTNum() {
        return ByteArrayUtil.twoBytesToInt(getSamTNumBytes(), 0);
    }

    public byte[] getSamTNumBytes() {
        byte[] samTNum = new byte[3];
        System.arraycopy(poResponse, offset + 17, samTNum, 0, 3);
        return samTNum;
    }

    @Override
    public String toString() {
        return "SvDebitLogRecord{" + "amount=" + getAmount() + ", balance=" + getBalance()
                + ", debitDate=" + getLoadDate() + ", debitTime=" + getLoadDate() + ", freeBytes="
                + ByteArrayUtil.toHex(getFreeByteBytes()) + ", samId="
                + ByteArrayUtil.toHex(getSamIdBytes()) + ", svTransactionNumber=" + getSvTNum()
                + ", svSamTransactionNumber=" + getSamTNum() + '}';
    }
}

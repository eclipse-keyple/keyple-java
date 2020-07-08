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

/**
 * The class {@code SvLoadLogRecord} contains the data of a Stored Value load log.
 *
 * @since 0.9
 */
public class SvLoadLogRecord {
    final int offset;
    final byte[] poResponse;

    /**
     * Constructor
     * 
     * @param poResponse the Sv Get response data
     * @param offset the load log offset in the response (may change from a PO to another)
     */
    public SvLoadLogRecord(byte[] poResponse, int offset) {
        this.poResponse = poResponse;
        this.offset = offset;
    }

    /**
     * Get the load amount
     * 
     * @return the amount value
     * @since 0.9
     */
    public int getAmount() {
        return ByteArrayUtil.threeBytesSignedToInt(poResponse, offset + 8);
    }

    /**
     * Get the SV balance
     * 
     * @return the balance value
     * @since 0.9
     */
    public int getBalance() {
        return ByteArrayUtil.threeBytesSignedToInt(poResponse, offset + 5);
    }

    /**
     * Get the load time
     * 
     * @return the time value as an int
     * @since 0.9
     */
    public int getLoadTime() {
        return ByteArrayUtil.twoBytesToInt(getLoadTimeBytes(), 0);
    }

    /**
     * Get the load time
     * 
     * @return the time value as a 2-byte byte array
     * @since 0.9
     */
    public byte[] getLoadTimeBytes() {
        final byte[] time = new byte[2];
        time[0] = poResponse[offset + 11];
        time[1] = poResponse[offset + 12];
        return time;
    }

    /**
     * Get the load date
     * 
     * @return the date value as an int
     * @since 0.9
     */
    public int getLoadDate() {
        return ByteArrayUtil.twoBytesToInt(getLoadDateBytes(), 0);
    }

    /**
     * Get the load date
     * 
     * @return the date value as a 2-byte byte array
     * @since 0.9
     */
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

    /**
     * Get the SAM ID
     * 
     * @return the SAM ID value as an int
     * @since 0.9
     */
    public long getSamId() {
        return ByteArrayUtil.fourBytesToInt(getSamIdBytes(), 0);
    }

    /**
     * Get the SAM ID
     * 
     * @return the SAM ID value as a 4-byte byte array
     * @since 0.9
     */
    public byte[] getSamIdBytes() {
        byte[] samId = new byte[4];
        System.arraycopy(poResponse, offset + 13, samId, 0, 4);
        return samId;
    }

    /**
     * Get the SV transaction number
     * 
     * @return the SV transaction number value as an int
     * @since 0.9
     */
    public int getSvTNum() {
        return ByteArrayUtil.twoBytesToInt(getSvTNumBytes(), 0);
    }

    /**
     * Get the SV transaction number
     * 
     * @return the SV transaction number value as a 2-byte byte array
     * @since 0.9
     */
    public byte[] getSvTNumBytes() {
        final byte[] tnNum = new byte[2];
        tnNum[0] = poResponse[offset + 20];
        tnNum[1] = poResponse[offset + 21];
        return tnNum;
    }

    /**
     * Get the SAM transaction number
     * 
     * @return the SAM transaction number value as an int
     * @since 0.9
     */
    public int getSamTNum() {
        return ByteArrayUtil.threeBytesToInt(getSamTNumBytes(), 0);
    }

    /**
     * Get the SAM transaction number
     * 
     * @return the SAM transaction number value as a 3-byte byte array
     * @since 0.9
     */
    public byte[] getSamTNumBytes() {
        byte[] samTNum = new byte[3];
        System.arraycopy(poResponse, offset + 17, samTNum, 0, 3);
        return samTNum;
    }

    @Override
    public String toString() {
        return "{\"SvLoadLogRecord\":{" + "\"amount\":" + getAmount() + ", \"balance\":"
                + getBalance() + ", \"debitDate\":" + getLoadDate() + ", \"debitTime\":"
                + getLoadDate() + ", \"freeBytes\":" + ByteArrayUtil.toHex(getFreeByteBytes())
                + ", \"samId\":" + ByteArrayUtil.toHex(getSamIdBytes())
                + ", \"svTransactionNumber\":" + getSvTNum() + ", \"svSamTransactionNumber\":"
                + getSamTNum() + "}}";
    }
}

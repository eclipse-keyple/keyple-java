/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.seproxy.message;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import org.eclipse.keyple.util.ByteArrayUtils;


/**
 * Single APDU response wrapper
 */
public final class ApduResponse implements Serializable {

    static final long serialVersionUID = 6418469841122636812L;

    /***
     * the success result of the processed APDU command to allow chaining responses in a group of
     * APDUs
     */
    private final boolean successful;


    /**
     * apdu response data buffer (including sw1sw2)
     */
    private final byte[] bytes;


    /**
     * Create a new ApduResponse from the provided byte array
     *
     * The internal successful status is determined by the current status code and the optional
     * successful status codes list.
     *
     * The list of additional successful status codes is used to set the successful flag if not
     * equal to 0x9000
     * 
     * @param buffer apdu response data buffer (including sw1sw2)
     * @param successfulStatusCodes optional list of successful status codes other than 0x9000
     */
    public ApduResponse(byte[] buffer, Set<Integer> successfulStatusCodes)
            throws IllegalArgumentException {

        this.bytes = buffer;
        if (buffer == null) {
            this.successful = false;
        } else {
            if (buffer.length < 2) {
                throw new IllegalArgumentException(
                        "Building an ApduResponse with a illegal buffer (length must be > 2): "
                                + buffer.length);
            }
            int statusCode = ((buffer[buffer.length - 2] & 0x000000FF) << 8)
                    + (buffer[buffer.length - 1] & 0x000000FF);
            // java is signed only
            if (statusCode < 0) {
                statusCode += -2 * Short.MIN_VALUE;
            }
            if (successfulStatusCodes != null) {
                this.successful =
                        statusCode == 0x9000 || successfulStatusCodes.contains(statusCode);
            } else {
                this.successful = statusCode == 0x9000;
            }
        }
    }

    /**
     * Checks if is successful.
     *
     * @return the status of the command transmission.
     */
    public boolean isSuccessful() {
        return successful;
    }

    public int getStatusCode() {
        int s = ((bytes[bytes.length - 2] & 0x000000FF) << 8)
                + (bytes[bytes.length - 1] & 0x000000FF);

        // java is signed only
        if (s < 0) {
            s += -2 * Short.MIN_VALUE;
        }
        return s;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    /**
     * Get the data before the statusCode
     * 
     * @return slice of the buffer before the status code
     */
    public byte[] getDataOut() {
        return Arrays.copyOfRange(this.bytes, 0, this.bytes.length - 2);
    }

    @Override
    public String toString() {
        String prefix;
        if (isSuccessful()) {
            prefix = "ApduResponse: SUCCESS, RAWDATA = ";
        } else {
            prefix = "ApduResponse: FAILURE, RAWDATA = ";
        }
        return prefix + ByteArrayUtils.toHex(this.bytes);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ApduResponse)) {
            return false;
        }

        ApduResponse resp = (ApduResponse) o;
        return Arrays.equals(resp.getBytes(), this.bytes) && resp.isSuccessful() == this.successful;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 19 * hash + (this.successful ? 0 : 1);
        hash = 31 * hash + (bytes == null ? 0 : Arrays.hashCode(bytes));
        return hash;
    }
}

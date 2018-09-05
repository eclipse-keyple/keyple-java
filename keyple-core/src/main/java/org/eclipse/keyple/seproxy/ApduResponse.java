/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import java.nio.ByteBuffer;
import java.util.Set;
import org.eclipse.keyple.util.ByteBufferUtils;


/**
 * Single APDU response wrapper
 */
public final class ApduResponse {

    /***
     * the success result of the processed APDU command to allow chaining responses in a group of
     * APDUs
     */
    private final boolean successful;


    /**
     * apdu response data buffer (including sw1sw2)
     */
    private final ByteBuffer bytes;


    /**
     * Create a new ApduResponse from the provided ByteBuffer
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
    public ApduResponse(ByteBuffer buffer, Set<Short> successfulStatusCodes) {

        this.bytes = buffer;
        if (buffer == null) {
            this.successful = false;
        } else {
            if (buffer.limit() < 2) {
                throw new IllegalArgumentException("Bad buffer (length < 2): " + buffer.limit());
            }
            int statusCode = buffer.getShort(buffer.limit() - 2);
            // java is signed only
            if (statusCode < 0) {
                statusCode += -2 * Short.MIN_VALUE;
            }
            if (successfulStatusCodes != null) {
                this.successful =
                        statusCode == 0x9000 || successfulStatusCodes.contains((short) statusCode);
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
        int s = bytes.getShort(bytes.limit() - 2);

        // java is signed only
        if (s < 0) {
            s += -2 * Short.MIN_VALUE;
        }
        return s;
    }

    public ByteBuffer getBytes() {
        return this.bytes;
    }

    /**
     * Get the data before the statusCode
     * 
     * @return slice of the buffer before the status code
     */
    public ByteBuffer getDataOut() {
        return ByteBufferUtils.subLen(bytes, 0, bytes.limit() - 2);
    }

    @Override
    public String toString() {
        return "ApduResponse: " + (isSuccessful() ? "SUCCESS" : "FAILURE") + ", RAWDATA = "
                + ByteBufferUtils.toHex(this.bytes);
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
        return (resp.getBytes() == null ? this.bytes == null : resp.getBytes().equals(this.bytes))
                && resp.isSuccessful() == this.successful;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 19 * hash + (this.successful ? 0 : 1);
        hash = 31 * hash + (bytes == null ? 0 : bytes.hashCode());
        return hash;
    }
}

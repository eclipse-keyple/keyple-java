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
public final class ApduResponse extends AbstractApduBuffer {

    /***
     * the success result of the processed APDU commandto allow chaining responses in a group of
     * APDUs
     */
    private final boolean successful;

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

        super(buffer);
        if (buffer == null) {
            this.successful = false;
        } else {
            // TODO shouldn't we check the case where length is < 2 and throw an exception?
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
        int s = buffer.getShort(buffer.limit() - 2);

        // java is signed only
        if (s < 0) {
            s += -2 * Short.MIN_VALUE;
        }
        return s;
    }


    /**
     * Get the data before the statusCode
     * 
     * @return slice of the buffer before the status code
     */
    public ByteBuffer getDataOut() {
        return ByteBufferUtils.subLen(buffer, 0, buffer.limit() - 2);
    }

    @Override
    public String toString() {
        return "Resp{" + super.toString() + "}";
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
        return (resp.getBytes() == null ? this.buffer == null : resp.getBytes().equals(this.buffer))
                && resp.isSuccessful() == this.successful;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 19 * hash + (this.successful ? 0x55555555 : 0x2AAAAAAA);
        hash = 31 * hash + (buffer == null ? 0 : buffer.hashCode());
        return hash;
    }
}

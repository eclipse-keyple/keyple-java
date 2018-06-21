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
     * Create a new ApduResponse, the successful status is set by the caller
     * 
     * @param buffer apdu response data buffer (including sw1sw2)
     * @param successful successful flag
     */
    public ApduResponse(ByteBuffer buffer, boolean successful) {
        super(buffer);
        this.successful = successful;
    }

    /**
     * Create a new ApduResponse from the provided ByteBuffer<br/>
     * The internal successful status is determined by the current status code and the optional
     * successful status codes list.<br/>
     * The list of additional successful status codes is used to set the successful flag if not
     * equal to 0x9000
     * 
     * @param buffer apdu response data buffer (including sw1sw2)
     * @param successfulStatusCodes optional list of successful status codes other than 0x9000
     */
    public ApduResponse(ByteBuffer buffer, Set<Short> successfulStatusCodes) {
        super(buffer);
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

    /**
     * the constructor called by a ProxyReader in order to build the APDU command response to push
     * to a ticketing application.
     *
     * @param bytes the bytes
     * @param successful the successful
     */
    public ApduResponse(byte[] bytes, boolean successful) {
        super(bytes);
        this.successful = successful;
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
        ByteBuffer b = buffer.duplicate();
        b.position(0);
        b.limit(b.limit() - 2);
        return b.slice();
    }

    @Override
    public String toString() {
        return "Resp{" + super.toString() + "}";
    }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.nio.ByteBuffer;


/**
 * Single APDU response wrapper
 */
public class ApduResponse extends AbstractApduBuffer {

    /***
     * the success result of the processed APDU commandto allow chaining responses in a group of
     * APDUs
     */
    private boolean successful;

    public ApduResponse(ByteBuffer buffer, boolean successful) {
        super(buffer);
        this.successful = successful;
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
     * The constructor called by a ProxyReader in order to build the APDU command response to push
     * to a ticketing application.
     *
     * @param bytes the bytes
     * @param successful the successful
     * @param statusCode the status code
     * @deprecated Only {@link ApduResponse#ApduResponse(byte[], boolean)} should be used instead.
     */
    public ApduResponse(byte[] bytes, boolean successful, byte[] statusCode) {
        super(ByteBuffer.allocate(
                (bytes != null ? bytes.length : 0) + (statusCode != null ? statusCode.length : 0)));
        if (bytes != null) {
            buffer.put(bytes);
        }
        if (statusCode != null) {
            buffer.put(statusCode);
        }
        buffer.position(0);
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
     * Get the old (two bytes) status code
     * 
     * @return Old status code format
     * @deprecated Prefer {@link #getStatusCode()}
     */
    public byte[] getStatusCodeOld() {
        byte[] statusCode = new byte[2];
        buffer.position(buffer.limit() - 2);
        buffer.get(statusCode);
        return statusCode;
    }

    public ByteBuffer getDataBeforeStatus() {
        ByteBuffer b = buffer.duplicate();
        b.position(0);
        b.limit(b.limit() - 2);
        return b.slice();
    }

    public byte[] getBytesBeforeStatus() {
        return ByteBufferUtils.toBytes(getDataBeforeStatus());
    }

    @Override
    public String toString() {
        return "APDU Response " + super.toString();
    }
}

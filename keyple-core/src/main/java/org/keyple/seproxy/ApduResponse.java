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
public class ApduResponse extends AbstractApduWrapper {

    /***
     * the success result of the processed APDU commandto allow chaining responses in a group of
     * APDUs
     */
    private boolean successful;

    /*
     * The status code.
     *
     * @deprecated This field is extracted from bytes
     */
    private byte[] statusCode;

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
     * the constructor called by a ProxyReader in order to build the APDU command response to push
     * to a ticketing application.
     *
     * @param bytes the bytes
     * @param successful the successful
     * @param statusCode the status code
     * @deprecated Only {@link ApduResponse#ApduResponse(byte[], boolean)} should be used instead.
     */
    public ApduResponse(byte[] bytes, boolean successful, byte[] statusCode) {
        super(bytes);
        this.successful = successful;
        this.statusCode = (statusCode == null ? null : statusCode.clone());
    }

    /**
     * Checks if is successful.
     *
     * @return the status of the command transmission.
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    public byte[] getStatusCode() { // TODO - to delete
        return statusCode.clone();
    }

    public int getStatusCodeV2() {
        int s = buffer.getShort(buffer.limit() - 2);

        // java is signed only
        if (s < 0) {
            s += -2 * Short.MIN_VALUE;
        }
        return s;
    }

    public ByteBuffer getDataBeforeStatus() {
        ByteBuffer b = buffer.duplicate();
        b.position(0);
        b.limit(b.limit() - 2);
        return b.slice();
    }

    public byte[] getBytesBeforeStatus() {
        ByteBuffer buf = getDataBeforeStatus();
        byte[] data = new byte[buf.limit()];
        buf.get(data);
        return data;
    }

    @Override
    public String toString() {
        return "APDU Response " + super.toString();
    }
}

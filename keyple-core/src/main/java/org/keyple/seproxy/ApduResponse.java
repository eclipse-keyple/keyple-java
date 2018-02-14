/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

/**
 * Single APDU response wrapper
 */
public class ApduResponse {

    /**
     * an array of the bytes of an APDU response (none structured, including the dataOut field and
     * the status of the command).
     */
    private byte[] bytes;

    /**
     * the success result of the processed APDU command to allow chaining responses in a group of
     * APDUs
     */
    private boolean successful;

    /** The status code. */
    private byte[] statusCode; // TODO - to delete


    /**
     * the constructor called by a ProxyReader in order to build the APDU command response to push
     * to a ticketing application.
     *
     * @param bytes the bytes
     * @param successful the successful
     */
    public ApduResponse(byte[] bytes, boolean successful) {
        this.bytes = (bytes == null ? null : bytes.clone());
        this.successful = successful;
    }

    /**
     * the constructor called by a ProxyReader in order to build the APDU command response to push
     * to a ticketing application.
     *
     * @param bytes the bytes
     * @param successful the successful
     * @param statusCode the status code
     */
    public ApduResponse(byte[] bytes, boolean successful, byte[] statusCode) { // TODO - to delete
        this.bytes = (bytes == null ? null : bytes.clone());
        this.successful = successful;
        this.statusCode = (statusCode == null ? null : statusCode.clone());
    }

    /**
     * Gets the bytes.
     *
     * @return the data of the APDU response.
     */
    public byte[] getbytes() {
        // return bytes.clone();
        return bytes;
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

}

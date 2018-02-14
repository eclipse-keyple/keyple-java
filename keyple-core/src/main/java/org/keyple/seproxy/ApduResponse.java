/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.util.regex.Pattern;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * Single APDU response wrapper
 */
public class ApduResponse {

    /**
     * an array of the bytes of an APDU response (none structured, including the dataOut field and
     * the status of the command).
     */
    private final byte[] bytes;

    /***
     * the success result of the processed APDU commandto allow chaining responses in a group of
     * APDUs
     */
    private boolean successful;

    /**
     * The status code.
     *
     * @deprecated This field is extracted from bytes
     */
    private byte[] statusCode;


    /**
     * Chars we will ignore when loading a sample HEX string. It allows to copy/paste the specs APDU
     */
    private static final Pattern HEX_IGNORED_CHARS = Pattern.compile(" |h");

    /**
     * Create an APDU from an hex string. Note: This is a convenience initialization and a temporary
     * solution. The bytes management will probably be handled by a {@link java.nio.ByteBuffer} in a
     * very near future.
     *
     * @param hexFormat APDU in hex format with spaces permitted
     */
    public ApduResponse(String hexFormat) {
        // Hex..hexFormat.replace(" ", "")
        // hexFormat
        try {
            this.bytes = Hex.decodeHex(HEX_IGNORED_CHARS.matcher(hexFormat).replaceAll(""));
        } catch (DecoderException e) {
            // This is unlikely and we don't want to impose everyone to catch this error
            throw new RuntimeException("Bad format", e);
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
     * @deprecated Only {@link ApduResponse#ApduResponse(byte[], boolean)} should be used instead.
     */
    public ApduResponse(byte[] bytes, boolean successful, byte[] statusCode) {
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


    @Override
    public String toString() {
        return Hex.encodeHexString(bytes) + "/" + successful;
    }
}

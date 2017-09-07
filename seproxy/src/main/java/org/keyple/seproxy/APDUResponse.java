package org.keyple.seproxy;

/**
 * The Class APDUResponse.
 * This class defines the elements of a single APDU command response:
 */
public class APDUResponse {

    /** an array of the bytes of an APDU response (none structured, including the dataOut field and the
status of the command).*/
    private byte[] bytes;

    /** the success result of the processed APDU command. */
    private boolean successful;

    /** The status code. */
    private byte[] statusCode;

    /**
     * the constructor called by a ProxyReader in order to build the APDU command response to push to a
ticketing application.
     *
     * @param bytes the bytes
     * @param successful the successful
     * @param statusCode the status code
     */
    public APDUResponse(byte[] bytes, boolean successful, byte[] statusCode) {
        this.bytes = bytes;
        this.successful = successful;
        this.statusCode = statusCode;
    }

    /**
     * Gets the bytes.
     *
     * @return the data of the APDU response.
     */
    public byte[] getbytes() {
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
    public byte[] getStatusCode() {
        return statusCode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
//    @S
}
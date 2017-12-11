package org.keyple.seproxy;

/**
 * The Class APDURequest.
 * This class defines the elements of a single APDU command request:
 * @author Ixxi
 */
public class ApduRequest {

    /**an array of the bytes of an APDU request (none structured, including the header and the dataIn of
the command). */
    private byte[] bytes;

    /** a ‘case 4’ flag in order to explicitly specify, if it’s expected that the APDU command returns data →
    * this flag is required to manage revision 2.4 Calypso Portable Objects and ‘S1Dx’ SAMs that presents a
    * behaviour not compliant with ISO 7816-3 in contacts mode (not returning the 61XYh status). 
    */
    private boolean case4;

    /**
     * the constructor called by a ticketing application in order to build the APDU command requests to
push to the ProxyReader.
     *
     * @param bytes the bytes
     * @param case4 the case 4
     */
    public ApduRequest(byte[] bytes, boolean case4) {
        super();
        this.bytes = (bytes == null ? null : bytes.clone());
        this.case4 = case4;
    }

    /**
     * Gets the bytes.
     *
     * @return the data of the APDU request.
     */
    public byte[] getbytes() {
        return bytes.clone();
    }

    /**
     * Checks if is case 4.
     *
     * @return the case4 flag.
     */
    public boolean isCase4() {
        return case4;
    }

}
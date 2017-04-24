package cna.sdk.calypso.commandset.dto;

/**
 * The Class SamHalfSessionSignature.
 * Half session signature return by the digest close APDU command
 */
public class SamHalfSessionSignature {
    
    /** The value. */
    byte[] value;

    /**
     * Instantiates a new SamHalfSessionSignature.
     *
     * @param value the value
     */
    public SamHalfSessionSignature(byte[] value) {
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public byte[] getValue() {
        return value;
    }
}

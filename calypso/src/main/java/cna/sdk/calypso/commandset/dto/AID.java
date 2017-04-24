package cna.sdk.calypso.commandset.dto;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.ArrayUtils;

/**
 * The Class AID.
 * AID: Application Identifier as defined in ISO/IEC 7816-4.
 * Value unique in a portable object, allowing to unambiguously identify an application.
 */
public class AID {

    /** The value. */
    private byte[] value;

    /**
     * Instantiates a new AID.
     *
     * @param value the byte value
     */
    public AID(byte[] value) {
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new String(value, StandardCharsets.US_ASCII);
    }
}

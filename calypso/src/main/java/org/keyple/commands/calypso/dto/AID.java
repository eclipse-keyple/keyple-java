package org.keyple.commands.calypso.dto;

/**
 * The Class AID. AID: Application Identifier as defined in ISO/IEC 7816-4.
 * Value unique in a portable object, allowing to unambiguously identify an
 * application.
 */
public class AID {

    /** The value. */
    private byte[] value;

    /**
     * Instantiates a new AID.
     *
     * @param value
     *            the byte value
     */
    public AID(byte[] value) {
        this.value = (value == null ? null : value.clone());
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public byte[] getValue() {
        return value.clone();
    }

}

package org.keyple.calypso.commandset.dto;

/**
 * The Class KIF. KIF:Key Identifier. Value identifying the type of key.
 */
public class KIF {

    /** The value. */
    private byte value;

    /**
     * Instantiates a new KIF.
     *
     * @param value
     *            the value
     */
    public KIF(byte value) {
        super();
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public byte getValue() {
        return value;
    }
}

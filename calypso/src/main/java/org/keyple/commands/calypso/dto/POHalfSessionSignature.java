package org.keyple.commands.calypso.dto;

// TODO: Auto-generated Javadoc
/**
 * The Class POHalfSessionSignature. Half session signature return by a close
 * secure session APDU command
 */
public class POHalfSessionSignature {

    /** The value. */
    private byte[] value;

    /** The postponed data. */
    private byte[] postponedData;

    /**
     * Instantiates a new POHalfSessionSignature.
     *
     * @param value
     *            the value
     * @param postponedData
     *            the postponed data
     */
    public POHalfSessionSignature(byte[] value, byte[] postponedData) {
        super();
        this.value = value;
        this.postponedData = postponedData;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public byte[] getValue() {
        if (value != null) {
            return value;
        } else {
            return new byte[0];
        }
    }

    /**
     * Gets the postponed data.
     *
     * @return the postponed data
     */
    public byte[] getPostponedData() {
        if (postponedData != null) {
            return postponedData;
        } else {
            return new byte[0];
        }
    }

}

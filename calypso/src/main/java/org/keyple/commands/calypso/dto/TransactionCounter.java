package org.keyple.commands.calypso.dto;

/**
 * The Class TransactionCounter. For security purposes, a Calypso portable
 * object shall maintain a Transaction Counter (24 bit unsigned value) in its
 * non-volatile memory, initialized with the Calypso application, and which may
 * only be decremented (reduced by 1) during the portable object uses.
 */
public class TransactionCounter {

    /** The value. */
    private int value;

    /**
     * Instantiates a new TransactionCounter.
     *
     * @param value
     *            the value
     */
    public TransactionCounter(int value) {
        super();
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

}

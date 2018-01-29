package org.keyple.commands.calypso.dto;

/**
 * The Class SamChallenge. Challenge return by the CSM Get Challenge APDU
 * command
 */
public class SamChallenge {

    /** The transaction counter. */
    private byte[] transactionCounter;

    /** The random number. */
    private byte[] randomNumber;

    /**
     * Instantiates a new SamChallenge.
     *
     * @param transactionCounter
     *            the transaction counter
     * @param randomNumber
     *            the random number
     */
    public SamChallenge(byte[] transactionCounter, byte[] randomNumber) {
        this.transactionCounter = transactionCounter;
        this.randomNumber = randomNumber;
    }

    /**
     * Gets the transaction counter.
     *
     * @return the transaction counter
     */
    public byte[] getTransactionCounter() {
        return transactionCounter;
    }

    /**
     * Gets the random number.
     *
     * @return the random number
     */
    public byte[] getRandomNumber() {
        return randomNumber;
    }

}

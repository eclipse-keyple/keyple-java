package org.keyple.commands.calypso.dto;

/**
 * The Class POChallenge. Challenge return by a PO Get Challenge APDU command
 */
public class POChallenge {

    /** The transaction counter. */
    private byte[] transactionCounter;

    /** The random number provide by the terminal */
    private byte[] randomNumber;

    /**
     * Instantiates a new POChallenge.
     *
     * @param transactionCounter
     *            the transaction counter
     * @param randomNumber
     *            the random number
     */
    public POChallenge(byte[] transactionCounter, byte[] randomNumber) {
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

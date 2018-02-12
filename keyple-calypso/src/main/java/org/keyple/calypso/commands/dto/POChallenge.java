/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

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
     * @param transactionCounter the transaction counter
     * @param randomNumber the random number
     */
    public POChallenge(byte[] transactionCounter, byte[] randomNumber) {
        this.transactionCounter = (transactionCounter == null ? null : transactionCounter.clone());
        this.randomNumber = (randomNumber == null ? null : randomNumber.clone());
    }

    /**
     * Gets the transaction counter.
     *
     * @return the transaction counter
     */
    public byte[] getTransactionCounter() {
        return transactionCounter.clone();
    }

    /**
     * Gets the random number.
     *
     * @return the random number
     */
    public byte[] getRandomNumber() {
        return randomNumber.clone();
    }

}

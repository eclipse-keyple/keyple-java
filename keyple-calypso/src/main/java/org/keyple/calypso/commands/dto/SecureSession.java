/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

/**
 * The Class SecureSession. A secure session is returned by a open secure session command
 */
public class SecureSession {

    /** The session challenge. */
    PoChallenge sessionChallenge;

    /** The previous session ratified boolean. */
    boolean previousSessionRatified;

    /** The manage secure session authorized boolean. */
    boolean manageSecureSessionAuthorized;

    /** The kif. */
    byte kif = (byte) 0xFF;

    /** The kvc. */
    byte kvc;

    /** The original data. */
    byte[] originalData;

    /** The secure session data. */
    byte[] secureSessionData;

    /**
     * Instantiates a new SecureSession for a Calypso application revision 3
     *
     * @param sessionChallenge the session challenge return by the open secure session APDU command
     * @param previousSessionRatified the previous session ratified
     * @param manageSecureSessionAuthorized the manage secure session authorized
     * @param kif the KIF from the response of the open secure session APDU command
     * @param kvc the KVC from the response of the open secure session APDU command
     * @param originalData the original data from the response of the open secure session APDU
     *        command
     * @param secureSessionData the secure session data from the response of open secure session
     *        APDU command
     */
    // Rev 3.1
    public SecureSession(PoChallenge sessionChallenge, boolean previousSessionRatified,
            boolean manageSecureSessionAuthorized, byte kif, byte kvc, byte[] originalData,
            byte[] secureSessionData) {
        this.sessionChallenge = sessionChallenge;
        this.previousSessionRatified = previousSessionRatified;
        this.manageSecureSessionAuthorized = manageSecureSessionAuthorized;
        this.kif = kif;
        this.kvc = kvc;
        this.originalData = (originalData == null ? null : originalData.clone());
        this.secureSessionData = (secureSessionData == null ? null : secureSessionData.clone());
    }

    /**
     * Instantiates a new SecureSession for a Calypso application revision 2.4
     *
     * @param sessionChallenge the session challenge return by the open secure session APDU command
     * @param previousSessionRatified the previous session ratified
     * @param manageSecureSessionAuthorized the manage secure session authorized
     * @param kvc the KVC from the response of the open secure session APDU command
     * @param originalData the original data from the response of the open secure session APDU
     *        command
     * @param secureSessionData the secure session data from the response of open secure session
     *        APDU command
     */
    // Rev 2.4
    public SecureSession(PoChallenge sessionChallenge, boolean previousSessionRatified,
            boolean manageSecureSessionAuthorized, byte kvc, byte[] originalData,
            byte[] secureSessionData) {
        this.sessionChallenge = sessionChallenge;
        this.previousSessionRatified = previousSessionRatified;
        this.manageSecureSessionAuthorized = manageSecureSessionAuthorized;
        this.kvc = kvc;
        this.originalData = (originalData == null ? null : originalData.clone());
        this.secureSessionData = (secureSessionData == null ? null : secureSessionData.clone());
    }

    /**
     * Gets the session challenge.
     *
     * @return the session challenge
     */
    public PoChallenge getSessionChallenge() {
        return sessionChallenge;
    }

    /**
     * Checks if is previous session ratified.
     *
     * @return the boolean
     */
    public boolean isPreviousSessionRatified() {
        return previousSessionRatified;
    }

    /**
     * Checks if is manage secure session authorized.
     *
     * @return the boolean
     */
    public boolean isManageSecureSessionAuthorized() {
        return manageSecureSessionAuthorized;
    }

    /**
     * Gets the kif.
     *
     * @return the kif
     */
    public byte getKIF() {
        return kif;
    }

    /**
     * Gets the kvc.
     *
     * @return the kvc
     */
    public byte getKVC() {
        return kvc;
    }

    /**
     * Gets the original data.
     *
     * @return the original data
     */
    public byte[] getOriginalData() {
        if (originalData == null) {
            return new byte[] {};
        }
        return originalData.clone();
    }

    /**
     * Gets the secure session data.
     *
     * @return the secure session data
     */
    public byte[] getSecureSessionData() {
        return secureSessionData.clone();
    }

    /**
     * The Class PoChallenge. Challenge return by a PO Get Challenge APDU command
     */
    public static class PoChallenge {

        /** The transaction counter. */
        private byte[] transactionCounter;

        /** The random number provide by the terminal */
        private byte[] randomNumber;

        /**
         * Instantiates a new PoChallenge.
         *
         * @param transactionCounter the transaction counter
         * @param randomNumber the random number
         */
        public PoChallenge(byte[] transactionCounter, byte[] randomNumber) {
            this.transactionCounter =
                    (transactionCounter == null ? null : transactionCounter.clone());
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
}

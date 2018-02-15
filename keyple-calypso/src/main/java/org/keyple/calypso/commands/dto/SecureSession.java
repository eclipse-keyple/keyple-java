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

    /** Challenge transaction counter */
    private final byte[] challengeTransactionCounter;

    /** Challenge random number */
    private final byte[] challengeRandomNumber;

    /** The previous session ratified boolean. */
    private final boolean previousSessionRatified;

    /** The manage secure session authorized boolean. */
    private final boolean manageSecureSessionAuthorized;

    /** The kif. */
    private final byte kif;

    /** The kvc. */
    private final byte kvc;

    /** The original data. */
    private final byte[] originalData;

    /** The secure session data. */
    private final byte[] secureSessionData;

    /**
     * Instantiates a new SecureSession for a Calypso application revision 3
     *
     * @param previousSessionRatified the previous session ratified
     * @param manageSecureSessionAuthorized the manage secure session authorized
     * @param kif the KIF from the response of the open secure session APDU command
     * @param kvc the KVC from the response of the open secure session APDU command
     * @param originalData the original data from the response of the open secure session APDU
     *        command
     * @param secureSessionData the secure session data from the response of open secure session
     *        APDU command
     */
    public SecureSession(byte[] challengeTransactionCounter, byte[] challengeRandomNumber,
            boolean previousSessionRatified, boolean manageSecureSessionAuthorized, byte kif,
            byte kvc, byte[] originalData, byte[] secureSessionData) {
        this.challengeTransactionCounter = challengeTransactionCounter;
        this.challengeRandomNumber = challengeRandomNumber;
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
     * @param previousSessionRatified the previous session ratified
     * @param manageSecureSessionAuthorized the manage secure session authorized
     * @param kvc the KVC from the response of the open secure session APDU command
     * @param originalData the original data from the response of the open secure session APDU
     *        command
     * @param secureSessionData the secure session data from the response of open secure session
     *        APDU command
     */
    public SecureSession(byte[] challengeTransactionCounter, byte[] challengeRandomNumber,
            boolean previousSessionRatified, boolean manageSecureSessionAuthorized, byte kvc,
            byte[] originalData, byte[] secureSessionData) {
        this.challengeTransactionCounter = challengeTransactionCounter;
        this.challengeRandomNumber = challengeRandomNumber;
        this.previousSessionRatified = previousSessionRatified;
        this.manageSecureSessionAuthorized = manageSecureSessionAuthorized;
        this.kif = (byte) 0xFF;
        this.kvc = kvc;
        this.originalData = (originalData == null ? null : originalData.clone());
        this.secureSessionData = (secureSessionData == null ? null : secureSessionData.clone());
    }

    public byte[] getChallengeTransactionCounter() {
        return challengeTransactionCounter;
    }

    public byte[] getChallengeRandomNumber() {
        return challengeRandomNumber;
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
}

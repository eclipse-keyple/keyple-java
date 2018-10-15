/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.command.po.parser.session;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;

/**
 * Open session response parser. See specs: Calypso / page 100 / 9.5.1 - Open secure session
 *
 */
public abstract class AbstractOpenSessionRespPars extends AbstractApduResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;
    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6700, new StatusProperties(false, "Lc value not supported."));
        m.put(0x6900, new StatusProperties(false, "Transaction Counter is 0"));
        m.put(0x6981, new StatusProperties(false,
                "Command forbidden (read requested and current EF is a Binary file)."));
        m.put(0x6982, new StatusProperties(false,
                "Security conditions not fulfilled (PIN code not presented, AES key forbidding the "
                        + "compatibility mode, encryption required)."));
        m.put(0x6985, new StatusProperties(false,
                "Access forbidden (Never access mode, Session already opened)."));
        m.put(0x6986, new StatusProperties(false,
                "Command not allowed (read requested and no current EF)."));
        m.put(0x6A81, new StatusProperties(false, "Wrong key index."));
        m.put(0x6A82, new StatusProperties(false, "File not found."));
        m.put(0x6A83,
                new StatusProperties(false, "Record not found (record index is above NumRec)."));
        m.put(0x6B00, new StatusProperties(false,
                "P1 or P2 value not supported (key index incorrect, wrong P2)."));
        STATUS_TABLE = m;
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        // At this stage, the status table is the same for everyone
        return STATUS_TABLE;
    }

    private final PoRevision revision;

    /** The secure session. */
    SecureSession secureSession;

    /**
     * Instantiates a new AbstractOpenSessionRespPars.
     *
     * @param response the response from Open secure session APDU command
     * @param revision the revision of the PO
     */
    AbstractOpenSessionRespPars(ApduResponse response, PoRevision revision) {
        super(response);
        this.revision = revision;
        this.secureSession = toSecureSession(response.getDataOut());
    }

    public static AbstractOpenSessionRespPars create(ApduResponse response, PoRevision revision) {
        switch (revision) {
            case REV1_0:
                return new OpenSession10RespPars(response);
            case REV2_4:
                return new OpenSession24RespPars(response);
            case REV3_1:
                return new OpenSession31RespPars(response);
            case REV3_2:
                return new OpenSession32RespPars(response);
            default:
                throw new IllegalArgumentException("Unknow revision " + revision);
        }
    }

    abstract SecureSession toSecureSession(byte[] apduResponseData);

    public byte[] getPoChallenge() {
        return secureSession.getChallengeRandomNumber();
    }


    public int getTransactionCounterValue() {
        return ByteBuffer.wrap(secureSession.getChallengeTransactionCounter())
                .order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public boolean wasRatified() {
        return secureSession.isPreviousSessionRatified();
    }

    public boolean isManageSecureSessionAuthorized() {
        return secureSession.isManageSecureSessionAuthorized();
    }

    public byte getSelectedKif() {
        return secureSession.getKIF();
    }

    public byte getSelectedKvc() {
        return secureSession.getKVC();
    }

    public byte[] getRecordDataRead() {
        return secureSession.getOriginalData();
    }

    /**
     * The Class SecureSession. A secure session is returned by a open secure session command
     */
    public static class SecureSession {

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

        /** The kvc (may be null if it doesn't exist in the considered PO [rev 1.0]). */
        private final Byte kvc;

        /** The original data. */
        private final byte[] originalData;

        /** The secure session data. */
        private final byte[] secureSessionData;

        /**
         * Instantiates a new SecureSession for a Calypso application revision 3
         *
         * @param challengeTransactionCounter Challenge transaction counter
         * @param challengeRandomNumber Challenge random number
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
            this.originalData = originalData;
            this.secureSessionData = secureSessionData;
        }

        /**
         * Instantiates a new SecureSession for a Calypso application revision 2.4
         * 
         * @param challengeTransactionCounter Challenge transaction counter
         * @param challengeRandomNumber Challenge random number
         * @param previousSessionRatified the previous session ratified
         * @param manageSecureSessionAuthorized the manage secure session authorized
         * @param kvc the KVC from the response of the open secure session APDU command
         * @param originalData the original data from the response of the open secure session APDU
         *        command
         * @param secureSessionData the secure session data from the response of open secure session
         *        APDU command
         */
        public SecureSession(byte[] challengeTransactionCounter, byte[] challengeRandomNumber,
                boolean previousSessionRatified, boolean manageSecureSessionAuthorized, Byte kvc,
                byte[] originalData, byte[] secureSessionData) {
            this.challengeTransactionCounter = challengeTransactionCounter;
            this.challengeRandomNumber = challengeRandomNumber;
            this.previousSessionRatified = previousSessionRatified;
            this.manageSecureSessionAuthorized = manageSecureSessionAuthorized;
            this.kif = (byte) 0xFF;
            this.kvc = kvc;
            this.originalData = originalData;
            this.secureSessionData = secureSessionData;
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
            return originalData;
        }

        /**
         * Gets the secure session data.
         *
         * @return the secure session data
         */
        public byte[] getSecureSessionData() {
            return secureSessionData;
        }
    }
}

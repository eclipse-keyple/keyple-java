/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;

public class KeyDescriptor {
    public enum Algorithm {
        SIMPLE_DES((byte) 0x00), DESX((byte) 0x40), TDES((byte) 0x90), AES((byte) 0xA0), AES_R32(
                (byte) 0xB0);
        private final byte identifier;

        Algorithm(byte identifier) {
            this.identifier = identifier;
        }

        public byte getIdentifier() {
            return identifier;
        }
    }

    private final String name;
    private final byte recordNumber;
    private final byte kif;
    private final byte kvc;
    private final Algorithm algorithm;

    public KeyDescriptor(String name, byte recordNumber, byte kif, byte kvc, Algorithm algorithm) {
        this.name = name;
        this.recordNumber = recordNumber;
        this.kif = kif;
        this.kvc = kvc;
        this.algorithm = algorithm;
    }

    public String getName() {
        return name;
    }

    public byte getRecordNumber() {
        return recordNumber;
    }

    public byte getKif() {
        return kif;
    }

    public byte getKvc() {
        return kvc;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public String toString() {
        return String.format("Key: %s, REC=%d, KIF=%02X, KVC=%02X, ALG=%02 (%s)", name,
                recordNumber, kif, kvc, algorithm.getIdentifier(), algorithm);
    }
}

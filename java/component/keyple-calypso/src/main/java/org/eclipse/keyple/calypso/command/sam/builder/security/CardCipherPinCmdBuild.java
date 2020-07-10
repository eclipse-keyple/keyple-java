/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.sam.builder.security;

import org.eclipse.keyple.calypso.KeyReference;
import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommand;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.parser.security.CardCipherPinRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * Builder for the SAM Card Cipher PIN APDU command.
 */
public class CardCipherPinCmdBuild extends AbstractSamCommandBuilder<CardCipherPinRespPars> {
    /** The command reference. */
    private static final CalypsoSamCommand command = CalypsoSamCommand.CARD_CIPHER_PIN;

    /**
     * Instantiates a new CardCipherPinCmdBuild and generate the ciphered data for a Verify PIN or
     * Change PIN PO command.
     * <p>
     * In the case of a PIN verification, only the current PIN must be provided (newPin must be set
     * to null).
     * <p>
     * In the case of a PIN update, the current and new PINs must be provided.
     *
     * @param revision of the SAM
     * @param cipheringKey the key used to encipher the PIN data
     * @param currentPin the current PIN (a 4-byte byte array)
     * @param newPin the new PIN (a 4-byte byte array if the operation in progress is a PIN update,
     *        null if the operation in progress is a PIN verification)
     */
    public CardCipherPinCmdBuild(SamRevision revision, KeyReference cipheringKey, byte[] currentPin,
            byte[] newPin) {
        super(command, null);

        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (currentPin == null || currentPin.length != 4) {
            throw new IllegalArgumentException("Bad current PIN value.");
        }

        if (newPin != null && newPin.length != 4) {
            throw new IllegalArgumentException("Bad new PIN value.");
        }

        byte cla = this.defaultRevision.getClassByte();

        byte p1;
        byte p2;
        byte[] data;

        if (newPin == null) {
            // no new PIN is provided, we consider it's a PIN verification
            p1 = (byte) 0x80;
            data = new byte[6];
        } else {
            // a new PIN is provided, we consider it's a PIN update
            p1 = (byte) 0x40;
            data = new byte[10];
            System.arraycopy(newPin, 0, data, 6, 4);
        }
        p2 = (byte) 0xFF; // KIF and KVC in incoming data

        data[0] = cipheringKey.getKif();
        data[1] = cipheringKey.getKvc();

        System.arraycopy(currentPin, 0, data, 2, 4);

        request = setApduRequest(cla, command, p1, p2, data, null);
    }

    @Override
    public CardCipherPinRespPars createResponseParser(ApduResponse apduResponse) {
        return new CardCipherPinRespPars(apduResponse, this);
    }
}

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
package org.eclipse.keyple.calypso.command.po.builder.security;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.parser.security.VerifyPinRespPars;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

public class VerifyPinCmdBuild extends AbstractPoCommandBuilder<VerifyPinRespPars> {
    private static final CalypsoPoCommand command = CalypsoPoCommand.VERIFY_PIN;

    private final byte cla;
    private final byte[] pin = new byte[4];

    /**
     * Verify the PIN
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     * @param pinTransmissionMode defines the way the PIN code is transmitted: in clear or encrypted
     *        form.
     * @param pin the PIN data. The PIN is always 4-byte long here, even in the case of a encrypted
     *        transmission (@see setCipheredPinData).
     */
    public VerifyPinCmdBuild(PoClass poClass, PoTransaction.PinTransmissionMode pinTransmissionMode,
            byte[] pin) {
        super(command, null);

        if (pin == null || pin.length != 4) {
            throw new IllegalArgumentException("The PIN must be 4 bytes long");
        }

        cla = poClass.getValue();

        if (PoTransaction.PinTransmissionMode.ENCRYPTED.equals(pinTransmissionMode)) {
            // only keep the pin
            System.arraycopy(pin, 0, this.pin, 0, 4);
        } else {
            byte p1 = (byte) 0x00;
            byte p2 = (byte) 0x00;

            this.request = setApduRequest(cla, command, p1, p2, pin, null);
            if (logger.isDebugEnabled()) {
                this.addSubName(pinTransmissionMode.toString());
            }
        }
    }

    /**
     * Alternate builder dedicated to the reading of the wrong presentation counter
     * 
     * @param poClass indicates which CLA byte should be used for the Apdu
     */
    public VerifyPinCmdBuild(PoClass poClass) {
        super(command, null);
        cla = poClass.getValue();

        byte p1 = (byte) 0x00;
        byte p2 = (byte) 0x00;

        this.request = setApduRequest(cla, command, p1, p2, null, null);
        if (logger.isDebugEnabled()) {
            this.addSubName("Read presentation counter");
        }
    }

    /**
     * @return the value of the PIN stored at the time of construction
     */
    public byte[] getPin() {
        return pin;
    }

    /**
     * Finalizes the builder in the case of an encrypted transmission
     * 
     * @param pinData the encrypted PIN
     */
    public void setCipheredPinData(byte[] pinData) {
        if (pinData == null || pinData.length != 8) {
            throw new IllegalArgumentException("Wrong length of the PIN encrypted data");
        }
        byte p1 = (byte) 0x00;
        byte p2 = (byte) 0x00;

        this.request = setApduRequest(cla, command, p1, p2, pinData, null);
        if (logger.isDebugEnabled()) {
            this.addSubName(PoTransaction.PinTransmissionMode.ENCRYPTED.toString());
        }
    }

    @Override
    public VerifyPinRespPars createResponseParser(ApduResponse apduResponse) {
        return new VerifyPinRespPars(apduResponse, this);
    }

    @Override
    public boolean isSessionBufferUsed() {
        return false;
    }
}

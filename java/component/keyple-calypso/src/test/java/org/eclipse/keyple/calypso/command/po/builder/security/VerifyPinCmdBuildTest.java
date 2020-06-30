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
package org.eclipse.keyple.calypso.command.po.builder.security;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.shouldHaveThrown;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.parser.security.VerifyPinRespPars;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class VerifyPinCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final String PIN_DATA = "31323334";
    private static final String CIPHERED_PIN_DATA = "0011223344556677";
    private static final String APDU_ISO_PLAIN = "00 20 0000 04" + PIN_DATA;
    private static final String APDU_ISO_ENCRYPTED = "00 20 0000 08" + CIPHERED_PIN_DATA;
    private static final String APDU_ISO_READ_COUNTER = "00 20 0000 00";

    @Test
    public void verifyPin_plain() {
        VerifyPinCmdBuild builder = new VerifyPinCmdBuild(PoClass.ISO,
                PoTransaction.PinTransmissionMode.PLAIN, ByteArrayUtil.fromHex(PIN_DATA));
        byte[] apduRequestBytes = builder.getApduRequest().getBytes();
        assertThat(apduRequestBytes).isEqualTo(ByteArrayUtil.fromHex(APDU_ISO_PLAIN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPin_pin_null() {
        VerifyPinCmdBuild builder =
                new VerifyPinCmdBuild(PoClass.ISO, PoTransaction.PinTransmissionMode.PLAIN, null);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPin_pin_bad_length() {
        VerifyPinCmdBuild builder =
                new VerifyPinCmdBuild(PoClass.ISO, PoTransaction.PinTransmissionMode.PLAIN,
                        ByteArrayUtil.fromHex(PIN_DATA + PIN_DATA));
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test
    public void verifyPin_encrypted() {
        VerifyPinCmdBuild builder =
                new VerifyPinCmdBuild(PoClass.ISO, PoTransaction.PinTransmissionMode.ENCRYPTED,
                        ByteArrayUtil.fromHex(CIPHERED_PIN_DATA));
        byte[] apduRequestBytes = builder.getApduRequest().getBytes();
        assertThat(apduRequestBytes).isEqualTo(ByteArrayUtil.fromHex(APDU_ISO_ENCRYPTED));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPin_encrypted_pin_null() {
        VerifyPinCmdBuild builder = new VerifyPinCmdBuild(PoClass.ISO,
                PoTransaction.PinTransmissionMode.ENCRYPTED, null);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPin_encrypted_pin_bad_length() {
        VerifyPinCmdBuild builder =
                new VerifyPinCmdBuild(PoClass.ISO, PoTransaction.PinTransmissionMode.ENCRYPTED,
                        ByteArrayUtil.fromHex(CIPHERED_PIN_DATA + CIPHERED_PIN_DATA));
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test
    public void verifyPin_read_presentation_counter() {
        VerifyPinCmdBuild builder = new VerifyPinCmdBuild(PoClass.ISO);
        byte[] apduRequestBytes = builder.getApduRequest().getBytes();
        assertThat(apduRequestBytes).isEqualTo(ByteArrayUtil.fromHex(APDU_ISO_READ_COUNTER));
    }

    @Test
    public void verifyPin_various_tests() {
        VerifyPinCmdBuild builder = new VerifyPinCmdBuild(PoClass.ISO,
                PoTransaction.PinTransmissionMode.PLAIN, ByteArrayUtil.fromHex(PIN_DATA));
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null);
        assertThat(builder.createResponseParser(apduResponse).getClass())
                .isEqualTo(VerifyPinRespPars.class);
        assertThat(builder.isSessionBufferUsed()).isFalse();
    }
}

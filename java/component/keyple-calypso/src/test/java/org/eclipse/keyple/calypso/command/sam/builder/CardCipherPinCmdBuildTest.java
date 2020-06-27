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
package org.eclipse.keyple.calypso.command.sam.builder;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.shouldHaveThrown;
import org.eclipse.keyple.calypso.KeyReference;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.security.CardCipherPinCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.CardCipherPinRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class CardCipherPinCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final String KIF_CIPH_KEY = "56";
    private static final String KVC_CIPH_KEY = "78";
    private static final KeyReference KEY_REFERENCE_CIPH_KEY = new KeyReference(
            ByteArrayUtil.fromHex(KIF_CIPH_KEY)[0], ByteArrayUtil.fromHex(KVC_CIPH_KEY)[0]);
    private static final String CURRENT_PIN = "31323334";
    private static final String NEW_PIN = "35363738";
    private static final String APDU_ISO_CHANGE_PIN =
            "80 12 40FF 0A" + KIF_CIPH_KEY + KVC_CIPH_KEY + CURRENT_PIN + NEW_PIN;
    private static final String APDU_ISO_VERIFY_PIN =
            "80 12 80FF 06" + KIF_CIPH_KEY + KVC_CIPH_KEY + CURRENT_PIN;

    @Test
    public void cardCipherPinCmdBuild_update_PIN() {
        CardCipherPinCmdBuild builder =
                new CardCipherPinCmdBuild(SamRevision.C1, KEY_REFERENCE_CIPH_KEY,
                        ByteArrayUtil.fromHex(CURRENT_PIN), ByteArrayUtil.fromHex(NEW_PIN));
        byte[] apduRequestBytes = builder.getApduRequest().getBytes();
        assertThat(apduRequestBytes).isEqualTo(ByteArrayUtil.fromHex(APDU_ISO_CHANGE_PIN));
    }

    @Test
    public void cardCipherPinCmdBuild_verify_PIN() {
        CardCipherPinCmdBuild builder = new CardCipherPinCmdBuild(SamRevision.C1,
                KEY_REFERENCE_CIPH_KEY, ByteArrayUtil.fromHex(CURRENT_PIN), null);
        byte[] apduRequestBytes = builder.getApduRequest().getBytes();
        assertThat(apduRequestBytes).isEqualTo(ByteArrayUtil.fromHex(APDU_ISO_VERIFY_PIN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cardCipherPinCmdBuild_update_PIN_bad_length_1() {
        CardCipherPinCmdBuild builder = new CardCipherPinCmdBuild(SamRevision.C1,
                KEY_REFERENCE_CIPH_KEY, ByteArrayUtil.fromHex(CURRENT_PIN + CURRENT_PIN),
                ByteArrayUtil.fromHex(NEW_PIN));
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cardCipherPinCmdBuild_update_PIN_bad_length_2() {
        CardCipherPinCmdBuild builder = new CardCipherPinCmdBuild(SamRevision.C1,
                KEY_REFERENCE_CIPH_KEY, ByteArrayUtil.fromHex(CURRENT_PIN),
                ByteArrayUtil.fromHex(NEW_PIN + NEW_PIN));
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test
    public void cardCipherPinCmdBuild_parser() {
        CardCipherPinCmdBuild builder =
                new CardCipherPinCmdBuild(SamRevision.C1, KEY_REFERENCE_CIPH_KEY,
                        ByteArrayUtil.fromHex(CURRENT_PIN), ByteArrayUtil.fromHex(NEW_PIN));
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null);
        assertThat(builder.createResponseParser(apduResponse).getClass())
                .isEqualTo(CardCipherPinRespPars.class);
    }
}

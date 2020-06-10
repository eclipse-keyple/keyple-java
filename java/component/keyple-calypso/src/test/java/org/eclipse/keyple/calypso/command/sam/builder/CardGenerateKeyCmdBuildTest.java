/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
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
import org.eclipse.keyple.calypso.command.sam.builder.security.CardGenerateKeyCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.CardGenerateKeyRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CardGenerateKeyCmdBuildTest {
    private static final byte KIF_SRC = (byte) 0x12;
    private static final byte KVC_SRC = (byte) 0x34;
    private static final byte KIF_CIPH_KEY = (byte) 0x56;
    private static final byte KVC_CIPH_KEY = (byte) 0x78;
    private static final KeyReference KEY_REFERENCE_SRC = new KeyReference(KIF_SRC, KVC_SRC);
    private static final KeyReference KEY_REFERENCE_CIPH_KEY =
            new KeyReference(KIF_CIPH_KEY, KVC_CIPH_KEY);
    private static final String SW1SW2_OK = "9000";
    private static final String APDU_CLA_80 = "8012FFFF05"
            + String.format("%02X%02X%02X%02X90", KIF_CIPH_KEY, KVC_CIPH_KEY, KIF_SRC, KVC_SRC);
    private static final String APDU_CLA_94 = "9412FFFF05"
            + String.format("%02X%02X%02X%02X90", KIF_CIPH_KEY, KVC_CIPH_KEY, KIF_SRC, KVC_SRC);
    private static final String APDU_CLA_80_CIPHERING_KEY_NULL =
            "8012FF0003" + String.format("%02X%02X90", KIF_SRC, KVC_SRC);

    @Test
    public void cardGenerateKeyCmdBuild_defaultRevision_createParser() {
        CardGenerateKeyCmdBuild cardGenerateKeyCmdBuild =
                new CardGenerateKeyCmdBuild(null, KEY_REFERENCE_CIPH_KEY, KEY_REFERENCE_SRC);
        assertThat(cardGenerateKeyCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
        CardGenerateKeyRespPars cardGenerateKeyRespPars = cardGenerateKeyCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(cardGenerateKeyRespPars.getClass()).isEqualTo(CardGenerateKeyRespPars.class);
    }

    @Test
    public void cardGenerateKeyCmdBuild_cla94() {
        CardGenerateKeyCmdBuild cardGenerateKeyCmdBuild = new CardGenerateKeyCmdBuild(
                SamRevision.S1D, KEY_REFERENCE_CIPH_KEY, KEY_REFERENCE_SRC);
        assertThat(cardGenerateKeyCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void cardGenerateKeyCmdBuild_cla80() {
        CardGenerateKeyCmdBuild cardGenerateKeyCmdBuild = new CardGenerateKeyCmdBuild(
                SamRevision.C1, KEY_REFERENCE_CIPH_KEY, KEY_REFERENCE_SRC);
        assertThat(cardGenerateKeyCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
    }


    @Test
    public void cardGenerateKeyCmdBuild_cipheringKeyNull() {
        CardGenerateKeyCmdBuild cardGenerateKeyCmdBuild =
                new CardGenerateKeyCmdBuild(SamRevision.C1, null, KEY_REFERENCE_SRC);
        assertThat(cardGenerateKeyCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_CIPHERING_KEY_NULL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cardGenerateKeyCmdBuild_sourceKeyNull() {
        new CardGenerateKeyCmdBuild(SamRevision.C1, KEY_REFERENCE_CIPH_KEY, null);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    //
    // @Test(expected = IllegalArgumentException.class)
    // public void cardGenerateKeyCmdBuild_nullSignature() {
    // new CardGenerateKeyCmdBuild(null, null);
    // shouldHaveThrown(IllegalArgumentException.class);
    // }
    //
    // @Test(expected = IllegalArgumentException.class)
    // public void cardGenerateKeyCmdBuild_badSignatureLength() {
    // new CardGenerateKeyCmdBuild(null, ByteArrayUtil.fromHex(SIGNATURE_PO_BAD_LENGTH));
    // shouldHaveThrown(IllegalArgumentException.class);
    // }
}

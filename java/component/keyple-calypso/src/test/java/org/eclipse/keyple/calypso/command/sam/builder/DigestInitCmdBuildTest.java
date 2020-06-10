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
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.security.DigestInitCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestInitRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestInitCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final byte KIF_OK = (byte) 0x12;
    private static final byte KVC_OK = (byte) 0x34;
    private static final byte KEY_REC_NBR_OK = (byte) 0x56;
    private static final byte KIF_ZERO = (byte) 0x00;
    private static final byte KVC_ZERO = (byte) 0x00;
    private static final byte KEY_REC_NBR_ZERO = (byte) 0x00;
    private static final byte KIF_FF = (byte) 0xFF;
    private static final boolean VERIFICATION_MODE_TRUE = true;
    private static final boolean VERIFICATION_MODE_FALSE = false;
    private static final boolean CONFIDENTIAL_SESSION_MODE_TRUE = true;
    private static final boolean CONFIDENTIAL_SESSION_MODE_FALSE = false;
    private static final String DIGEST_DATA = "112233445566778899AA";
    private static final String APDU_CLA_80_KIF_OK =
            "808A00FF0C" + String.format("%02X%02X", KIF_OK, KVC_OK) + DIGEST_DATA;
    private static final String APDU_CLA_80_VERIF_MODE =
            "808A01FF0C" + String.format("%02X%02X", KIF_OK, KVC_OK) + DIGEST_DATA;
    private static final String APDU_CLA_80_CONFIDENTIAL_SESSION_MODE =
            "808A02FF0C" + String.format("%02X%02X", KIF_OK, KVC_OK) + DIGEST_DATA;
    private static final String APDU_CLA_80_KIF_FF =
            "808A00" + String.format("%02X", KEY_REC_NBR_OK) + "0A" + DIGEST_DATA;
    private static final String APDU_CLA_94_KIF_OK =
            "948A00FF0C" + String.format("%02X%02X", KIF_OK, KVC_OK) + DIGEST_DATA;

    @Test
    public void digestInitCmdBuild_defaultRevision_nominal_createParser() {
        DigestInitCmdBuild digestInitCmdBuild = new DigestInitCmdBuild(null,
                VERIFICATION_MODE_FALSE, CONFIDENTIAL_SESSION_MODE_FALSE, KEY_REC_NBR_OK, KIF_OK,
                KVC_OK, ByteArrayUtil.fromHex(DIGEST_DATA));
        assertThat(digestInitCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_KIF_OK));
        DigestInitRespPars digestInitRespPars = digestInitCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(digestInitRespPars.getClass()).isEqualTo(DigestInitRespPars.class);
    }

    @Test
    public void digestInitCmdBuild_kifFF() {
        DigestInitCmdBuild digestInitCmdBuild = new DigestInitCmdBuild(SamRevision.C1,
                VERIFICATION_MODE_FALSE, CONFIDENTIAL_SESSION_MODE_FALSE, KEY_REC_NBR_OK, KIF_FF,
                KVC_OK, ByteArrayUtil.fromHex(DIGEST_DATA));
        assertThat(digestInitCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_KIF_FF));
    }

    @Test(expected = IllegalArgumentException.class)
    public void digestInitCmdBuild_keyRec00_kif00() {
        new DigestInitCmdBuild(SamRevision.C1, VERIFICATION_MODE_FALSE,
                CONFIDENTIAL_SESSION_MODE_FALSE, KEY_REC_NBR_ZERO, KIF_ZERO, KVC_OK,
                ByteArrayUtil.fromHex(DIGEST_DATA));
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void digestInitCmdBuild_keyRec00_kvc00() {
        new DigestInitCmdBuild(SamRevision.C1, VERIFICATION_MODE_FALSE,
                CONFIDENTIAL_SESSION_MODE_FALSE, KEY_REC_NBR_ZERO, KIF_OK, KVC_ZERO,
                ByteArrayUtil.fromHex(DIGEST_DATA));
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void digestInitCmdBuild_digestDataNull() {
        new DigestInitCmdBuild(SamRevision.C1, VERIFICATION_MODE_FALSE,
                CONFIDENTIAL_SESSION_MODE_FALSE, KEY_REC_NBR_OK, KIF_OK, KVC_OK, null);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test
    public void digestInitCmdBuild_cla94() {
        DigestInitCmdBuild digestInitCmdBuild = new DigestInitCmdBuild(SamRevision.S1D,
                VERIFICATION_MODE_FALSE, CONFIDENTIAL_SESSION_MODE_FALSE, KEY_REC_NBR_OK, KIF_OK,
                KVC_OK, ByteArrayUtil.fromHex(DIGEST_DATA));
        assertThat(digestInitCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94_KIF_OK));
    }

    @Test
    public void digestInitCmdBuild_verificationMode() {
        DigestInitCmdBuild digestInitCmdBuild = new DigestInitCmdBuild(SamRevision.C1,
                VERIFICATION_MODE_TRUE, CONFIDENTIAL_SESSION_MODE_FALSE, KEY_REC_NBR_OK, KIF_OK,
                KVC_OK, ByteArrayUtil.fromHex(DIGEST_DATA));
        ApduRequest ApduRequest = digestInitCmdBuild.getApduRequest();
        assertThat(digestInitCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_VERIF_MODE));
    }

    @Test
    public void digestInitCmdBuild_confidentialSessionMode() {
        DigestInitCmdBuild digestInitCmdBuild = new DigestInitCmdBuild(SamRevision.C1,
                VERIFICATION_MODE_FALSE, CONFIDENTIAL_SESSION_MODE_TRUE, KEY_REC_NBR_OK, KIF_OK,
                KVC_OK, ByteArrayUtil.fromHex(DIGEST_DATA));
        ApduRequest ApduRequest = digestInitCmdBuild.getApduRequest();
        assertThat(digestInitCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_CONFIDENTIAL_SESSION_MODE));
    }
}

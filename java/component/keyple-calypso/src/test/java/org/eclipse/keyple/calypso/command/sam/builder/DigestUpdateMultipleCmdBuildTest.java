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
import org.eclipse.keyple.calypso.command.sam.builder.security.DigestUpdateMultipleCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestUpdateMultipleRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestUpdateMultipleCmdBuildTest {
    private static final boolean ENCRYPTED_SESSION_TRUE = true;
    private static final boolean ENCRYPTED_SESSION_FALSE = false;
    private static final String DIGEST_DATA = "112233445566778899AA";
    private static final String SW1SW2_OK = "9000";
    private static final byte LENGTH_4 = (byte) 0x04;
    private static final String APDU_CLA_80 = "808C00000A" + DIGEST_DATA;
    private static final String APDU_CLA_80_ENCRYPTED_SESSION = "808C00800A" + DIGEST_DATA;
    private static final String APDU_CLA_94 = "948C00000A" + DIGEST_DATA;

    @Test
    public void digestUpdateMultipleCmdBuild_defaultRevision_createParser() {

        DigestUpdateMultipleCmdBuild digestUpdateMultipleCmdBuild =
                new DigestUpdateMultipleCmdBuild(null, ENCRYPTED_SESSION_FALSE,
                        ByteArrayUtil.fromHex(DIGEST_DATA));
        assertThat(digestUpdateMultipleCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
        DigestUpdateMultipleRespPars digestUpdateMultipleRespPars = digestUpdateMultipleCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(digestUpdateMultipleRespPars.getClass())
                .isEqualTo(DigestUpdateMultipleRespPars.class);
    }

    @Test
    public void digestUpdateMultipleCmdBuild_cla94() {

        DigestUpdateMultipleCmdBuild digestUpdateMultipleCmdBuild =
                new DigestUpdateMultipleCmdBuild(SamRevision.S1D, ENCRYPTED_SESSION_FALSE,
                        ByteArrayUtil.fromHex(DIGEST_DATA));
        assertThat(digestUpdateMultipleCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void digestUpdateMultipleCmdBuild_cla80() {

        DigestUpdateMultipleCmdBuild digestUpdateMultipleCmdBuild =
                new DigestUpdateMultipleCmdBuild(SamRevision.C1, ENCRYPTED_SESSION_FALSE,
                        ByteArrayUtil.fromHex(DIGEST_DATA));
        assertThat(digestUpdateMultipleCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
    }

    @Test
    public void digestUpdateMultipleCmdBuild_cla80_encryptedSession() {

        DigestUpdateMultipleCmdBuild digestUpdateMultipleCmdBuild =
                new DigestUpdateMultipleCmdBuild(SamRevision.C1, ENCRYPTED_SESSION_TRUE,
                        ByteArrayUtil.fromHex(DIGEST_DATA));
        assertThat(digestUpdateMultipleCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_ENCRYPTED_SESSION));
    }

    @Test(expected = IllegalArgumentException.class)
    public void digestUpdateMultipleCmdBuild_digestDataNull() {
        new DigestUpdateMultipleCmdBuild(null, ENCRYPTED_SESSION_FALSE, null);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void digestUpdateMultipleCmdBuild_badDigestDataLength() {
        // create digest data > 255 bytes
        String digestData = "";
        while (digestData.length() < (255 * 2)) {
            digestData = digestData + DIGEST_DATA;
        }
        new DigestUpdateMultipleCmdBuild(null, ENCRYPTED_SESSION_FALSE,
                ByteArrayUtil.fromHex(digestData));
        shouldHaveThrown(IllegalArgumentException.class);
    }
}

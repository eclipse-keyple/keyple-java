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
import org.eclipse.keyple.calypso.command.sam.builder.security.DigestAuthenticateCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestAuthenticateRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestAuthenticateCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final String SIGNATURE_PO = "FEDCBA98";
    private static final String SIGNATURE_PO_BAD_LENGTH = "FEDC";
    private static final String APDU_CLA_80 = "8082000004" + SIGNATURE_PO;
    private static final String APDU_CLA_94 = "9482000004" + SIGNATURE_PO;

    @Test
    public void digestAuthenticateCmdBuild_defaultRevision_createParser() {
        DigestAuthenticateCmdBuild digestAuthenticateCmdBuild =
                new DigestAuthenticateCmdBuild(null, ByteArrayUtil.fromHex(SIGNATURE_PO));
        assertThat(digestAuthenticateCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
        DigestAuthenticateRespPars digestAuthenticateRespPars = digestAuthenticateCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(digestAuthenticateRespPars.getClass())
                .isEqualTo(DigestAuthenticateRespPars.class);
    }

    @Test
    public void digestAuthenticateCmdBuild_cla94() {
        DigestAuthenticateCmdBuild digestAuthenticateCmdBuild = new DigestAuthenticateCmdBuild(
                SamRevision.S1D, ByteArrayUtil.fromHex(SIGNATURE_PO));
        assertThat(digestAuthenticateCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void digestAuthenticateCmdBuild_cla80() {
        DigestAuthenticateCmdBuild digestAuthenticateCmdBuild =
                new DigestAuthenticateCmdBuild(SamRevision.C1, ByteArrayUtil.fromHex(SIGNATURE_PO));
        assertThat(digestAuthenticateCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
    }

    @Test(expected = IllegalArgumentException.class)
    public void digestAuthenticateCmdBuild_nullSignature() {
        new DigestAuthenticateCmdBuild(null, null);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void digestAuthenticateCmdBuild_badSignatureLength() {
        new DigestAuthenticateCmdBuild(null, ByteArrayUtil.fromHex(SIGNATURE_PO_BAD_LENGTH));
        shouldHaveThrown(IllegalArgumentException.class);
    }
}

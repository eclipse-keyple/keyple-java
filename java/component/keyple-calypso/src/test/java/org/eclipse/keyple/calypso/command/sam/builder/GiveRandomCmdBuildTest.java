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
import org.eclipse.keyple.calypso.command.sam.builder.security.GiveRandomCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.GiveRandomRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GiveRandomCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final String PO_CHALLENGE = "FEDCBA9889ABCDEF";
    private static final String PO_CHALLENGE_BAD_LENGTH = "FEDC";
    private static final String APDU_CLA_80 = "8086000008" + PO_CHALLENGE;
    private static final String APDU_CLA_94 = "9486000008" + PO_CHALLENGE;

    @Test
    public void giveRandomCmdBuild_defaultRevision_createParser() {
        GiveRandomCmdBuild giveRandomCmdBuild =
                new GiveRandomCmdBuild(null, ByteArrayUtil.fromHex(PO_CHALLENGE));
        assertThat(giveRandomCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
        GiveRandomRespPars giveRandomRespPars = giveRandomCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(giveRandomRespPars.getClass()).isEqualTo(GiveRandomRespPars.class);
    }

    @Test
    public void giveRandomCmdBuild_cla94() {
        GiveRandomCmdBuild giveRandomCmdBuild =
                new GiveRandomCmdBuild(SamRevision.S1D, ByteArrayUtil.fromHex(PO_CHALLENGE));
        assertThat(giveRandomCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void giveRandomCmdBuild_cla80() {
        GiveRandomCmdBuild giveRandomCmdBuild =
                new GiveRandomCmdBuild(SamRevision.C1, ByteArrayUtil.fromHex(PO_CHALLENGE));
        assertThat(giveRandomCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
    }

    @Test(expected = IllegalArgumentException.class)
    public void giveRandomCmdBuild_nullSignature() {
        new GiveRandomCmdBuild(null, null);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void giveRandomCmdBuild_badSignatureLength() {
        new GiveRandomCmdBuild(null, ByteArrayUtil.fromHex(PO_CHALLENGE_BAD_LENGTH));
        shouldHaveThrown(IllegalArgumentException.class);
    }
}

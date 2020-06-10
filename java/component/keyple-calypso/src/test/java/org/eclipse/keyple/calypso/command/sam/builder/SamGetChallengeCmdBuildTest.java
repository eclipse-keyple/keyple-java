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
import org.eclipse.keyple.calypso.command.sam.builder.security.SamGetChallengeCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.SamGetChallengeRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SamGetChallengeCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final byte LENGTH_4 = (byte) 4;
    private static final byte LENGTH_8 = (byte) 8;
    private static final String APDU_CLA_80_L4 = "8084000004";
    private static final String APDU_CLA_80_L8 = "8084000008";
    private static final String APDU_CLA_94 = "9484000004";

    @Test
    public void samGetChallengeCmdBuild_defaultRevision_createParser() {
        SamGetChallengeCmdBuild samGetChallengeCmdBuild =
                new SamGetChallengeCmdBuild(null, LENGTH_4);
        assertThat(samGetChallengeCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_L4));
        SamGetChallengeRespPars samGetChallengeRespPars = samGetChallengeCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(samGetChallengeRespPars.getClass()).isEqualTo(SamGetChallengeRespPars.class);
    }

    @Test
    public void samGetChallengeCmdBuild_cla94() {
        SamGetChallengeCmdBuild samGetChallengeCmdBuild =
                new SamGetChallengeCmdBuild(SamRevision.S1D, LENGTH_4);
        assertThat(samGetChallengeCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void samGetChallengeCmdBuild_length8() {
        SamGetChallengeCmdBuild samGetChallengeCmdBuild =
                new SamGetChallengeCmdBuild(SamRevision.C1, LENGTH_8);
        assertThat(samGetChallengeCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_L8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void samGetChallengeCmdBuild_badLength() {
        new SamGetChallengeCmdBuild(null, (byte) (LENGTH_4 + 1));
        shouldHaveThrown(IllegalArgumentException.class);
    }
}

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
import org.eclipse.keyple.calypso.command.sam.builder.security.UnlockCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.UnlockRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UnlockCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final String UNLOCK_DATA_8 = "12345678FEDCBA98";
    private static final String UNLOCK_DATA_16 = "12345678FEDCBA9889ABCDEF87654321";
    private static final String UNLOCK_DATA_BAD_LENGTH = "12345678FEDCBA";
    private static final String APDU_CLA_80 = "8020000008" + UNLOCK_DATA_8;
    private static final String APDU_CLA_80_16 = "8020000010" + UNLOCK_DATA_16;
    private static final String APDU_CLA_94 = "9420000008" + UNLOCK_DATA_8;

    @Test
    public void unlockCmdBuild_defaultRevision_createParser() {
        UnlockCmdBuild unlockCmdBuild =
                new UnlockCmdBuild(null, ByteArrayUtil.fromHex(UNLOCK_DATA_8));
        assertThat(unlockCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
        UnlockRespPars unlockRespPars = unlockCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(unlockRespPars.getClass()).isEqualTo(UnlockRespPars.class);
    }

    @Test
    public void unlockCmdBuild_cla94() {
        UnlockCmdBuild unlockCmdBuild =
                new UnlockCmdBuild(SamRevision.S1D, ByteArrayUtil.fromHex(UNLOCK_DATA_8));
        assertThat(unlockCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void unlockCmdBuild_cla80() {
        UnlockCmdBuild unlockCmdBuild =
                new UnlockCmdBuild(SamRevision.C1, ByteArrayUtil.fromHex(UNLOCK_DATA_8));
        assertThat(unlockCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
    }

    @Test
    public void unlockCmdBuild_cla80_16() {
        UnlockCmdBuild unlockCmdBuild =
                new UnlockCmdBuild(SamRevision.C1, ByteArrayUtil.fromHex(UNLOCK_DATA_16));
        assertThat(unlockCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_16));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unlockCmdBuild_nullDiversifier() {
        new UnlockCmdBuild(null, null);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unlockCmdBuild_badDiversifierLength() {
        new UnlockCmdBuild(null, ByteArrayUtil.fromHex(UNLOCK_DATA_BAD_LENGTH));
        shouldHaveThrown(IllegalArgumentException.class);
    }
}

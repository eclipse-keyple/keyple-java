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
import org.eclipse.keyple.calypso.command.sam.builder.security.SelectDiversifierCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.SelectDiversifierRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SelectDiversifierCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final String PO_DIVERSIFIER = "12345678FEDCBA98";
    private static final String PO_DIVERSIFIER_BAD_LENGTH = "12345678FEDCBA";
    private static final String APDU_CLA_80 = "8014000008" + PO_DIVERSIFIER;
    private static final String APDU_CLA_94 = "9414000008" + PO_DIVERSIFIER;

    @Test
    public void selectDiversifierCmdBuild_defaultRevision_createParser() {
        SelectDiversifierCmdBuild selectDiversifierCmdBuild =
                new SelectDiversifierCmdBuild(null, ByteArrayUtil.fromHex(PO_DIVERSIFIER));
        assertThat(selectDiversifierCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
        SelectDiversifierRespPars selectDiversifierRespPars = selectDiversifierCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(selectDiversifierRespPars.getClass()).isEqualTo(SelectDiversifierRespPars.class);
    }

    @Test
    public void selectDiversifierCmdBuild_cla94() {
        SelectDiversifierCmdBuild selectDiversifierCmdBuild = new SelectDiversifierCmdBuild(
                SamRevision.S1D, ByteArrayUtil.fromHex(PO_DIVERSIFIER));
        assertThat(selectDiversifierCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void selectDiversifierCmdBuild_cla80() {
        SelectDiversifierCmdBuild selectDiversifierCmdBuild = new SelectDiversifierCmdBuild(
                SamRevision.C1, ByteArrayUtil.fromHex(PO_DIVERSIFIER));
        assertThat(selectDiversifierCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
    }

    @Test(expected = IllegalArgumentException.class)
    public void selectDiversifierCmdBuild_nullDiversifier() {
        new SelectDiversifierCmdBuild(null, null);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void selectDiversifierCmdBuild_badDiversifierLength() {
        new SelectDiversifierCmdBuild(null, ByteArrayUtil.fromHex(PO_DIVERSIFIER_BAD_LENGTH));
        shouldHaveThrown(IllegalArgumentException.class);
    }
}

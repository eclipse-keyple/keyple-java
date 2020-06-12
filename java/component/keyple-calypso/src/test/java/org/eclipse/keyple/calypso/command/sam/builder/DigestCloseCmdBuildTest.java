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
import org.eclipse.keyple.calypso.command.sam.builder.security.DigestCloseCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestCloseRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestCloseCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final byte LENGTH_4 = (byte) 0x04;
    private static final String APDU_CLA_80 = "808E000004";
    private static final String APDU_CLA_94 = "948E000004";

    @Test
    public void digestCloseCmdBuild_defaultRevision_createParser() {
        DigestCloseCmdBuild digestCloseCmdBuild = new DigestCloseCmdBuild(null, LENGTH_4);
        assertThat(digestCloseCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
        DigestCloseRespPars digestCloseRespPars = digestCloseCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(digestCloseRespPars.getClass()).isEqualTo(DigestCloseRespPars.class);
    }

    @Test
    public void digestCloseCmdBuild_cla94() {
        DigestCloseCmdBuild digestCloseCmdBuild =
                new DigestCloseCmdBuild(SamRevision.S1D, LENGTH_4);
        assertThat(digestCloseCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void digestCloseCmdBuild_cla80() {
        DigestCloseCmdBuild digestCloseCmdBuild = new DigestCloseCmdBuild(SamRevision.C1, LENGTH_4);
        assertThat(digestCloseCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
    }

    @Test(expected = IllegalArgumentException.class)
    public void digestCloseCmdBuild_badExpectedLength() {
        new DigestCloseCmdBuild(null, (byte) (LENGTH_4 + 1));
        shouldHaveThrown(IllegalArgumentException.class);
    }
}

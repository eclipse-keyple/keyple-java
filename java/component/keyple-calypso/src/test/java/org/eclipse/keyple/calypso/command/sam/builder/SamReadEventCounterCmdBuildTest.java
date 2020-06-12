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
import org.eclipse.keyple.calypso.command.sam.builder.security.SamReadEventCounterCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.SamReadEventCounterRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SamReadEventCounterCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final int INDEX_M1 = -1;
    private static final int INDEX1 = 1;
    private static final int INDEX4 = 4;
    private static final int INDEX27 = 27;
    private static final String APDU_CLA_80_SINGLE = "80BE008100";
    private static final String APDU_CLA_80_RECORD = "80BE00E100";
    private static final String APDU_CLA_94 = "94BE008100";

    @Test
    public void samReadEventCounterCmdBuild_defaultRevision_createParser() {
        SamReadEventCounterCmdBuild samReadEventCounterCmdBuild = new SamReadEventCounterCmdBuild(
                null, SamReadEventCounterCmdBuild.SamEventCounterOperationType.SINGLE_COUNTER,
                INDEX1);
        assertThat(samReadEventCounterCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_SINGLE));
        SamReadEventCounterRespPars samReadEventCounterRespPars = samReadEventCounterCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(samReadEventCounterRespPars.getClass())
                .isEqualTo(SamReadEventCounterRespPars.class);
    }

    @Test
    public void samReadEventCounterCmdBuild_cla94() {
        SamReadEventCounterCmdBuild samReadEventCounterCmdBuild = new SamReadEventCounterCmdBuild(
                SamRevision.S1D,
                SamReadEventCounterCmdBuild.SamEventCounterOperationType.SINGLE_COUNTER, INDEX1);
        assertThat(samReadEventCounterCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void samReadEventCounterCmdBuild_cla80_single() {
        SamReadEventCounterCmdBuild samReadEventCounterCmdBuild = new SamReadEventCounterCmdBuild(
                SamRevision.C1,
                SamReadEventCounterCmdBuild.SamEventCounterOperationType.SINGLE_COUNTER, INDEX1);
        assertThat(samReadEventCounterCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_SINGLE));
    }

    @Test
    public void samReadEventCounterCmdBuild_cla80_record() {
        SamReadEventCounterCmdBuild samReadEventCounterCmdBuild = new SamReadEventCounterCmdBuild(
                SamRevision.C1,
                SamReadEventCounterCmdBuild.SamEventCounterOperationType.COUNTER_RECORD, INDEX1);
        assertThat(samReadEventCounterCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_RECORD));
    }

    @Test(expected = IllegalArgumentException.class)
    public void samReadEventCounterCmdBuild_cla80_single_indexOutOfBound_1() {
        new SamReadEventCounterCmdBuild(SamRevision.C1,
                SamReadEventCounterCmdBuild.SamEventCounterOperationType.SINGLE_COUNTER, INDEX_M1);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void samReadEventCounterCmdBuild_cla80_single_indexOutOfBound_2() {
        new SamReadEventCounterCmdBuild(SamRevision.C1,
                SamReadEventCounterCmdBuild.SamEventCounterOperationType.SINGLE_COUNTER, INDEX27);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void samReadEventCounterCmdBuild_cla80_record_indexOutOfBound_1() {
        new SamReadEventCounterCmdBuild(SamRevision.C1,
                SamReadEventCounterCmdBuild.SamEventCounterOperationType.COUNTER_RECORD, INDEX_M1);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void samReadEventCounterCmdBuild_cla80_record_indexOutOfBound_2() {
        new SamReadEventCounterCmdBuild(SamRevision.C1,
                SamReadEventCounterCmdBuild.SamEventCounterOperationType.COUNTER_RECORD, INDEX4);
        shouldHaveThrown(IllegalArgumentException.class);
    }
}

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
import org.eclipse.keyple.calypso.command.sam.builder.security.SamReadCeilingsCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.SamReadCeilingsRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SamReadCeilingsCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final int INDEX_M1 = -1;
    private static final int INDEX1 = 1;
    private static final int INDEX4 = 4;
    private static final int INDEX27 = 27;
    private static final String APDU_CLA_80_SINGLE = "80BE01B800";
    private static final String APDU_CLA_80_RECORD = "80BE00B100";
    private static final String APDU_CLA_94 = "94BE01B800";

    @Test
    public void samReadCeilingsCmdBuild_defaultRevision_createParser() {
        SamReadCeilingsCmdBuild samReadCeilingsCmdBuild = new SamReadCeilingsCmdBuild(null,
                SamReadCeilingsCmdBuild.CeilingsOperationType.SINGLE_CEILING, INDEX1);
        assertThat(samReadCeilingsCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_SINGLE));
        SamReadCeilingsRespPars samReadCeilingsRespPars = samReadCeilingsCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(samReadCeilingsRespPars.getClass()).isEqualTo(SamReadCeilingsRespPars.class);
    }

    @Test
    public void samReadCeilingsCmdBuild_cla94() {
        SamReadCeilingsCmdBuild samReadCeilingsCmdBuild =
                new SamReadCeilingsCmdBuild(SamRevision.S1D,
                        SamReadCeilingsCmdBuild.CeilingsOperationType.SINGLE_CEILING, INDEX1);
        assertThat(samReadCeilingsCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void samReadCeilingsCmdBuild_cla80_single() {
        SamReadCeilingsCmdBuild samReadCeilingsCmdBuild =
                new SamReadCeilingsCmdBuild(SamRevision.C1,
                        SamReadCeilingsCmdBuild.CeilingsOperationType.SINGLE_CEILING, INDEX1);
        assertThat(samReadCeilingsCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_SINGLE));
    }

    @Test
    public void samReadCeilingsCmdBuild_cla80_record() {
        SamReadCeilingsCmdBuild samReadCeilingsCmdBuild =
                new SamReadCeilingsCmdBuild(SamRevision.C1,
                        SamReadCeilingsCmdBuild.CeilingsOperationType.CEILING_RECORD, INDEX1);
        assertThat(samReadCeilingsCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80_RECORD));
    }

    @Test(expected = IllegalArgumentException.class)
    public void samReadCeilingsCmdBuild_cla80_single_indexOutOfBound_1() {
        new SamReadCeilingsCmdBuild(SamRevision.C1,
                SamReadCeilingsCmdBuild.CeilingsOperationType.CEILING_RECORD, INDEX_M1);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void samReadCeilingsCmdBuild_cla80_single_indexOutOfBound_2() {
        new SamReadCeilingsCmdBuild(SamRevision.C1,
                SamReadCeilingsCmdBuild.CeilingsOperationType.CEILING_RECORD, INDEX27);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void samReadCeilingsCmdBuild_cla80_record_indexOutOfBound_1() {
        new SamReadCeilingsCmdBuild(SamRevision.C1,
                SamReadCeilingsCmdBuild.CeilingsOperationType.CEILING_RECORD, INDEX_M1);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void samReadCeilingsCmdBuild_cla80_record_indexOutOfBound_2() {
        new SamReadCeilingsCmdBuild(SamRevision.C1,
                SamReadCeilingsCmdBuild.CeilingsOperationType.CEILING_RECORD, INDEX4);
        shouldHaveThrown(IllegalArgumentException.class);
    }
}

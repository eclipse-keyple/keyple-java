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
import org.eclipse.keyple.calypso.command.sam.builder.security.SamWriteKeyCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.security.SamWriteKeyRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SamWriteKeyCmdBuildTest {
    private static final String SW1SW2_OK = "9000";
    private static final byte WRITING_MODE = (byte) 0x12;
    private static final byte KEY_REF = (byte) 0x34;
    private static final String KEY_DATA15 = "00112233445566778899AABBCCDDEE";
    private static final String KEY_DATA16 = "00112233445566778899AABBCCDDEEFF";
    private static final String KEY_DATA17 = "00112233445566778899AABBCCDDEEFF00";
    private static final String KEY_DATA = KEY_DATA16 + KEY_DATA16 + KEY_DATA16;
    private static final String KEY_DATA_BAD_LENGTH1 = KEY_DATA16 + KEY_DATA16 + KEY_DATA15;
    private static final String KEY_DATA_BAD_LENGTH2 =
            KEY_DATA16 + KEY_DATA16 + KEY_DATA16 + KEY_DATA16 + KEY_DATA17;
    private static final String APDU_CLA_80 = "801A123430" + KEY_DATA;
    private static final String APDU_CLA_94 = "941A123430" + KEY_DATA;

    @Test
    public void samWriteKeyCmdBuild_defaultRevision_createParser() {
        SamWriteKeyCmdBuild samWriteKeyCmdBuild = new SamWriteKeyCmdBuild(null, WRITING_MODE,
                KEY_REF, ByteArrayUtil.fromHex(KEY_DATA));
        assertThat(samWriteKeyCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
        SamWriteKeyRespPars samWriteKeyRespPars = samWriteKeyCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null));
        assertThat(samWriteKeyRespPars.getClass()).isEqualTo(SamWriteKeyRespPars.class);
    }

    @Test
    public void samWriteKeyCmdBuild_cla94() {
        SamWriteKeyCmdBuild samWriteKeyCmdBuild = new SamWriteKeyCmdBuild(SamRevision.S1D,
                WRITING_MODE, KEY_REF, ByteArrayUtil.fromHex(KEY_DATA));
        assertThat(samWriteKeyCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_94));
    }

    @Test
    public void samWriteKeyCmdBuild_cla80() {
        SamWriteKeyCmdBuild samWriteKeyCmdBuild = new SamWriteKeyCmdBuild(SamRevision.C1,
                WRITING_MODE, KEY_REF, ByteArrayUtil.fromHex(KEY_DATA));
        assertThat(samWriteKeyCmdBuild.getApduRequest().getBytes())
                .isEqualTo(ByteArrayUtil.fromHex(APDU_CLA_80));
    }

    @Test(expected = IllegalArgumentException.class)
    public void samWriteKeyCmdBuild_nullSignature() {
        new SamWriteKeyCmdBuild(null, WRITING_MODE, KEY_REF, null);
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void samWriteKeyCmdBuild_badSignatureLength_1() {
        new SamWriteKeyCmdBuild(null, WRITING_MODE, KEY_REF,
                ByteArrayUtil.fromHex(KEY_DATA_BAD_LENGTH1));
        shouldHaveThrown(IllegalArgumentException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void samWriteKeyCmdBuild_badSignatureLength_2() {
        new SamWriteKeyCmdBuild(null, WRITING_MODE, KEY_REF,
                ByteArrayUtil.fromHex(KEY_DATA_BAD_LENGTH2));
        shouldHaveThrown(IllegalArgumentException.class);
    }
}

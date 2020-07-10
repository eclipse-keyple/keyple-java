/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.builder.storedvalue;

import static org.assertj.core.api.Java6Assertions.assertThat;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.parser.storedvalue.SvUndebitRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;

public class SvUndebitCmdBuildTest {
    @Test
    public void svUndebitCmdBuild_mode_compat_base() {
        /* */
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ 1, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD5566771234561122334455"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
        Assert.assertEquals("00BC55661477000111223344AAAABBCCDD1234561122334455", apdu);
    }


    @Test
    public void svUndebitCmdBuild_mode_compat_not_finalized() {

        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ 1, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        ApduRequest request = svUndebitCmdBuild.getApduRequest();
        assertThat(request).isEqualTo(null);
    }

    @Test
    public void svUndebitCmdBuild_mode_compat_256() {
        /* */
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ 256, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD5566771234561122334455"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
        Assert.assertEquals("00BC55661477010011223344AAAABBCCDD1234561122334455", apdu);
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_compat_negative_amount() {
        /** @see Calypso Layer ID 8.02 (200108) */
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ -1, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD5566771234561122334455"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_compat_overlimit_amount() {
        /** @see Calypso Layer ID 8.02 (200108) */
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ 32768, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD5566771234561122334455"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_compat_bad_signature_length_1() {
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ 1, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD55667712345611223344556677889900"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_compat_bad_signature_length_2() {
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ 1, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD556677123456112233"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_compat_illegal_date_1() {
        // long date
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ 1, /*
                                 * KVC
                                 */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("112233"),
                /* time */ ByteArrayUtil.fromHex("3344"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_compat_illegal_date_2() {
        // long date
        SvUndebitCmdBuild svUndebitCmdBuild =
                new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1, /* amount */ 1,
                        /*
                         * KVC
                         */ (byte) 0xAA, /* date */ null, /* time */ ByteArrayUtil.fromHex("3344"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_compat_illegal_time_1() {
        // long date
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ 1, /*
                                 * KVC
                                 */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("334455"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_compat_illegal_time_2() {
        // long date
        SvUndebitCmdBuild svUndebitCmdBuild =
                new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1, /* amount */ 1,
                        /*
                         * KVC
                         */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"), /* time */ null);
    }

    @Test
    public void svUndebitCmdBuild_mode_compat_get_debit_data() {
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ 1, /*
                                 * KVC
                                 */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        byte[] svUndebitData = svUndebitCmdBuild.getSvUndebitData();
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("BC00001400000111223344AA"), svUndebitData);
    }

    @Test
    public void svUndebitCmdBuild_mode_compat_response_parser() {
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                /* amount */ 1, /*
                                 * KVC
                                 */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        byte[] svUndebitData = svUndebitCmdBuild.getSvUndebitData();
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD5566771234561122334455"));
        SvUndebitRespPars svBuildCmdParser = svUndebitCmdBuild
                .createResponseParser(new ApduResponse(ByteArrayUtil.fromHex("1122339000"), null));
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("112233"),
                svBuildCmdParser.getSignatureLo());
    }

    @Test
    public void svUndebitCmdBuild_mode_rev3_2_base() {
        /* */
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                /* amount */ 1, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD55667712345611223344556677889900"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
        Assert.assertEquals("00BC55661977000111223344AAAABBCCDD12345611223344556677889900", apdu);
    }

    @Test
    public void svUndebitCmdBuild_mode_rev3_2_256() {
        /* */
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                /* amount */ 256, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD55667712345611223344556677889900"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
        Assert.assertEquals("00BC55661977010011223344AAAABBCCDD12345611223344556677889900", apdu);
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_rev3_2_negative_amount() {
        /** @see Calypso Layer ID 8.02 (200108) */
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                /* amount */ -1, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD55667712345611223344556677889900"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_rev3_2_overlimit_amount() {
        /** @see Calypso Layer ID 8.02 (200108) */
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                /* amount */ 32769, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD55667712345611223344556677889900"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_rev3_2_bad_signature_length_1() {
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                /* amount */ 1, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD556677123456112233445566778899001122"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_rev3_2_bad_signature_length_2() {
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                /* amount */ 1, /* KVC */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD5566771234561122334455667788"));
        String apdu = ByteArrayUtil.toHex(svUndebitCmdBuild.getApduRequest().getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_rev3_2_illegal_date_1() {
        // long date
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                /* amount */ 1, /*
                                 * KVC
                                 */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("112233"),
                /* time */ ByteArrayUtil.fromHex("3344"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_rev3_2_illegal_date_2() {
        // long date
        SvUndebitCmdBuild svUndebitCmdBuild =
                new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2, /* amount */ 1,
                        /*
                         * KVC
                         */ (byte) 0xAA, /* date */ null, /* time */ ByteArrayUtil.fromHex("3344"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_rev3_2_illegal_time_1() {
        // long date
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                /* amount */ 1, /*
                                 * KVC
                                 */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("334455"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void svUndebitCmdBuild_mode_rev3_2_illegal_time_2() {
        // long date
        SvUndebitCmdBuild svUndebitCmdBuild =
                new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2, /* amount */ 1,
                        /*
                         * KVC
                         */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"), /* time */ null);
    }

    @Test
    public void svUndebitCmdBuild_mode_rev3_2_get_debit_data() {
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                /* amount */ 1, /*
                                 * KVC
                                 */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        byte[] svUndebitData = svUndebitCmdBuild.getSvUndebitData();
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("BC00001900000111223344AA"), svUndebitData);
    }

    @Test
    public void svUndebitCmdBuild_mode_rev3_2_response_parser() {
        SvUndebitCmdBuild svUndebitCmdBuild = new SvUndebitCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                /* amount */ 1, /*
                                 * KVC
                                 */ (byte) 0xAA, /* date */ ByteArrayUtil.fromHex("1122"),
                /* time */ ByteArrayUtil.fromHex("3344"));
        byte[] svUndebitData = svUndebitCmdBuild.getSvUndebitData();
        svUndebitCmdBuild.finalizeBuilder(/* SAM ID + prepare SV Debit data */ ByteArrayUtil
                .fromHex("AABBCCDD55667712345611223344556677889900"));
        SvUndebitRespPars svBuildCmdParser = svUndebitCmdBuild.createResponseParser(
                new ApduResponse(ByteArrayUtil.fromHex("1122334455669000"), null));
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("112233445566"),
                svBuildCmdParser.getSignatureLo());
    }
}

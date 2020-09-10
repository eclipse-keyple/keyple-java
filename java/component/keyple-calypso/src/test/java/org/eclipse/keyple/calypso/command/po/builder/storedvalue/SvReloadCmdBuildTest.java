/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.calypso.command.po.builder.storedvalue;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;

public class SvReloadCmdBuildTest {
  @Test
  public void svReloadCmdBuild_mode_compat_base() {

    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 1, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD5566771234561122334455"));
    Assert.assertArrayEquals(
        ByteArrayUtil.fromHex("00B8556617771122F3AAEE0000013344AABBCCDD1234561122334455"),
        svReloadCmdBuild.getApduRequest().getBytes());
    Assert.assertArrayEquals(
        ByteArrayUtil.fromHex("B8000017771122F3AAEE0000013344"),
        svReloadCmdBuild.getSvReloadData());
  }

  @Test
  public void svReloadCmdBuild_mode_compat_not_finalized() {
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 1, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    ApduRequest request = svReloadCmdBuild.getApduRequest();
    assertThat(request).isEqualTo(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_compat_overlimit_negative_amount() {
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ -8388609, /* KVC */
            (byte) 0xAA,
            /* date */ ByteArrayUtil.fromHex("1122"), /* time */
            ByteArrayUtil.fromHex("3344"),
            /* free */ ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD5566771234561122334455"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_compat_overlimit_positive_amount() {
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 8388608, /* KVC */
            (byte) 0xAA,
            /* date */ ByteArrayUtil.fromHex("1122"), /* time */
            ByteArrayUtil.fromHex("3344"),
            /* free */ ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD5566771234561122334455"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_compat_bad_signature_length_1() {
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 1, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD55667712345611223344556677889900"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_compat_bad_signature_length_2() {
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 1, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex("AABBCCDD556677123456112233"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_compat_illegal_date_1() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("112233"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("5566"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_compat_illegal_date_2() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            null,
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("5566"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_compat_illegal_time_1() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("334455"),
            /* free */ ByteArrayUtil.fromHex("5566"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_compat_illegal_time_2() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ null, /* free */
            ByteArrayUtil.fromHex("5566"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_compat_illegal_free_1() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"),
            /* free */ ByteArrayUtil.fromHex("556677"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_compat_illegal_free_2() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_1,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            null);
  }

  @Test
  public void svReloadCmdBuild_mode_rev3_2_base() {

    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 1, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD55667712345611223344556677889900"));
    String apdu = ByteArrayUtil.toHex(svReloadCmdBuild.getApduRequest().getBytes());
    Assert.assertEquals("00B855661C771122F3AAEE0000013344AABBCCDD12345611223344556677889900", apdu);
    Assert.assertArrayEquals(
        ByteArrayUtil.fromHex("B800001C771122F3AAEE0000013344"),
        svReloadCmdBuild.getSvReloadData());
  }

  @Test
  public void svReloadCmdBuild_mode_rev3_2_not_finalized() {
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 1, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    ApduRequest request = svReloadCmdBuild.getApduRequest();
    assertThat(request).isEqualTo(null);
  }

  @Test
  public void svReloadCmdBuild_mode_rev3_2_amout_256() {

    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 256, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD55667712345611223344556677889900"));
    String apdu = ByteArrayUtil.toHex(svReloadCmdBuild.getApduRequest().getBytes());
    Assert.assertEquals("00B855661C771122F3AAEE0001003344AABBCCDD12345611223344556677889900", apdu);
  }

  @Test
  public void svReloadCmdBuild_mode_rev3_2_amout_65536() {

    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 65536, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD55667712345611223344556677889900"));
    String apdu = ByteArrayUtil.toHex(svReloadCmdBuild.getApduRequest().getBytes());
    Assert.assertEquals("00B855661C771122F3AAEE0100003344AABBCCDD12345611223344556677889900", apdu);
  }

  @Test
  public void svReloadCmdBuild_mode_rev3_2_amout_m1() {

    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ -1, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD55667712345611223344556677889900"));
    String apdu = ByteArrayUtil.toHex(svReloadCmdBuild.getApduRequest().getBytes());
    Assert.assertEquals("00B855661C771122F3AAEEFFFFFF3344AABBCCDD12345611223344556677889900", apdu);
  }

  @Test
  public void svReloadCmdBuild_mode_rev3_2_amout_m256() {

    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ -256, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD55667712345611223344556677889900"));
    String apdu = ByteArrayUtil.toHex(svReloadCmdBuild.getApduRequest().getBytes());
    Assert.assertEquals("00B855661C771122F3AAEEFFFF003344AABBCCDD12345611223344556677889900", apdu);
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_rev3_2_overlimit_negative_amount() {
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ -8388609, /* KVC */
            (byte) 0xAA,
            /* date */ ByteArrayUtil.fromHex("1122"), /* time */
            ByteArrayUtil.fromHex("3344"),
            /* free */ ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD55667712345611223344556677889900"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_rev3_2_overlimit_positive_amount() {
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 8388608, /* KVC */
            (byte) 0xAA,
            /* date */ ByteArrayUtil.fromHex("1122"), /* time */
            ByteArrayUtil.fromHex("3344"),
            /* free */ ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD55667712345611223344556677889900"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_rev3_2_bad_signature_length_1() {
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 1, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex(
            "AABBCCDD5566771234561122334455"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_rev3_2_bad_signature_length_2() {
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 1, /* KVC */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("F3EE"));
    svReloadCmdBuild.finalizeBuilder(
        /* SAM ID + prepare SV Reload data */ ByteArrayUtil.fromHex("AABBCCDD5566771234561122"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_rev3_2_illegal_date_1() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("112233"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("5566"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_rev3_2_illegal_date_2() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            null,
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            ByteArrayUtil.fromHex("5566"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_rev3_2_illegal_time_1() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("334455"),
            /* free */ ByteArrayUtil.fromHex("5566"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_rev3_2_illegal_time_2() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ null, /* free */
            ByteArrayUtil.fromHex("5566"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_rev3_2_illegal_free_1() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"),
            /* free */ ByteArrayUtil.fromHex("556677"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void svReloadCmdBuild_mode_rev3_2_illegal_free_2() {
    // long date
    SvReloadCmdBuild svReloadCmdBuild =
        new SvReloadCmdBuild(
            PoClass.ISO,
            PoRevision.REV3_2,
            /* amount */ 1, /*
                             * KVC
                             */
            (byte) 0xAA, /* date */
            ByteArrayUtil.fromHex("1122"),
            /* time */ ByteArrayUtil.fromHex("3344"), /* free */
            null);
  }
}

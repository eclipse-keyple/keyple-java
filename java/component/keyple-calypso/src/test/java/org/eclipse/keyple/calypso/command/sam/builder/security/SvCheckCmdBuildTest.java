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
package org.eclipse.keyple.calypso.command.sam.builder.security;

import static org.junit.Assert.*;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;

public class SvCheckCmdBuildTest {
    @Test
    public void svCheckCmdBuild_mode_compat_base() {
        SvCheckCmdBuild svCheckCmdBuild =
                new SvCheckCmdBuild(SamRevision.C1, ByteArrayUtil.fromHex("112233"));
        String apdu = ByteArrayUtil.toHex(svCheckCmdBuild.getApduRequest().getBytes());
        Assert.assertEquals("8058000003112233", apdu);
    }

    @Test
    public void svCheckCmdBuild_mode_rev3_2_base() {
        SvCheckCmdBuild svCheckCmdBuild =
                new SvCheckCmdBuild(SamRevision.C1, ByteArrayUtil.fromHex("112233445566"));
        String apdu = ByteArrayUtil.toHex(svCheckCmdBuild.getApduRequest().getBytes());
        Assert.assertEquals("8058000006112233445566", apdu);
    }

    @Test
    public void svCheckCmdBuild_abort() {
        SvCheckCmdBuild svCheckCmdBuild = new SvCheckCmdBuild(SamRevision.C1, null);
        String apdu = ByteArrayUtil.toHex(svCheckCmdBuild.getApduRequest().getBytes());
        Assert.assertEquals("8058000000", apdu);
    }

    @Test(expected = IllegalArgumentException.class)
    public void svCheckCmdBuild_bad_signature() {
        SvCheckCmdBuild svCheckCmdBuild =
                new SvCheckCmdBuild(SamRevision.C1, ByteArrayUtil.fromHex("1122"));
    }
}

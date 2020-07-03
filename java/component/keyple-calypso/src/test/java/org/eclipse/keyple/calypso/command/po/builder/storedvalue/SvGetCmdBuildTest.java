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

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.transaction.PoTransaction.SvSettings;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;

public class SvGetCmdBuildTest {
    @Test
    public void svGetCmdBuild_mode_compat_reload() {
        SvGetCmdBuild svGetCmdBuild = new SvGetCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                SvSettings.Operation.RELOAD, new Exception().getStackTrace()[0].getMethodName());

        String cmdBytes = ByteArrayUtil.toHex(svGetCmdBuild.getApduRequest().getBytes());

        Assert.assertEquals("007C000700", cmdBytes);
    }

    @Test
    public void svGetCmdBuild_mode_compat_debit() {
        SvGetCmdBuild svGetCmdBuild = new SvGetCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                SvSettings.Operation.DEBIT, new Exception().getStackTrace()[0].getMethodName());

        String cmdBytes = ByteArrayUtil.toHex(svGetCmdBuild.getApduRequest().getBytes());

        Assert.assertEquals("007C000900", cmdBytes);
    }

    @Test
    public void svGetCmdBuild_mode_compat_get_sv_operation() {
        SvGetCmdBuild svGetCmdBuild = new SvGetCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                SvSettings.Operation.RELOAD, new Exception().getStackTrace()[0].getMethodName());
        Assert.assertEquals(SvSettings.Operation.RELOAD, svGetCmdBuild.getSvOperation());
        svGetCmdBuild = new SvGetCmdBuild(PoClass.ISO, PoRevision.REV3_1,
                SvSettings.Operation.DEBIT, new Exception().getStackTrace()[0].getMethodName());
        Assert.assertEquals(SvSettings.Operation.DEBIT, svGetCmdBuild.getSvOperation());
    }

    @Test
    public void svGetCmdBuild_mode_rev32_reload() {
        SvGetCmdBuild svGetCmdBuild = new SvGetCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                SvSettings.Operation.RELOAD, new Exception().getStackTrace()[0].getMethodName());

        String cmdBytes = ByteArrayUtil.toHex(svGetCmdBuild.getApduRequest().getBytes());

        Assert.assertEquals("007C010700", cmdBytes);
    }

    @Test
    public void svGetCmdBuild_mode_rev32_debit() {
        SvGetCmdBuild svGetCmdBuild = new SvGetCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                SvSettings.Operation.DEBIT, new Exception().getStackTrace()[0].getMethodName());

        String cmdBytes = ByteArrayUtil.toHex(svGetCmdBuild.getApduRequest().getBytes());

        Assert.assertEquals("007C010900", cmdBytes);
    }

    @Test
    public void svGetCmdBuild_mode_rev32_get_sv_operation() {
        SvGetCmdBuild svGetCmdBuild = new SvGetCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                SvSettings.Operation.RELOAD, new Exception().getStackTrace()[0].getMethodName());
        Assert.assertEquals(SvSettings.Operation.RELOAD, svGetCmdBuild.getSvOperation());
        svGetCmdBuild = new SvGetCmdBuild(PoClass.ISO, PoRevision.REV3_2,
                SvSettings.Operation.DEBIT, new Exception().getStackTrace()[0].getMethodName());
        Assert.assertEquals(SvSettings.Operation.DEBIT, svGetCmdBuild.getSvOperation());
    }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.csm.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.csm.CsmRevision;
import org.keyple.calypso.commands.csm.builder.DigestCloseCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

public class DigestCloseCmdBuildTest {

    @Test
    public void digestCloseCmdBuild() throws InconsistentCommandException {

        byte[] request = {(byte) 0x94, (byte) 0x8E, 0x00, 0x00, (byte) 0x04};
        ApduCommandBuilder apduCommandBuilder =
                new DigestCloseCmdBuild(CsmRevision.S1D, (byte) 0x04);// 94
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getbytes());

        byte[] request1 = {(byte) 0x80, (byte) 0x8E, 0x00, 0x00, (byte) 0x04};
        apduCommandBuilder = new DigestCloseCmdBuild(CsmRevision.C1, (byte) 0x04);// 94
        ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request1, ApduRequest.getbytes());

    }
}

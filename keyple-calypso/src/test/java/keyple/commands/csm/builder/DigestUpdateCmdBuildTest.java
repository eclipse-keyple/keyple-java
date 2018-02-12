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
import org.keyple.calypso.commands.csm.builder.DigestUpdateCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

public class DigestUpdateCmdBuildTest {

    @Test
    public void digestUpdateCmdBuild() throws InconsistentCommandException {
        byte[] digestDAta = {(byte) 0x94, (byte) 0xAE, 0x01, 0x02};
        byte[] request = {(byte) 0x94, (byte) 0x8C, 0x00, (byte) 0x80, (byte) digestDAta.length,
                (byte) 0x94, (byte) 0xAE, 0x01, 0x02};

        ApduCommandBuilder apduCommandBuilder =
                new DigestUpdateCmdBuild(CsmRevision.S1D, true, digestDAta);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getbytes());

        byte[] request2 = {(byte) 0x80, (byte) 0x8C, 0x00, (byte) 0x80, (byte) digestDAta.length,
                (byte) 0x94, (byte) 0xAE, 0x01, 0x02};

        ApduCommandBuilder apduCommandBuilder2 =
                new DigestUpdateCmdBuild(CsmRevision.C1, true, digestDAta);
        ApduRequest ApduRequest2 = apduCommandBuilder2.getApduRequest();

        Assert.assertArrayEquals(request2, ApduRequest2.getbytes());
    }
}

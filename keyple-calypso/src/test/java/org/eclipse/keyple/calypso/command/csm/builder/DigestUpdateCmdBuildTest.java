/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.csm.builder;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.calypso.command.csm.CsmRevision;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestUpdateCmdBuildTest {

    @Test
    public void digestUpdateCmdBuild() throws IllegalArgumentException {
        ByteBuffer digestDAta = ByteBuffer.wrap(new byte[] {(byte) 0x94, (byte) 0xAE, 0x01, 0x02});
        ByteBuffer request = ByteBuffer.wrap(new byte[] {(byte) 0x94, (byte) 0x8C, 0x00,
                (byte) 0x80, (byte) digestDAta.limit(), (byte) 0x94, (byte) 0xAE, 0x01, 0x02});

        AbstractApduCommandBuilder apduCommandBuilder =
                new DigestUpdateCmdBuild(CsmRevision.S1D, true, digestDAta);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertEquals(request, ApduRequest.getBytes());

        ByteBuffer request2 = ByteBuffer.wrap(new byte[] {(byte) 0x80, (byte) 0x8C, 0x00,
                (byte) 0x80, (byte) digestDAta.limit(), (byte) 0x94, (byte) 0xAE, 0x01, 0x02});

        AbstractApduCommandBuilder apduCommandBuilder2 =
                new DigestUpdateCmdBuild(CsmRevision.C1, true, digestDAta);
        ApduRequest apduReq = apduCommandBuilder2.getApduRequest();
        Assert.assertEquals(request2, apduReq.getBytes());
    }
}

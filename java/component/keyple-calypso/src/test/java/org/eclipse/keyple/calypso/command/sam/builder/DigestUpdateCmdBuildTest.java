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


import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.session.DigestUpdateCmdBuild;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestUpdateCmdBuildTest {

    @Test
    public void digestUpdateCmdBuild() throws IllegalArgumentException {
        byte[] digestDAta = new byte[] {(byte) 0x94, (byte) 0xAE, 0x01, 0x02};
        byte[] request = new byte[] {(byte) 0x94, (byte) 0x8C, 0x00, (byte) 0x80,
                (byte) digestDAta.length, (byte) 0x94, (byte) 0xAE, 0x01, 0x02};

        AbstractApduCommandBuilder apduCommandBuilder =
                new org.eclipse.keyple.calypso.command.sam.builder.session.DigestUpdateCmdBuild(
                        SamRevision.S1D, true, digestDAta);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getBytes());

        byte[] request2 = new byte[] {(byte) 0x80, (byte) 0x8C, 0x00, (byte) 0x80,
                (byte) digestDAta.length, (byte) 0x94, (byte) 0xAE, 0x01, 0x02};

        AbstractApduCommandBuilder apduCommandBuilder2 =
                new DigestUpdateCmdBuild(SamRevision.C1, true, digestDAta);
        ApduRequest apduReq = apduCommandBuilder2.getApduRequest();
        Assert.assertArrayEquals(request2, apduReq.getBytes());
    }
}

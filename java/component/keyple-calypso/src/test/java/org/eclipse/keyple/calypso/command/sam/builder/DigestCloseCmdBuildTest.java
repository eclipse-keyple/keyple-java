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
import org.eclipse.keyple.calypso.command.sam.builder.session.DigestCloseCmdBuild;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestCloseCmdBuildTest {

    @Test
    public void digestCloseCmdBuild() throws IllegalArgumentException {

        byte[] request = new byte[] {(byte) 0x94, (byte) 0x8E, 0x00, 0x00, (byte) 0x04};
        AbstractApduCommandBuilder apduCommandBuilder =
                new org.eclipse.keyple.calypso.command.sam.builder.session.DigestCloseCmdBuild(
                        SamRevision.S1D, (byte) 0x04);// 94
        ApduRequest apduReq = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, apduReq.getBytes());

        byte[] request1 = new byte[] {(byte) 0x80, (byte) 0x8E, 0x00, 0x00, (byte) 0x04};
        apduCommandBuilder = new DigestCloseCmdBuild(SamRevision.C1, (byte) 0x04);// 94
        apduReq = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request1, apduReq.getBytes());
    }
}

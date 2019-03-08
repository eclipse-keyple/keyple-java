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
package org.eclipse.keyple.calypso.command.po.builder;


import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetDataCmdBuildTest {

    @Test
    public void getDataFCICmdBuild() {
        byte[] request = new byte[] {(byte) 0x94, (byte) 0xCA, (byte) 0x00, 0x6F, 0x00};
        AbstractApduCommandBuilder apduCommandBuilder = new GetDataFciCmdBuild(PoClass.LEGACY);
        ApduRequest apduReq = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request, apduReq.getBytes());
    }


    @Test
    public void getDataFCICmdBuild2() {
        byte[] request2 = new byte[] {(byte) 0x00, (byte) 0xCA, (byte) 0x00, 0x6F, 0x00};
        AbstractApduCommandBuilder apduCommandBuilder = new GetDataFciCmdBuild(PoClass.ISO);
        ApduRequest apduReq = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request2, apduReq.getBytes());
    }
}

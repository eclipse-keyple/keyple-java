/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.builder;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetDataCmdBuildTest {

    @Test
    public void getDataFCICmdBuild() {
        ByteBuffer request =
                ByteBuffer.wrap(new byte[] {(byte) 0x94, (byte) 0xCA, (byte) 0x00, 0x6F, 0x00});
        AbstractApduCommandBuilder apduCommandBuilder = new GetDataFciCmdBuild(PoRevision.REV2_4);
        ApduRequest apduReq = apduCommandBuilder.getApduRequest();
        Assert.assertEquals(request, apduReq.getBytes());
    }


    @Test
    public void getDataFCICmdBuild2() {
        ByteBuffer request2 =
                ByteBuffer.wrap(new byte[] {(byte) 0x00, (byte) 0xCA, (byte) 0x00, 0x6F, 0x00});
        AbstractApduCommandBuilder apduCommandBuilder = new GetDataFciCmdBuild(PoRevision.REV3_1);
        ApduRequest apduReq = apduCommandBuilder.getApduRequest();
        Assert.assertEquals(request2, apduReq.getBytes());
    }
}

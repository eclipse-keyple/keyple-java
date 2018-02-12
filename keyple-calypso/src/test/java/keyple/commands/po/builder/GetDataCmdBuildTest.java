/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.po.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.GetDataFciCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.seproxy.ApduRequest;

public class GetDataCmdBuildTest {

    @Test
    public void getDataFCICmdBuild() {
        byte[] request = {(byte) 0x94, (byte) 0xCA, (byte) 0x00, 0x6F, 0x00};
        ApduCommandBuilder apduCommandBuilder = new GetDataFciCmdBuild(PoRevision.REV2_4);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request, ApduRequest.getbytes());


        byte[] request2 = {(byte) 0x00, (byte) 0xCA, (byte) 0x00, 0x6F, 0x00};
        ApduCommandBuilder apduCommandBuilder2 = new GetDataFciCmdBuild(PoRevision.REV3_1);
        ApduRequest ApduRequest2 = apduCommandBuilder2.getApduRequest();
        Assert.assertArrayEquals(request2, ApduRequest2.getbytes());
    }
}

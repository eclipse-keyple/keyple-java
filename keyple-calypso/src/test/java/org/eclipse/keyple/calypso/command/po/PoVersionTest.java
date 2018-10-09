/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po;


import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PoVersionTest {

    /* Building FCI data with the application byte as a variant */
    private static class BuildFci {
        private static ApduResponse getFci(byte applicationByte) {
            return new ApduResponse(ByteArrayUtils.fromHex(String
                    .format("6F 22 84 08 315449432E494341" + "A5 16 BF0C 13 C7 08 0000000011223344"
                            + "53 07 060A %02X 02200311 9000", applicationByte)),
                    null);
        }
    }

    @Test
    public void computePoRevision() {
        Assert.assertEquals(new GetDataFciRespPars(BuildFci.getFci((byte) 0x01)).getPoRevision(),
                PoRevision.REV2_4);
        Assert.assertEquals(new GetDataFciRespPars(BuildFci.getFci((byte) 0x04)).getPoRevision(),
                PoRevision.REV2_4);
        Assert.assertEquals(new GetDataFciRespPars(BuildFci.getFci((byte) 0x06)).getPoRevision(),
                PoRevision.REV2_4);
        Assert.assertEquals(new GetDataFciRespPars(BuildFci.getFci((byte) 0x1F)).getPoRevision(),
                PoRevision.REV2_4);
        Assert.assertEquals(new GetDataFciRespPars(BuildFci.getFci((byte) 0x20)).getPoRevision(),
                PoRevision.REV3_1);
        Assert.assertEquals(new GetDataFciRespPars(BuildFci.getFci((byte) 0x27)).getPoRevision(),
                PoRevision.REV3_1);
        Assert.assertEquals(new GetDataFciRespPars(BuildFci.getFci((byte) 0x28)).getPoRevision(),
                PoRevision.REV3_2);
        Assert.assertEquals(new GetDataFciRespPars(BuildFci.getFci((byte) 0x2F)).getPoRevision(),
                PoRevision.REV3_2);
    }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.transaction;


import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CalypsoPOTest {
    /* Building FCI data with the application byte as a variant and initialize PO */
    public static CalypsoPO getPoApplicationByte(byte applicationByte) {
        ApduResponse fciData =
                new ApduResponse(ByteArrayUtils.fromHex(String.format("6F 22 84 08 315449432E494341"
                        + "A5 16 BF0C 13 C7 08 0000000011223344" + "53 07 060A %02X 02200311 9000",
                        applicationByte)), null);
        SeResponse selectionData = new SeResponse(true, null, fciData, null);
        return new CalypsoPO(selectionData);
    }

    @Test
    public void computePoRevision() {
        Assert.assertEquals(getPoApplicationByte((byte) 0x01).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x04).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x06).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x1F).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x20).getRevision(), PoRevision.REV3_1);

        Assert.assertEquals(getPoApplicationByte((byte) 0x27).getRevision(), PoRevision.REV3_1);

        Assert.assertEquals(getPoApplicationByte((byte) 0x28).getRevision(), PoRevision.REV3_2);

        Assert.assertEquals(getPoApplicationByte((byte) 0x2F).getRevision(), PoRevision.REV3_2);
    }
}

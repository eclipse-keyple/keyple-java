/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso;


import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PortableObjectTest {
    static PortableObject portableObject = PortableObject.getInstance();

    /* Building FCI data with the application byte as a variant and initialize PO */
    public static void setPoApplicationByte(byte applicationByte) {
        ApduResponse fciData =
                new ApduResponse(ByteArrayUtils.fromHex(String.format("6F 22 84 08 315449432E494341"
                        + "A5 16 BF0C 13 C7 08 0000000011223344" + "53 07 060A %02X 02200311 9000",
                        applicationByte)), null);
        SeResponse selectionData = new SeResponse(true, null, fciData, null);
        portableObject.intialize(selectionData);
    }

    @Test
    public void computePoRevision() {
        setPoApplicationByte((byte) 0x01);
        Assert.assertEquals(portableObject.getRevision(), PoRevision.REV2_4);

        setPoApplicationByte((byte) 0x04);
        Assert.assertEquals(portableObject.getRevision(), PoRevision.REV2_4);

        setPoApplicationByte((byte) 0x06);
        Assert.assertEquals(portableObject.getRevision(), PoRevision.REV2_4);

        setPoApplicationByte((byte) 0x01F);
        Assert.assertEquals(portableObject.getRevision(), PoRevision.REV2_4);

        setPoApplicationByte((byte) 0x20);
        Assert.assertEquals(portableObject.getRevision(), PoRevision.REV3_1);

        setPoApplicationByte((byte) 0x27);
        Assert.assertEquals(portableObject.getRevision(), PoRevision.REV3_1);

        setPoApplicationByte((byte) 0x28);
        Assert.assertEquals(portableObject.getRevision(), PoRevision.REV3_2);

        setPoApplicationByte((byte) 0x2F);
        Assert.assertEquals(portableObject.getRevision(), PoRevision.REV3_2);
    }
}

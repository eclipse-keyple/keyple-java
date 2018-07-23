/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.Test;

public class AbstractApduBufferTest {

    @Test
    public void testSlice() {
        ByteBuffer command = ByteBuffer.allocate(3);
        command.put((byte) 0x01);
        command.put((byte) 0x02);
        command.put((byte) 0x03);
        ByteBuffer result = ByteBuffer.allocate(1);
        result.put((byte) 0x01);
        result.position(0);
        ApduRequest request = new ApduRequest(command, true);
        ByteBuffer slice = request.slice(0, 1);
        Assert.assertEquals(result, slice);
    }

    @Test
    public void getBytes() {
        ByteBuffer command = ByteBuffer.allocate(3);
        command.put((byte) 0x01);
        ApduRequest request = new ApduRequest(command, true);
        Assert.assertEquals(command, request.getBytes());
    }

    @Test
    public void testToString() {
        ApduRequest request = new ApduRequest(null, true);
        System.out.println(request.toString());
        Assert.assertNotNull(request.toString());
    }
}

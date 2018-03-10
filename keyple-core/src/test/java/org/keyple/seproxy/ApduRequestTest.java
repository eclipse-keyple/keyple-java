/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import org.junit.Test;

public class ApduRequestTest {

    @Test
    public void testAPDURequest() {
        ApduRequest request = new ApduRequest(new byte[] {(byte) 0x01, (byte) 0x02}, true);
        assertNotNull(request);
    }

    @Test
    public void testGetbytes() {
        ApduRequest request = new ApduRequest(new byte[] {(byte) 0x01, (byte) 0x02}, true);
        assertArrayEquals(new byte[] {(byte) 0x01, (byte) 0x02}, request.getBytes());
    }

    @Test
    public void testIsCase4() {
        ApduRequest request = new ApduRequest(new byte[] {(byte) 0x01, (byte) 0x02}, true);
        assertTrue(request.isCase4());
    }

    @Test
    public void testToString() {
        ApduRequest request = new ApduRequest(new byte[] {(byte) 0x01, (byte) 0x02}, true);
        assertEquals("Req{0102}", request.toString());
    }

    @Test
    public void byteBufferDefault() {
        workOnApdu(new ApduRequest());
    }

    @Test
    public void byteBufferAllocate20() {
        workOnApdu(new ApduRequest(ByteBuffer.allocate(20), false));
    }

    @Test
    public void byteBufferWrap() {
        workOnApdu(new ApduRequest(new byte[10], 5, 5, false));
    }

    private void workOnApdu(ApduRequest request) {
        byte[] data = new byte[] {0x11, 0x12, 0x13, 0x14};
        request.put((byte) data.length);
        request.put(data);
        assertEquals("Req{0411 1213 14}", request.toString());
    }

}

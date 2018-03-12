/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import static org.junit.Assert.*;
import org.junit.Test;


public class ApduResponseTest {

    @Test
    public void testAPDUResponse() {
        ApduResponse response = new ApduResponse(new byte[] {(byte) 0x01, (byte) 0x02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        assertNotNull(response);
    }

    @Test
    public void testGetbytes() {
        ApduResponse response = new ApduResponse(new byte[] {(byte) 0x01, (byte) 0x02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        assertArrayEquals(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04},
                response.getBytes());
    }

    @Test
    public void testIsSuccessful() {
        ApduResponse response = new ApduResponse(new byte[] {(byte) 0x01, (byte) 0x02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        assertTrue(response.isSuccessful());
    }

    @Test
    public void testGetStatusCode() {
        ApduResponse response = new ApduResponse(new byte[] {(byte) 0x01, (byte) 0x02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        assertEquals(0x03 * 256 + 0x04, response.getStatusCode());
        // assertArrayEquals(new byte[] {(byte) 0x03, (byte) 0x04}, response.getStatusCodeOld());
    }

    @Test
    public void niceFormat() {
        ApduResponse response = new ApduResponse(ByteBufferUtils.fromHex("FEDCBA98 9000h"), true);
        assertEquals("FEDCBA989000", ByteBufferUtils.toHex(response.getBuffer()));
    }

    @Test
    public void statusCode() {
        ApduResponse response = new ApduResponse(ByteBufferUtils.fromHex("FEDCBA98 9000h"), true);
        assertEquals(0x9000, response.getStatusCode());
    }

}

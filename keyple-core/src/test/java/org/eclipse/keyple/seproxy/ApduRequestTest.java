/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import static org.junit.Assert.*;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.junit.Test;

public class ApduRequestTest {

    @Test
    public void testAPDURequest() {
        ApduRequest request = new ApduRequest(ByteBufferUtils.fromHex("0102"), true);
        assertNotNull(request);
    }

    // @Test
    // public void testGetbytes() {
    // ApduRequest request = new ApduRequest(new byte[] {(byte) 0x01, (byte) 0x02}, true);
    // assertArrayEquals(new byte[] {(byte) 0x01, (byte) 0x02}, request.getBytes());
    // }

    @Test
    public void testIsCase4() {
        ApduRequest request = new ApduRequest(ByteBufferUtils.fromHex("0102"), true);
        assertTrue(request.isCase4());
    }

    // @Test
    // public void testToString() {
    // ApduRequest request = new ApduRequest(ByteBufferUtils.fromHex("0102"), true);
    // assertEquals("Req{0102}", request.toString());
    // }
}

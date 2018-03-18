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

public class ByteBufferUtilsTest {

    @Test
    public void fromHex() {
        assertEquals(ByteBuffer.wrap(new byte[] {0x01, 0x02, 0x03, 0x04}),
                ByteBufferUtils.fromHex("0102 03 04h"));
        assertEquals(ByteBufferUtils.fromHex("01020304"), ByteBufferUtils.fromHex("0102 03 04h"));
        assertEquals(ByteBufferUtils.fromHex("FEDCBA98 9000h"),
                ByteBufferUtils.fromHex("fedcba98 9000h"));
        assertEquals(ByteBufferUtils.fromHex("FFFE"),
                ByteBuffer.wrap(new byte[] {(byte) 0xFF, (byte) 0xFE}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromHexBad() {
        ByteBufferUtils.fromHex("010203045");
    }

    @Test
    public void toHexCutLen() {
        assertEquals("0102 0304 05060708",
                ByteBufferUtils.toHexCutLen(ByteBufferUtils.fromHex("0102030405060708"), 2, 2));
    }

    @Test
    public void toHex() {
        assertEquals("010203 040506", ByteBufferUtils.toHex(ByteBufferUtils.fromHex("010203"),
                ByteBufferUtils.fromHex("040506")));
    }

    @Test
    public void toHexCut() {
        assertEquals("010203 040506",
                ByteBufferUtils.toHexCutLen(ByteBufferUtils.fromHex("010203040506"), 3));
    }

    @Test
    public void toHexCut2() {
        assertEquals("010203 040506",
                ByteBufferUtils.toHexCutLen(ByteBufferUtils.fromHex("010203040506"), 3, 3));
    }
}

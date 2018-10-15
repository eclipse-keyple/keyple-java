/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy;

import static org.junit.Assert.assertEquals;
import java.nio.ByteBuffer;
import org.junit.Test;

/**
 * This is to explain to everyone how ByteBuffer are supposed to be used
 */
public class ByteBufferTest {

    @Test
    public void testSlices() {
        slices();
    }

    public static void slices() {
        // We're creating a buffer wrapper an array of 6 bytes, starting at position 1 with a limit
        // of 4
        // The array offset is still 0 and the capacity still 6
        ByteBuffer buf = ByteBuffer.wrap(new byte[6], 1, 4);
        // Hard limits:
        assertEquals(0, buf.arrayOffset());
        assertEquals(6, buf.capacity());
        // Soft limits:
        assertEquals(1, buf.position());
        assertEquals(5, buf.limit());

        // By creating the slice, we're defining boundaries within an existing array. The wrapped
        // array is not
        // modified or copied.
        ByteBuffer slice = buf.slice();

        // The soft limits lose their offset:
        assertEquals(0, slice.position());
        assertEquals(4, slice.limit());
        // And the soft limits become the hard limits:
        assertEquals(1, slice.arrayOffset());
        assertEquals(4, slice.capacity());


        // We write one byte
        slice.put((byte) 0x02);

        // Save the current position
        slice.mark();

        // Write 2 other bytes
        slice.put(new byte[] {(byte) 0x10, 0x11});

        // We're in position 3
        assertEquals(3, slice.position());

        slice.reset();

        // We're in position 1
        assertEquals(1, slice.position());

        slice.limit(slice.position() + 2);
        ByteBuffer slice2 = slice.slice();
        assertEquals(2, slice2.arrayOffset());
        assertEquals(0, slice2.position());
        assertEquals(2, slice2.limit());
        assertEquals(2, slice2.capacity());
    }
}

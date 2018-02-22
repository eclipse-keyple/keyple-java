/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.nio.ByteBuffer;

public abstract class AbstractApduBuffer {
    /**
     * Internal buffer
     */
    final ByteBuffer buffer;


    AbstractApduBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    AbstractApduBuffer(byte[] data) {
        // TODO: Drop the null compatibility behavior, it's confusing
        this(data != null ? ByteBuffer.wrap(data) : ByteBuffer.allocate(0));
        // buffer.limit(data.length);
    }

    AbstractApduBuffer() {
        this(ByteBuffer.allocate(255));
    }

    AbstractApduBuffer(byte[] data, int offset, int length) {
        this(ByteBuffer.wrap(data, offset, length).slice());
    }

    /**
     * Get the content as a new byte array. Please note this operation should be avoided as much as
     * possible
     *
     * @return Newly created array with a copy of the content
     */
    public byte[] getBytes() {
        byte[] data = new byte[buffer.limit()];
        buffer.rewind();
        buffer.get(data);
        return data;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public String toString() {
        return toHex(buffer);
    }

    /**
     * Represent the ByteBuffer. We only show the buffer from the array's offset to the limit.
     * 
     * @param buffer ByteBuffer
     * @return
     */
    public static String toHex(ByteBuffer buffer) {
        StringBuilder str = new StringBuilder(buffer.limit() * 2);
        final byte[] array = buffer.array();
        for (int i = buffer.arrayOffset(), e = i + buffer.limit(); i < e; i++) {
            str.append(String.format("%02X", array[i]));
        }

        return str.toString();
    }
}

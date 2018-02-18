/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.nio.ByteBuffer;

public abstract class AbstractApduWrapper {
    final ByteBuffer buffer;


    AbstractApduWrapper(ByteBuffer buffer) {
        this.buffer = buffer;
        buffer.mark();
    }

    AbstractApduWrapper(byte[] data) {
        // TODO: Drop the null compatibility behavior, it's confusing
        this(data != null ? ByteBuffer.wrap(data) : ByteBuffer.allocate(0));
        buffer.position(data != null ? data.length : 0);
        // buffer.limit(data.length);
    }

    AbstractApduWrapper() {
        this(ByteBuffer.allocate(255));
    }

    AbstractApduWrapper(byte[] data, int offset, int length) {
        this(ByteBuffer.wrap(data, offset, length).slice());
    }

    // public abstract <T extends AbstractApduWrapper> T getSlice(int offset, int length);


    public ByteBuffer getBufferSlice(int offset, int length) {
        ByteBuffer slice = buffer.slice();
        buffer.position(offset).limit(offset + length);
        return slice;
    }

    /**
     * Get the content
     *
     * @return
     */
    public byte[] getBytes() {
        byte[] data = new byte[buffer.limit()];
        buffer.reset();
        buffer.get(data);
        return data;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public String toString() {
        final int end = buffer.position();
        buffer.reset();
        final int mark = buffer.position();
        buffer.position(end);

        StringBuilder sb = new StringBuilder(buffer.limit() * 2);
        final byte[] array = buffer.array();
        for (int i = mark; i < end; i++) {
            sb.append(String.format("%02X", array[i]));
        }

        return sb.toString();
    }
}

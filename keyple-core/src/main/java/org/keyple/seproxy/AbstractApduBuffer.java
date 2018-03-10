/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.nio.ByteBuffer;

/**
 * APDU Buffer. It's mostly to avoid to inherit directly the {@link ByteBuffer} and have many
 * methods inherited from it.
 */
class AbstractApduBuffer {
    /**
     * Internal buffer
     */
    final ByteBuffer buffer;


    AbstractApduBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Abstract constructor
     * 
     * @param data Array to wrap. Cannot be null.
     */
    AbstractApduBuffer(byte[] data) {
        // null data is not allowed anymore
        this(ByteBuffer.wrap(data));
    }

    AbstractApduBuffer() {
        this(ByteBuffer.allocate(261));
    }

    AbstractApduBuffer(byte[] data, int offset, int length) {
        this(ByteBuffer.wrap(data, offset, length).slice());
    }

    /**
     * Get the content as a new byte array. Please note this operation should be avoided as much as
     * possible
     *
     * @return Newly created array with a copy of the content
     * @deprecated Use something that relies on the ByteBuffer
     */
    public byte[] getBytes() {
        return ByteBufferUtils.toBytes(buffer);
    }


    /**
     * Get a slice from the buffer
     *
     * @param position Position of the new slice in the buffer
     * @param length Length of the slice in the buffer
     * @return A byte buffer coming from the inner {@link #buffer}
     */
    public ByteBuffer slice(int position, int length) {
        ByteBuffer duplicate = buffer.duplicate();
        duplicate.position(position);
        duplicate.limit(position + length);
        return duplicate.slice();
    }

    /**
     * Get the internal buffer
     * 
     * @return Buffer
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public String toString() {
        return ByteBufferUtils.toHex(buffer);
    }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.nio.ByteBuffer;
import org.openjdk.jmh.annotations.Benchmark;

public class ByteBufferBenchmark {
    @Benchmark
    public void allocateByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(255);
    }

    @Benchmark
    public void allocateArray() {
        byte[] buf = new byte[255];
    }

    @Benchmark
    public void createSubByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(255);

        buf.position(0).limit(4);
        ByteBuffer head = buf.slice();
        buf.clear();

        buf.position(buf.limit() - 4);
        ByteBuffer tail = buf.slice();
        buf.clear();

        if (!head.equals(tail)) {
            throw new RuntimeException("Buffers aren't the same");
        }
    }

    @Benchmark
    public void createSubArray() {
        byte[] buf = new byte[255];
        byte[] head = new byte[4];
        System.arraycopy(head, 0, buf, 0, 4);
    }
}

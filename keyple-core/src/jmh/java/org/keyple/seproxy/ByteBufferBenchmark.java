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
    public void byteBuffer() {
        byte[] array = ByteBuffer.allocate(255).array();
    }

    @Benchmark
    public void array() {
        byte[] array = new byte[255];
    }
}

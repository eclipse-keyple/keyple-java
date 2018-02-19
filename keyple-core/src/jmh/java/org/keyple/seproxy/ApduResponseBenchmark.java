/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.openjdk.jmh.annotations.Benchmark;

public class ApduResponseBenchmark {
    private static final byte[] MESSAGE_1;

    static {
        try {
            MESSAGE_1 =
                    Hex.decodeHex("8517 08 0404 1D03 1F101010 00030303 00 0000 0000000000 2010 9000"
                            .replaceAll(" ", ""));
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void load() {
        ApduRequest request = new ApduRequest(MESSAGE_1, true);
        request.getbytes();
    }
}

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
    private static final String HEX =
            "8517 08 0404 1D03 1F101010 00030303 00 0000 0000000000 2010 9000";
    private static final byte[] MESSAGE_1;

    static {
        try {
            MESSAGE_1 = Hex.decodeHex(HEX.replaceAll(" ", ""));
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void loadFromByte() {
        ApduResponse request = new ApduResponse(MESSAGE_1, true);
        assert (request.getBytes().length != 0);
    }

    @Benchmark
    public void loadFromString() {
        ApduResponse request = new ApduResponse(HEX, true);
        assert (request.getBytes().length != 0);
    }
}

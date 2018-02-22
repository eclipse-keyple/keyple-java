/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import static org.junit.Assert.*;
import org.apache.commons.codec.DecoderException;
import org.junit.Test;

import java.nio.ByteBuffer;

public class ByteBufferUtilsTest {

    @Test
    public void fromHex() throws DecoderException {
        assertEquals(ByteBuffer.wrap(new byte[] {0x01, 0x02, 0x03, 0x04}), ByteBufferUtils.fromHex("0102 03 04h"));
        assertEquals(ByteBufferUtils.fromHex("01020304"), ByteBufferUtils.fromHex("0102 03 04h"));
    }
}

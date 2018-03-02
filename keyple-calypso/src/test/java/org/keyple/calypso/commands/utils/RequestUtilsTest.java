/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.utils;

import static org.junit.Assert.*;
import org.apache.commons.codec.DecoderException;
import org.junit.Test;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ByteBufferUtils;

public class RequestUtilsTest {

    @Test
    public void constructAPDURequest() throws DecoderException {
        ApduRequest req = RequestUtils.constructAPDURequest((byte) 1, (byte) 2, (byte) 3, (byte) 4,
                ByteBufferUtils.fromHex("0506"), (byte) 0x07);
        assertEquals("0102030402050600", ByteBufferUtils.toHex(req.getBuffer()));
    }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.junit.Test;

public class SeRequestTest {

    private static final ByteBuffer aid =
            ByteBufferUtils.wrap(new byte[] {(byte) 0x01, (byte) 0x02});

    @Test
    public void testSERequest() {
        SeRequestSet request = new SeRequestSet(aid, new ArrayList<ApduRequest>(), true);
        assertNotNull(request);
    }

    @Test
    public void testGetAidToSelect() {
        SeRequestSet request = new SeRequestSet(aid, new ArrayList<ApduRequest>(), true);
        assertEquals(aid, request.getAidToSelect());
    }

    @Test
    public void testGetApduRequests() {
        SeRequestSet request = new SeRequestSet(aid, new ArrayList<ApduRequest>(), true);
        assertArrayEquals(new ArrayList<ApduRequest>().toArray(),
                request.getApduRequests().toArray());
    }

    @Test
    public void testAskKeepChannelOpen() {
        SeRequestSet request = new SeRequestSet(aid, new ArrayList<ApduRequest>(), true);
        assertTrue(request.keepChannelOpen());
    }

}

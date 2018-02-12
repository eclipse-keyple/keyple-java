/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.Test;

public class SeRequestTest {

    @Test
    public void testSERequest() {
        SeRequest request = new SeRequest(new byte[] {(byte) 0x01, (byte) 0x02},
                new ArrayList<ApduRequest>(), true);
        assertNotNull(request);
    }

    @Test
    public void testGetAidToSelect() {
        SeRequest request = new SeRequest(new byte[] {(byte) 0x01, (byte) 0x02},
                new ArrayList<ApduRequest>(), true);
        assertArrayEquals(new byte[] {(byte) 0x01, (byte) 0x02}, request.getAidToSelect());
    }

    @Test
    public void testGetApduRequests() {
        SeRequest request = new SeRequest(new byte[] {(byte) 0x01, (byte) 0x02},
                new ArrayList<ApduRequest>(), true);
        assertArrayEquals(new ArrayList<ApduRequest>().toArray(),
                request.getApduRequests().toArray());
    }

    @Test
    public void testAskKeepChannelOpen() {
        SeRequest request = new SeRequest(new byte[] {(byte) 0x01, (byte) 0x02},
                new ArrayList<ApduRequest>(), true);
        assertTrue(request.askKeepChannelOpen());
    }

}

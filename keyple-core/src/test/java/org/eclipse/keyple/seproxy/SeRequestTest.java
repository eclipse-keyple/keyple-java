/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.eclipse.keyple.util.ByteBufferUtils;
import org.junit.Test;

public class SeRequestTest {

    private static final ByteBuffer aid = ByteBufferUtils.fromHex("1122334455667788");
    private static final ByteBuffer aidTooShort = ByteBufferUtils.fromHex("11223344");
    private static final ByteBuffer aidTooLong = ByteBufferUtils.fromHex("11223344556677889900AABBCCDDEEFF0011");

    @Test
    public void testSERequest() {
        SeRequestSet request = new SeRequestSet(
                new SeRequest(new SeRequest.AidSelector(aid), new ArrayList<ApduRequest>(), true));
        assertNotNull(request);
    }

    @Test
    public void testGetAidToSelect() {
        SeRequestSet request = new SeRequestSet(
                new SeRequest(new SeRequest.AidSelector(aid), new ArrayList<ApduRequest>(), true));
        assertEquals(aid, ((SeRequest.AidSelector) request.getSingleRequest().getSelector())
                .getAidToSelect());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAidToSelectTooShort() {
        SeRequestSet request = new SeRequestSet(
                new SeRequest(new SeRequest.AidSelector(aidTooShort), new ArrayList<ApduRequest>(), true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAidToSelectTooLong() {
        SeRequestSet request = new SeRequestSet(
                new SeRequest(new SeRequest.AidSelector(aidTooLong), new ArrayList<ApduRequest>(), true));
    }

    @Test
    public void testGetApduRequests() {
        SeRequestSet request = new SeRequestSet(
                new SeRequest(new SeRequest.AidSelector(aid), new ArrayList<ApduRequest>(), true));
        assertArrayEquals(new ArrayList<ApduRequest>().toArray(),
                request.getSingleRequest().getApduRequests().toArray());
    }

    @Test
    public void testAskKeepChannelOpen() {
        SeRequestSet request = new SeRequestSet(
                new SeRequest(new SeRequest.AidSelector(aid), new ArrayList<ApduRequest>(), true));
        assertTrue(request.getSingleRequest().isKeepChannelOpen());
    }

}

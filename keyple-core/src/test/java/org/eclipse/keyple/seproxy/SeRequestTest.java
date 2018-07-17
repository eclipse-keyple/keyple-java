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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SeRequestTest {

    private static final ByteBuffer aid = ByteBuffer.wrap(new byte[] {(byte) 0x01, (byte) 0x02});

    // object to test
    SeRequest seRequest;

    public List<ApduRequest> getApdus() {
        return apdus;
    }

    // attributes
    List<ApduRequest> apdus;
    Boolean keepChannelOpen;
    SeProtocol seProtocol;
    Set<Short> selectionStatusCode;



    @Before
    public void setUp() throws Exception {

        ByteBuffer bytes = ByteBufferUtils.fromHex("01");
        apdus = new ArrayList<ApduRequest>();
        apdus.add(new ApduRequest(bytes, true));
        apdus.add(new ApduRequest(bytes, false));

        keepChannelOpen = true;

        seProtocol = ContactlessProtocols.PROTOCOL_B_PRIME;

        selectionStatusCode = new HashSet<Short>();
        selectionStatusCode.add(new Short("1"));

        seRequest = new SeRequest(aid, apdus, keepChannelOpen, seProtocol, selectionStatusCode);
    }

    @Test
    public void testSERequest() {
        assertNotNull(seRequest);
    }


    @Test
    public void getAidToSelect() {
        // test
        Assert.assertEquals(aid, seRequest.getAidToSelect());

    }

    @Test
    public void getApduRequests() {
        // test
        seRequest = new SeRequest(aid, apdus, false);
        Assert.assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
    }

    @Test
    public void isKeepChannelOpen() {
        assertTrue(seRequest.isKeepChannelOpen());
    }

    @Test
    public void getProtocolFlag() {
        seRequest = new SeRequest(aid, new ArrayList<ApduRequest>(), true, seProtocol);
        Assert.assertEquals(seProtocol, seRequest.getProtocolFlag());
    }

    @Test
    public void getSuccessfulSelectionStatusCodes() {
        seRequest = new SeRequest(aid, new ArrayList<ApduRequest>(), true,
                ContactlessProtocols.PROTOCOL_B_PRIME, selectionStatusCode);
        Assert.assertArrayEquals(selectionStatusCode.toArray(),
                seRequest.getSuccessfulSelectionStatusCodes().toArray());
    }

    @Test
    public void toStringNull() {
        seRequest = new SeRequest(null, null, true, null, null);
        Assert.assertNotNull(seRequest.toString());
    }

    /*
     * Constructors
     */
    @Test
    public void constructor1() {
        seRequest = new SeRequest(aid, apdus, keepChannelOpen);
        Assert.assertEquals(aid, seRequest.getAidToSelect());
        Assert.assertEquals(keepChannelOpen, seRequest.isKeepChannelOpen());
        Assert.assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        //
        Assert.assertNull(seRequest.getProtocolFlag());
        Assert.assertNull(seRequest.getSuccessfulSelectionStatusCodes());
    }

    @Test
    public void constructor2() {
        seRequest = new SeRequest(aid, apdus, keepChannelOpen, seProtocol);
        Assert.assertEquals(aid, seRequest.getAidToSelect());
        Assert.assertEquals(keepChannelOpen, seRequest.isKeepChannelOpen());
        Assert.assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        Assert.assertEquals(seProtocol, seRequest.getProtocolFlag());
        //
        Assert.assertNull(seRequest.getSuccessfulSelectionStatusCodes());
    }

    @Test
    public void constructor2b() {
        seRequest = new SeRequest(aid, apdus, keepChannelOpen, selectionStatusCode);
        Assert.assertEquals(aid, seRequest.getAidToSelect());
        Assert.assertEquals(keepChannelOpen, seRequest.isKeepChannelOpen());
        Assert.assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        Assert.assertNull(seRequest.getProtocolFlag());
        //
        Assert.assertArrayEquals(selectionStatusCode.toArray(),
                seRequest.getSuccessfulSelectionStatusCodes().toArray());
    }

    @Test
    public void constructor3() {
        seRequest = new SeRequest(aid, apdus, keepChannelOpen, seProtocol, selectionStatusCode);
        Assert.assertEquals(aid, seRequest.getAidToSelect());
        Assert.assertEquals(keepChannelOpen, seRequest.isKeepChannelOpen());
        Assert.assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        Assert.assertEquals(seProtocol, seRequest.getProtocolFlag());
        Assert.assertArrayEquals(selectionStatusCode.toArray(),
                seRequest.getSuccessfulSelectionStatusCodes().toArray());
    }


    /*
     * HELPERS FOR OTHERS TESTS SUITE
     */

    static SeRequest getSeRequestSample() {
        List<ApduRequest> apdus;
        Boolean keepChannelOpen;
        SeProtocol seProtocol;
        Set<Short> selectionStatusCode;
        ByteBuffer bytes = ByteBufferUtils.fromHex("01");


        apdus = new ArrayList<ApduRequest>();
        apdus.add(new ApduRequest(bytes, true));
        apdus.add(new ApduRequest(bytes, false));

        keepChannelOpen = true;

        seProtocol = ContactlessProtocols.PROTOCOL_B_PRIME;

        selectionStatusCode = new HashSet<Short>();
        selectionStatusCode.add(new Short("1"));

        return new SeRequest(aid, apdus, keepChannelOpen, seProtocol, selectionStatusCode);

    }



}

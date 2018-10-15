/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
public class SeRequestTest {

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
    SeRequest.Selector selector;



    @Before
    public void setUp() {

        apdus = getAapduLists();
        keepChannelOpen = true;
        seProtocol = getASeProtocol();
        selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();
        selector = getAidSelector();
        seRequest = new SeRequest(getAidSelector(), apdus, keepChannelOpen, seProtocol,
                selectionStatusCode);
    }

    @Test
    public void testSERequest() {
        assertNotNull(seRequest);
    }


    @Test
    public void getSelector() {
        // test
        assertEquals(getAidSelector().toString(), seRequest.getSelector().toString());

    }

    @Test
    public void getApduRequests() {
        // test
        seRequest = new SeRequest(getAidSelector(), apdus, false);
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
    }

    @Test
    public void isKeepChannelOpen() {
        assertTrue(seRequest.isKeepChannelOpen());
    }

    @Test
    public void getProtocolFlag() {
        seRequest = new SeRequest(getAidSelector(), new ArrayList<ApduRequest>(), true, seProtocol);
        assertEquals(seProtocol, seRequest.getProtocolFlag());
    }

    @Test
    public void getSuccessfulSelectionStatusCodes() {
        seRequest = new SeRequest(getAidSelector(), new ArrayList<ApduRequest>(), true,
                ContactlessProtocols.PROTOCOL_B_PRIME, selectionStatusCode);
        assertArrayEquals(selectionStatusCode.toArray(),
                seRequest.getSuccessfulSelectionStatusCodes().toArray());
    }

    @Test
    public void toStringNull() {
        seRequest = new SeRequest(null, null, true, null, null);
        assertNotNull(seRequest.toString());
    }

    /*
     * Constructors
     */
    @Test
    public void constructor1() {
        seRequest = new SeRequest(getAidSelector(), apdus, keepChannelOpen);
        assertEquals(getAidSelector().toString(), seRequest.getSelector().toString());
        assertEquals(keepChannelOpen, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        //
        assertNull(seRequest.getProtocolFlag());
        assertNull(seRequest.getSuccessfulSelectionStatusCodes());
    }

    @Test
    public void constructor2() {
        seRequest = new SeRequest(getAidSelector(), apdus, keepChannelOpen, seProtocol);
        assertEquals(getAidSelector().toString(), seRequest.getSelector().toString());
        assertEquals(keepChannelOpen, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(seProtocol, seRequest.getProtocolFlag());
        //
        assertNull(seRequest.getSuccessfulSelectionStatusCodes());
    }

    @Test
    public void constructor2b() {
        seRequest = new SeRequest(getAidSelector(), apdus, keepChannelOpen, selectionStatusCode);
        assertEquals(getAidSelector().toString(), seRequest.getSelector().toString());
        assertEquals(keepChannelOpen, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertNull(seRequest.getProtocolFlag());
        //
        assertArrayEquals(selectionStatusCode.toArray(),
                seRequest.getSuccessfulSelectionStatusCodes().toArray());
    }

    @Test
    public void constructor3() {
        seRequest = new SeRequest(getAidSelector(), apdus, keepChannelOpen, seProtocol,
                selectionStatusCode);
        assertEquals(getAidSelector().toString(), seRequest.getSelector().toString());
        assertEquals(keepChannelOpen, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(seProtocol, seRequest.getProtocolFlag());
        assertArrayEquals(selectionStatusCode.toArray(),
                seRequest.getSuccessfulSelectionStatusCodes().toArray());
    }


    /*
     * HELPERS FOR OTHERS TESTS SUITE
     */

    static SeRequest getSeRequestSample() {

        List<ApduRequest> apdus = getAapduLists();
        Boolean keepChannelOpen = true;
        SeProtocol seProtocol = getASeProtocol();
        Set<Short> selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();

        return new SeRequest(getAidSelector(), apdus, keepChannelOpen, seProtocol,
                selectionStatusCode);

    }

    static List<ApduRequest> getAapduLists() {
        List<ApduRequest> apdus;
        apdus = new ArrayList<ApduRequest>();
        apdus.add(ApduRequestTest.getApduSample());
        apdus.add(ApduRequestTest.getApduSample());
        return apdus;
    }

    static SeProtocol getASeProtocol() {
        return ContactlessProtocols.PROTOCOL_B_PRIME;
    }

    static SeRequest.Selector getAidSelector() {
        return new SeRequest.AidSelector(ByteArrayUtils.fromHex("A000000291A000000191"));
    }

}

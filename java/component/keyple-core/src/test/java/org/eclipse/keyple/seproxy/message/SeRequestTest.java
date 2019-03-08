/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.seproxy.message;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
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
    ChannelState channelState;
    SeProtocol seProtocol;
    Set<Integer> selectionStatusCode;
    SeSelector selector;



    @Before
    public void setUp() {

        apdus = getAapduLists();
        channelState = ChannelState.KEEP_OPEN;
        seProtocol = getASeProtocol();
        selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();
        selector = getSelector(selectionStatusCode);
        seRequest = new SeRequest(selector, apdus, channelState, seProtocol);
    }

    @Test
    public void testSERequest() {
        assertNotNull(seRequest);
    }


    @Test
    public void getSelector() {
        // test
        assertEquals(getSelector(selectionStatusCode).toString(),
                seRequest.getSeSelector().toString());

    }

    @Test
    public void getApduRequests() {
        // test
        seRequest = new SeRequest(getSelector(null), apdus, ChannelState.CLOSE_AFTER, Protocol.ANY);
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
    }

    @Test
    public void isKeepChannelOpen() {
        assertTrue(seRequest.isKeepChannelOpen());
    }

    @Test
    public void getProtocolFlag() {
        seRequest = new SeRequest(getSelector(null), new ArrayList<ApduRequest>(),
                ChannelState.KEEP_OPEN, seProtocol);
        assertEquals(seProtocol, seRequest.getProtocolFlag());
    }

    @Test
    public void getSuccessfulSelectionStatusCodes() {
        seRequest = new SeRequest(getSelector(selectionStatusCode), new ArrayList<ApduRequest>(),
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_B_PRIME);
        assertArrayEquals(selectionStatusCode.toArray(), seRequest.getSeSelector().getAidSelector()
                .getSuccessfulSelectionStatusCodes().toArray());
    }

    @Test
    public void toStringNull() {
        seRequest = new SeRequest(null, null, null, null);
        assertNotNull(seRequest.toString());
    }

    /*
     * Constructors
     */
    @Test
    public void constructor1() {
        seRequest = new SeRequest(getSelector(null), apdus, channelState, Protocol.ANY);
        assertEquals(getSelector(null).toString(), seRequest.getSeSelector().toString());
        assertEquals(channelState == ChannelState.KEEP_OPEN, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        //
        assertEquals(Protocol.ANY, seRequest.getProtocolFlag());
        assertNull(seRequest.getSeSelector().getAidSelector().getSuccessfulSelectionStatusCodes());
    }

    @Test
    public void constructor2() {
        seRequest = new SeRequest(getSelector(null), apdus, channelState, seProtocol);
        assertEquals(getSelector(null).toString(), seRequest.getSeSelector().toString());
        assertEquals(channelState == ChannelState.KEEP_OPEN, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(seProtocol, seRequest.getProtocolFlag());
        //
        assertNull(seRequest.getSeSelector().getAidSelector().getSuccessfulSelectionStatusCodes());
    }

    @Test
    public void constructor2b() {
        seRequest =
                new SeRequest(getSelector(selectionStatusCode), apdus, channelState, Protocol.ANY);
        assertEquals(getSelector(selectionStatusCode).toString(),
                seRequest.getSeSelector().toString());
        assertEquals(channelState == ChannelState.KEEP_OPEN, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(Protocol.ANY, seRequest.getProtocolFlag());
        //
        assertArrayEquals(selectionStatusCode.toArray(), seRequest.getSeSelector().getAidSelector()
                .getSuccessfulSelectionStatusCodes().toArray());
    }

    @Test
    public void constructor3() {
        seRequest =
                new SeRequest(getSelector(selectionStatusCode), apdus, channelState, seProtocol);
        assertEquals(getSelector(selectionStatusCode).toString(),
                seRequest.getSeSelector().toString());
        assertEquals(channelState == ChannelState.KEEP_OPEN, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(seProtocol, seRequest.getProtocolFlag());
        assertArrayEquals(selectionStatusCode.toArray(), seRequest.getSeSelector().getAidSelector()
                .getSuccessfulSelectionStatusCodes().toArray());
    }


    /*
     * HELPERS FOR OTHERS TESTS SUITE
     */

    static SeRequest getSeRequestSample() {

        List<ApduRequest> apdus = getAapduLists();
        ChannelState channelState = ChannelState.KEEP_OPEN;
        SeProtocol seProtocol = getASeProtocol();
        Set<Integer> selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();

        return new SeRequest(getSelector(selectionStatusCode), apdus, channelState, seProtocol);

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

    static SeSelector getSelector(Set<Integer> selectionStatusCode) {
        /*
         * We can use a fake AID here because it is not fully interpreted, the purpose of this unit
         * test is to verify the proper format of the request.
         */
        SeSelector.AidSelector aidSelector = new SeSelector.AidSelector(
                ByteArrayUtils.fromHex("AABBCCDDEEFF"), selectionStatusCode);
        SeSelector seSelector = new SeSelector(aidSelector, null, null);
        return seSelector;
    }

}

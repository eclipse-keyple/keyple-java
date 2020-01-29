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
package org.eclipse.keyple.core.seproxy.message;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.util.ByteArrayUtil;
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
    SeProtocol seProtocol;
    Set<Integer> selectionStatusCode;
    SeSelector selector;



    @Before
    public void setUp() {

        apdus = getAapduLists();
        seProtocol = getASeProtocol();
        selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();
        selector = getSelector(selectionStatusCode);
        seRequest = new SeRequest(selector, apdus);
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
        seRequest = new SeRequest(getSelector(null), apdus);
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
    }

    @Test
    public void getSeProtocol() {
        seRequest = new SeRequest(getSelector(null), new ArrayList<ApduRequest>());
        assertEquals(seProtocol, seRequest.getSeSelector().getSeProtocol());
    }

    @Test
    public void getSuccessfulSelectionStatusCodes() {
        seRequest = new SeRequest(getSelector(selectionStatusCode), new ArrayList<ApduRequest>());
        assertArrayEquals(selectionStatusCode.toArray(), seRequest.getSeSelector().getAidSelector()
                .getSuccessfulSelectionStatusCodes().toArray());
    }

    @Test
    public void toStringNull() {
        seRequest = new SeRequest(null, null);
        assertNotNull(seRequest.toString());
    }

    /*
     * Constructors
     */
    @Test
    public void constructor1() {
        seRequest = new SeRequest(getSelector(null), apdus);
        assertEquals(getSelector(null).toString(), seRequest.getSeSelector().toString());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        //
        assertEquals(SeCommonProtocols.PROTOCOL_ISO14443_4,
                seRequest.getSeSelector().getSeProtocol());
        assertNull(seRequest.getSeSelector().getAidSelector().getSuccessfulSelectionStatusCodes());
    }

    @Test
    public void constructor2() {
        seRequest = new SeRequest(getSelector(null), apdus);
        assertEquals(getSelector(null).toString(), seRequest.getSeSelector().toString());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(seProtocol, seRequest.getSeSelector().getSeProtocol());
        //
        assertNull(seRequest.getSeSelector().getAidSelector().getSuccessfulSelectionStatusCodes());
    }

    @Test
    public void constructor2b() {
        seRequest = new SeRequest(getSelector(selectionStatusCode), apdus);
        assertEquals(getSelector(selectionStatusCode).toString(),
                seRequest.getSeSelector().toString());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(SeCommonProtocols.PROTOCOL_ISO14443_4,
                seRequest.getSeSelector().getSeProtocol());
        //
        assertArrayEquals(selectionStatusCode.toArray(), seRequest.getSeSelector().getAidSelector()
                .getSuccessfulSelectionStatusCodes().toArray());
    }

    @Test
    public void constructor3() {
        seRequest = new SeRequest(getSelector(selectionStatusCode), apdus);
        assertEquals(getSelector(selectionStatusCode).toString(),
                seRequest.getSeSelector().toString());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(seProtocol, seRequest.getSeSelector().getSeProtocol());
        assertArrayEquals(selectionStatusCode.toArray(), seRequest.getSeSelector().getAidSelector()
                .getSuccessfulSelectionStatusCodes().toArray());
    }


    /*
     * HELPERS FOR OTHERS TESTS SUITE
     */

    public static SeRequest getSeRequestSample() {

        List<ApduRequest> apdus = getAapduLists();
        Set<Integer> selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();
        return new SeRequest(getSelector(selectionStatusCode), apdus);

    }

    static List<ApduRequest> getAapduLists() {
        List<ApduRequest> apdus;
        apdus = new ArrayList<ApduRequest>();
        apdus.add(ApduRequestTest.getApduSample());
        apdus.add(ApduRequestTest.getApduSample());
        return apdus;
    }

    static SeProtocol getASeProtocol() {
        return SeCommonProtocols.PROTOCOL_ISO14443_4;
    }

    static SeSelector getSelector(Set<Integer> selectionStatusCode) {
        /*
         * We can use a fake AID here because it is not fully interpreted, the purpose of this unit
         * test is to verify the proper format of the request.
         */
        SeSelector.AidSelector aidSelector = new SeSelector.AidSelector(
                new SeSelector.AidSelector.IsoAid(ByteArrayUtil.fromHex("AABBCCDDEEFF")),
                selectionStatusCode);
        SeSelector seSelector = new SeSelector(getASeProtocol(), null, aidSelector, null);
        return seSelector;
    }

}

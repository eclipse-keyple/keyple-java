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
import java.util.List;
import org.junit.Test;

public class SeResponseTest {

    @Test
    public void testSEResponse() {
        ApduResponse fciData = new ApduResponse(new byte[] {(byte) 0x01, (byte) 02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        SeResponseSet response = new SeResponseSet(true, fciData, new ArrayList<ApduResponse>());
        // assertNotNull(response);
        assertArrayEquals(new ArrayList<ApduResponse>().toArray(),
                response.getApduResponses().toArray());
    }

    @Test
    public void testWasChannelPreviouslyOpen() {
        SeResponseSet response = new SeResponseSet(true, null, new ArrayList<ApduResponse>());
        assertTrue(response.wasChannelPreviouslyOpen());
    }

    @Test
    public void testGetFciData() {
        ApduResponse fciData = new ApduResponse(new byte[] {(byte) 0x01, (byte) 02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        SeResponseSet response = new SeResponseSet(true, fciData, new ArrayList<ApduResponse>());
        assertEquals(fciData, response.getFci());

    }

    @Test
    public void testGetFciDataNull() {
        ApduResponse fciData = null;
        SeResponseSet response = new SeResponseSet(false, fciData, new ArrayList<ApduResponse>());
        assertNull(response.getFci());

    }

    @Test
    public void testGetApduResponses() {
        SeResponseSet response = new SeResponseSet(true, null, new ArrayList<ApduResponse>());
        assertArrayEquals(new ArrayList<ApduResponse>().toArray(),
                response.getApduResponses().toArray());
    }

    @Test
    public void testToString() {
        ApduResponse fciData = new ApduResponse(new byte[] {(byte) 0x01, (byte) 02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        responses.add(fciData);
        responses.add(fciData);
        SeResponseSet response = new SeResponseSet(true, fciData, responses);
        assertEquals(response.getApduResponses().size(), 2);
        assertEquals(fciData, response.getFci());
        for (ApduResponse resp : response.getApduResponses()) {
            assertEquals(resp, fciData);
        }
    }

}

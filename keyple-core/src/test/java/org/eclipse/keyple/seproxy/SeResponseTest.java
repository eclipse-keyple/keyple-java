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
import java.util.List;
import org.eclipse.keyple.seproxy.exception.InconsistentParameterValueException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.junit.Test;

public class SeResponseTest {

    @Test
    public void testSEResponse() throws InconsistentParameterValueException {
        ApduResponse fciData = new ApduResponse(
                ByteBuffer.wrap(new byte[] {(byte) 0x01, (byte) 02, (byte) 0x03, (byte) 0x04}),
                null);
        SeResponseSet response = new SeResponseSet(
                new SeResponse(true, null, fciData, new ArrayList<ApduResponse>()));
        // assertNotNull(response);
        assertArrayEquals(new ArrayList<ApduResponse>().toArray(),
                response.getSingleResponse().getApduResponses().toArray());
    }

    @Test
    public void testWasChannelPreviouslyOpen() throws InconsistentParameterValueException {
        SeResponseSet response = new SeResponseSet(
                new SeResponse(true, null, new ApduResponse(ByteBufferUtils.fromHex("9000"), null),
                        new ArrayList<ApduResponse>()));
        assertTrue(response.getSingleResponse().wasChannelPreviouslyOpen());
    }

    @Test
    public void testGetFciData() throws InconsistentParameterValueException {
        ApduResponse fciData = new ApduResponse(
                ByteBuffer.wrap(new byte[] {(byte) 0x01, (byte) 02, (byte) 0x03, (byte) 0x04}),
                null);
        SeResponseSet response = new SeResponseSet(
                new SeResponse(true, null, fciData, new ArrayList<ApduResponse>()));
        assertEquals(fciData, response.getSingleResponse().getFci());

    }

    @Test
    public void testGetFciDataNull() throws InconsistentParameterValueException {
        ApduResponse fciData = null;
        SeResponseSet response = new SeResponseSet(
                new SeResponse(false, new ApduResponse(ByteBufferUtils.fromHex("9000"), null),
                        fciData, new ArrayList<ApduResponse>()));
        assertNull(response.getSingleResponse().getFci());

    }

    @Test
    public void testGetApduResponses() throws InconsistentParameterValueException {
        SeResponseSet response = new SeResponseSet(
                new SeResponse(true, null, new ApduResponse(ByteBufferUtils.fromHex("9000"), null),
                        new ArrayList<ApduResponse>()));
        assertArrayEquals(new ArrayList<ApduResponse>().toArray(),
                response.getSingleResponse().getApduResponses().toArray());
    }

    @Test
    public void testToString() throws InconsistentParameterValueException {
        ApduResponse fciData = new ApduResponse(
                ByteBuffer.wrap(new byte[] {(byte) 0x01, (byte) 02, (byte) 0x03, (byte) 0x04}),
                null);
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        responses.add(fciData);
        responses.add(fciData);
        SeResponseSet response = new SeResponseSet(new SeResponse(true, null, fciData, responses));
        assertEquals(response.getSingleResponse().getApduResponses().size(), 2);
        assertEquals(fciData, response.getSingleResponse().getFci());
        for (ApduResponse resp : response.getSingleResponse().getApduResponses()) {
            assertEquals(resp, fciData);
        }
    }

}

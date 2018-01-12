package org.keyple.seproxy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SeResponseTest {

    @Test
    public void testSEResponse() {
        ApduResponse fciData = new ApduResponse(new byte[] { (byte) 0x01, (byte) 02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        SeResponse response = new SeResponse(true, fciData, new ArrayList<ApduResponse>());
        assertNotNull(response);
        assertArrayEquals(new ArrayList<ApduResponse>().toArray(), response.getApduResponses().toArray());
    }

    @Test
    public void testWasChannelPreviouslyOpen() {
        SeResponse response = new SeResponse(true, null, new ArrayList<ApduResponse>());
        assertEquals(true, response.wasChannelPreviouslyOpen());
    }

    @Test
    public void testGetFciData() {
        ApduResponse fciData = new ApduResponse(new byte[] { (byte) 0x01, (byte) 02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        SeResponse response = new SeResponse(true, fciData, new ArrayList<ApduResponse>());
        assertEquals(fciData, response.getFci());

    }
    @Test
    public void testGetFciDataNull() {
        ApduResponse fciData = null;
        SeResponse response = new SeResponse(false, fciData, new ArrayList<ApduResponse>());
        assertNull(response.getFci());

    }

    @Test
    public void testGetApduResponses() {
        SeResponse response = new SeResponse(true, null, new ArrayList<ApduResponse>());
        assertArrayEquals(new ArrayList<ApduResponse>().toArray(), response.getApduResponses().toArray());
    }

    @Test
    public void testToString() {
        ApduResponse fciData = new ApduResponse(new byte[] { (byte) 0x01, (byte) 02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        responses.add(fciData);
        responses.add(fciData);
        SeResponse response = new SeResponse(true, fciData, responses);
        assertEquals(response.getApduResponses().size(), 2);
        assertEquals(fciData, response.getFci());
        for(ApduResponse resp : response.getApduResponses()){
            assertEquals(resp, fciData);
        }
    }

}

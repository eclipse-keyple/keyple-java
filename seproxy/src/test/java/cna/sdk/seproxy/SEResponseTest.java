package cna.sdk.seproxy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SEResponseTest {
    Logger logger = LoggerFactory.getLogger(SEResponseTest.class);

    @Test
    public void testSEResponse() {
        APDUResponse fciData = new APDUResponse(new byte[] { (byte) 0x01, (byte) 02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        SEResponse response = new SEResponse(true, fciData, new ArrayList<APDUResponse>());
        assertNotNull(response);
        assertArrayEquals(new ArrayList<APDUResponse>().toArray(), response.getApduResponses().toArray());
    }

    @Test
    public void testWasChannelPreviouslyOpen() {
        SEResponse response = new SEResponse(true, null, new ArrayList<APDUResponse>());
        assertEquals(true, response.wasChannelPreviouslyOpen());
    }

    @Test
    public void testGetFciData() {
        APDUResponse fciData = new APDUResponse(new byte[] { (byte) 0x01, (byte) 02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        SEResponse response = new SEResponse(true, fciData, new ArrayList<APDUResponse>());
        assertEquals(fciData, response.getFci());

    }
    @Test
    public void testGetFciDataNull() {
        APDUResponse fciData = new APDUResponse(new byte[] { (byte) 0x01, (byte) 02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        SEResponse response = new SEResponse(false, fciData, new ArrayList<APDUResponse>());
        assertNull(response.getFci());

    }

    @Test
    public void testGetApduResponses() {
        SEResponse response = new SEResponse(true, null, new ArrayList<APDUResponse>());
        assertArrayEquals(new ArrayList<APDUResponse>().toArray(), response.getApduResponses().toArray());
    }

    @Test
    public void testToString() {
        APDUResponse fciData = new APDUResponse(new byte[] { (byte) 0x01, (byte) 02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        List<APDUResponse> responses = new ArrayList<APDUResponse>();
        responses.add(fciData);
        responses.add(fciData);
        SEResponse response = new SEResponse(true, fciData, responses);
        assertEquals("0102, 0102", response.toString());
    }

}

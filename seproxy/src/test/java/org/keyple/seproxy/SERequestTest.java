package org.keyple.seproxy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Test;
import org.keyple.seproxy.APDURequest;
import org.keyple.seproxy.SERequest;

public class SERequestTest {

    @Test
    public void testSERequest() {
        SERequest request = new SERequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new ArrayList<APDURequest>());
        assertNotNull(request);
    }

    @Test
    public void testGetAidToSelect() {
        SERequest request = new SERequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new ArrayList<APDURequest>());
        assertArrayEquals(new byte[] { (byte) 0x01, (byte) 0x02 }, request.getAidToSelect());
    }

    @Test
    public void testGetApduRequests() {
        SERequest request = new SERequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new ArrayList<APDURequest>());
        assertArrayEquals(new ArrayList<APDURequest>().toArray(), request.getApduRequests().toArray());
    }

    @Test
    public void testAskKeepChannelOpen() {
        SERequest request = new SERequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new ArrayList<APDURequest>());
        assertEquals(true, request.askKeepChannelOpen());
    }

}

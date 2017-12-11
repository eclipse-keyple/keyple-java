package org.keyple.seproxy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Test;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.SeRequest;

public class SeRequestTest {

    @Test
    public void testSERequest() {
        SeRequest request = new SeRequest(new byte[] { (byte) 0x01, (byte) 0x02 }, new ArrayList<ApduRequest>(),
                true);
        assertNotNull(request);
    }

    @Test
    public void testGetAidToSelect() {
        SeRequest request = new SeRequest(new byte[] { (byte) 0x01, (byte) 0x02 }, new ArrayList<ApduRequest>(),
                true);
        assertArrayEquals(new byte[] { (byte) 0x01, (byte) 0x02 }, request.getAidToSelect());
    }

    @Test
    public void testGetApduRequests() {
        SeRequest request = new SeRequest(new byte[] { (byte) 0x01, (byte) 0x02 }, new ArrayList<ApduRequest>(),
                true);
        assertArrayEquals(new ArrayList<ApduRequest>().toArray(), request.getApduRequests().toArray());
    }

    @Test
    public void testAskKeepChannelOpen() {
        SeRequest request = new SeRequest(new byte[] { (byte) 0x01, (byte) 0x02 }, new ArrayList<ApduRequest>(),
                true);
        assertEquals(true, request.askKeepChannelOpen());
    }

}

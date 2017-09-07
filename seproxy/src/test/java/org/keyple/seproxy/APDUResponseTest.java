package org.keyple.seproxy;

import static org.junit.Assert.*;

import org.junit.Test;
import org.keyple.seproxy.APDUResponse;

public class APDUResponseTest {

    @Test
    public void testAPDUResponse() {
        APDUResponse response = new APDUResponse(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        assertNotNull(response);
    }

    @Test
    public void testGetbytes() {
        APDUResponse response = new APDUResponse(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        assertArrayEquals(new byte[] { (byte) 0x01, (byte) 0x02 }, response.getbytes());
    }

    @Test
    public void testIsSuccessful() {
        APDUResponse response = new APDUResponse(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        assertEquals(true, response.isSuccessful());
    }

    @Test
    public void testGetStatusCode() {
        APDUResponse response = new APDUResponse(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        assertArrayEquals(new byte[] { (byte) 0x03, (byte) 0x04 }, response.getStatusCode());
    }

}

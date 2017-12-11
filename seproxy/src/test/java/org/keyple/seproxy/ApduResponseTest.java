package org.keyple.seproxy;

import static org.junit.Assert.*;

import org.junit.Test;
import org.keyple.seproxy.ApduResponse;

public class ApduResponseTest {

    @Test
    public void testAPDUResponse() {
        ApduResponse response = new ApduResponse(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        assertNotNull(response);
    }

    @Test
    public void testGetbytes() {
        ApduResponse response = new ApduResponse(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        assertArrayEquals(new byte[] { (byte) 0x01, (byte) 0x02 }, response.getbytes());
    }

    @Test
    public void testIsSuccessful() {
        ApduResponse response = new ApduResponse(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        assertEquals(true, response.isSuccessful());
    }

    @Test
    public void testGetStatusCode() {
        ApduResponse response = new ApduResponse(new byte[] { (byte) 0x01, (byte) 0x02 }, true,
                new byte[] { (byte) 0x03, (byte) 0x04 });
        assertArrayEquals(new byte[] { (byte) 0x03, (byte) 0x04 }, response.getStatusCode());
    }

}

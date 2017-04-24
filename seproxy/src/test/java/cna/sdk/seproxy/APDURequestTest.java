package cna.sdk.seproxy;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.seproxy.APDURequest;

public class APDURequestTest {
    Logger logger = LoggerFactory.getLogger(APDURequestTest.class);

    @Test
    public void testAPDURequest() {
        APDURequest request = new APDURequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true);
        assertNotNull(request);
    }

    @Test
    public void testGetbytes() {
        APDURequest request = new APDURequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true);
        assertArrayEquals(new byte[] { (byte) 0x01, (byte) 0x02 }, request.getbytes());
    }

    @Test
    public void testIsCase4() {
        APDURequest request = new APDURequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true);
        assertEquals(true, request.isCase4());
    }

    @Test
    public void testToString() {
        APDURequest request = new APDURequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true);
        assertEquals("0102", request.toString());
    }

}

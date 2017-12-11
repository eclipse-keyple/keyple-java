package org.keyple.seproxy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ApduRequestTest {

    @Test
    public void testAPDURequest() {
        ApduRequest request = new ApduRequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true);
        assertNotNull(request);
    }

    @Test
    public void testGetbytes() {
        ApduRequest request = new ApduRequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true);
        assertArrayEquals(new byte[] { (byte) 0x01, (byte) 0x02 }, request.getbytes());
    }

    @Test
    public void testIsCase4() {
        ApduRequest request = new ApduRequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true);
        assertEquals(true, request.isCase4());
    }

    @Test
    public void testToString() {
        ApduRequest request = new ApduRequest(new byte[] { (byte) 0x01, (byte) 0x02 }, true);
        assertEquals(request.getClass().getName()+"@"+Integer.toHexString(request.hashCode()), request.toString());
    }

}

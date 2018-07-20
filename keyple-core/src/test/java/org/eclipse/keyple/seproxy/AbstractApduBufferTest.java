package org.eclipse.keyple.seproxy;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class AbstractApduBufferTest {

    @Test
    public void testSlice() {
        ByteBuffer command = ByteBuffer.allocate(3);
        command.put((byte) 0x01);
        command.put((byte) 0x02);
        command.put((byte) 0x03);
        ByteBuffer result = ByteBuffer.allocate(1);
        result.put((byte) 0x01);
        result.position(0);
        ApduRequest request = new ApduRequest(command,true);
        ByteBuffer slice = request.slice(0, 1);
        Assert.assertEquals(result, slice);
    }

    @Test
    public void getBytes() {
        ByteBuffer command = ByteBuffer.allocate(3);
        command.put((byte) 0x01);
        ApduRequest request = new ApduRequest(command,true);
        Assert.assertEquals(command, request.getBytes());
    }

    @Test
    public void testToString() {
        ApduRequest request = new ApduRequest(null, true);
        System.out.println(request.toString());
        Assert.assertNotNull(request.toString());
    }
}
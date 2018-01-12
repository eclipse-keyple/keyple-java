package org.keyple.commands.calypso.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.keyple.commands.calypso.CalypsoCommands;
import org.keyple.commands.calypso.dto.CalypsoRequest;
import org.keyple.commands.calypso.utils.RequestUtils;
import org.keyple.seproxy.ApduRequest;

@RunWith(BlockJUnit4ClassRunner.class)
public class RequestUtilsTest {

    private CalypsoRequest request;

    private boolean isCase4;

    private ApduRequest ApduRequestExpected;

    private byte cla;

    private CalypsoCommands ins;

    private byte pUn;

    private byte pDeux;

    private byte[] dataIn;

    private byte option;
    
    private byte optionExptected;

    private byte[] fci;

    @Test
    public void testConstructApduRequest() {
        fci = new byte[] { (byte) 0x00, (byte) 0xCA, 0x00, 0x6F };
        isCase4 = false;
        ApduRequestExpected = new ApduRequest(fci, isCase4);
        cla = (byte) 0x00;
        ins = CalypsoCommands.PO_GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = null;

        request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn);
        ApduRequest ApduRequestActual = RequestUtils.constructAPDURequest(request);
        Assert.assertArrayEquals(ApduRequestExpected.getbytes(), ApduRequestActual.getbytes());
        Assert.assertEquals(ApduRequestExpected.isCase4(), ApduRequestActual.isCase4());
    }

    @Test
    public void testConstructApduRequestCase4() {
        fci = new byte[] { (byte) 0x00, (byte) 0xCA, 0x00, 0x6F, 0x00 };
        isCase4 = true;
        ApduRequestExpected = new ApduRequest(fci, isCase4);
        cla = (byte) 0x00;
        ins = CalypsoCommands.PO_GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = null;
        option = 0x00;

        request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn, option);
        ApduRequest ApduRequestActual = RequestUtils.constructAPDURequest(request);
        Assert.assertArrayEquals(ApduRequestExpected.getbytes(), ApduRequestActual.getbytes());
        Assert.assertNotEquals(ApduRequestExpected.isCase4(), ApduRequestActual.isCase4());
    }

    @Test
    public void testConstructApduRequestData() {
        fci = new byte[] { (byte) 0x00, (byte) 0xCA, 0x00, 0x6F, 0x02, 0x00, 0x00 };
        isCase4 = false;
        ApduRequestExpected = new ApduRequest(fci, isCase4);
        cla = (byte) 0x00;
        ins = CalypsoCommands.PO_GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = new byte[] { 0x00, 0x00 };

        request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn);
        ApduRequest ApduRequestActual = RequestUtils.constructAPDURequest(request);
        Assert.assertArrayEquals(ApduRequestExpected.getbytes(), ApduRequestActual.getbytes());
        Assert.assertEquals(ApduRequestExpected.isCase4(), ApduRequestActual.isCase4());
    }

    @Test
    public void testConstructApduRequestCase4Data() {
        isCase4 = true;
        cla = (byte) 0x00;
        ins = CalypsoCommands.PO_GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, 0x00 };
        option = (byte) 0x01;
        optionExptected = (byte) 0x00;
        fci = new byte[] { (byte) 0x00, (byte) 0xCA, 0x00, 0x6F, (byte) dataIn.length, (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, 0x00 , optionExptected };
        ApduRequestExpected = new ApduRequest(fci, isCase4);

        request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn, option);
        ApduRequest ApduRequestActual = RequestUtils.constructAPDURequest(request);
        Assert.assertArrayEquals(ApduRequestExpected.getbytes(), ApduRequestActual.getbytes());
        Assert.assertEquals(ApduRequestExpected.isCase4(), ApduRequestActual.isCase4());
    }
}

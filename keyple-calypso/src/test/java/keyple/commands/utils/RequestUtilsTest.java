/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.calypso.commands.utils.RequestUtils;
import org.keyple.seproxy.ApduRequest;

@RunWith(BlockJUnit4ClassRunner.class)
public class RequestUtilsTest {

    private boolean isCase4;

    private ApduRequest expected;

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
        fci = new byte[] {(byte) 0x00, (byte) 0xCA, 0x00, 0x6F};
        isCase4 = false;
        expected = new ApduRequest(fci, isCase4);
        cla = (byte) 0x00;
        ins = CalypsoCommands.PO_GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = null;

        // request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn);
        ApduRequest actual = RequestUtils.constructAPDURequest(cla, ins, pUn, pDeux, dataIn);
        // System.out.println("APDU Actual: " + Hex.encodeHexString(actual.getbytes()));
        // System.out.println("APDU Expected: " + Hex.encodeHexString(expected.getbytes()));
        Assert.assertArrayEquals(expected.getbytes(), actual.getbytes());
        Assert.assertEquals(expected.isCase4(), actual.isCase4());
    }

    @Test
    public void testConstructApduRequestCase4() {
        fci = new byte[] {(byte) 0x00, (byte) 0xCA, 0x00, 0x6F, 0x00};
        isCase4 = true;
        expected = new ApduRequest(fci, isCase4);
        cla = (byte) 0x00;
        ins = CalypsoCommands.PO_GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = null;
        option = 0x00;

        // request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn, option);
        ApduRequest actual =
                RequestUtils.constructAPDURequest(cla, ins, pUn, pDeux, dataIn, option);
        // System.out.println("APDU Actual: " + Hex.encodeHexString(actual.getbytes()));
        // System.out.println("APDU Expected: " + Hex.encodeHexString(expected.getbytes()));
        Assert.assertArrayEquals(expected.getbytes(), actual.getbytes());
        Assert.assertNotEquals(expected.isCase4(), actual.isCase4());
    }

    @Test
    public void testConstructApduRequestData() {
        fci = new byte[] {(byte) 0x00, (byte) 0xCA, 0x00, 0x6F, 0x02, 0x00, 0x00};
        isCase4 = false;
        expected = new ApduRequest(fci, isCase4);
        cla = (byte) 0x00;
        ins = CalypsoCommands.PO_GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = new byte[] {0x00, 0x00};

        // request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn);
        ApduRequest actual = RequestUtils.constructAPDURequest(cla, ins, pUn, pDeux, dataIn);
        // System.out.println("APDU Actual: " + Hex.encodeHexString(actual.getbytes()));
        // System.out.println("APDU Expected: " + Hex.encodeHexString(expected.getbytes()));
        Assert.assertArrayEquals(expected.getbytes(), actual.getbytes());
        Assert.assertEquals(expected.isCase4(), actual.isCase4());
    }

    @Test
    public void testConstructApduRequestCase4Data() {
        isCase4 = true;
        cla = (byte) 0x00;
        ins = CalypsoCommands.PO_GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, 0x00};
        option = (byte) 0x01;
        optionExptected = (byte) 0x00;
        fci = new byte[] {(byte) 0x00, (byte) 0xCA, 0x00, 0x6F, (byte) dataIn.length, (byte) 0xA8,
                0x31, (byte) 0xC3, 0x3E, 0x00, optionExptected};
        expected = new ApduRequest(fci, isCase4);

        // request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn, option);
        ApduRequest actual =
                RequestUtils.constructAPDURequest(cla, ins, pUn, pDeux, dataIn, option);
        // System.out.println("APDU Actual: " + Hex.encodeHexString(actual.getbytes()));
        // System.out.println("APDU Expected: " + Hex.encodeHexString(expected.getbytes()));
        Assert.assertArrayEquals(expected.getbytes(), actual.getbytes());
        Assert.assertEquals(expected.isCase4(), actual.isCase4());
    }
}

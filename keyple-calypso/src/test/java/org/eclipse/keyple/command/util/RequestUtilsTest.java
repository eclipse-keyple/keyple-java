/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.command.util;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.util.RequestUtils;
import org.eclipse.keyple.command.CommandsTable;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class RequestUtilsTest {

    private boolean isCase4;

    private ApduRequest expected;

    private byte cla;

    private CommandsTable ins;

    private byte pUn;

    private byte pDeux;

    private ByteBuffer dataIn;

    private byte option;

    private byte optionExptected;

    private ByteBuffer fci;

    @Test
    public void testConstructApduRequest() {
        fci = ByteBuffer.wrap(new byte[] {(byte) 0x00, (byte) 0xCA, 0x00, 0x6F});
        isCase4 = false;
        expected = new ApduRequest(fci, isCase4);
        cla = (byte) 0x00;
        ins = CalypsoPoCommands.GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = null;

        ApduRequest actual = RequestUtils.constructAPDURequest(cla, ins, pUn, pDeux, dataIn);
        Assert.assertEquals(expected.getBytes(), actual.getBytes());
        Assert.assertEquals(expected.isCase4(), actual.isCase4());
    }

    @Test
    public void testConstructApduRequestCase4() {
        fci = ByteBuffer.wrap(new byte[] {(byte) 0x00, (byte) 0xCA, 0x00, 0x6F, 0x00});
        isCase4 = true;
        expected = new ApduRequest(fci, isCase4);
        cla = (byte) 0x00;
        ins = CalypsoPoCommands.GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = null;
        option = 0x00;

        ApduRequest actual =
                RequestUtils.constructAPDURequest(cla, ins, pUn, pDeux, dataIn, option);
        Assert.assertEquals(expected.getBytes(), actual.getBytes());
        Assert.assertNotEquals(expected.isCase4(), actual.isCase4());
    }

    @Test
    public void testConstructApduRequestData() {
        fci = ByteBuffer.wrap(new byte[] {(byte) 0x00, (byte) 0xCA, 0x00, 0x6F, 0x02, 0x00, 0x00});
        isCase4 = false;
        expected = new ApduRequest(fci, isCase4);
        cla = (byte) 0x00;
        ins = CalypsoPoCommands.GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = ByteBuffer.wrap(new byte[] {0x00, 0x00});

        ApduRequest actual = RequestUtils.constructAPDURequest(cla, ins, pUn, pDeux, dataIn);
        Assert.assertEquals(expected.getBytes(), actual.getBytes());
        Assert.assertEquals(expected.isCase4(), actual.isCase4());
    }

    @Test
    public void testConstructApduRequestCase4Data() {
        isCase4 = true;
        cla = (byte) 0x00;
        ins = CalypsoPoCommands.GET_DATA_FCI;
        pUn = 0x00;
        pDeux = 0x6F;
        dataIn = ByteBuffer.wrap(new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, 0x00});
        option = (byte) 0x01;
        optionExptected = (byte) 0x00;
        fci = ByteBuffer
                .wrap(new byte[] {(byte) 0x00, (byte) 0xCA, 0x00, 0x6F, (byte) dataIn.limit(),
                        (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, 0x00, optionExptected});
        expected = new ApduRequest(fci, isCase4);

        ApduRequest actual =
                RequestUtils.constructAPDURequest(cla, ins, pUn, pDeux, dataIn, option);
        Assert.assertEquals(expected.getBytes(), actual.getBytes());
        Assert.assertEquals(expected.isCase4(), actual.isCase4());
    }
}

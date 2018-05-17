/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.po.builder;


import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.AbstractOpenSessionCmdBuild;
import org.keyple.commands.AbstractApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ByteBufferUtils;


public class OpenSessionCmdBuildTest {

    ByteBuffer samChallenge = ByteBuffer.wrap(new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E});

    AbstractApduCommandBuilder apduCommandBuilder;

    ApduRequest ApduRequest;


    @Test(expected = InconsistentCommandException.class)
    public void openSessionCmdBuild_rev_2_4_exception() throws InconsistentCommandException {
        byte keyIndex = (byte) 0x00;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;
        byte cla = (byte) 0x94;
        byte p1 = (byte) (0x80 + (recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) (sfiToSelect * 8);
        byte cmd = (byte) 0x8A;
        ByteBuffer dataIn = samChallenge;

        apduCommandBuilder = AbstractOpenSessionCmdBuild.create(PoRevision.REV2_4, keyIndex, dataIn,
                sfiToSelect, recordNumberToRead);
    }

    @Test
    public void openSessionCmdBuild_rev_2_4() throws InconsistentCommandException {
        byte keyIndex = (byte) 0x03;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;
        byte cla = (byte) 0x94;
        byte p1 = (byte) (0x80 + (recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) (sfiToSelect * 8);
        byte cmd = (byte) 0x8A;
        ByteBuffer dataIn = samChallenge;
        // revision 2.4
        byte[] request2_4 =
                {cla, cmd, p1, p2, (byte) dataIn.limit(), (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E};

        apduCommandBuilder = AbstractOpenSessionCmdBuild.create(PoRevision.REV2_4, keyIndex, dataIn,
                sfiToSelect, recordNumberToRead);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request2_4, ApduRequest.getBytes());
    }

    @Test
    public void openSessionCmdBuild_rev_3_1() throws InconsistentCommandException {
        byte keyIndex = (byte) 0x03;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;
        byte cla = (byte) 0x00;
        byte p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) ((sfiToSelect * 8) + 1);
        byte cmd = (byte) 0x8A;
        ByteBuffer dataIn = samChallenge;

        // revision 3.1
        byte[] request3_1 =
                {cla, cmd, p1, p2, (byte) dataIn.limit(), (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E};
        apduCommandBuilder = AbstractOpenSessionCmdBuild.create(PoRevision.REV3_1, keyIndex, dataIn,
                sfiToSelect, recordNumberToRead);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_1, ApduRequest.getBytes());
    }

    @Test
    public void openSessionCmdBuild_rev_3_2() throws InconsistentCommandException {
        byte keyIndex = (byte) 0x03;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;
        byte cla = (byte) 0x00;
        byte p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) ((sfiToSelect * 8) + 2);
        byte cmd = (byte) 0x8A;
        byte[] dataIn = new byte[samChallenge.limit() + 1];
        System.arraycopy(ByteBufferUtils.toBytes(samChallenge), 0, dataIn, 1, samChallenge.limit());
        // revision 3.2
        ByteBuffer request3_2 =
                ByteBuffer.wrap(new byte[] {cla, cmd, p1, p2, (byte) (samChallenge.limit() + 1),
                        (byte) 0x00, (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E});
        apduCommandBuilder = AbstractOpenSessionCmdBuild.create(PoRevision.REV3_2, keyIndex,
                samChallenge, sfiToSelect, recordNumberToRead);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertEquals(ByteBufferUtils.toHex(request3_2),
                ByteBufferUtils.toHex(ApduRequest.getBuffer()));
    }



}

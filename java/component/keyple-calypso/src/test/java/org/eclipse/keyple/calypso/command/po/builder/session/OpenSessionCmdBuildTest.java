/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.builder.session;



import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenSessionCmdBuildTest {

    private final byte[] samChallenge = new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E};

    private AbstractApduCommandBuilder apduCommandBuilder;

    private ApduRequest apduRequest;


    @Test(expected = IllegalArgumentException.class)
    public void openSessionCmdBuild_rev_2_4_exception() throws IllegalArgumentException {
        byte keyIndex = (byte) 0x00;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;


        apduCommandBuilder = AbstractOpenSessionCmdBuild.create(PoRevision.REV2_4, keyIndex,
                samChallenge, sfiToSelect, recordNumberToRead, "");
    }

    @Test
    public void openSessionCmdBuild_rev_2_4() throws IllegalArgumentException {
        byte keyIndex = (byte) 0x03;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;
        byte cla = (byte) 0x94;
        byte p1 = (byte) (0x80 + (recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) (sfiToSelect * 8);
        byte cmd = (byte) 0x8A;
        byte[] dataIn = samChallenge;
        // revision 2.4
        byte le = 0; /* case 4 */
        byte[] request2_4 =
                {cla, cmd, p1, p2, (byte) dataIn.length, (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, le};

        apduCommandBuilder = AbstractOpenSessionCmdBuild.create(PoRevision.REV2_4, keyIndex, dataIn,
                sfiToSelect, recordNumberToRead, "");
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request2_4, apduRequest.getBytes());
    }

    @Test
    public void openSessionCmdBuild_rev_3_1() throws IllegalArgumentException {
        byte keyIndex = (byte) 0x03;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;
        byte cla = (byte) 0x00;
        byte p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) ((sfiToSelect * 8) + 1);
        byte cmd = (byte) 0x8A;
        byte[] dataIn = samChallenge;
        byte le = 0; /* case 4 */

        // revision 3.1
        byte[] request3_1 =
                {cla, cmd, p1, p2, (byte) dataIn.length, (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, le};
        apduCommandBuilder = AbstractOpenSessionCmdBuild.create(PoRevision.REV3_1, keyIndex, dataIn,
                sfiToSelect, recordNumberToRead, "");
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_1, apduRequest.getBytes());
    }

    @Test
    public void openSessionCmdBuild_rev_3_2() throws IllegalArgumentException {
        byte keyIndex = (byte) 0x03;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;
        byte cla = (byte) 0x00;
        byte p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) ((sfiToSelect * 8) + 2);
        byte cmd = (byte) 0x8A;
        byte[] dataIn = new byte[samChallenge.length + 1];
        System.arraycopy(samChallenge, 0, dataIn, 1, samChallenge.length);
        byte le = 0; /* case 4 */
        // revision 3.2
        byte[] request3_2 = new byte[] {cla, cmd, p1, p2, (byte) (samChallenge.length + 1),
                (byte) 0x00, (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, le};
        apduCommandBuilder = AbstractOpenSessionCmdBuild.create(PoRevision.REV3_2, keyIndex,
                samChallenge, sfiToSelect, recordNumberToRead, "");
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertEquals(ByteArrayUtils.toHex(request3_2),
                ByteArrayUtils.toHex(apduRequest.getBytes()));
    }
}

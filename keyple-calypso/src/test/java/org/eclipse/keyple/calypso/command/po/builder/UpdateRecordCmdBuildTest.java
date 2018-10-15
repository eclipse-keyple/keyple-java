/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.command.po.builder;


import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateRecordCmdBuildTest {

    private final byte record_number = 0x01;

    private final byte[] newRecordData = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04};

    private AbstractApduCommandBuilder apduCommandBuilder;

    private ApduRequest ApduRequest;

    @Test
    public void updateRecordCmdBuild_rev2_4() throws IllegalArgumentException {

        // revision 2.4
        byte[] request2_4 = new byte[] {(byte) 0x94, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05,
                0x00, 0x01, 0x02, 0x03, 0x04};
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV2_4, (byte) 0x08, record_number,
                newRecordData, "TestRev2_4");
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request2_4, ApduRequest.getBytes());
    }

    @Test
    public void updateRecordCmdBuild_rev3_1() throws IllegalArgumentException {
        // revision 3.1
        byte[] request3_1 = new byte[] {(byte) 0x00, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05,
                0x00, 0x01, 0x02, 0x03, 0x04};
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV3_1, (byte) 0x08, record_number,
                newRecordData, "TestRev3_1");
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_1, ApduRequest.getBytes());
    }

    @Test
    public void updateRecordCmdBuild_rev3_2() throws IllegalArgumentException {
        // revision 3.2
        byte[] request3_2 = new byte[] {(byte) 0x00, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05,
                0x00, 0x01, 0x02, 0x03, 0x04};
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV3_2, (byte) 0x08, record_number,
                newRecordData, "TestRev3_2");
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_2, ApduRequest.getBytes());
    }

}

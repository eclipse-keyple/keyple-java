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

package org.eclipse.keyple.calypso.command.po.builder.session;



import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloseSessionCmdBuildTest {

    @Test
    public void closeSessionCmdBuild() throws IllegalArgumentException {
        byte[] request2_4 = new byte[] {(byte) 0x94, (byte) 0x8E, 0x00, 0x00, (byte) 0x04,
                (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, 0x00};
        byte[] request3_1 = new byte[] {(byte) 0x00, (byte) 0x8E, (byte) 0x80, 0x00, (byte) 0x04,
                (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, 0x00};
        byte[] terminalSessionSignature = new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E};
        AbstractApduCommandBuilder apduCommandBuilder =
                new CloseSessionCmdBuild(PoRevision.REV2_4, false, terminalSessionSignature);
        ApduRequest reqApdu = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request2_4, reqApdu.getBytes());

        apduCommandBuilder =
                new CloseSessionCmdBuild(PoRevision.REV3_1, true, terminalSessionSignature);
        reqApdu = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request3_1, reqApdu.getBytes());
    }
}

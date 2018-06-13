/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.commands.po.builder;


import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.commands.po.PoRevision;
import org.eclipse.keyple.calypso.commands.po.builder.CloseSessionCmdBuild;
import org.eclipse.keyple.commands.AbstractApduCommandBuilder;
import org.eclipse.keyple.commands.InconsistentCommandException;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.junit.Assert;
import org.junit.Test;

public class CloseSessionCmdBuidTest {
    @Test
    public void closeSessionCmdBuild() throws InconsistentCommandException {
        ByteBuffer request2_4 = ByteBuffer.wrap(new byte[] {(byte) 0x94, (byte) 0x8E, 0x00, 0x00,
                (byte) 0x04, (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E});
        ByteBuffer request3_1 = ByteBuffer.wrap(new byte[] {(byte) 0x00, (byte) 0x8E, (byte) 0x80,
                0x00, (byte) 0x04, (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E});
        ByteBuffer terminalSessionSignature =
                ByteBuffer.wrap(new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E});
        AbstractApduCommandBuilder apduCommandBuilder =
                new CloseSessionCmdBuild(PoRevision.REV2_4, false, terminalSessionSignature);
        ApduRequest reqApdu = apduCommandBuilder.getApduRequest();

        Assert.assertEquals(request2_4, reqApdu.getBytes());

        apduCommandBuilder =
                new CloseSessionCmdBuild(PoRevision.REV3_1, true, terminalSessionSignature);
        reqApdu = apduCommandBuilder.getApduRequest();

        Assert.assertEquals(request3_1, reqApdu.getBytes());
    }
}

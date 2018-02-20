/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.po.builder;


import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.CloseSessionCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

public class CloseSessionCmdBuidTest {
    @Test
    public void closeSessionCmdBuild() throws InconsistentCommandException {
        byte[] request2_4 = {(byte) 0x94, (byte) 0x8E, 0x00, 0x00, (byte) 0x04, (byte) 0xA8, 0x31,
                (byte) 0xC3, 0x3E};
        byte[] request3_1 = {(byte) 0x00, (byte) 0x8E, (byte) 0x80, 0x00, (byte) 0x04, (byte) 0xA8,
                0x31, (byte) 0xC3, 0x3E};
        byte[] terminalSessionSiganture = {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E};
        ApduCommandBuilder apduCommandBuilder =
                new CloseSessionCmdBuild(PoRevision.REV2_4, false, terminalSessionSiganture);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request2_4, ApduRequest.getbytes());

        apduCommandBuilder =
                new CloseSessionCmdBuild(PoRevision.REV3_1, true, terminalSessionSiganture);
        ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request3_1, ApduRequest.getbytes());
    }
}

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
import org.keyple.calypso.commands.po.builder.UpdateRecordCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

public class UpdateRecordCmdBuildTest {

    byte record_number = 0x01;

    byte[] newRecordData = {0x00, 0x01, 0x02, 0x03, 0x04};

    ApduCommandBuilder apduCommandBuilder;

    ApduRequest ApduRequest;

    @Test
    public void updateRecordCmdBuild_rev2_4() throws InconsistentCommandException {

        // revision 2.4
        byte[] request2_4 = {(byte) 0x94, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05, 0x00, 0x01,
                0x02, 0x03, 0x04};
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV2_4, record_number, (byte) 0x08,
                newRecordData);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request2_4, ApduRequest.getbytes());
    }

    @Test
    public void updateRecordCmdBuild_rev3_1() throws InconsistentCommandException {
        // revision 3.1
        byte[] request3_1 = {(byte) 0x00, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05, 0x00, 0x01,
                0x02, 0x03, 0x04};
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV3_1, record_number, (byte) 0x08,
                newRecordData);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_1, ApduRequest.getbytes());
    }

    @Test
    public void updateRecordCmdBuild_rev3_2() throws InconsistentCommandException {
        // revision 3.2
        byte[] request3_2 = {(byte) 0x00, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05, 0x00, 0x01,
                0x02, 0x03, 0x04};
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV3_2, record_number, (byte) 0x08,
                newRecordData);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_2, ApduRequest.getbytes());
    }

}

package org.keyple.commands.calypso.po.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduCommandBuilder;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.po.builder.UpdateRecordCmdBuild;
import org.keyple.seproxy.ApduRequest;

public class UpdateRecordCmdBuildTest {

    byte record_number = 0x01;

    byte[] newRecordData = { 0x00, 0x01, 0x02, 0x03, 0x04 };

    ApduCommandBuilder apduCommandBuilder;

    ApduRequest ApduRequest;

    @Test
    public void updateRecordCmdBuild_rev2_4() throws InconsistentCommandException {

        // revision 2.4
        byte[] request2_4 = { (byte) 0x94, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05, 0x00, 0x01, 0x02, 0x03, 0x04 };
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV2_4, record_number, (byte) 0x08, newRecordData);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request2_4, ApduRequest.getbytes());
    }

    @Test
    public void updateRecordCmdBuild_rev3_1() throws InconsistentCommandException {
        // revision 3.1
        byte[] request3_1 = { (byte) 0x00, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05, 0x00, 0x01, 0x02, 0x03, 0x04 };
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV3_1, record_number, (byte) 0x08, newRecordData);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_1, ApduRequest.getbytes());
    }

    @Test
    public void updateRecordCmdBuild_rev3_2() throws InconsistentCommandException {
        // revision 3.2
        byte[] request3_2 = { (byte) 0x00, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05, 0x00, 0x01, 0x02, 0x03, 0x04 };
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV3_2, record_number, (byte) 0x08, newRecordData);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_2, ApduRequest.getbytes());
    }

}
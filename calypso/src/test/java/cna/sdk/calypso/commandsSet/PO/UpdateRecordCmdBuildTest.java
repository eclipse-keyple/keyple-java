package cna.sdk.calypso.commandsSet.PO;

import org.junit.Assert;
import org.junit.Test;
import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.enumCmdWriteRecords;
import cna.sdk.calypso.commandset.enumSFI;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.calypso.commandset.po.builder.UpdateRecordCmdBuild;
import cna.sdk.seproxy.APDURequest;

public class UpdateRecordCmdBuildTest {

    byte record_number = 0x01;

    byte[] newRecordData = { 0x00, 0x01, 0x02, 0x03, 0x04 };

    ApduCommandBuilder apduCommandBuilder;

    APDURequest apduRequest;

    @Test
    public void updateRecordCmdBuild_rev2_4() {

        // revision 2.4
        byte[] request2_4 = { (byte) 0x94, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05, 0x00, 0x01, 0x02, 0x03, 0x04 };
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV2_4, record_number, enumSFI.EVENT_LOG_FILE.getSfi(),
                newRecordData);
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request2_4, apduRequest.getbytes());
    }

    @Test
    public void updateRecordCmdBuild_rev3_1() {
        // revision 3.1
        byte[] request3_1 = { (byte) 0x00, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05, 0x00, 0x01, 0x02, 0x03, 0x04 };
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV3_1, record_number, enumSFI.EVENT_LOG_FILE.getSfi(),
                newRecordData);
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_1, apduRequest.getbytes());
    }

    @Test
    public void updateRecordCmdBuild_rev3_2() {
        // revision 3.2
        byte[] request3_2 = { (byte) 0x00, (byte) 0xDC, (byte) 0x01, 0x44, (byte) 0x05, 0x00, 0x01, 0x02, 0x03, 0x04 };
        apduCommandBuilder = new UpdateRecordCmdBuild(PoRevision.REV3_2, record_number, enumSFI.EVENT_LOG_FILE.getSfi(),
                newRecordData);
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_2, apduRequest.getbytes());
    }

}
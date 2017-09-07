package org.keyple.calypso.commandsSet.PO;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.enumCmdReadRecords;
import org.keyple.calypso.commandset.enumSFI;
import org.keyple.calypso.commandset.po.PoRevision;
import org.keyple.calypso.commandset.po.builder.ReadRecordsCmdBuild;
import org.keyple.seproxy.APDURequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadRecordsCmdBuildTest {

    Logger logger = LoggerFactory.getLogger(ReadRecordsCmdBuildTest.class);

    byte record_number = 0x01;

    byte expectedLenght = 0x00;

    ApduCommandBuilder apduCommandBuilder;

    APDURequest apduRequest;

    @Test
    public void readRecords_rev2_4() {

        // revision 2.4
        byte[] request2_4 = { (byte) 0x94, (byte) 0xB2, (byte) 0x01, 0x44 };
        apduCommandBuilder = new ReadRecordsCmdBuild(PoRevision.CLASS_0x94, record_number, enumSFI.EVENT_LOG_FILE.getSfi(),
                expectedLenght);
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request2_4, apduRequest.getbytes());
    }

    @Test
    public void readRecords_rev3_1() {
        // revision 3.1
        byte[] request3_1 = { (byte) 0x00, (byte) 0xB2, (byte) 0x01, 0x04 };
        apduCommandBuilder = new ReadRecordsCmdBuild(PoRevision.CLASS_0x00, record_number, enumSFI.UNKNOWN_FILE.getSfi(),
                expectedLenght);
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_1, apduRequest.getbytes());
    }

    @Test
    public void readRecords_rev3_2() {
        // revision 3.2
        byte[] request3_2 = { (byte) 0x00, (byte) 0xB2, (byte) 0x01, 0x05 };
        apduCommandBuilder = new ReadRecordsCmdBuild(PoRevision.CLASS_0x00, record_number,
                enumSFI.ENVIRONMENT_FILE.getSfi(), expectedLenght);
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_2, apduRequest.getbytes());
    }

}

package org.keyple.commands.calypso.po.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduCommandBuilder;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.po.builder.ReadRecordsCmdBuild;
import org.keyple.seproxy.ApduRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadRecordsCmdBuildTest {

    Logger logger = LoggerFactory.getLogger(ReadRecordsCmdBuildTest.class);

    byte record_number = 0x01;

    byte expectedLenght = 0x00;

    ApduCommandBuilder apduCommandBuilder;

    ApduRequest ApduRequest;

    @Test
    public void readRecords_rev2_4() throws InconsistentCommandException {

        byte cla = (byte) 0x94;
        byte cmd = (byte) 0xB2;
        byte firstRecordNumber = record_number;
        boolean readJustOneRecord = false;
        byte sfi = (byte) 0x08;
        byte p1 = firstRecordNumber;
        byte p2 = (byte) ((byte) (sfi * 8) + 5);
        byte[] dataIn = null;
        
        // revision 2.4
        byte[] request2_4 = { cla, cmd, p1, p2, 0x00 };
        apduCommandBuilder = new ReadRecordsCmdBuild(PoRevision.REV2_4, record_number, readJustOneRecord, sfi,
                expectedLenght);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request2_4, ApduRequest.getbytes());
    }

    @Test
    public void readRecords_rev3_1() throws InconsistentCommandException {
        
        byte cla = (byte) 0x00;
        byte cmd = (byte) 0xB2;
        byte firstRecordNumber = record_number;
        boolean readJustOneRecord = false;
        byte sfi = (byte) 0x08;
        byte p1 = firstRecordNumber;
        byte p2 = (byte) ((byte) (sfi * 8) + 5);
        byte[] dataIn = null;
        
        
        // revision 3.1
        byte[] request3_1 = {  cla, cmd, p1, p2 ,0x00 };
        apduCommandBuilder = new ReadRecordsCmdBuild(PoRevision.REV3_1, record_number, readJustOneRecord, sfi,
                expectedLenght);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_1, ApduRequest.getbytes());
    }

    @Test
    public void readRecords_rev3_2() throws InconsistentCommandException {
        byte cla = (byte) 0x00;
        byte cmd = (byte) 0xB2;
        byte firstRecordNumber = record_number;
        boolean readJustOneRecord = false;
        byte sfi = (byte) 0x08;
        byte p1 = firstRecordNumber;
        byte p2 = (byte) ((byte) (sfi * 8) + 5);
        byte[] dataIn = null;
        
        // revision 3.2
        byte[] request3_2 = {  cla, cmd, p1, p2, 0x00 };
        apduCommandBuilder = new ReadRecordsCmdBuild(PoRevision.REV3_2, record_number, readJustOneRecord, sfi,
                expectedLenght);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_2, ApduRequest.getbytes());
    }

}

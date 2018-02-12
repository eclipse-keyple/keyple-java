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
import org.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

public class ReadRecordsCmdBuildTest {

    // Logger logger = Logger.getLogger(ReadRecordsCmdBuildTest.class);

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
        byte[] request2_4 = {cla, cmd, p1, p2, 0x00};
        apduCommandBuilder = new ReadRecordsCmdBuild(PoRevision.REV2_4, record_number,
                readJustOneRecord, sfi, expectedLenght);
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
        byte[] request3_1 = {cla, cmd, p1, p2, 0x00};
        apduCommandBuilder = new ReadRecordsCmdBuild(PoRevision.REV3_1, record_number,
                readJustOneRecord, sfi, expectedLenght);
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
        byte[] request3_2 = {cla, cmd, p1, p2, 0x00};
        apduCommandBuilder = new ReadRecordsCmdBuild(PoRevision.REV3_2, record_number,
                readJustOneRecord, sfi, expectedLenght);
        ApduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_2, ApduRequest.getbytes());
    }

}

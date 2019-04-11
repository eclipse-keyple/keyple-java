/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.builder;


import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReadRecordsCmdBuildTest {

    // Logger logger = Logger.getLogger(ReadRecordsCmdBuildTest.class);

    private final byte record_number = 0x01;

    private final byte expectedLength = 0x00;

    private AbstractApduCommandBuilder apduCommandBuilder;

    private ApduRequest apduRequest;

    @Test
    public void readRecords_rev2_4() throws IllegalArgumentException {

        byte cla = (byte) 0x94;
        byte cmd = (byte) 0xB2;
        boolean readJustOneRecord = false;
        byte sfi = (byte) 0x08;
        byte p2 = (byte) ((byte) (sfi * 8) + 5);

        // revision 2.4
        byte[] request2_4 = {cla, cmd, record_number, p2, 0x00};
        apduCommandBuilder =
                new ReadRecordsCmdBuild(PoClass.LEGACY, sfi, ReadDataStructure.SINGLE_RECORD_DATA,
                        record_number, readJustOneRecord, expectedLength, "TestRev2_4");
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request2_4, apduRequest.getBytes());
    }

    @Test
    public void readRecords_rev3_1() throws IllegalArgumentException {

        byte cla = (byte) 0x00;
        byte cmd = (byte) 0xB2;
        boolean readJustOneRecord = false;
        byte sfi = (byte) 0x08;
        byte p2 = (byte) ((byte) (sfi * 8) + 5);


        // revision 3.1
        byte[] request3_1 = {cla, cmd, record_number, p2, 0x00};
        apduCommandBuilder =
                new ReadRecordsCmdBuild(PoClass.ISO, sfi, ReadDataStructure.SINGLE_RECORD_DATA,
                        record_number, readJustOneRecord, expectedLength, "TestRev3_1");
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_1, apduRequest.getBytes());
    }

    @Test
    public void readRecords_rev3_2() throws IllegalArgumentException {
        byte cla = (byte) 0x00;
        byte cmd = (byte) 0xB2;
        boolean readJustOneRecord = false;
        byte sfi = (byte) 0x08;
        byte p2 = (byte) ((byte) (sfi * 8) + 5);

        // revision 3.2
        byte[] request3_2 = {cla, cmd, record_number, p2, 0x00};
        apduCommandBuilder =
                new ReadRecordsCmdBuild(PoClass.ISO, sfi, ReadDataStructure.SINGLE_RECORD_DATA,
                        record_number, readJustOneRecord, expectedLength, "TestRev3_2");
        apduRequest = apduCommandBuilder.getApduRequest();
        Assert.assertArrayEquals(request3_2, apduRequest.getBytes());
    }

}

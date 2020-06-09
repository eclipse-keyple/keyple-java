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


import static org.assertj.core.api.Assertions.assertThat;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReadRecordsCmdBuildTest {

    // Logger logger = Logger.getLogger(ReadRecordsCmdBuildTest.class);

    private final byte record_number = 0x01;

    private final byte expectedLength = 0x00;

    private ReadRecordsCmdBuild readRecordsCmdBuilder;

    private ApduRequest apduRequest;

    @Test
    public void readRecords_rev2_4() {

        byte cla = (byte) 0x94;
        byte cmd = (byte) 0xB2;
        byte sfi = (byte) 0x08;
        byte p2 = (byte) ((byte) (sfi * 8) + 4); // one records

        // revision 2.4
        byte[] request2_4 = {cla, cmd, record_number, p2, 0x00};
        readRecordsCmdBuilder = new ReadRecordsCmdBuild(PoClass.LEGACY, sfi, record_number,
                ReadRecordsCmdBuild.ReadMode.ONE_RECORD, expectedLength);
        apduRequest = readRecordsCmdBuilder.getApduRequest();
        assertThat(apduRequest.getBytes()).isEqualTo(request2_4);
        assertThat(readRecordsCmdBuilder.getReadMode())
                .isEqualTo(ReadRecordsCmdBuild.ReadMode.ONE_RECORD);
    }

    @Test
    public void readRecords_rev2_4_2() {

        byte cla = (byte) 0x94;
        byte cmd = (byte) 0xB2;
        byte sfi = (byte) 0x08;
        byte p2 = (byte) ((byte) (sfi * 8) + 5); // all records

        // revision 2.4
        byte[] request2_4 = {cla, cmd, record_number, p2, 0x00};
        readRecordsCmdBuilder = new ReadRecordsCmdBuild(PoClass.LEGACY, sfi, record_number,
                ReadRecordsCmdBuild.ReadMode.MULTIPLE_RECORD, expectedLength);
        apduRequest = readRecordsCmdBuilder.getApduRequest();
        assertThat(apduRequest.getBytes()).isEqualTo(request2_4);
        assertThat(readRecordsCmdBuilder.getReadMode())
                .isEqualTo(ReadRecordsCmdBuild.ReadMode.MULTIPLE_RECORD);
    }

    @Test
    public void readRecords_rev3_1() {

        byte cla = (byte) 0x00;
        byte cmd = (byte) 0xB2;
        byte sfi = (byte) 0x08;
        byte p2 = (byte) ((byte) (sfi * 8) + 5); // all records

        // revision 3.1
        byte[] request3_1 = {cla, cmd, record_number, p2, 0x00};
        readRecordsCmdBuilder = new ReadRecordsCmdBuild(PoClass.ISO, sfi, record_number,
                ReadRecordsCmdBuild.ReadMode.MULTIPLE_RECORD, expectedLength);
        apduRequest = readRecordsCmdBuilder.getApduRequest();
        assertThat(apduRequest.getBytes()).isEqualTo(request3_1);
        assertThat(readRecordsCmdBuilder.getReadMode())
                .isEqualTo(ReadRecordsCmdBuild.ReadMode.MULTIPLE_RECORD);
    }

    @Test
    public void readRecords_rev3_2() {
        byte cla = (byte) 0x00;
        byte cmd = (byte) 0xB2;
        byte sfi = (byte) 0x08;
        byte p2 = (byte) ((byte) (sfi * 8) + 5); // all records

        // revision 3.2
        byte[] request3_2 = {cla, cmd, record_number, p2, 0x00};
        readRecordsCmdBuilder = new ReadRecordsCmdBuild(PoClass.ISO, sfi, record_number,
                ReadRecordsCmdBuild.ReadMode.MULTIPLE_RECORD, expectedLength);
        apduRequest = readRecordsCmdBuilder.getApduRequest();
        assertThat(apduRequest.getBytes()).isEqualTo(request3_2);
        assertThat(readRecordsCmdBuilder.getReadMode())
                .isEqualTo(ReadRecordsCmdBuild.ReadMode.MULTIPLE_RECORD);
    }
}

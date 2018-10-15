/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.command.po.parser;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReadRecordsRespParsTest {

    @Test
    // TODO: Fix the parsing code so that the test makes sense
    public void readRecordRespPars_one_record() {
        byte[] response = new byte[] {0x04, 0x02, 0x01, 0x01, (byte) 0x90, 0x00};
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, null);
        responses.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(new SeResponse(true, null,
                new ApduResponse(ByteArrayUtils.fromHex("9000"), null), responses));

        ReadRecordsRespPars readRecordsResponse =
                new ReadRecordsRespPars(seResponse.getSingleResponse().getApduResponses().get(0));
        byte[] responseActual = readRecordsResponse.getApduResponse().getBytes();
        Assert.assertEquals(response, responseActual);

        // fclairamb (2018-02-28): This is how the code behaves right now but something seems fishy
        List<ReadRecordsRespPars.Record> records = readRecordsResponse.getRecords();
        Assert.assertEquals(1, records.size());
        ReadRecordsRespPars.Record record = records.get(0);
        Assert.assertEquals(4, record.getRecordNumber());
        Assert.assertEquals("01", ByteArrayUtils.toHex(record.getData()));
    }

    @Test
    // TODO: Fix the parsing code so that the test makes sense
    public void readRecordRespPars_records() {
        byte[] response = new byte[] {0x01, 0x01, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, 0x01, 0x01,
                0x30, 0x01, 0x01, 0x30, (byte) 0x90, 0x00};
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, null);
        responses.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(new SeResponse(true, null,
                new ApduResponse(ByteArrayUtils.fromHex("9000"), null), responses));

        ReadRecordsRespPars apduResponseParser =
                new ReadRecordsRespPars(seResponse.getSingleResponse().getApduResponses().get(0));
        byte[] responseActual = apduResponseParser.getApduResponse().getBytes();
        Assert.assertEquals(response, responseActual);

        // fclairamb (2018-02-28): This is how the code behaves right now but something seems fishy
        List<ReadRecordsRespPars.Record> records = apduResponseParser.getRecords();
        Assert.assertEquals(2, records.size());
        {
            ReadRecordsRespPars.Record record = records.get(0);
            Assert.assertEquals(1, record.getRecordNumber());
            Assert.assertEquals("01", ByteArrayUtils.toHex(record.getData()));
        }
        {
            ReadRecordsRespPars.Record record = records.get(1);
            Assert.assertEquals(1, record.getRecordNumber());
            Assert.assertEquals("0101300101300101", ByteArrayUtils.toHex(record.getData()));
        }
    }

    @Test
    // TODO: Fix the parsing code so that the test makes sense
    public void sampleMultipleRecordsParsing() {
        byte[] apdu = ByteArrayUtils.fromHex("1415 2425 3435 4445 9000h");
        ReadRecordsRespPars recordsParsing = new ReadRecordsRespPars(new ApduResponse(apdu, null));
        List<ReadRecordsRespPars.Record> records = recordsParsing.getRecords();
        Assert.assertEquals(1, records.size());
        {
            ReadRecordsRespPars.Record record = records.get(0);
            Assert.assertEquals(20, record.getRecordNumber());
            Assert.assertEquals("2425343544", ByteArrayUtils.toHex(record.getData()));
        }
    }

    @Test
    public void readRecordRespPars_one_record_sfi() {
        byte[] response = new byte[] {0x01, 0x01, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, 0x01, 0x01,
                0x30, 0x01, 0x01, 0x30, (byte) 0x90, 0x00};
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, null);
        responses.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(new SeResponse(true, null,
                new ApduResponse(ByteArrayUtils.fromHex("9000"), null), responses));

        AbstractApduResponseParser apduResponseParser =
                new ReadRecordsRespPars(seResponse.getSingleResponse().getApduResponses().get(0));
        byte[] responseActual = apduResponseParser.getApduResponse().getBytes();
        Assert.assertEquals(response, responseActual);
    }

    @Test
    public void readRecordRespPars_records_sfi() {
        byte[] response = new byte[] {0x01, 0x01, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, 0x01, 0x01,
                0x30, 0x01, 0x01, 0x30, (byte) 0x90, 0x00};
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, null);
        responses.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(new SeResponse(true, null,
                new ApduResponse(ByteArrayUtils.fromHex("9000"), null), responses));

        AbstractApduResponseParser apduResponseParser =
                new ReadRecordsRespPars(seResponse.getSingleResponse().getApduResponses().get(0));
        byte[] responseActual = apduResponseParser.getApduResponse().getBytes();
        Assert.assertEquals(response, responseActual);
    }
}

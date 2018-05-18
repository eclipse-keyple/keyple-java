/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.po.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.po.parser.ReadRecordsRespPars;
import org.keyple.commands.AbstractApduResponseParser;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ByteBufferUtils;
import org.keyple.seproxy.SeResponseSet;

public class ReadRecordsRespParsTest {

    @Test
    // TODO: Fix the parsing code so that the test makes sense
    public void readRecordRespPars_one_record() {
        ByteBuffer response =
                ByteBuffer.wrap(new byte[] {0x04, 0x02, 0x01, 0x01, (byte) 0x90, 0x00});
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true);
        listeResponse.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(true, null, listeResponse);

        ReadRecordsRespPars readRecordsResponse =
                new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        ByteBuffer responseActual = readRecordsResponse.getApduResponse().getBuffer();
        Assert.assertEquals(response, responseActual);

        // fclairamb (2018-02-28): This is how the code behaves right now but something seems fishy
        List<ReadRecordsRespPars.Record> records = readRecordsResponse.getRecords();
        Assert.assertEquals(1, records.size());
        ReadRecordsRespPars.Record record = records.get(0);
        Assert.assertEquals(4, record.getRecordNumber());
        Assert.assertEquals("01", ByteBufferUtils.toHex(record.getData()));
    }

    @Test
    // TODO: Fix the parsing code so that the test makes sense
    public void readRecordRespPars_records() {
        ByteBuffer response = ByteBuffer.wrap(new byte[] {0x01, 0x01, 0x01, 0x01, 0x30, 0x01, 0x01,
                0x30, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, (byte) 0x90, 0x00});
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true);
        listeResponse.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(true, null, listeResponse);

        ReadRecordsRespPars apduResponseParser =
                new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        ByteBuffer responseActual = apduResponseParser.getApduResponse().getBuffer();
        Assert.assertEquals(response, responseActual);

        // fclairamb (2018-02-28): This is how the code behaves right now but something seems fishy
        List<ReadRecordsRespPars.Record> records = apduResponseParser.getRecords();
        Assert.assertEquals(2, records.size());
        {
            ReadRecordsRespPars.Record record = records.get(0);
            Assert.assertEquals(1, record.getRecordNumber());
            Assert.assertEquals("01", ByteBufferUtils.toHex(record.getData()));
        }
        {
            ReadRecordsRespPars.Record record = records.get(1);
            Assert.assertEquals(1, record.getRecordNumber());
            Assert.assertEquals("0101300101300101", ByteBufferUtils.toHex(record.getData()));
        }
    }

    @Test
    // TODO: Fix the parsing code so that the test makes sense
    public void sampleMultipleRecordsParsing() {
        ByteBuffer apdu = ByteBufferUtils.fromHex("1415 2425 3435 4445 9000h");
        ReadRecordsRespPars recordsPasing = new ReadRecordsRespPars(new ApduResponse(apdu, true));
        List<ReadRecordsRespPars.Record> records = recordsPasing.getRecords();
        Assert.assertEquals(1, records.size());
        {
            ReadRecordsRespPars.Record record = records.get(0);
            Assert.assertEquals(20, record.getRecordNumber());
            Assert.assertEquals("2425343544", ByteBufferUtils.toHex(record.getData()));
        }
    }

    @Test
    public void readRecordRespPars_one_record_sfi() {
        ByteBuffer response = ByteBuffer.wrap(new byte[] {0x01, 0x01, 0x01, 0x01, 0x30, 0x01, 0x01,
                0x30, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, (byte) 0x90, 0x00});
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true);
        listeResponse.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(true, null, listeResponse);

        AbstractApduResponseParser apduResponseParser =
                new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        ByteBuffer reponseActual = apduResponseParser.getApduResponse().getBuffer();
        Assert.assertEquals(response, reponseActual);
    }

    @Test
    public void readRecordRespPars_records_sfi() {
        ByteBuffer response = ByteBuffer.wrap(new byte[] {0x01, 0x01, 0x01, 0x01, 0x30, 0x01, 0x01,
                0x30, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, (byte) 0x90, 0x00});
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true);
        listeResponse.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(true, null, listeResponse);

        AbstractApduResponseParser apduResponseParser =
                new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        ByteBuffer reponseActual = apduResponseParser.getApduResponse().getBuffer();
        Assert.assertEquals(response, reponseActual);
    }
}

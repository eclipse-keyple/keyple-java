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
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;

public class ReadRecordsRespParsTest {

    // TODO: Actually read records
    // fclairamb: I improved the unit tests code but I didn't change the logic. This code doesn't
    // read a single record.

    @Test
    public void readRecordRespPars_one_record() {
        ByteBuffer response =
                ByteBuffer.wrap(new byte[] {0x04, 0x02, 0x01, 0x01, (byte) 0x90, 0x00});
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true);
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ReadRecordsRespPars readRecordsResponse =
                new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        ByteBuffer responseActual = readRecordsResponse.getApduResponse().getBuffer();
        Assert.assertEquals(response, responseActual);

    }

    @Test
    public void readRecordRespPars_records() {
        ByteBuffer response = ByteBuffer.wrap(new byte[] {0x01, 0x01, 0x01, 0x01, 0x30, 0x01, 0x01,
                0x30, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, (byte) 0x90, 0x00});
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true);
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ReadRecordsRespPars apduResponseParser =
                new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        ByteBuffer responseActual = apduResponseParser.getApduResponse().getBuffer();
        Assert.assertEquals(response, responseActual);
    }

    @Test
    public void readRecordRespPars_one_record_sfi() {
        ByteBuffer response = ByteBuffer.wrap(new byte[] {0x01, 0x01, 0x01, 0x01, 0x30, 0x01, 0x01,
                0x30, 0x01, 0x01, 0x30, 0x01, 0x01, 0x30, (byte) 0x90, 0x00});
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true);
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ApduResponseParser apduResponseParser =
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
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ApduResponseParser apduResponseParser =
                new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        ByteBuffer reponseActual = apduResponseParser.getApduResponse().getBuffer();
        Assert.assertEquals(response, reponseActual);
    }
}

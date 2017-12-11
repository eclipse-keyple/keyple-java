package org.keyple.commands.calypso.po.parser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.commands.calypso.po.parser.ReadRecordsRespPars;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeResponse;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadRecordsRespParsTest {

    Logger logger = LoggerFactory.getLogger(ReadRecordsRespParsTest.class);

    SeRequest request;

    ProxyReader reader = Mockito.mock(ProxyReader.class);

    String msgParser;

    String msgParserDur = "File not found";

    @Test
    public void readRecordRespPars_one_record() {
        byte[] response = { 0x04, 0x02, 0x01, 0x01 };
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true, new byte[] { (byte) 0x90, 0x00 });
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ApduResponseParser apduResponseParser = new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
        Assert.assertArrayEquals(response, reponseActual);

    }

    @Test
    public void readRecordRespPars_records() {
        byte[] response = { 0x01, 0x01, 0x01, 0x01, 0x30 , 0x01, 0x01, 0x30 , 0x01, 0x01, 0x30 , 0x01, 0x01, 0x30 };
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true, new byte[] { (byte) 0x90, 0x00 });
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ReadRecordsRespPars apduResponseParser = new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
        Assert.assertArrayEquals(response, reponseActual);
    }

    @Test
    public void readRecordRespPars_one_record_sfi() {
        byte[] response = { 0x01, 0x01, 0x01, 0x01, 0x30 , 0x01, 0x01, 0x30 , 0x01, 0x01, 0x30 , 0x01, 0x01, 0x30 };
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true, new byte[] { (byte) 0x90, 0x00 });
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ApduResponseParser apduResponseParser = new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
        Assert.assertArrayEquals(response, reponseActual);
    }

    @Test
    public void readRecordRespPars_records_sfi() {
        byte[] response = { 0x01, 0x01, 0x01, 0x01, 0x30 , 0x01, 0x01, 0x30 , 0x01, 0x01, 0x30 , 0x01, 0x01, 0x30 };
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true, new byte[] { (byte) 0x90, 0x00 });
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ApduResponseParser apduResponseParser = new ReadRecordsRespPars(seResponse.getApduResponses().get(0));
        byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
        Assert.assertArrayEquals(response, reponseActual);
    }
}

package cna.sdk.calypso.commandsSet.PO;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.enumCmdReadRecords;
import cna.sdk.calypso.commandset.po.parser.ReadRecordsRespPars;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.ProxyReader;
import cna.sdk.seproxy.SERequest;
import cna.sdk.seproxy.SEResponse;

public class ReadRecordsRespParsTest {

	Logger logger = LoggerFactory.getLogger(ReadRecordsRespParsTest.class);

	SERequest request;

	ProxyReader reader = Mockito.mock(ProxyReader.class);

	String msgParser;

	String msgParserDur = "File not found";

	@Test
	public void readRecordRespPars_one_record() {
		byte[] response = { 0x01, 0x02, 0x03, 0x04, 0x05, (byte) 0x90, 0x00 };
		List<APDUResponse> listeResponse = new ArrayList<>();
		APDUResponse apduResponse = new APDUResponse(response, true, new byte[] { (byte) 0x90, 0x00 });
		listeResponse.add(apduResponse);
		SEResponse seResponse = new SEResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new ReadRecordsRespPars(seResponse.getApduResponses().get(0), enumCmdReadRecords.READ_ONE_RECORD);
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(response, reponseActual);

	}

	@Test
	public void readRecordRespPars_records() {
		byte[] response = { 0x01, 0x02, 0x03, 0x04, 0x05, (byte) 0x90, 0x00 };
		List<APDUResponse> listeResponse = new ArrayList<>();
		APDUResponse apduResponse = new APDUResponse(response, true, new byte[] { (byte) 0x90, 0x00 });
		listeResponse.add(apduResponse);
		SEResponse seResponse = new SEResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new ReadRecordsRespPars(seResponse.getApduResponses().get(0), enumCmdReadRecords.READ_RECORDS);
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(response, reponseActual);
	}

	@Test
	public void readRecordRespPars_one_record_sfi() {
		byte[] response = { 0x01, 0x02, 0x03, 0x04, 0x05, (byte) 0x90, 0x00 };
		List<APDUResponse> listeResponse = new ArrayList<>();
		APDUResponse apduResponse = new APDUResponse(response, true, new byte[] { (byte) 0x90, 0x00 });
		listeResponse.add(apduResponse);
		SEResponse seResponse = new SEResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new ReadRecordsRespPars(seResponse.getApduResponses().get(0), enumCmdReadRecords.READ_ONE_RECORD_FROM_EF_USING_SFI);
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(response, reponseActual);
	}

	@Test
	public void readRecordRespPars_records_sfi() {
		byte[] response = { 0x01, 0x02, 0x03, 0x04, 0x05, (byte) 0x90, 0x00 };
		List<APDUResponse> listeResponse = new ArrayList<>();
		APDUResponse apduResponse = new APDUResponse(response, true, new byte[] { (byte) 0x90, 0x00 });
		listeResponse.add(apduResponse);
		SEResponse seResponse = new SEResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new ReadRecordsRespPars(seResponse.getApduResponses().get(0), enumCmdReadRecords.READ_ONE_RECORD_FROM_EF_USING_SFI);
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(response, reponseActual);
	}
}

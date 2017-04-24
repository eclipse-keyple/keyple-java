package cna.sdk.calypso.commandsSet.PO;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.calypso.commandset.po.parser.OpenSessionRespPars;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.SEResponse;
import junit.framework.TestCase;

public class OpenSessionRespParsTest extends TestCase {

	Logger logger = LoggerFactory.getLogger(OpenSessionRespParsTest.class);

	String msgParser;

	String msgParserDur = "File not found";

	private byte[] returnOK = { (byte) 0x90, 0x00 };

	public void testgetResponse() {

		// code de la reponse attendu

		APDUResponse responseMockOS = new APDUResponse(new byte[] { 0x69, (byte) 0x82 }, true, returnOK);
		APDUResponse responseMockFci = new APDUResponse(new byte[] { 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54,
				0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00,
				0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01,
				(byte) 0x90, 0x00 }, true, returnOK);
		List<APDUResponse> apduResponses = new ArrayList<>();
		apduResponses.add(responseMockOS);

		SEResponse reponseMock = new SEResponse(true, responseMockFci, apduResponses);
		APDUResponse response = reponseMock.getApduResponses().get(0);

		new OpenSessionRespPars(response, PoRevision.REV2_4);
		// msgParser = parser.getMsg(response);
		// assertEquals(msgParser, msgParserDur);
		logger.info("depuis la methode: " + msgParser);
		logger.info("test: " + msgParserDur);

	}

}

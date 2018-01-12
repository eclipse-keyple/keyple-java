package org.keyple.commands.calypso.po.parser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.po.parser.OpenSessionRespPars;
import org.keyple.commands.calypso.utils.TestsUtilsResponseTabByteGenerator;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

@RunWith(MockitoJUnitRunner.class)
public class OpenSessionRespParsTest {

	Logger logger = LoggerFactory.getLogger(OpenSessionRespParsTest.class);

	String msgParser;

	String msgParserDur = "File not found";

	
	@Test
	public void testgetResponse_rev2_4() {

		// code de la reponse attendu

		ApduResponse responseMockFci = TestsUtilsResponseTabByteGenerator.generateApduResponseValideRev2_4();
		List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
		apduResponses.add(responseMockFci);

		SeResponse reponseMock = new SeResponse(true, responseMockFci, apduResponses);
		ApduResponse response = reponseMock.getApduResponses().get(0);

		new OpenSessionRespPars(response, PoRevision.REV2_4);
		logger.info("depuis la methode: " + msgParser);
		logger.info("test: " + msgParserDur);

	}
	
	@Test
	public void testgetResponse_rev3_1() {

        // code de la reponse attendu

        ApduResponse responseMockFci = TestsUtilsResponseTabByteGenerator.generateApduResponseValideRev3_1();
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockFci);
        
        SeResponse reponseMock = new SeResponse(true, responseMockFci, apduResponses);
        ApduResponse response = reponseMock.getApduResponses().get(0);

        new OpenSessionRespPars(response, PoRevision.REV3_1);
        logger.info("depuis la methode: " + msgParser);
        logger.info("test: " + msgParserDur);

    }
	
	@Test
    public void testgetResponse_rev3_2() {

        // code de la reponse attendu

	    ApduResponse responseMockOS = TestsUtilsResponseTabByteGenerator.generateApduResponseValideRev3_2();
        ApduResponse responseMockFci = TestsUtilsResponseTabByteGenerator.generateApduResponseValideRev3_2();
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockOS);

        SeResponse reponseMock = new SeResponse(true, responseMockFci, apduResponses);
        ApduResponse response = reponseMock.getApduResponses().get(0);

        new OpenSessionRespPars(response, PoRevision.REV3_2);
        logger.info("depuis la methode: " + msgParser);
        logger.info("test: " + msgParserDur);

    }

}

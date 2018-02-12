/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.po.parser;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.parser.OpenSessionRespPars;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;
import org.mockito.runners.MockitoJUnitRunner;
import keyple.commands.utils.TestsUtilsResponseTabByteGenerator;

@RunWith(MockitoJUnitRunner.class)
public class OpenSessionRespParsTest {

    Logger logger = LogManager.getLogger(OpenSessionRespParsTest.class);

    String msgParser;

    String msgParserDur = "File not found";


    @Test
    public void testgetResponse_rev2_4() {

        // code de la reponse attendu

        ApduResponse responseMockFci =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValideRev2_4();
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

        ApduResponse responseMockFci =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValideRev3_1();
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

        ApduResponse responseMockOS =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValideRev3_2();
        ApduResponse responseMockFci =
                TestsUtilsResponseTabByteGenerator.generateApduResponseValideRev3_2();
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(responseMockOS);

        SeResponse reponseMock = new SeResponse(true, responseMockFci, apduResponses);
        ApduResponse response = reponseMock.getApduResponses().get(0);

        new OpenSessionRespPars(response, PoRevision.REV3_2);
        logger.info("depuis la methode: " + msgParser);
        logger.info("test: " + msgParserDur);

    }

}

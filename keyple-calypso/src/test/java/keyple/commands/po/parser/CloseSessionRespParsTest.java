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
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.po.parser.CloseSessionRespPars;
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;

public class CloseSessionRespParsTest {

    @Test
    public void closeSessionRespPars() { // by ixxi
        byte[] response = {0x4D, (byte) 0xBD, (byte) 0xC9, 0x60, (byte) 0x90, 0x00};
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true, new byte[] {90, 00});
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ApduResponseParser apduResponseParser =
                new CloseSessionRespPars(seResponse.getApduResponses().get(0));
        byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
        Assert.assertArrayEquals(response, reponseActual);
    }

    @Test
    public void TestToPOHalfSessionSignature() { // by ixxi from ResponseUtilsTest

        byte[] apduResponse =
                new byte[] {(byte) 0x4D, (byte) 0xBD, (byte) 0xC9, 0x60, (byte) 0x90, 0x00};
        byte[] apduResponseCaseTwo = new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0xA7,
                0x21, (byte) 0xC2, 0x2E, (byte) 0x90, 0x00};
        byte[] apduResponseCaseThree =
                new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, (byte) 0x90, 0x00};

        byte[] sessionSignature = new byte[] {(byte) 0x4D, (byte) 0xBD, (byte) 0xC9, 0x60};
        byte[] sessionSignatureCaseTwo = new byte[] {(byte) 0xA7, 0x21, (byte) 0xC2, 0x2E};

        {// Case Length = 4
            CloseSessionRespPars pars =
                    new CloseSessionRespPars(new ApduResponse(apduResponse, true));
            Assert.assertArrayEquals(sessionSignature, pars.getSignatureLo());
        }

        {// Case Length = 8
            CloseSessionRespPars pars =
                    new CloseSessionRespPars(new ApduResponse(apduResponseCaseTwo, true));
            Assert.assertArrayEquals(sessionSignatureCaseTwo, pars.getSignatureLo());
        }

        {// Case Other
            CloseSessionRespPars pars =
                    new CloseSessionRespPars(new ApduResponse(apduResponseCaseThree, true));
            Assert.assertEquals(0, pars.getSignatureLo().length);
        }
    }

    @Test
    public void existingTestConverted() {
        CloseSessionRespPars parser = new CloseSessionRespPars(new ApduResponse("9000h"));
        // This assert wasn't passing
        Assert.assertEquals("", Hex.encodeHexString(parser.getSignatureLo()));
        Assert.assertEquals("", Hex.encodeHexString(parser.getPostponedData()));
    }

    @Test // Calypso / page 105 / Example command aborting a session:
    public void abortingASession() {
        CloseSessionRespPars parser = new CloseSessionRespPars(new ApduResponse("FEDCBA98 9000h"));
    }

    @Test // Calypso / page 105 / Example command, Lc=4, without postponed data:
    public void lc4withoutPostponedData() {
        CloseSessionRespPars parser = new CloseSessionRespPars(new ApduResponse("FEDCBA98 9000h"));
        Assert.assertEquals("fedcba98", Hex.encodeHexString(parser.getSignatureLo()));
        Assert.assertEquals("", Hex.encodeHexString(parser.getPostponedData()));
    }

    @Test // Calypso / page 105 / Example command, Lc=4, with postponed data:
    public void lc4WithPostponedData() {
        CloseSessionRespPars parser =
                new CloseSessionRespPars(new ApduResponse("04 345678 FEDCBA98 9000h"));
        Assert.assertEquals("fedcba98", Hex.encodeHexString(parser.getSignatureLo()));
        Assert.assertEquals("04345678", Hex.encodeHexString(parser.getPostponedData()));
    }
}

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
import org.keyple.calypso.commands.po.parser.CloseSessionRespPars;
import org.keyple.commands.AbstractApduResponseParser;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ByteBufferUtils;
import org.keyple.seproxy.SeResponseSet;

public class CloseSessionRespParsTest {

    @Test
    public void closeSessionRespPars() { // by ixxi
        byte[] response = {0x4D, (byte) 0xBD, (byte) 0xC9, 0x60, (byte) 0x90, 0x00};
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(response, true);
        listeResponse.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(true, null, listeResponse);

        AbstractApduResponseParser apduResponseParser =
                new CloseSessionRespPars(seResponse.getApduResponses().get(0));
        byte[] reponseActual = apduResponseParser.getApduResponse().getBytes();
        Assert.assertArrayEquals(response, reponseActual);
    }

    @Test
    public void TestToPOHalfSessionSignature() { // by ixxi from ResponseUtilsTest

        ByteBuffer apduResponse = ByteBuffer
                .wrap(new byte[] {(byte) 0x4D, (byte) 0xBD, (byte) 0xC9, 0x60, (byte) 0x90, 0x00});
        ByteBuffer apduResponseCaseTwo = ByteBuffer.wrap(new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3,
                0x3E, (byte) 0xA7, 0x21, (byte) 0xC2, 0x2E, (byte) 0x90, 0x00});
        ByteBuffer apduResponseCaseThree =
                ByteBuffer.wrap(new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, (byte) 0x90, 0x00});

        ByteBuffer sessionSignature =
                ByteBuffer.wrap(new byte[] {(byte) 0x4D, (byte) 0xBD, (byte) 0xC9, 0x60});
        ByteBuffer sessionSignatureCaseTwo =
                ByteBuffer.wrap(new byte[] {(byte) 0xA7, 0x21, (byte) 0xC2, 0x2E});

        {// Case Length = 4
            CloseSessionRespPars pars =
                    new CloseSessionRespPars(new ApduResponse(apduResponse, true));
            Assert.assertEquals(sessionSignature, pars.getSignatureLo());
        }

        {// Case Length = 8
            CloseSessionRespPars pars =
                    new CloseSessionRespPars(new ApduResponse(apduResponseCaseTwo, true));
            Assert.assertEquals(sessionSignatureCaseTwo, pars.getSignatureLo());
        }

        {// Case Other
            CloseSessionRespPars pars =
                    new CloseSessionRespPars(new ApduResponse(apduResponseCaseThree, true));
            Assert.assertEquals("", ByteBufferUtils.toHex(pars.getSignatureLo()));
        }
    }

    @Test
    public void existingTestConverted() {
        CloseSessionRespPars parser =
                new CloseSessionRespPars(new ApduResponse(ByteBufferUtils.fromHex("9000h"), true));
        // This assert wasn't passing
        Assert.assertEquals("", ByteBufferUtils.toHex(parser.getSignatureLo()));
        Assert.assertEquals("", ByteBufferUtils.toHex(parser.getPostponedData()));
    }

    @Test // Calypso / page 105 / Example command aborting a session:
    public void abortingASession() {
        CloseSessionRespPars parser = new CloseSessionRespPars(
                new ApduResponse(ByteBufferUtils.fromHex("FEDCBA98 9000h"), true));
    }

    @Test // Calypso / page 105 / Example command, Lc=4, without postponed data:
    public void lc4withoutPostponedData() {
        CloseSessionRespPars parser = new CloseSessionRespPars(
                new ApduResponse(ByteBufferUtils.fromHex("FEDCBA98 9000h"), true));
        Assert.assertEquals("FEDCBA98", ByteBufferUtils.toHex(parser.getSignatureLo()));
        Assert.assertEquals("", ByteBufferUtils.toHex(parser.getPostponedData()));
    }

    @Test // Calypso / page 105 / Example command, Lc=4, with postponed data:
    public void lc4WithPostponedData() {
        CloseSessionRespPars parser = new CloseSessionRespPars(
                new ApduResponse(ByteBufferUtils.fromHex("04 345678 FEDCBA98 9000h"), true));
        Assert.assertEquals("FEDCBA98", ByteBufferUtils.toHex(parser.getSignatureLo()));
        Assert.assertEquals("04345678", ByteBufferUtils.toHex(parser.getPostponedData()));
    }
}

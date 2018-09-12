/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.parser.session;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.InconsistentParameterValueException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloseSessionRespParsTest {

    @Test
    public void closeSessionRespPars() throws InconsistentParameterValueException {
        byte[] response = {0x4D, (byte) 0xBD, (byte) 0xC9, 0x60, (byte) 0x90, 0x00};
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(ByteBuffer.wrap(response), null);
        responses.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(new SeResponse(true, null,
                new ApduResponse(ByteBufferUtils.fromHex("9000"), null), responses));

        AbstractApduResponseParser apduResponseParser =
                new CloseSessionRespPars(seResponse.getSingleResponse().getApduResponses().get(0));
        Assert.assertArrayEquals(response,
                ByteBufferUtils.toBytes(apduResponseParser.getApduResponse().getBytes()));
    }

    @Test
    public void TestToPOHalfSessionSignature() {

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
                    new CloseSessionRespPars(new ApduResponse(apduResponse, null));
            Assert.assertEquals(sessionSignature, pars.getSignatureLo());
        }

        {// Case Length = 8
            CloseSessionRespPars pars =
                    new CloseSessionRespPars(new ApduResponse(apduResponseCaseTwo, null));
            Assert.assertEquals(sessionSignatureCaseTwo, pars.getSignatureLo());
        }

        {// Case Other
            CloseSessionRespPars pars =
                    new CloseSessionRespPars(new ApduResponse(apduResponseCaseThree, null));
            Assert.assertEquals("", ByteBufferUtils.toHex(pars.getSignatureLo()));
        }
    }

    @Test
    public void existingTestConverted() {
        CloseSessionRespPars parser =
                new CloseSessionRespPars(new ApduResponse(ByteBufferUtils.fromHex("9000h"), null));
        // This assert wasn't passing
        Assert.assertEquals("", ByteBufferUtils.toHex(parser.getSignatureLo()));
        Assert.assertEquals("", ByteBufferUtils.toHex(parser.getPostponedData()));
    }

    @Test // Calypso / page 105 / Example command aborting a session:
    public void abortingASession() {
        CloseSessionRespPars parser = new CloseSessionRespPars(
                new ApduResponse(ByteBufferUtils.fromHex("FEDCBA98 9000h"), null));
    }

    @Test // Calypso / page 105 / Example command, Lc=4, without postponed data:
    public void lc4withoutPostponedData() {
        CloseSessionRespPars parser = new CloseSessionRespPars(
                new ApduResponse(ByteBufferUtils.fromHex("FEDCBA98 9000h"), null));
        Assert.assertEquals("FEDCBA98", ByteBufferUtils.toHex(parser.getSignatureLo()));
        Assert.assertEquals("", ByteBufferUtils.toHex(parser.getPostponedData()));
    }

    @Test // Calypso / page 105 / Example command, Lc=4, with postponed data:
    public void lc4WithPostponedData() {
        CloseSessionRespPars parser = new CloseSessionRespPars(
                new ApduResponse(ByteBufferUtils.fromHex("04 345678 FEDCBA98 9000h"), null));
        Assert.assertEquals("FEDCBA98", ByteBufferUtils.toHex(parser.getSignatureLo()));
        Assert.assertEquals("04345678", ByteBufferUtils.toHex(parser.getPostponedData()));
    }
}

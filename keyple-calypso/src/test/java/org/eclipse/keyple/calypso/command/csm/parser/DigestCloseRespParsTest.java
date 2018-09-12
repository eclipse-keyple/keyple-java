/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.csm.parser;

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
public class DigestCloseRespParsTest {

    @Test
    public void digestCloseRespPars() throws InconsistentParameterValueException {
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(
                ByteBuffer
                        .wrap(new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0x90, 0x00}),
                null);
        responses.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(new SeResponse(true, null,
                new ApduResponse(ByteBufferUtils.fromHex("9000"), null), responses));

        AbstractApduResponseParser apduResponseParser =
                new DigestCloseRespPars(seResponse.getSingleResponse().getApduResponses().get(0));
        ByteBuffer responseActual = apduResponseParser.getApduResponse().getBytes();
        Assert.assertEquals(
                ByteBuffer
                        .wrap(new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0x90, 0x00}),
                responseActual);
    }
}

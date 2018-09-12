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
public class PoGetChallengeRespParsTest {

    @Test
    public void POGetChallengeRespPars() throws InconsistentParameterValueException {
        byte[] response = {0x03, 0x0D, 0x0E, (byte) 0xFA, (byte) 0x9C, (byte) 0x8C, (byte) 0xB7,
                0x27, (byte) 0x90, 0x00};
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(ByteBuffer.wrap(response), null);
        responses.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(new SeResponse(true, null,
                new ApduResponse(ByteBufferUtils.fromHex("9000"), null), responses));

        AbstractApduResponseParser apduResponseParser = new PoGetChallengeRespPars(
                seResponse.getSingleResponse().getApduResponses().get(0));
        Assert.assertArrayEquals(response,
                ByteBufferUtils.toBytes(apduResponseParser.getApduResponse().getBytes()));
        Assert.assertEquals("Success", apduResponseParser.getStatusInformation());
    }
}

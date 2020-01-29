/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.sam.parser;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestCloseRespPars;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestCloseRespParsTest {

    @Test
    public void digestCloseRespPars() {
        List<ApduResponse> responses = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(
                new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0x90, 0x00}, null);
        responses.add(apduResponse);
        SeResponse seResponse =
                new SeResponse(true, true,
                        new SelectionStatus(null,
                                new ApduResponse(ByteArrayUtil.fromHex("9000"), null), true),
                        responses);

        AbstractApduResponseParser apduResponseParser =
                new DigestCloseRespPars(seResponse.getApduResponses().get(0));
        byte[] responseActual = apduResponseParser.getApduResponse().getBytes();
        Assert.assertArrayEquals(
                new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0x90, 0x00},
                responseActual);
    }
}

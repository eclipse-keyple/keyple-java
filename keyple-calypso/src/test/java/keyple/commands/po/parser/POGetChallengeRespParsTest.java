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
import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.po.parser.PoGetChallengeRespPars;
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;

public class POGetChallengeRespParsTest {

    @Test
    public void POGetChallengetRespPars() {
        byte[] response = {0x03, 0x0D, 0x0E, (byte) 0xFA, (byte) 0x9C, (byte) 0x8C, (byte) 0xB7,
                0x27, (byte) 0x90, 0x00};
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse =
                new ApduResponse(response, true, new byte[] {(byte) 0x90, 0x00});
        listeResponse.add(apduResponse);
        SeResponse seResponse = new SeResponse(true, null, listeResponse);

        ApduResponseParser apduResponseParser =
                new PoGetChallengeRespPars(seResponse.getApduResponses().get(0));
        byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
        Assert.assertArrayEquals(response, reponseActual);
        Assert.assertEquals("Successful execution.", apduResponseParser.getStatusInformation());

    }
}

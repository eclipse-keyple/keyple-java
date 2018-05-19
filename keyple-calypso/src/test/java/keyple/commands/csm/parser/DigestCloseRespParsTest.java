/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.csm.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.csm.parser.DigestCloseRespPars;
import org.keyple.commands.AbstractApduResponseParser;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponseSet;

public class DigestCloseRespParsTest {

    @Test
    public void digestCloseRespPars() {
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(
                new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0x90, 0x00}, true);
        listeResponse.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(true, null, listeResponse);

        AbstractApduResponseParser apduResponseParser =
                new DigestCloseRespPars(seResponse.getApduResponses().get(0));
        ByteBuffer reponseActual = apduResponseParser.getApduResponse().getBuffer();
        Assert.assertEquals(
                ByteBuffer
                        .wrap(new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0x90, 0x00}),
                reponseActual);
    }
}

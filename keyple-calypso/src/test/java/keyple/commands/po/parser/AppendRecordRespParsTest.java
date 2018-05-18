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
import org.keyple.calypso.commands.po.parser.AppendRecordRespPars;
import org.keyple.commands.AbstractApduResponseParser;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponseSet;

public class AppendRecordRespParsTest {

    @Test
    public void appendRecordRespPars() {
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(new byte[] {90, 00}, true);
        listeResponse.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(true, null, listeResponse);

        AbstractApduResponseParser apduResponseParser =
                new AppendRecordRespPars(seResponse.getApduResponses().get(0));
        byte[] reponseActual = apduResponseParser.getApduResponse().getBytes();
        Assert.assertArrayEquals(new byte[] {90, 00}, reponseActual);
    }
}

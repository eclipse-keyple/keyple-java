/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.commands.po.parser;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.commands.po.parser.AppendRecordRespPars;
import org.eclipse.keyple.commands.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.junit.Assert;
import org.junit.Test;

public class AppendRecordRespParsTest {

    @Test
    public void appendRecordRespPars() {
        List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
        ApduResponse apduResponse = new ApduResponse(new byte[] {90, 00}, true);
        listeResponse.add(apduResponse);
        SeResponseSet seResponse = new SeResponseSet(new SeResponse(true, null, listeResponse));

        AbstractApduResponseParser apduResponseParser =
                new AppendRecordRespPars(seResponse.getSingleElement().getApduResponses().get(0));
        Assert.assertArrayEquals(new byte[] {90, 00},
                ByteBufferUtils.toBytes(apduResponseParser.getApduResponse().getBuffer()));
    }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.csm.parser;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.csm.parser.SelectDiversifierRespPars;
import org.keyple.commands.AbstractApduResponseParser;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponseSet;
import org.mockito.Mockito;

public class SelectDiversifierRespParsTest {

    @Test
    public void selectDiversifierResp() {
        List<ApduResponse> list = new ArrayList<ApduResponse>();
        List<ApduResponse> list1 = new ArrayList<ApduResponse>();
        List<ApduResponse> list2 = new ArrayList<ApduResponse>();

        SeResponseSet seResponse = Mockito.mock(SeResponseSet.class);

        ApduResponse apduResponse = new ApduResponse(null, true, new byte[] {(byte) 0x90, 0x00});
        ApduResponse apduResponse1 = new ApduResponse(null, true, new byte[] {(byte) 0x80, 0x00});
        ApduResponse apduResponse2 = new ApduResponse(null, true, null);

        list.add(apduResponse);
        list1.add(apduResponse1);
        list2.add(apduResponse2);

        Mockito.when(seResponse.getApduResponses()).thenReturn(list);
        AbstractApduResponseParser apduResponseParser =
                new SelectDiversifierRespPars(seResponse.getApduResponses().get(0));

        Assert.assertEquals(0x9000, apduResponseParser.getApduResponse().getStatusCode());

        Mockito.when(seResponse.getApduResponses()).thenReturn(list1);
        apduResponseParser = new SelectDiversifierRespPars(seResponse.getApduResponses().get(0));

        Assert.assertThat(apduResponseParser.getApduResponse().getStatusCode(), IsNot.not(0x9000));
        apduResponseParser = new SelectDiversifierRespPars(seResponse.getApduResponses().get(0));

    }
}

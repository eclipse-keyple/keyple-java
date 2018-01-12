package org.keyple.commands.calypso.csm.parser;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.commands.calypso.csm.parser.SelectDiversifierRespPars;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;
import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InvalidApduReaderException;
import org.keyple.seproxy.exceptions.TimeoutReaderException;
import org.keyple.seproxy.exceptions.UnexpectedReaderException;
import org.mockito.Mockito;

public class SelectDiversifierRespParsTest {

    @Test
    public void selectDiviersifierResp() throws IOReaderException, UnexpectedReaderException,
            ChannelStateReaderException, InvalidApduReaderException, TimeoutReaderException {
        List<ApduResponse> list = new ArrayList<ApduResponse>();
        List<ApduResponse> list1 = new ArrayList<ApduResponse>();
        List<ApduResponse> list2 = new ArrayList<ApduResponse>();

        SeResponse seResponse = Mockito.mock(SeResponse.class);

        ApduResponse apduResponse = new ApduResponse(null, true, new byte[] { 90, 00 });
        ApduResponse apduResponse1 = new ApduResponse(null, true, new byte[] { 80, 00 });
        ApduResponse apduResponse2 = new ApduResponse(null, true, null);

        list.add(apduResponse);
        list1.add(apduResponse1);
        list2.add(apduResponse2);

        Mockito.when(seResponse.getApduResponses()).thenReturn(list);
        ApduResponseParser apduResponseParser = new SelectDiversifierRespPars(seResponse.getApduResponses().get(0));

        Assert.assertArrayEquals(new byte[] { 90, 00 }, apduResponseParser.getApduResponse().getStatusCode());

        Mockito.when(seResponse.getApduResponses()).thenReturn(list1);
        apduResponseParser = new SelectDiversifierRespPars(seResponse.getApduResponses().get(0));

        Assert.assertThat(apduResponseParser.getApduResponse().getStatusCode(), IsNot.not(new byte[] { 90, 00 }));
        apduResponseParser = new SelectDiversifierRespPars(seResponse.getApduResponses().get(0));

    }
}
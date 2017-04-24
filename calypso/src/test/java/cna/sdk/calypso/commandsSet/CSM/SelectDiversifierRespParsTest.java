package cna.sdk.calypso.commandsSet.CSM;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.csm.parser.SelectDiversifierRespPars;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.ReaderException;
import cna.sdk.seproxy.SEResponse;

public class SelectDiversifierRespParsTest {

	@Test
	public void selectDiviersifierResp() throws ReaderException {
		List<APDUResponse> list = new ArrayList<>();
		List<APDUResponse> list1 = new ArrayList<>();
		List<APDUResponse> list2 = new ArrayList<>();

		SEResponse seResponse = Mockito.mock(SEResponse.class);
		
		APDUResponse apduResponse = new APDUResponse(null, true, new byte[] { 90, 00 });
		APDUResponse apduResponse1 = new APDUResponse(null, true, new byte[] { 80, 00 });
		APDUResponse apduResponse2 = new APDUResponse(null, true, null);

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
package cna.sdk.calypso.commandsSet.CSM;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.enumTagUtils;
import cna.sdk.calypso.commandset.csm.builder.SelectDiversifierCmdBuild;
import cna.sdk.calypso.commandset.dto.FCI;
import cna.sdk.calypso.commandset.po.PoCommandBuilder;
import cna.sdk.calypso.commandset.po.builder.GetDataFciCmdBuild;
import cna.sdk.seproxy.APDURequest;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.ProxyReader;
import cna.sdk.seproxy.ReaderException;
import cna.sdk.seproxy.SERequest;
import cna.sdk.seproxy.SEResponse;

public class SelectDiversiferCmdBuildTest {

	Logger logger = LoggerFactory.getLogger(SelectDiversiferCmdBuildTest.class);

	private byte[] dataIn;

	private ProxyReader fakeSpecificReader;

	private List<APDURequest> apduRequests = new ArrayList<>();

	private SERequest seRequest;

	private List<APDUResponse> apduResponses;

	private SEResponse seResponseExpected;

	private byte[] returnOK = { (byte) 0x90, 0x00 };

	private APDUResponse responseExpected = new APDUResponse(null, true, returnOK);

	private ApduCommandBuilder apduCommandBuilder = new GetDataFciCmdBuild(PoCommandBuilder.defaultRevision);

	private List<APDUResponse> list = new ArrayList<>();

	private List<APDURequest> apduRequests2 = new ArrayList<>();

	@Test
	public void selectDiviersifier() throws ReaderException {

		APDURequest apdu = apduCommandBuilder.getApduRequest();
		apduRequests.add(apdu);

		seRequest = new SERequest(null, true, apduRequests);
		list.add(new APDUResponse(
				new byte[] { 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5,
						0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
						(byte) 0x9A, (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 },
				true, new byte[] { (byte) 0x90, 0x00 }));
		list.add(new APDUResponse(
				new byte[] { 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5,
						0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00, 0x27, 0x4A,
						(byte) 0x9A, (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 },
				true, new byte[] { (byte) 0x80, 0x00 }));

		SEResponse seResponse = new SEResponse(true, null, list);

		SEResponse responseFci = Mockito.mock(SEResponse.class);
		fakeSpecificReader = Mockito.mock(ProxyReader.class);

		Mockito.when(responseFci.getApduResponses()).thenReturn(list);
		Mockito.when(fakeSpecificReader.transmit(seRequest)).thenReturn(seResponse);

		FCI fci = ResponseUtils.toFCI(responseFci.getApduResponses().get(0).getbytes());
		dataIn = fci.getApplicationSN();

		ApduCommandBuilder apduCommandBuilder2 = new SelectDiversifierCmdBuild(null, dataIn);
		APDURequest apdurequest = apduCommandBuilder2.getApduRequest();

		apduRequests2.add(apdurequest);

		apduResponses = new ArrayList<>();
		apduResponses.add(responseExpected);

		seResponseExpected = new SEResponse(true, responseExpected, apduResponses);
		SERequest seRequest2 = new SERequest(null, true, apduRequests2);

		Mockito.when(fakeSpecificReader.transmit(seRequest2)).thenReturn(seResponse);
		SEResponse seResponse1 = fakeSpecificReader.transmit(seRequest2);

		Assert.assertArrayEquals(seResponseExpected.getApduResponses().get(0).getStatusCode(),
				seResponse1.getApduResponses().get(0).getStatusCode());

		Assert.assertThat(seResponseExpected.getApduResponses().get(0).getStatusCode(),
				IsNot.not(IsEqual.equalTo(seResponse1.getApduResponses().get(1).getStatusCode())));
	}
}

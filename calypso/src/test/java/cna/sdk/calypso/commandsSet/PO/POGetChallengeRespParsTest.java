package cna.sdk.calypso.commandsSet.PO;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.po.parser.POGetChallengeRespPars;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.SEResponse;

public class POGetChallengeRespParsTest {

	@Test
	public void POGetChallengetRespPars() {
		byte[] response = { 0x03, 0x0D, 0x0E, (byte) 0xFA, (byte) 0x9C, (byte) 0x8C, (byte) 0xB7, 0x27, (byte) 0x90,
				0x00 };
		List<APDUResponse> listeResponse = new ArrayList<>();
		APDUResponse apduResponse = new APDUResponse(response, true, new byte[] { (byte) 0x90, 0x00 });
		listeResponse.add(apduResponse);
		SEResponse seResponse = new SEResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new POGetChallengeRespPars(seResponse.getApduResponses().get(0));
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(response, reponseActual);
		Assert.assertEquals("Successful execution.", apduResponseParser.getStatusInformation());

	}
}

package org.keyple.commands.calypso.po.parser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.commands.calypso.po.parser.PoGetChallengeRespPars;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;

public class POGetChallengeRespParsTest {

	@Test
	public void POGetChallengetRespPars() {
		byte[] response = { 0x03, 0x0D, 0x0E, (byte) 0xFA, (byte) 0x9C, (byte) 0x8C, (byte) 0xB7, 0x27, (byte) 0x90,
				0x00 };
		List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
		ApduResponse apduResponse = new ApduResponse(response, true, new byte[] { (byte) 0x90, 0x00 });
		listeResponse.add(apduResponse);
		SeResponse seResponse = new SeResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new PoGetChallengeRespPars(seResponse.getApduResponses().get(0));
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(response, reponseActual);
		Assert.assertEquals("Successful execution.", apduResponseParser.getStatusInformation());

	}
}

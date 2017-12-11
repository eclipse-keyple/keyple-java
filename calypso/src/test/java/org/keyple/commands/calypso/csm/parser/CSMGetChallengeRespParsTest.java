package org.keyple.commands.calypso.csm.parser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.commands.calypso.csm.parser.CsmGetChallengeRespPars;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;

public class CSMGetChallengeRespParsTest {

	@Test
	public void getChallengeRespPars() {
		List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
		ApduResponse apduResponse = new ApduResponse(
				new byte[] { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0x90, 0x00 }, true,
				new byte[] { (byte) 0x90, 0x00 });
		listeResponse.add(apduResponse);
		SeResponse seResponse = new SeResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new CsmGetChallengeRespPars(seResponse.getApduResponses().get(0));
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(new byte[] { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0x90, 0x00 }, reponseActual);
	}
}
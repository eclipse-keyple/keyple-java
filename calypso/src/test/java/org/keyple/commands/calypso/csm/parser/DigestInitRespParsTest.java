package org.keyple.commands.calypso.csm.parser;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.commands.calypso.csm.parser.DigestInitRespPars;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;

public class DigestInitRespParsTest {

	@Test
	public void digestInitRespPars() {
		List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
		ApduResponse apduResponse = new ApduResponse(new byte[] { 90, 00 }, true, new byte[] { 90, 00 });
		listeResponse.add(apduResponse);
		SeResponse seResponse = new SeResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new DigestInitRespPars(seResponse.getApduResponses().get(0));
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(new byte[] { 90, 00 }, reponseActual);
	}
}

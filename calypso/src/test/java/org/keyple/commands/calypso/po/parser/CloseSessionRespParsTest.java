package org.keyple.commands.calypso.po.parser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduResponseParser;
import org.keyple.commands.calypso.po.parser.CloseSessionRespPars;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.SeResponse;

public class CloseSessionRespParsTest {

	@Test
	public void closeSessionRespPars() {
		byte[] response = { 0x4D, (byte) 0xBD, (byte) 0xC9, 0x60, (byte) 0x90, 0x00 };
		List<ApduResponse> listeResponse = new ArrayList<ApduResponse>();
		ApduResponse apduResponse = new ApduResponse(response, true, new byte[] { 90, 00 });
		listeResponse.add(apduResponse);
		SeResponse seResponse = new SeResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new CloseSessionRespPars(seResponse.getApduResponses().get(0));
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(response, reponseActual);

	}
}

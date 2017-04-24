package cna.sdk.calypso.commandsSet.PO;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.po.parser.CloseSessionRespPars;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.SEResponse;

public class CloseSessionRespParsTest {

	@Test
	public void closeSessionRespPars() {
		byte[] response = { 0x4D, (byte) 0xBD, (byte) 0xC9, 0x60, (byte) 0x90, 0x00 };
		List<APDUResponse> listeResponse = new ArrayList<>();
		APDUResponse apduResponse = new APDUResponse(response, true, new byte[] { 90, 00 });
		listeResponse.add(apduResponse);
		SEResponse seResponse = new SEResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new CloseSessionRespPars(seResponse.getApduResponses().get(0));
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(response, reponseActual);

	}
}

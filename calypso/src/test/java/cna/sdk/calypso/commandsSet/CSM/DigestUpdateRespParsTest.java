package cna.sdk.calypso.commandsSet.CSM;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.csm.parser.DigestUpdateRespPars;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.SEResponse;

public class DigestUpdateRespParsTest {

	@Test
	public void digestUpdateRespPars() {
		List<APDUResponse> listeResponse = new ArrayList<>();
		APDUResponse apduResponse = new APDUResponse(new byte[] { 90, 00 }, true, new byte[] { 90, 00 });
		listeResponse.add(apduResponse);
		SEResponse seResponse = new SEResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new DigestUpdateRespPars(seResponse.getApduResponses().get(0));
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(new byte[] { 90, 00 }, reponseActual);
	}
}

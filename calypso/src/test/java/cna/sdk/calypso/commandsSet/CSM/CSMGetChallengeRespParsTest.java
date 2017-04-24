package cna.sdk.calypso.commandsSet.CSM;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.csm.parser.CsmGetChallengeRespPars;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.SEResponse;

public class CSMGetChallengeRespParsTest {

	@Test
	public void getChallengeRespPars() {
		List<APDUResponse> listeResponse = new ArrayList<>();
		APDUResponse apduResponse = new APDUResponse(
				new byte[] { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0x90, 0x00 }, true,
				new byte[] { (byte) 0x90, 0x00 });
		listeResponse.add(apduResponse);
		SEResponse seResponse = new SEResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new CsmGetChallengeRespPars(seResponse.getApduResponses().get(0));
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(new byte[] { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E, (byte) 0x90, 0x00 }, reponseActual);
	}
}
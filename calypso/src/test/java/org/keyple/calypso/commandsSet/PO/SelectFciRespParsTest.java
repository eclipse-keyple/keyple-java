package org.keyple.calypso.commandsSet.PO;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduResponseParser;
import org.keyple.calypso.commandset.po.parser.SelectFciRespPars;
import org.keyple.seproxy.APDUResponse;
import org.keyple.seproxy.SEResponse;

public class SelectFciRespParsTest {

	@Test
	public void selectFciRespPars() {
		List<APDUResponse> listeResponse = new ArrayList<>();
		APDUResponse apduResponse = new APDUResponse(new byte[] { 90, 00 }, true, new byte[] { 90, 00 });
		listeResponse.add(apduResponse);
		SEResponse seResponse = new SEResponse(true, null, listeResponse);

		ApduResponseParser apduResponseParser = new SelectFciRespPars(seResponse.getApduResponses().get(0));
		byte[] reponseActual = apduResponseParser.getApduResponse().getbytes();
		Assert.assertArrayEquals(new byte[] { 90, 00 }, reponseActual);
	}
}

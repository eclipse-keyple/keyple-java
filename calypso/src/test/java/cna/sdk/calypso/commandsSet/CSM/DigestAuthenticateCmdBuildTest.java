package cna.sdk.calypso.commandsSet.CSM;

import org.junit.Assert;
import org.junit.Test;
import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.csm.builder.DigestAuthenticateCmdBuild;
import cna.sdk.seproxy.APDURequest;
import cna.sdk.seproxy.ReaderException;

public class DigestAuthenticateCmdBuildTest {

	@Test
	public void digestAuthenticate() throws ReaderException {

		byte[] signaturePO = { 0x00, 0x01, 0x02, 0x03 };
		byte[] request = { (byte) 0x80, (byte) 0x82, 0x00, 0x00, 0x04, 0x00, 0x01, 0x02, 0x03 };

		ApduCommandBuilder apduCommandBuilder = new DigestAuthenticateCmdBuild(null, signaturePO);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

	}
}

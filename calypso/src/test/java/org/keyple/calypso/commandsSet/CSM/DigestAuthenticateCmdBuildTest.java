package org.keyple.calypso.commandsSet.CSM;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.csm.builder.DigestAuthenticateCmdBuild;
import org.keyple.seproxy.APDURequest;
import org.keyple.seproxy.exceptions.ReaderException;

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

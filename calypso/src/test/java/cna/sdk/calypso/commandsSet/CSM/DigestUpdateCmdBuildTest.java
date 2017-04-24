package cna.sdk.calypso.commandsSet.CSM;

import org.junit.Assert;
import org.junit.Test;
import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.csm.builder.DigestUpdateCmdBuild;
import cna.sdk.seproxy.APDURequest;

public class DigestUpdateCmdBuildTest {

	@Test
	public void digestUpdateCmdBuild() {
		byte[] digestDAta = { (byte) 0x94, (byte) 0xAE, 0x01, 0x02 };
		byte[] request = { (byte) 0x80, (byte) 0x8C, 0x00, 0x00, 0x04, (byte) 0x94, (byte) 0xAE, 0x01, 0x02 };

		ApduCommandBuilder apduCommandBuilder = new DigestUpdateCmdBuild(null, true, digestDAta);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());
	}
}

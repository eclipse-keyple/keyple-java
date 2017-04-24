package cna.sdk.calypso.commandsSet.CSM;

import org.junit.Assert;
import org.junit.Test;
import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.csm.CsmRevision;
import cna.sdk.calypso.commandset.csm.builder.DigestCloseCmdBuild;
import cna.sdk.seproxy.APDURequest;

public class DigestCloseCmdBuildTest {

	@Test
	public void digestCloseCmdBuild() {

		byte[] request = { (byte) 0x80, (byte) 0x8E, 0x00, 0x00, (byte) 0x08 };
		ApduCommandBuilder apduCommandBuilder = new DigestCloseCmdBuild(CsmRevision.C1);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

		byte[] request1 = { (byte) 0x80, (byte) 0x8E, 0x00, 0x00, (byte) 0x04 };
		apduCommandBuilder = new DigestCloseCmdBuild(CsmRevision.C1);
		apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request1, apduRequest.getbytes());

	}
}
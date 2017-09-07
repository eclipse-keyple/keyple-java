package org.keyple.calypso.commandsSet.CSM;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.csm.CsmRevision;
import org.keyple.calypso.commandset.csm.builder.DigestCloseCmdBuild;
import org.keyple.seproxy.APDURequest;

public class DigestCloseCmdBuildTest {

	@Test
	public void digestCloseCmdBuild() {

		byte[] request = { (byte) 0x80, (byte) 0x8E, 0x00, 0x00, (byte) 0x08 };
		ApduCommandBuilder apduCommandBuilder = new DigestCloseCmdBuild(CsmRevision.S1D);//94
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

		byte[] request1 = { (byte) 0x80, (byte) 0x8E, 0x00, 0x00, (byte) 0x04 };
		apduCommandBuilder = new DigestCloseCmdBuild(CsmRevision.S1D);//94
		apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request1, apduRequest.getbytes());

	}
}
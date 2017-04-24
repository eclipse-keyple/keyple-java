package cna.sdk.calypso.commandsSet.CSM;

import org.junit.Assert;
import org.junit.Test;

import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.csm.CsmRevision;
import cna.sdk.calypso.commandset.csm.builder.CsmGetChallengeCmdBuild;
import cna.sdk.seproxy.APDURequest;

public class CSMGetChallengeCmdBuildTest {

	@Test
	public void getChallengeCmdBuild() {

		byte[] request = { (byte) 0x80, (byte) 0x84, 0x00, 0x00, 0x04 };

		ApduCommandBuilder apduCommandBuilder = new CsmGetChallengeCmdBuild(CsmRevision.C1);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

	}
}

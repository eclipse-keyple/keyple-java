package org.keyple.calypso.commandsSet.CSM;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.csm.CsmRevision;
import org.keyple.calypso.commandset.csm.builder.CsmGetChallengeCmdBuild;
import org.keyple.seproxy.APDURequest;

public class CSMGetChallengeCmdBuildTest {

	@Test
	public void getChallengeCmdBuild() {

		byte[] request = { (byte) 0x80, (byte) 0x84, 0x00, 0x00, 0x04 };

		ApduCommandBuilder apduCommandBuilder = new CsmGetChallengeCmdBuild(CsmRevision.S1D);//94
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

	}
}

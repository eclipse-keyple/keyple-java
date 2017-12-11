package org.keyple.commands.calypso.po.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduCommandBuilder;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.po.builder.PoGetChallengeCmdBuild;
import org.keyple.seproxy.ApduRequest;

public class POGetChallengeCmdBuildTest {

	@Test
	public void POGetChallenge_Rev2_4() {

		byte[] request = { (byte) 0x94, (byte) 0x84, 0x01, 0x10, 0x08 };

		ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.REV2_4);
		ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, ApduRequest.getbytes());

	}

	@Test
	public void POGetChallenge_Rev3_1() {

		byte[] request = { (byte) 0x00, (byte) 0x84, 0x01, 0x10, 0x08 };

		ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.REV3_1);
		ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, ApduRequest.getbytes());

	}

	@Test
	public void POGetChallenge_Rev3_2() {

		byte[] request = { (byte) 0x00, (byte) 0x84, 0x01, 0x10, 0x08 };

		ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.REV3_2);
		ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, ApduRequest.getbytes());

	}


}

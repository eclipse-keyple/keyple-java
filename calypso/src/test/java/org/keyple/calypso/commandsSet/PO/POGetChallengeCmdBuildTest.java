package org.keyple.calypso.commandsSet.PO;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.po.PoRevision;
import org.keyple.calypso.commandset.po.builder.PoGetChallengeCmdBuild;
import org.keyple.seproxy.APDURequest;

public class POGetChallengeCmdBuildTest {

	@Test
	public void POGetChallenge_Rev2_4() {

		byte[] request = { (byte) 0x94, (byte) 0x84, 0x01, 0x10, 0x08 };

		ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.CLASS_0x94);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

	}

	@Test
	public void POGetChallenge_Rev3_1() {

		byte[] request = { (byte) 0x00, (byte) 0x84, 0x01, 0x10, 0x08 };

		ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.CLASS_0x00);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

	}

	@Test
	public void POGetChallenge_Rev3_2() {

		byte[] request = { (byte) 0x00, (byte) 0x84, 0x01, 0x10, 0x08 };

		ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.CLASS_0x00);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

	}


}

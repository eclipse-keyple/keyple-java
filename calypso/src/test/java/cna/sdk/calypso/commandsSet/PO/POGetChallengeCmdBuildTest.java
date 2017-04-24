package cna.sdk.calypso.commandsSet.PO;

import org.junit.Assert;
import org.junit.Test;
import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.calypso.commandset.po.builder.PoGetChallengeCmdBuild;
import cna.sdk.seproxy.APDURequest;

public class POGetChallengeCmdBuildTest {

	@Test
	public void POGetChallenge_Rev2_4() {

		byte[] request = { (byte) 0x94, (byte) 0x84, 0x01, 0x10, 0x08 };

		ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.REV2_4);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

	}

	@Test
	public void POGetChallenge_Rev3_1() {

		byte[] request = { (byte) 0x00, (byte) 0x84, 0x01, 0x10, 0x08 };

		ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.REV3_1);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

	}

	@Test
	public void POGetChallenge_Rev3_2() {

		byte[] request = { (byte) 0x00, (byte) 0x84, 0x01, 0x10, 0x08 };

		ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.REV3_2);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());

	}


}

package org.keyple.calypso.commandsSet.PO;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.po.PoRevision;
import org.keyple.calypso.commandset.po.builder.OpenSessionCmdBuild;
import org.keyple.seproxy.APDURequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSessionCmdBuildTest {

	Logger logger = LoggerFactory.getLogger(OpenSessionCmdBuildTest.class);

	byte[] samChallenge = { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };

	ApduCommandBuilder apduCommandBuilder;

	APDURequest apduRequest;

	@Test
	public void openSessionCmdBuild_rev_2_4() {

		// revision 2.4
		byte[] request2_4 = { (byte) 0x94, (byte) 0x8A, (byte) 0x8B, 0x40, (byte) 0x04, (byte) 0xA8, 0x31, (byte) 0xC3,
				0x3E };
		apduCommandBuilder = new OpenSessionCmdBuild(PoRevision.CLASS_0x94, (byte) 0x03, samChallenge, (byte) 0x01,
				(byte) 0x08);
		apduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request2_4, apduRequest.getbytes());
	}

	@Test
	public void openSessionCmdBuild_rev_3_1() {
		// revision 3.1
		byte[] request3_1 = { (byte) 0x00, (byte) 0x8A, (byte) 0x0B, 0x41, (byte) 0x04, (byte) 0xA8, 0x31, (byte) 0xC3,
				0x3E };
		apduCommandBuilder = new OpenSessionCmdBuild(PoRevision.CLASS_0x00, (byte) 0x03, samChallenge, (byte) 0x01,
				(byte) 0x08);
		apduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request3_1, apduRequest.getbytes());
	}

	@Test
	public void openSessionCmdBuild_rev_3_2() {
		// revision 3.2
		byte[] request3_2 = { (byte) 0x00, (byte) 0x8A, (byte) 0x0B, 0x42, (byte) 0x04, (byte) 0xA8, 0x31, (byte) 0xC3,
				0x3E };
		apduCommandBuilder = new OpenSessionCmdBuild(PoRevision.CLASS_0x00, (byte) 0x03, samChallenge, (byte) 0x01,
				(byte) 0x08);
		apduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request3_2, apduRequest.getbytes());
	}



}
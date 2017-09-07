package org.keyple.calypso.commandsSet.CSM;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.csm.builder.DigestInitCmdBuild;
import org.keyple.calypso.commandset.dto.KIF;
import org.keyple.calypso.commandset.dto.KVC;
import org.keyple.seproxy.APDURequest;

public class DigestInitCmdBuildTest {

	@Test
	public void digestInitCmd() {
		byte[] digestData = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
		byte[] request = { (byte) 0x80, (byte) 0x8A, 0x00, (byte) 0xFF, 0x09, 0x30, 0x7E, 0x01, 0x02, 0x03, 0x04, 0x05,
				0x06, 0x07 };
		KIF kif = new KIF((byte) 0x30);
		KVC kvc = new KVC((byte) 0x7E);
		ApduCommandBuilder apduCommandBuilder = new DigestInitCmdBuild(null, kif, kvc, digestData);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request, apduRequest.getbytes());
	}
}

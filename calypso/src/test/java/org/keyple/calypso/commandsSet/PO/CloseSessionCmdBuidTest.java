package org.keyple.calypso.commandsSet.PO;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.po.PoRevision;
import org.keyple.calypso.commandset.po.builder.CloseSessionCmdBuild;
import org.keyple.seproxy.APDURequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseSessionCmdBuidTest {

	Logger logger = LoggerFactory.getLogger(CloseSessionCmdBuidTest.class);

	@Test
	public void closeSessionCmdBuild() {
		byte[] request2_4 = { (byte) 0x94, (byte) 0x8E, 0x00, 0x00, (byte) 0x04, (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };
		byte[] request3_1 = { (byte) 0x00, (byte) 0x8E, (byte) 0x80, 0x00, (byte) 0x04, (byte) 0xA8, 0x31, (byte) 0xC3,
				0x3E };
		byte[] terminalSessionSiganture = { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };
		ApduCommandBuilder apduCommandBuilder = new CloseSessionCmdBuild(PoRevision.REV2_4, false,
				terminalSessionSiganture);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request2_4, apduRequest.getbytes());

		apduCommandBuilder = new CloseSessionCmdBuild(PoRevision.REV3_1, true, terminalSessionSiganture);
		apduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request3_1, apduRequest.getbytes());
	}
}

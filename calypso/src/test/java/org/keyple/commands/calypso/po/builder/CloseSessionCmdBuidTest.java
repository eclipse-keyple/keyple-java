package org.keyple.commands.calypso.po.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduCommandBuilder;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.po.builder.CloseSessionCmdBuild;
import org.keyple.seproxy.ApduRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseSessionCmdBuidTest {

	Logger logger = LoggerFactory.getLogger(CloseSessionCmdBuidTest.class);

	@Test
	public void closeSessionCmdBuild() throws InconsistentCommandException {
		byte[] request2_4 = { (byte) 0x94, (byte) 0x8E, 0x00, 0x00, (byte) 0x04, (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };
		byte[] request3_1 = { (byte) 0x00, (byte) 0x8E, (byte) 0x80, 0x00, (byte) 0x04, (byte) 0xA8, 0x31, (byte) 0xC3,
				0x3E };
		byte[] terminalSessionSiganture = { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };
		ApduCommandBuilder apduCommandBuilder = new CloseSessionCmdBuild(PoRevision.REV2_4,false,terminalSessionSiganture);
		ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request2_4, ApduRequest.getbytes());

		apduCommandBuilder = new CloseSessionCmdBuild(PoRevision.REV3_1, true, terminalSessionSiganture);
		ApduRequest = apduCommandBuilder.getApduRequest();

		Assert.assertArrayEquals(request3_1, ApduRequest.getbytes());
	}
}

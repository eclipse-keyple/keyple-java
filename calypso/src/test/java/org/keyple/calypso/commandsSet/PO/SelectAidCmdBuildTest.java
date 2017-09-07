package org.keyple.calypso.commandsSet.PO;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.dto.AID;
import org.keyple.calypso.commandset.po.builder.SelectAidCmdBuild;
import org.keyple.seproxy.APDURequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectAidCmdBuildTest {

	Logger logger = LoggerFactory.getLogger(SelectAidCmdBuildTest.class);
	@Test
	public void selectAidCMdBuild() {
		AID aid = new AID(new byte[] { 0x00, 0x01, 0x02, 0x03 });
		byte[] request = { (byte) 0x94, (byte) 0xA4, (byte) 0x04, 0x00, 0x04, 0x00, 0x01, 0x02, 0x03 };
		ApduCommandBuilder apduCommandBuilder = new SelectAidCmdBuild(aid);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request, apduRequest.getbytes());
	}
}

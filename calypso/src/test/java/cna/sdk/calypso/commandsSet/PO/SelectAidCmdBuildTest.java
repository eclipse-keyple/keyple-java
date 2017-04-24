package cna.sdk.calypso.commandsSet.PO;

import org.junit.Assert;
import org.junit.Test;

import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.dto.AID;
import cna.sdk.calypso.commandset.po.builder.SelectAidCmdBuild;
import cna.sdk.seproxy.APDURequest;

public class SelectAidCmdBuildTest {

	@Test
	public void selectAidCMdBuild() {
		AID aid = new AID(new byte[] { 0x00, 0x01, 0x02, 0x03 });
		byte[] request = { (byte) 0x00, (byte) 0xA4, (byte) 0x04, 0x00, 0x04, 0x00, 0x01, 0x02, 0x03 };
		ApduCommandBuilder apduCommandBuilder = new SelectAidCmdBuild(aid);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request, apduRequest.getbytes());
	}
}

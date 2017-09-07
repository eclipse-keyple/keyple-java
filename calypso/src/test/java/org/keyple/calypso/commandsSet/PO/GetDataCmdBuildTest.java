package org.keyple.calypso.commandsSet.PO;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commandset.ApduCommandBuilder;
import org.keyple.calypso.commandset.enumTagUtils;
import org.keyple.calypso.commandset.po.PoCommandBuilder;
import org.keyple.calypso.commandset.po.builder.GetDataFciCmdBuild;
import org.keyple.seproxy.APDURequest;

public class GetDataCmdBuildTest {

	@Test
	public void getDataFCICmdBuild() {
		byte[] request = { (byte) 0x94, (byte) 0xCA, (byte) 0x00, 0x6F, (byte) 0x00 };
		ApduCommandBuilder apduCommandBuilder = new GetDataFciCmdBuild(PoCommandBuilder.defaultRevision);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request, apduRequest.getbytes());
	}
}
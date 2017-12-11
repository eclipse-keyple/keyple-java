package org.keyple.commands.calypso.po.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduCommandBuilder;
import org.keyple.commands.calypso.po.PoCommandBuilder;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.po.builder.GetDataFciCmdBuild;
import org.keyple.seproxy.ApduRequest;

public class GetDataCmdBuildTest {

	@Test
	public void getDataFCICmdBuild() {
		byte[] request = { (byte) 0x94, (byte) 0xCA, (byte) 0x00, 0x6F, 0x00 };
		ApduCommandBuilder apduCommandBuilder = new GetDataFciCmdBuild(PoRevision.REV2_4);
		ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request, ApduRequest.getbytes());
		
		
		byte[] request2 = { (byte) 0x00, (byte) 0xCA, (byte) 0x00, 0x6F, 0x00 };
        ApduCommandBuilder apduCommandBuilder2 = new GetDataFciCmdBuild(PoRevision.REV3_1);
        ApduRequest ApduRequest2 = apduCommandBuilder2.getApduRequest();
        Assert.assertArrayEquals(request2, ApduRequest2.getbytes());
	}
}
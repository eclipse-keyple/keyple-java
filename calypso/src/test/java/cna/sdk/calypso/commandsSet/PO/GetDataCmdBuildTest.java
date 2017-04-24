package cna.sdk.calypso.commandsSet.PO;

import org.junit.Assert;
import org.junit.Test;

import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.enumTagUtils;
import cna.sdk.calypso.commandset.po.PoCommandBuilder;
import cna.sdk.calypso.commandset.po.builder.GetDataFciCmdBuild;
import cna.sdk.seproxy.APDURequest;

public class GetDataCmdBuildTest {

	@Test
	public void getDataFCICmdBuild() {
		byte[] request = { (byte) 0x94, (byte) 0xCA, (byte) 0x00, 0x6F, (byte) 0x00 };
		ApduCommandBuilder apduCommandBuilder = new GetDataFciCmdBuild(PoCommandBuilder.defaultRevision);
		APDURequest apduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request, apduRequest.getbytes());
	}
}
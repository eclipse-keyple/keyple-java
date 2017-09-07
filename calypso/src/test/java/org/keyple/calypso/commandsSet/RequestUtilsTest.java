package org.keyple.calypso.commandsSet;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.keyple.calypso.commandset.CalypsoCommands;
import org.keyple.calypso.commandset.RequestUtils;
import org.keyple.calypso.commandset.dto.CalypsoRequest;
import org.keyple.seproxy.APDURequest;

@RunWith(BlockJUnit4ClassRunner.class)
public class RequestUtilsTest {

	private CalypsoRequest request;

	private boolean isCase4;

	private APDURequest apduRequestExpected;

	private byte cla;

	private CalypsoCommands ins;

	private byte pUn;

	private byte pDeux;

	private byte[] dataIn;

	private byte option;

	private byte[] fci;

	@Test
	public void testConstructAPDURequest() {
		fci = new byte[] { (byte) 0x00, (byte) 0xCA, 0x00, 0x6F };
		isCase4 = false;
		apduRequestExpected = new APDURequest(fci, isCase4);
		cla = (byte) 0x00;
		ins = CalypsoCommands.PO_GET_DATA_FCI;
		pUn = 0x00;
		pDeux = 0x6F;
		dataIn = null;

		request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn);
		APDURequest apduRequestActual = RequestUtils.constructAPDURequest(request);
		Assert.assertArrayEquals(apduRequestExpected.getbytes(), apduRequestActual.getbytes());
		Assert.assertEquals(apduRequestExpected.isCase4(), apduRequestActual.isCase4());
	}

	@Test
	public void testConstructApduRequestCase4() {
		fci = new byte[] { (byte) 0x00, (byte) 0xCA, 0x00, 0x6F };
		isCase4 = true;
		apduRequestExpected = new APDURequest(fci, isCase4);
		cla = (byte) 0x00;
		ins = CalypsoCommands.PO_GET_DATA_FCI;
		pUn = 0x00;
		pDeux = 0x6F;
		dataIn = null;
		option = 0x00;

		request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn, option);
		APDURequest apduRequestActual = RequestUtils.constructAPDURequest(request);
		Assert.assertArrayEquals(apduRequestExpected.getbytes(), apduRequestActual.getbytes());
		Assert.assertNotEquals(apduRequestExpected.isCase4(), apduRequestActual.isCase4());
	}

	@Test
	public void testConstructAPDURequestData() {
		fci = new byte[] { (byte) 0x00, (byte) 0xCA, 0x00, 0x6F, 0x02, 0x00, 0x00 };
		isCase4 = false;
		apduRequestExpected = new APDURequest(fci, isCase4);
		cla = (byte) 0x00;
		ins = CalypsoCommands.PO_GET_DATA_FCI;
		pUn = 0x00;
		pDeux = 0x6F;
		dataIn = new byte[] { 0x00, 0x00 };

		request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn);
		APDURequest apduRequestActual = RequestUtils.constructAPDURequest(request);
		Assert.assertArrayEquals(apduRequestExpected.getbytes(), apduRequestActual.getbytes());
		Assert.assertEquals(apduRequestExpected.isCase4(), apduRequestActual.isCase4());
	}

	@Test
	public void testConstructApduRequestCase4Data() {
		fci = new byte[] { (byte) 0x00, (byte) 0xCA, 0x00, 0x6F, 0x02, 0x00, 0x00, 0x00 };
		isCase4 = true;
		apduRequestExpected = new APDURequest(fci, isCase4);
		cla = (byte) 0x00;
		ins = CalypsoCommands.PO_GET_DATA_FCI;
		pUn = 0x00;
		pDeux = 0x6F;
		dataIn = new byte[] { 0x00, 0x00 };
		option = 0x00;

		request = new CalypsoRequest(cla, ins, pUn, pDeux, dataIn, option);
		APDURequest apduRequestActual = RequestUtils.constructAPDURequest(request);
		Assert.assertArrayEquals(apduRequestExpected.getbytes(), apduRequestActual.getbytes());
		Assert.assertEquals(apduRequestExpected.isCase4(), apduRequestActual.isCase4());
	}
}

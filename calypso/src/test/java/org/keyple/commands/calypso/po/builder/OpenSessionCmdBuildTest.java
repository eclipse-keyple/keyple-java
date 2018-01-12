package org.keyple.commands.calypso.po.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduCommandBuilder;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.po.builder.OpenSessionCmdBuild;
import org.keyple.seproxy.ApduRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSessionCmdBuildTest {

	Logger logger = LoggerFactory.getLogger(OpenSessionCmdBuildTest.class);

	byte[] samChallenge = { (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };

	ApduCommandBuilder apduCommandBuilder;

	ApduRequest ApduRequest;

	
	@Test (expected = InconsistentCommandException.class)
    public void openSessionCmdBuild_rev_2_4_exception() throws InconsistentCommandException {

        byte keyIndex = (byte) 0x00;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;
        byte cla = (byte) 0x94;
        byte p1 = (byte) (0x80 + (recordNumberToRead * 8) +  keyIndex);
        byte p2 = (byte) (sfiToSelect * 8);
        byte cmd = (byte) 0x8A;
        byte[] dataIn = samChallenge;
        // revision 2.4
        byte[] request2_4 = { cla, cmd, p1, p2, (byte) dataIn.length,  (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };
        
        apduCommandBuilder = new OpenSessionCmdBuild(PoRevision.REV2_4, keyIndex, dataIn, sfiToSelect,recordNumberToRead);
    }
	
	@Test
	public void openSessionCmdBuild_rev_2_4() throws InconsistentCommandException {

	    byte keyIndex = (byte) 0x03;
	    byte recordNumberToRead = (byte) 0x01;
	    byte sfiToSelect = (byte) 0x08;
	    byte cla = (byte) 0x94;
	    byte p1 = (byte) (0x80 + (recordNumberToRead * 8) +  keyIndex);
	    byte p2 = (byte) (sfiToSelect * 8);
	    byte cmd = (byte) 0x8A;
	    byte[] dataIn = samChallenge;
		// revision 2.4
		byte[] request2_4 = { cla, cmd, p1, p2, (byte) dataIn.length, (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };
		
		apduCommandBuilder = new OpenSessionCmdBuild(PoRevision.REV2_4, keyIndex, dataIn, sfiToSelect,recordNumberToRead);
		ApduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request2_4, ApduRequest.getbytes());
	}

	@Test
	public void openSessionCmdBuild_rev_3_1() throws InconsistentCommandException {
	    
	    byte keyIndex = (byte) 0x03;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;
        byte cla = (byte) 0x00;
        byte p1 = (byte) ((recordNumberToRead * 8) +  keyIndex);
        byte p2 = (byte) ((sfiToSelect * 8) + 1);
        byte cmd = (byte) 0x8A;
        byte[] dataIn = samChallenge;
        
		// revision 3.1
		byte[] request3_1 = {cla, cmd, p1, p2, (byte) dataIn.length,  (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E};
		apduCommandBuilder = new OpenSessionCmdBuild(PoRevision.REV3_1, keyIndex, dataIn, sfiToSelect,recordNumberToRead);
		ApduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request3_1, ApduRequest.getbytes());
	}

	@Test
	public void openSessionCmdBuild_rev_3_2() throws InconsistentCommandException {
	    
	    byte keyIndex = (byte) 0x03;
        byte recordNumberToRead = (byte) 0x01;
        byte sfiToSelect = (byte) 0x08;
        byte cla = (byte) 0x00;
        byte p1 = (byte) ((recordNumberToRead * 8) +  keyIndex);
        byte p2 = (byte) ((sfiToSelect * 8) + 2);
        byte cmd = (byte) 0x8A;
        byte[] dataIn = new byte[samChallenge.length + 1];
        System.arraycopy(samChallenge, 0, dataIn, 1, samChallenge.length);
		// revision 3.2
		byte[] request3_2 = {cla, cmd, p1, p2, (byte) (samChallenge.length +1) , (byte) 0x00 , (byte) 0xA8, 0x31, (byte) 0xC3, 0x3E };
		apduCommandBuilder = new OpenSessionCmdBuild(PoRevision.REV3_2 , keyIndex, samChallenge, sfiToSelect,recordNumberToRead);
		ApduRequest = apduCommandBuilder.getApduRequest();
		Assert.assertArrayEquals(request3_2, ApduRequest.getbytes());
	}



}
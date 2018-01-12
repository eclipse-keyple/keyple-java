package org.keyple.commands.calypso.csm.builder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.commands.calypso.ApduCommandBuilder;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.csm.CsmRevision;
import org.keyple.commands.calypso.csm.builder.DigestInitCmdBuild;
import org.keyple.commands.calypso.dto.KIF;
import org.keyple.commands.calypso.dto.KVC;
import org.keyple.seproxy.ApduRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestInitCmdBuildTest {

    @Test (expected = InconsistentCommandException.class)
    public void digestInitCmd_inconsistent() throws InconsistentCommandException {
        
        byte[] digestData = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        byte[] request = { (byte) 0x80, (byte) 0x8A, 0x00, (byte) 0xFF, 0x09, 0x30, 0x7E, 0x01, 0x02, 0x03, 0x04, 0x05,
                0x06, 0x07 };
        
        boolean verificationMode = false;
        boolean rev3_2Mode = false;
        byte workKeyRecordNumber = (byte) 0x00 ;
        byte workKeyKif = (byte) 0x00;
        byte workKeyKVC = (byte) 0x7E;
        CsmRevision revision = CsmRevision.S1D; 
        
        ApduCommandBuilder apduCommandBuilder = new DigestInitCmdBuild(revision, verificationMode, rev3_2Mode, workKeyRecordNumber, workKeyKif,
                workKeyKVC, digestData);
    }
    
    @Test (expected = InconsistentCommandException.class)
    public void digestInitCmd_inconsistent_digestNull() throws InconsistentCommandException {
        
        byte[] digestData = null;
        byte[] request = { (byte) 0x80, (byte) 0x8A, 0x00, (byte) 0xFF, 0x09, 0x30, 0x7E, 0x01, 0x02, 0x03, 0x04, 0x05,
                0x06, 0x07 };
        
        boolean verificationMode = false;
        boolean rev3_2Mode = false;
        byte workKeyRecordNumber = (byte) 0x10 ;
        byte workKeyKif = (byte) 0x30;
        byte workKeyKVC = (byte) 0x7E;
        CsmRevision revision = CsmRevision.S1D; 
        
        ApduCommandBuilder apduCommandBuilder = new DigestInitCmdBuild(revision, verificationMode, rev3_2Mode, workKeyRecordNumber, workKeyKif,
                workKeyKVC, digestData);
    }
    
    @Test
    public void digestInitCmd() throws InconsistentCommandException {
        
        byte[] digestData = new byte[]{(byte) 0x80, (byte) 0x8A, 0x00};
        byte cla = (byte) 0x94;
        byte zero = (byte) 0x00;
        byte p1 = (byte) (zero + 1);
        byte p1_2 = (byte) (p1 + 2 );
        byte p2 = (byte) 0xFF;
        
        boolean verificationMode = true;
        boolean rev3_2Mode = true;
        byte workKeyRecordNumber = (byte) 0xFF ;
        byte workKeyKif = (byte) 0x30;
        byte workKeyKVC = (byte) 0x7E;
        CsmRevision revision = CsmRevision.S1D; 
        
        int size = digestData.length + 2;
        byte[] request = { cla, (byte) 0x8A, p1_2, p2, (byte) size , workKeyKif, workKeyKVC, (byte) 0x80, (byte) 0x8A, 0x00};
        
        ApduCommandBuilder apduCommandBuilder = new DigestInitCmdBuild(revision, verificationMode, rev3_2Mode, workKeyRecordNumber, workKeyKif,
                workKeyKVC, digestData);

        assertArrayEquals(request, apduCommandBuilder.getApduRequest().getbytes());
    }
}

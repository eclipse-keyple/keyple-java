package org.keyple.commands.calypso.csm.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.calypso.ApduCommandBuilder;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.csm.CsmRevision;
import org.keyple.commands.calypso.csm.builder.DigestCloseCmdBuild;
import org.keyple.seproxy.ApduRequest;

public class DigestCloseCmdBuildTest {

    @Test
    public void digestCloseCmdBuild() throws InconsistentCommandException {
        
        byte[] request = { (byte) 0x94, (byte) 0x8E, 0x00, 0x00, (byte) 0x04 };
        ApduCommandBuilder apduCommandBuilder = new DigestCloseCmdBuild(CsmRevision.S1D, (byte) 0x04);// 94
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getbytes());

        byte[] request1 = { (byte) 0x80, (byte) 0x8E,  0x00, 0x00, (byte) 0x04 };
        apduCommandBuilder = new DigestCloseCmdBuild(CsmRevision.C1, (byte) 0x04);// 94
        ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request1, ApduRequest.getbytes());

    }
}
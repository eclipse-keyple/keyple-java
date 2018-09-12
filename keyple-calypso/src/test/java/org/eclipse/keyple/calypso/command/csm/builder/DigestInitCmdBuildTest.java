/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.csm.builder;

import static org.junit.Assert.assertEquals;
import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.calypso.command.csm.CsmRevision;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestInitCmdBuildTest {

    @Test(expected = IllegalArgumentException.class)
    public void digestInitCmd_inconsistent() throws IllegalArgumentException {

        ByteBuffer digestData =
                ByteBuffer.wrap(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07});

        boolean verificationMode = false;
        boolean rev3_2Mode = false;
        byte workKeyRecordNumber = (byte) 0x00;
        byte workKeyKif = (byte) 0x00;
        byte workKeyKVC = (byte) 0x7E;
        CsmRevision revision = CsmRevision.S1D;

        AbstractApduCommandBuilder apduCommandBuilder =
                new DigestInitCmdBuild(revision, verificationMode, rev3_2Mode, workKeyRecordNumber,
                        workKeyKif, workKeyKVC, digestData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void digestInitCmd_inconsistent_digestNull() throws IllegalArgumentException {

        ByteBuffer digestData = null;

        boolean verificationMode = false;
        boolean rev3_2Mode = false;
        byte workKeyRecordNumber = (byte) 0x10;
        byte workKeyKif = (byte) 0x30;
        byte workKeyKVC = (byte) 0x7E;
        CsmRevision revision = CsmRevision.S1D;

        AbstractApduCommandBuilder apduCommandBuilder =
                new DigestInitCmdBuild(revision, verificationMode, rev3_2Mode, workKeyRecordNumber,
                        workKeyKif, workKeyKVC, digestData);
    }

    @Test
    public void digestInitCmd() throws IllegalArgumentException {

        ByteBuffer digestData = ByteBuffer.wrap(new byte[] {(byte) 0x80, (byte) 0x8A, 0x00});
        byte cla = (byte) 0x94;
        byte zero = (byte) 0x00;
        byte p1 = (byte) (zero + 1);
        byte p1_2 = (byte) (p1 + 2);
        byte p2 = (byte) 0xFF;

        boolean verificationMode = true;
        boolean rev3_2Mode = true;
        byte workKeyRecordNumber = (byte) 0xFF;
        byte workKeyKif = (byte) 0x30;
        byte workKeyKVC = (byte) 0x7E;
        CsmRevision revision = CsmRevision.S1D;

        int size = digestData.limit() + 2;
        ByteBuffer request = ByteBuffer.wrap(new byte[] {cla, (byte) 0x8A, p1_2, p2, (byte) size,
                workKeyKif, workKeyKVC, (byte) 0x80, (byte) 0x8A, 0x00});

        AbstractApduCommandBuilder apduCommandBuilder =
                new DigestInitCmdBuild(revision, verificationMode, rev3_2Mode, workKeyRecordNumber,
                        workKeyKif, workKeyKVC, digestData);

        assertEquals(request, apduCommandBuilder.getApduRequest().getBytes());
    }
}

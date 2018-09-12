/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.csm.builder;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.calypso.command.csm.CsmRevision;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CsmGetChallengeCmdBuildTest {

    @Test
    public void getChallengeCmdBuild() throws IllegalArgumentException {
        ByteBuffer request =
                ByteBuffer.wrap(new byte[] {(byte) 0x94, (byte) 0x84, 0x00, 0x00, 0x04});

        AbstractApduCommandBuilder apduCommandBuilder =
                new CsmGetChallengeCmdBuild(CsmRevision.S1D, (byte) 0x04);// 94
        ApduRequest apduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertEquals(request, apduRequest.getBytes());
    }
}

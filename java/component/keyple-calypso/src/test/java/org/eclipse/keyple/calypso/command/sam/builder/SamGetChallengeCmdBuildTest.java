/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.sam.builder;


import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.session.SamGetChallengeCmdBuild;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SamGetChallengeCmdBuildTest {

    @Test
    public void getChallengeCmdBuild() throws IllegalArgumentException {
        byte[] request = new byte[] {(byte) 0x94, (byte) 0x84, 0x00, 0x00, 0x04};

        AbstractApduCommandBuilder apduCommandBuilder =
                new SamGetChallengeCmdBuild(SamRevision.S1D, (byte) 0x04);// 94
        ApduRequest apduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, apduRequest.getBytes());
    }
}

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
package org.eclipse.keyple.calypso.command.po.builder.security;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PoGetChallengeCmdBuildTest {

    @Test
    public void POGetChallenge_Rev2_4() {

        byte[] request = {(byte) 0x94, (byte) 0x84, 0x00, 0x00, 0x08};

        AbstractApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoClass.LEGACY);
        ApduRequest apduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, apduRequest.getBytes());

    }

    @Test
    public void POGetChallenge_Rev3_1() {

        byte[] request = {(byte) 0x00, (byte) 0x84, 0x00, 0x00, 0x08};

        AbstractApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoClass.ISO);
        ApduRequest apduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, apduRequest.getBytes());

    }

    @Test
    public void POGetChallenge_Rev3_2() {

        byte[] request = {(byte) 0x00, (byte) 0x84, 0x00, 0x00, 0x08};

        AbstractApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoClass.ISO);
        ApduRequest apduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, apduRequest.getBytes());

    }


}

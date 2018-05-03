/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.po.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.PoGetChallengeCmdBuild;
import org.keyple.commands.AbstractApduCommandBuilder;
import org.keyple.seproxy.ApduRequest;

public class POGetChallengeCmdBuildTest {

    @Test
    public void POGetChallenge_Rev2_4() {

        byte[] request = {(byte) 0x94, (byte) 0x84, 0x01, 0x10, 0x08};

        AbstractApduCommandBuilder apduCommandBuilder =
                new PoGetChallengeCmdBuild(PoRevision.REV2_4);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getBytes());

    }

    @Test
    public void POGetChallenge_Rev3_1() {

        byte[] request = {(byte) 0x00, (byte) 0x84, 0x01, 0x10, 0x08};

        AbstractApduCommandBuilder apduCommandBuilder =
                new PoGetChallengeCmdBuild(PoRevision.REV3_1);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getBytes());

    }

    @Test
    public void POGetChallenge_Rev3_2() {

        byte[] request = {(byte) 0x00, (byte) 0x84, 0x01, 0x10, 0x08};

        AbstractApduCommandBuilder apduCommandBuilder =
                new PoGetChallengeCmdBuild(PoRevision.REV3_2);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getBytes());

    }


}

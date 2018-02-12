/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.commands.csm.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.csm.CsmRevision;
import org.keyple.calypso.commands.csm.builder.CsmGetChallengeCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

public class CSMGetChallengeCmdBuildTest {

    @Test
    public void getChallengeCmdBuild() throws InconsistentCommandException {

        byte[] request = {(byte) 0x94, (byte) 0x84, 0x00, 0x00, 0x04};

        ApduCommandBuilder apduCommandBuilder =
                new CsmGetChallengeCmdBuild(CsmRevision.S1D, (byte) 0x04);// 94
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getbytes());

    }
}

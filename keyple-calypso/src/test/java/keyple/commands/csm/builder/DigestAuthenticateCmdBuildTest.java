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
import org.keyple.calypso.commands.csm.builder.DigestAuthenticateCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InvalidApduReaderException;
import org.keyple.seproxy.exceptions.TimeoutReaderException;
import org.keyple.seproxy.exceptions.UnexpectedReaderException;

public class DigestAuthenticateCmdBuildTest {

    @Test
    public void digestAuthenticate()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        byte[] signaturePO = {0x00, 0x01, 0x02, 0x03};
        byte[] request = {(byte) 0x80, (byte) 0x82, 0x00, 0x00, 0x04, 0x00, 0x01, 0x02, 0x03};

        ApduCommandBuilder apduCommandBuilder = new DigestAuthenticateCmdBuild(null, signaturePO);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getbytes());

    }
}

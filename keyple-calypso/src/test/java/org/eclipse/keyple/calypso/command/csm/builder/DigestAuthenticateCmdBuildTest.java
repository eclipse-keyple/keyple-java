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
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DigestAuthenticateCmdBuildTest {

    @Test
    public void digestAuthenticate() throws IllegalArgumentException {

        ByteBuffer signaturePO = ByteBuffer.wrap(new byte[] {0x00, 0x01, 0x02, 0x03});
        ByteBuffer request = ByteBuffer.wrap(
                new byte[] {(byte) 0x94, (byte) 0x82, 0x00, 0x00, 0x04, 0x00, 0x01, 0x02, 0x03});

        AbstractApduCommandBuilder apduCommandBuilder =
                new DigestAuthenticateCmdBuild(null, signaturePO);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertEquals(ByteBufferUtils.toHex(request),
                ByteBufferUtils.toHex(ApduRequest.getBytes().slice()));
    }
}

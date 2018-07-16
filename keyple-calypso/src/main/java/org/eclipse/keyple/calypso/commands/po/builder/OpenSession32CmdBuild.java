/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.commands.po.builder;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.commands.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.commands.po.PoRevision;
import org.eclipse.keyple.calypso.commands.utils.RequestUtils;

public class OpenSession32CmdBuild extends AbstractOpenSessionCmdBuild {
    /**
     * Instantiates a new AbstractOpenSessionCmdBuild.
     *
     * @param keyIndex the key index
     * @param samChallenge the sam challenge returned by the CSM Get Challenge APDU command
     * @param sfiToSelect the sfi to select
     * @param recordNumberToRead the record number to read
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public OpenSession32CmdBuild(byte keyIndex, ByteBuffer samChallenge, byte sfiToSelect,
            byte recordNumberToRead) throws IllegalArgumentException {
        super(PoRevision.REV3_2);

        byte p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) ((sfiToSelect * 8) + 2);

        ByteBuffer dataIn = ByteBuffer.allocate(samChallenge.limit() + 1);
        dataIn.put((byte) 0x00);
        dataIn.put(samChallenge);

        this.request = RequestUtils.constructAPDURequest((byte) 0x00,
                CalypsoPoCommands.getOpenSessionForRev(defaultRevision), p1, p2, dataIn);
    }
}

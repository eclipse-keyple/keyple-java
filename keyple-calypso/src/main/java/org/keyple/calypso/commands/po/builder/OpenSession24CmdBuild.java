/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.builder;

import java.nio.ByteBuffer;
import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;

public class OpenSession24CmdBuild extends AbstractOpenSessionCmdBuild {
    /**
     * Instantiates a new AbstractOpenSessionCmdBuild.
     *
     * @param keyIndex the key index
     * @param samChallenge the sam challenge returned by the CSM Get Challenge APDU command
     * @param sfiToSelect the sfi to select
     * @param recordNumberToRead the record number to read
     * @throws InconsistentCommandException thrown if rev 2.4 and key index is 0
     */
    public OpenSession24CmdBuild(byte keyIndex, ByteBuffer samChallenge, byte sfiToSelect,
            byte recordNumberToRead) throws InconsistentCommandException {
        super(PoRevision.REV2_4);

        if (keyIndex == 0x00) {
            throw new InconsistentCommandException();
        }

        byte p1 = (byte) (0x80 + (recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) (sfiToSelect * 8);

        this.request = RequestUtils.constructAPDURequest((byte) 0x94,
                CalypsoCommands.getOpenSessionForRev(defaultRevision), p1, p2, samChallenge);
    }
}

/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.command.csm.builder;

import org.eclipse.keyple.calypso.command.csm.CalypsoSmCommands;
import org.eclipse.keyple.calypso.command.csm.CsmCommandBuilder;
import org.eclipse.keyple.calypso.command.csm.CsmRevision;

/**
 * Builder for the CSM Digest Close APDU command.
 */
public class DigestCloseCmdBuild extends CsmCommandBuilder {

    /** The command. */
    private static final CalypsoSmCommands command = CalypsoSmCommands.DIGEST_CLOSE;

    /**
     * Instantiates a new DigestCloseCmdBuild .
     *
     * @param revision of the CSM(SAM)
     * @param expectedResponseLength the expected response length
     * @throws java.lang.IllegalArgumentException - if the expected response length is wrong.
     */
    public DigestCloseCmdBuild(org.eclipse.keyple.calypso.command.csm.CsmRevision revision,
            byte expectedResponseLength) throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (expectedResponseLength != 0x04 && expectedResponseLength != 0x08) {
            throw new IllegalArgumentException(String
                    .format("Bad digest length! Expected 4 or 8, got %s", expectedResponseLength));
        }

        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = 0x00;
        byte p2 = (byte) 0x00;

        // CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, p2, null,
        // expectedResponseLength);
        request = setApduRequest(cla, command, p1, p2, null, expectedResponseLength);
    }
}

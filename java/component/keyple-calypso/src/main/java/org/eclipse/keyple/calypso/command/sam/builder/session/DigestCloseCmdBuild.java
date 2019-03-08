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
package org.eclipse.keyple.calypso.command.sam.builder.session;

import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommands;
import org.eclipse.keyple.calypso.command.sam.SamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.SamRevision;

/**
 * Builder for the SAM Digest Close APDU command.
 */
public class DigestCloseCmdBuild extends SamCommandBuilder {

    /** The command. */
    private static final CalypsoSamCommands command = CalypsoSamCommands.DIGEST_CLOSE;

    /**
     * Instantiates a new DigestCloseCmdBuild .
     *
     * @param revision of the SAM
     * @param expectedResponseLength the expected response length
     * @throws java.lang.IllegalArgumentException - if the expected response length is wrong.
     */
    public DigestCloseCmdBuild(org.eclipse.keyple.calypso.command.sam.SamRevision revision,
            byte expectedResponseLength) throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (expectedResponseLength != 0x04 && expectedResponseLength != 0x08) {
            throw new IllegalArgumentException(String
                    .format("Bad digest length! Expected 4 or 8, got %s", expectedResponseLength));
        }

        byte cla = SamRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = 0x00;
        byte p2 = (byte) 0x00;

        // CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, p2, null,
        // expectedResponseLength);
        request = setApduRequest(cla, command, p1, p2, null, expectedResponseLength);
    }
}

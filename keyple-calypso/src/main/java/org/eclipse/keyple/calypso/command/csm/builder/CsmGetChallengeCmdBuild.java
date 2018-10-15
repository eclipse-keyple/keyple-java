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
 * Builder for the CSM Get Challenge APDU command.
 */
public class CsmGetChallengeCmdBuild extends CsmCommandBuilder {

    /** The command reference. */
    private static final CalypsoSmCommands command = CalypsoSmCommands.GET_CHALLENGE;

    /**
     * Instantiates a new CsmGetChallengeCmdBuild.
     *
     * @param revision of the CSM (SAM)
     * @param expectedResponseLength the expected response length
     * @throws java.lang.IllegalArgumentException - if the expected response length has wrong value.
     */
    public CsmGetChallengeCmdBuild(org.eclipse.keyple.calypso.command.csm.CsmRevision revision,
            byte expectedResponseLength) throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (expectedResponseLength != 0x04 && expectedResponseLength != 0x08) {
            throw new IllegalArgumentException(String.format(
                    "Bad challenge length! Expected 4 or 8, got %s", expectedResponseLength));
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;
        byte p1 = 0x00;
        byte p2 = 0x00;

        // CalypsoRequest calypsoRequest = new CalypsoRequest();
        request = setApduRequest(cla, command, p1, p2, null, expectedResponseLength);
    }
}

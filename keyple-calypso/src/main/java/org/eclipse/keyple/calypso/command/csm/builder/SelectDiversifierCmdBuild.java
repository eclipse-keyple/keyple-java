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

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the CSM Select Diversifier APDU command.
 *
 */
public class SelectDiversifierCmdBuild extends CsmCommandBuilder {

    /** The command. */
    private static final CalypsoSmCommands command = CalypsoSmCommands.SELECT_DIVERSIFIER;

    /**
     * Instantiates a new SelectDiversifierCmdBuild.
     *
     * @param revision the CSM(SAM) revision
     * @param diversifier the application serial number
     * @throws java.lang.IllegalArgumentException - if the diversifier is null or has a wrong length
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public SelectDiversifierCmdBuild(org.eclipse.keyple.calypso.command.csm.CsmRevision revision,
            byte[] diversifier) throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (diversifier == null || (diversifier.length != 4 && diversifier.length != 8)) {
            throw new IllegalArgumentException("Bad diversifier value!");
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = 0x00;
        byte p2 = 0x00;

        request = setApduRequest(cla, command, p1, p2, diversifier, null);

    }
}

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
package org.eclipse.keyple.calypso.command.sam.builder.security;


import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommand;
import org.eclipse.keyple.calypso.command.sam.SamRevision;

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the SAM Select Diversifier APDU command.
 *
 */
public class SelectDiversifierCmdBuild extends AbstractSamCommandBuilder {

    /** The command. */
    private static final CalypsoSamCommand command = CalypsoSamCommand.SELECT_DIVERSIFIER;

    /**
     * Instantiates a new SelectDiversifierCmdBuild.
     *
     * @param revision the SAM revision
     * @param diversifier the application serial number
     * @throws IllegalArgumentException - if the diversifier is null or has a wrong length
     * @throws IllegalArgumentException - if the request is inconsistent
     */
    public SelectDiversifierCmdBuild(SamRevision revision, byte[] diversifier)
            throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (diversifier == null || (diversifier.length != 4 && diversifier.length != 8)) {
            throw new IllegalArgumentException("Bad diversifier value!");
        }
        byte cla = this.defaultRevision.getClassByte();
        byte p1 = 0x00;
        byte p2 = 0x00;

        request = setApduRequest(cla, command, p1, p2, diversifier, null);

    }
}

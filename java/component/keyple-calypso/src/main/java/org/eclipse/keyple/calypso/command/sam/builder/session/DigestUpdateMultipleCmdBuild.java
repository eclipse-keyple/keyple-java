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

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the SAM Digest Update Multiple APDU
 * command.
 *
 */
public class DigestUpdateMultipleCmdBuild extends SamCommandBuilder {

    /** The command. */
    private static final CalypsoSamCommands command = CalypsoSamCommands.DIGEST_UPDATE_MULTIPLE;

    /**
     * Instantiates a new DigestUpdateMultipleCmdBuild.
     *
     * @param revision the revision
     * @param digestData the digest data
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public DigestUpdateMultipleCmdBuild(org.eclipse.keyple.calypso.command.sam.SamRevision revision,
            byte[] digestData) throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = SamRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = (byte) 0x80;
        byte p2 = (byte) 0x00;

        request = setApduRequest(cla, command, p1, p2, digestData, null);
    }
}

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
 * This class provides the dedicated constructor to build the CSM Digest Update Multiple APDU
 * command.
 *
 */
public class DigestUpdateMultipleCmdBuild extends CsmCommandBuilder {

    /** The command. */
    private static final CalypsoSmCommands command = CalypsoSmCommands.DIGEST_UPDATE_MULTIPLE;

    /**
     * Instantiates a new DigestUpdateMultipleCmdBuild.
     *
     * @param revision the revision
     * @param digestData the digest data
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public DigestUpdateMultipleCmdBuild(org.eclipse.keyple.calypso.command.csm.CsmRevision revision,
            byte[] digestData) throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = (byte) 0x80;
        byte p2 = (byte) 0x00;

        request = setApduRequest(cla, command, p1, p2, digestData, null);
    }
}

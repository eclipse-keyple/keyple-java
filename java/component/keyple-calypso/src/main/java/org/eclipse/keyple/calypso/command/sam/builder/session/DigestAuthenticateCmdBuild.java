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
 * Builder for the Digest Authenticate APDU command.
 */
public class DigestAuthenticateCmdBuild extends SamCommandBuilder {

    /** The command. */
    private static final CalypsoSamCommands command = CalypsoSamCommands.DIGEST_AUTHENTICATE;

    /**
     * Instantiates a new DigestAuthenticateCmdBuild .
     *
     * @param revision of the SAM
     * @param signature the signature
     * @throws java.lang.IllegalArgumentException - if the signature is null or has a wrong length.
     */
    public DigestAuthenticateCmdBuild(SamRevision revision, byte[] signature)
            throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (signature == null) {
            throw new IllegalArgumentException("Signature can't be null");
        }
        if (signature.length != 4 && signature.length != 8 && signature.length != 16) {
            throw new IllegalArgumentException(
                    "Signature is not the right length : length is " + signature.length);
        }
        byte cla = SamRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = 0x00;
        byte p2 = (byte) 0x00;

        request = setApduRequest(cla, command, p1, p2, signature, null);
    }
}

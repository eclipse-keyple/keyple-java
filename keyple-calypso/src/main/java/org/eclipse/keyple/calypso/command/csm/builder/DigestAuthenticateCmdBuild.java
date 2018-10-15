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
 * Builder for the Digest Authenticate APDU command.
 */
public class DigestAuthenticateCmdBuild extends CsmCommandBuilder {

    /** The command. */
    private static final CalypsoSmCommands command = CalypsoSmCommands.DIGEST_AUTHENTICATE;

    /**
     * Instantiates a new DigestAuthenticateCmdBuild .
     *
     * @param revision of the CSM(SAM)
     * @param signature the signature
     * @throws java.lang.IllegalArgumentException - if the signature is null or has a wrong length.
     */
    public DigestAuthenticateCmdBuild(CsmRevision revision, byte[] signature)
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
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = 0x00;
        byte p2 = (byte) 0x00;

        request = setApduRequest(cla, command, p1, p2, signature, null);
    }
}

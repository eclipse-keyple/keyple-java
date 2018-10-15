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
 * Builder for the CSM Digest Update APDU command. This command have to be sent twice for each
 * command executed during a session. First time for the command sent and second time for the answer
 * received
 */
public class DigestUpdateCmdBuild extends CsmCommandBuilder {

    /** The command reference. */

    private static final CalypsoSmCommands command = CalypsoSmCommands.DIGEST_UPDATE;

    /**
     * Instantiates a new DigestUpdateCmdBuild.
     *
     * @param revision of the CSM(SAM)
     * @param encryptedSession the encrypted session
     * @param digestData all bytes from command sent by the PO or response from the command
     * @throws java.lang.IllegalArgumentException - if the digest data is null or has a length &gt;
     *         255
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public DigestUpdateCmdBuild(org.eclipse.keyple.calypso.command.csm.CsmRevision revision,
            boolean encryptedSession, byte[] digestData) throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = (byte) 0x00;
        byte p2 = (byte) 0x00;
        if (encryptedSession) {
            p2 = (byte) 0x80;
        }

        if (digestData != null && digestData.length > 255) {
            throw new IllegalArgumentException("Digest data null or too long!");
        }

        // CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, p2, digestData);
        request = setApduRequest(cla, command, p1, p2, digestData, null);
    }
}

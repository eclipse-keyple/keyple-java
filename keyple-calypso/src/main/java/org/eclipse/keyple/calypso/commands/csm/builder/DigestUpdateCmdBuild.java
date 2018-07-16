/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.commands.csm.builder;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.commands.csm.AbstractCsmCommandBuilder;
import org.eclipse.keyple.calypso.commands.csm.CalypsoSmCommands;
import org.eclipse.keyple.calypso.commands.csm.CsmRevision;
import org.eclipse.keyple.calypso.commands.utils.RequestUtils;
import org.eclipse.keyple.seproxy.ApduRequest;

/**
 * Builder for the CSM Digest Update APDU command. This command have to be sent twice for each
 * command executed during a session. First time for the command sent and second time for the answer
 * received
 */
public class DigestUpdateCmdBuild extends AbstractCsmCommandBuilder {

    /** The command reference. */

    private static CalypsoSmCommands command = CalypsoSmCommands.DIGEST_UPDATE;

    /**
     * Instantiates a new DigestUpdateCmdBuild.
     *
     * @param revision of the CSM(SAM)
     * @param encryptedSession the encrypted session
     * @param digestData all bytes from command sent by the PO or response from the command
     * @throws java.lang.IllegalArgumentException - if the digest data is null or has a length > 255
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public DigestUpdateCmdBuild(CsmRevision revision, boolean encryptedSession,
            ByteBuffer digestData) throws IllegalArgumentException {
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

        if (digestData != null && digestData.limit() > 255) {
            throw new IllegalArgumentException("Digest data null or too long!");
        }

        // CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, p2, digestData);
        request = RequestUtils.constructAPDURequest(cla, command, p1, p2,
                digestData != null ? digestData.asReadOnlyBuffer() : null);
    }

    /**
     * Instantiates a new digest update cmd build.
     *
     * @param request the request
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public DigestUpdateCmdBuild(ApduRequest request) throws IllegalArgumentException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}

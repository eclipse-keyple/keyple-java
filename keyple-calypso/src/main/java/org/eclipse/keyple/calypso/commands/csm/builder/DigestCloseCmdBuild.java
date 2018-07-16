/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.commands.csm.builder;

import org.eclipse.keyple.calypso.commands.csm.AbstractCsmCommandBuilder;
import org.eclipse.keyple.calypso.commands.csm.CalypsoSmCommands;
import org.eclipse.keyple.calypso.commands.csm.CsmRevision;
import org.eclipse.keyple.calypso.commands.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.commands.utils.RequestUtils;
import org.eclipse.keyple.seproxy.ApduRequest;

/**
 * Builder for the CSM Digest Close APDU command.
 */
public class DigestCloseCmdBuild extends AbstractCsmCommandBuilder {

    /** The command. */
    private static CalypsoSmCommands command = CalypsoSmCommands.DIGEST_CLOSE;

    /**
     * Instantiates a new DigestCloseCmdBuild .
     *
     * @param revision of the CSM(SAM)
     * @param expectedResponseLength the expected response length
     * @throws java.lang.IllegalArgumentException - if the expected response length is wrong.
     */
    public DigestCloseCmdBuild(CsmRevision revision, byte expectedResponseLength)
            throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (expectedResponseLength != 0x04 && expectedResponseLength != 0x08) {
            throw new IllegalArgumentException(String
                    .format("Bad digest length! Expected 4 or 8, got %s", expectedResponseLength));
        }

        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = 0x00;
        byte p2 = (byte) 0x00;

        // CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, p2, null,
        // expectedResponseLength);
        request = RequestUtils.constructAPDURequest(cla, command, p1, p2, null,
                expectedResponseLength);
    }

    /**
     * Instantiates a new digest close cmd build.
     *
     * @param request the request
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public DigestCloseCmdBuild(ApduRequest request) throws IllegalArgumentException {
        super(CalypsoPoCommands.APPEND_RECORD, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}

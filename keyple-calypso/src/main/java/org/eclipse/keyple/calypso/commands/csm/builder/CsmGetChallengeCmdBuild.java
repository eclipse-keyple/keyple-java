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
import org.eclipse.keyple.calypso.commands.utils.RequestUtils;
import org.eclipse.keyple.commands.InconsistentCommandException;
import org.eclipse.keyple.seproxy.ApduRequest;

/**
 * Builder for the CSM Get Challenge APDU command.
 */
public class CsmGetChallengeCmdBuild extends AbstractCsmCommandBuilder {

    /** The command reference. */
    private static CalypsoSmCommands command = CalypsoSmCommands.GET_CHALLENGE;

    /**
     * Instantiates a new CsmGetChallengeCmdBuild.
     *
     * @param revision of the CSM (SAM)
     * @param expectedResponseLength the expected response length
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public CsmGetChallengeCmdBuild(CsmRevision revision, byte expectedResponseLength)
            throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (expectedResponseLength != 0x04 && expectedResponseLength != 0x08) {
            throw new InconsistentCommandException();
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;
        byte p1 = 0x00;
        byte p2 = 0x00;

        // CalypsoRequest calypsoRequest = new CalypsoRequest();
        request = RequestUtils.constructAPDURequest(cla, command, p1, p2, null,
                expectedResponseLength);
    }

    /**
     * Instantiates a new csm get challenge cmd build.
     *
     * @param request the request
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public CsmGetChallengeCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}

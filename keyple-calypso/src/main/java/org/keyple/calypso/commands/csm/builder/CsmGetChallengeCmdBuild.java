/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.csm.builder;

import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.calypso.commands.csm.CsmCommandBuilder;
import org.keyple.calypso.commands.csm.CsmRevision;
import org.keyple.calypso.commands.dto.CalypsoRequest;
import org.keyple.calypso.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

/**
 * This class provides the dedicated constructor to build the CSM Get Challenge APDU command.
 *
 * @author Ixxi
 *
 */
public class CsmGetChallengeCmdBuild extends CsmCommandBuilder {

    /** The command reference. */
    private static CalypsoCommands command = CalypsoCommands.CSM_GET_CHALLENGE;

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

        CalypsoRequest calypsoRequest =
                new CalypsoRequest(cla, command, p1, p2, null, expectedResponseLength);
        request = RequestUtils.constructAPDURequest(calypsoRequest);
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

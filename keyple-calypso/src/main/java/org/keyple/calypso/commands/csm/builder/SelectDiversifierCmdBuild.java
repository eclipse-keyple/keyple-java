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
import org.keyple.calypso.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the CSM Select Diversifier APDU command.
 *
 * @author Ixxi
 *
 */
public class SelectDiversifierCmdBuild extends CsmCommandBuilder {

    /** The command. */
    private static CalypsoCommands command = CalypsoCommands.CSM_SELECT_DIVERSIFIER;

    /**
     * Instantiates a new SelectDiversifierCmdBuild.
     *
     * @param revision the CSM(SAM) revision
     * @param diversifier the application serial number
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public SelectDiversifierCmdBuild(CsmRevision revision, byte[] diversifier)
            throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (diversifier == null || (diversifier.length != 4 && diversifier.length != 8)) {
            throw new InconsistentCommandException();
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = 0x00;
        byte p2 = 0x00;
        // CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, p2, diversifier);

        request = RequestUtils.constructAPDURequest(cla, command, p1, p2, diversifier);

    }

    /**
     * Instantiates a new select diversifier cmd build.
     *
     * @param request the request
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public SelectDiversifierCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}

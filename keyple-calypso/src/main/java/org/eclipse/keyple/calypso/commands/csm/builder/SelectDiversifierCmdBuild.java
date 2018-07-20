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

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the CSM Select Diversifier APDU command.
 *
 * @author Ixxi
 *
 */
public class SelectDiversifierCmdBuild extends AbstractCsmCommandBuilder {

    /** The command. */
    private static CalypsoSmCommands command = CalypsoSmCommands.SELECT_DIVERSIFIER;

    /**
     * Instantiates a new SelectDiversifierCmdBuild.
     *
     * @param revision the CSM(SAM) revision
     * @param diversifier the application serial number
     * @throws java.lang.IllegalArgumentException - if the diversifier is null or has a wrong length
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public SelectDiversifierCmdBuild(CsmRevision revision, ByteBuffer diversifier)
            throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (diversifier == null || (diversifier.limit() != 4 && diversifier.limit() != 8)) {
            throw new IllegalArgumentException("Bad diversifier value!");
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = 0x00;
        byte p2 = 0x00;

        request = RequestUtils.constructAPDURequest(cla, command, p1, p2, diversifier);

    }

    /**
     * Instantiates a new select diversifier cmd build.
     *
     * @param request the request
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public SelectDiversifierCmdBuild(ApduRequest request) throws IllegalArgumentException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}

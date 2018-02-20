/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.builder;

import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.calypso.commands.po.PoCommandBuilder;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the Close Secure Session APDU command.
 *
 * @author Ixxi
 */
public class CloseSessionCmdBuild extends PoCommandBuilder {

    /** The command. */
    private final static CalypsoCommands command = CalypsoCommands.PO_CLOSE_SESSION;

    /**
     * Instantiates a new CloseSessionCmdBuild depending of the revision of the PO.
     *
     * @param revision of the PO
     * @param ratificationAsked the ratification asked
     * @param terminalSessionSignature the sam half session signature
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public CloseSessionCmdBuild(PoRevision revision, boolean ratificationAsked,
            byte[] terminalSessionSignature) throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        // The optional parameter terminalSessionSignature could contain 4 or 8
        // bytes.
        if (terminalSessionSignature != null) {
            if (terminalSessionSignature.length != 4 && terminalSessionSignature.length != 8) {
                throw new InconsistentCommandException();
            }
        }

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;

        byte p1 = ratificationAsked ? (byte) 0x80 : (byte) 0x00;
        // CalypsoRequest calypsoRequest = new CalypsoRequest(cla, command, p1, (byte) 0x00,
        // terminalSessionSignature);
        request = RequestUtils.constructAPDURequest(cla, command, p1, (byte) 0x00,
                terminalSessionSignature);
    }

    /**
     * Instantiates a new close session cmd build.
     *
     * @param request the request
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public CloseSessionCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }
}

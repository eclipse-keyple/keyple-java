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

package org.eclipse.keyple.calypso.command.po.builder.session;


import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.PoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.util.ByteArrayUtils;

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the Close Secure Session APDU command.
 */
public class CloseSessionCmdBuild extends PoCommandBuilder {

    /** The command. */
    private final static CalypsoPoCommands command = CalypsoPoCommands.CLOSE_SESSION;

    /**
     * Instantiates a new CloseSessionCmdBuild depending of the revision of the PO.
     *
     * @param revision of the PO
     * @param ratificationAsked the ratification asked
     * @param terminalSessionSignature the sam half session signature
     * @throws java.lang.IllegalArgumentException - if the signature is null or has a wrong length
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public CloseSessionCmdBuild(PoRevision revision, boolean ratificationAsked,
            byte[] terminalSessionSignature) throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        // The optional parameter terminalSessionSignature could contain 4 or 8
        // bytes.
        if (terminalSessionSignature != null && terminalSessionSignature.length != 4
                && terminalSessionSignature.length != 8) {
            throw new IllegalArgumentException("Invalid terminal sessionSignature: "
                    + ByteArrayUtils.toHex(terminalSessionSignature));
        }

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;

        byte p1 = ratificationAsked ? (byte) 0x80 : (byte) 0x00;
        /*
         * case 4: this command contains incoming and outgoing data. We define le = 0, the actual
         * length will be processed by the lower layers.
         */
        byte le = 0;

        request = setApduRequest(cla, command, p1, (byte) 0x00, terminalSessionSignature, le);
    }
}

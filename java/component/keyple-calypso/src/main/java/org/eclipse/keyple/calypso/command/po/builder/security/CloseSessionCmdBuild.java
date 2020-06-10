/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.builder.security;


import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.parser.security.CloseSessionRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * This class provides the dedicated constructor to build the Close Secure Session APDU command.
 */
public final class CloseSessionCmdBuild extends AbstractPoCommandBuilder<CloseSessionRespPars> {

    /** The command. */
    private static final CalypsoPoCommand command = CalypsoPoCommand.CLOSE_SESSION;

    /**
     * Instantiates a new CloseSessionCmdBuild depending of the revision of the PO.
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     * @param ratificationAsked the ratification asked
     * @param terminalSessionSignature the sam half session signature
     * @throws IllegalArgumentException - if the signature is null or has a wrong length
     * @throws IllegalArgumentException - if the command is inconsistent
     */
    public CloseSessionCmdBuild(PoClass poClass, boolean ratificationAsked,
            byte[] terminalSessionSignature) {
        super(command, null);
        // The optional parameter terminalSessionSignature could contain 4 or 8
        // bytes.
        if (terminalSessionSignature != null && terminalSessionSignature.length != 4
                && terminalSessionSignature.length != 8) {
            throw new IllegalArgumentException("Invalid terminal sessionSignature: "
                    + ByteArrayUtil.toHex(terminalSessionSignature));
        }

        byte p1 = ratificationAsked ? (byte) 0x80 : (byte) 0x00;
        /*
         * case 4: this command contains incoming and outgoing data. We define le = 0, the actual
         * length will be processed by the lower layers.
         */
        byte le = 0;

        request = setApduRequest(poClass.getValue(), command, p1, (byte) 0x00,
                terminalSessionSignature, le);
    }

    /**
     * Instantiates a new CloseSessionCmdBuild based on the revision of the PO to generate an abort
     * session command (Close Secure Session with p1 = p2 = lc = 0).
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     */
    public CloseSessionCmdBuild(PoClass poClass) {
        super(command, null);
        request = setApduRequest(poClass.getValue(), command, (byte) 0x00, (byte) 0x00, null,
                (byte) 0);
        /* Add "Abort session" to command name for logging purposes */
        this.addSubName("Abort session");
    }

    @Override
    public CloseSessionRespPars createResponseParser(ApduResponse apduResponse) {
        return new CloseSessionRespPars(apduResponse, this);
    }

    /**
     *
     * This command can't be executed in session and therefore doesn't uses the session buffer.
     * 
     * @return false
     */
    @Override
    public boolean isSessionBufferUsed() {
        return false;
    }
}

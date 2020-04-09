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
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.parser.security.OpenSession32RespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

public final class OpenSession32CmdBuild
        extends AbstractOpenSessionCmdBuild<OpenSession32RespPars> {
    /**
     * Instantiates a new AbstractOpenSessionCmdBuild.
     *
     * @param keyIndex the key index
     * @param samChallenge the sam challenge returned by the SAM Get Challenge APDU command
     * @param sfiToSelect the sfi to select
     * @param recordNumberToRead the record number to read
     * @throws IllegalArgumentException - if the request is inconsistent
     */
    public OpenSession32CmdBuild(byte keyIndex, byte[] samChallenge, byte sfiToSelect,
            byte recordNumberToRead) throws IllegalArgumentException {
        super(PoRevision.REV3_2);

        byte p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) ((sfiToSelect * 8) + 2);
        /*
         * case 4: this command contains incoming and outgoing data. We define le = 0, the actual
         * length will be processed by the lower layers.
         */
        byte le = 0;

        byte[] dataIn = new byte[samChallenge.length + 1];
        dataIn[0] = (byte) 0x00;
        System.arraycopy(samChallenge, 0, dataIn, 1, samChallenge.length);

        this.request = setApduRequest(PoClass.ISO.getValue(),
                CalypsoPoCommand.getOpenSessionForRev(PoRevision.REV3_2), p1, p2, dataIn, le);

        if (logger.isDebugEnabled()) {
            String extraInfo = String.format("KEYINDEX=%d, SFI=%02X, REC=%d", keyIndex, sfiToSelect,
                    recordNumberToRead);
            this.addSubName(extraInfo);
        }
    }

    @Override
    public OpenSession32RespPars createResponseParser(ApduResponse apduResponse) {
        return new OpenSession32RespPars(apduResponse, this);
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

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
package org.eclipse.keyple.calypso.command.po.builder;


import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.*;
import org.eclipse.keyple.calypso.command.po.parser.AppendRecordRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * The Class AppendRecordCmdBuild. This class provides the dedicated constructor to build the Update
 * Record APDU command.
 */
public final class AppendRecordCmdBuild extends AbstractPoCommandBuilder<AppendRecordRespPars> {

    /** The command. */
    private static final CalypsoPoCommand command = CalypsoPoCommand.APPEND_RECORD;

    /* Construction arguments */
    private final int sfi;
    private final byte[] data;

    /**
     * Instantiates a new UpdateRecordCmdBuild.
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     * @param sfi the sfi to select
     * @param newRecordData the new record data to write
     * @throws IllegalArgumentException - if the command is inconsistent
     */
    public AppendRecordCmdBuild(PoClass poClass, byte sfi, byte[] newRecordData) {
        super(command, null);
        byte cla = poClass.getValue();

        this.sfi = sfi;
        this.data = newRecordData;

        byte p1 = (byte) 0x00;
        byte p2 = (sfi == 0) ? (byte) 0x00 : (byte) (sfi * 8);

        this.request = setApduRequest(cla, command, p1, p2, newRecordData, null);

        if (logger.isDebugEnabled()) {
            String extraInfo = String.format("SFI=%02X", sfi);
            this.addSubName(extraInfo);
        }
    }

    @Override
    public AppendRecordRespPars createResponseParser(ApduResponse apduResponse) {
        return new AppendRecordRespPars(apduResponse, this);
    }

    /**
     * This command can modify the contents of the PO in session and therefore uses the session
     * buffer.
     * 
     * @return true
     */
    @Override
    public boolean isSessionBufferUsed() {
        return true;
    }

    /**
     * @return the SFI of the accessed file
     */
    public int getSfi() {
        return sfi;
    }

    /**
     * @return the data sent to the PO
     */
    public byte[] getData() {
        return data;
    }
}

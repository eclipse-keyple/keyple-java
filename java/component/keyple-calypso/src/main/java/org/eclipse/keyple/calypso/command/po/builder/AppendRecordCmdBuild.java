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

// TODO: Auto-generated Javadoc
/**
 * The Class AppendRecordCmdBuild. This class provides the dedicated constructor to build the Update
 * Record APDU command.
 *
 */
public final class AppendRecordCmdBuild extends PoCommandBuilder
        implements PoSendableInSession, PoModificationCommand {

    /** The command. */
    private static final CalypsoPoCommands command = CalypsoPoCommands.APPEND_RECORD;

    /**
     * Instantiates a new UpdateRecordCmdBuild.
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     * @param sfi the sfi to select
     * @param newRecordData the new record data to write
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public AppendRecordCmdBuild(PoClass poClass, byte sfi, byte[] newRecordData, String extraInfo) {
        super(command, null);
        byte cla = poClass.getValue();
        byte p1 = (byte) 0x00;
        byte p2 = (sfi == 0) ? (byte) 0x00 : (byte) (sfi * 8);

        this.request = setApduRequest(cla, command, p1, p2, newRecordData, null);
        if (extraInfo != null) {
            this.addSubName(extraInfo);
        }
    }
}

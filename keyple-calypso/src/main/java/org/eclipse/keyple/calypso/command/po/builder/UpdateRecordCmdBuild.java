/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.builder;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.PoSendableInSession;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.util.RequestUtils;
import org.eclipse.keyple.command.CommandsTable;
import org.eclipse.keyple.seproxy.ApduRequest;

// TODO: Auto-generated Javadoc
/**
 * The Class UpdateRecordCmdBuild. This class provides the dedicated constructor to build the Update
 * Record APDU command.
 *
 */
public class UpdateRecordCmdBuild extends AbstractPoCommandBuilder implements PoSendableInSession {

    /** The command. */
    private static CommandsTable command = CalypsoPoCommands.UPDATE_RECORD;

    /**
     * Instantiates a new UpdateRecordCmdBuild.
     *
     * @param revision the revision of the PO
     * @param sfi the sfi to select
     * @param recordNumber the record number to update
     * @param newRecordData the new record data to write
     * @throws java.lang.IllegalArgumentException - if record number is < 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public UpdateRecordCmdBuild(PoRevision revision, byte sfi, byte recordNumber,
            ByteBuffer newRecordData) throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (recordNumber < 1) {
            throw new IllegalArgumentException("Bad record number (< 1)");
        }
        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;
        byte p1 = recordNumber;
        byte p2 = (sfi == 0) ? (byte) 0x04 : (byte) ((byte) (sfi * 8) + 4);

        this.request = RequestUtils.constructAPDURequest(cla, command, p1, p2, newRecordData);
    }

    /**
     * Instantiates a new update record cmd build.
     *
     * @param request the request
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public UpdateRecordCmdBuild(ApduRequest request) throws IllegalArgumentException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}

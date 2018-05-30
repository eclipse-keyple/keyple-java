/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.commands.po.builder;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.commands.SendableInSession;
import org.eclipse.keyple.calypso.commands.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.commands.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.commands.po.PoRevision;
import org.eclipse.keyple.calypso.commands.utils.RequestUtils;
import org.eclipse.keyple.commands.CommandsTable;
import org.eclipse.keyple.commands.InconsistentCommandException;
import org.eclipse.keyple.seproxy.ApduRequest;

// TODO: Auto-generated Javadoc
/**
 * The Class UpdateRecordCmdBuild. This class provides the dedicated constructor to build the Update
 * Record APDU command.
 *
 */
public class UpdateRecordCmdBuild extends AbstractPoCommandBuilder implements SendableInSession {

    /** The command. */
    private static CommandsTable command = CalypsoPoCommands.UPDATE_RECORD;

    /**
     * Instantiates a new UpdateRecordCmdBuild.
     *
     * @param revision the revision of the PO
     * @param recordNumber the record number to update
     * @param sfi the sfi to select
     * @param newRecordData the new record data to write
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public UpdateRecordCmdBuild(PoRevision revision, byte recordNumber, byte sfi,
            ByteBuffer newRecordData) throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (recordNumber < 1) {
            throw new InconsistentCommandException();
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
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public UpdateRecordCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}
